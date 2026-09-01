package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.ApiResponse;
import com.protonestiot.dynamaticball.Dto.ForgetPassword;
import com.protonestiot.dynamaticball.Dto.LoginRequest;
import com.protonestiot.dynamaticball.Dto.LoginResponse;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Entity.VerificationToken;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import com.protonestiot.dynamaticball.Repository.VerificationTokenRepository;
import com.protonestiot.dynamaticball.Service.CustomUserDetailsService;
import com.protonestiot.dynamaticball.Service.EmailService;
import com.protonestiot.dynamaticball.util.JwtHelper;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication and password reset APIs")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtHelper jwtUtil;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final long OTP_EXPIRATION_MS = 5 * 60 * 1000; // 5 mins

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }


    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        User user = userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);
        List<String> roles = List.of(user.getRole().name());

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) // Set to true in production if using HTTPS
                .path("/")
                .maxAge(jwtExpiration / 1000)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new LoginResponse(jwt, roles));
    }

    @GetMapping("/logout")
    @Operation(summary = "Logout", description = "Clears JWT token from HTTP-only cookie")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .build());
    }


    @PostMapping("/reset-password/request")
    @Operation(summary = "Request OTP", description = "Requests an OTP to be emailed for password reset")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.getEmail()));

        String otp = generateOtp();
        String otpHash = String.valueOf(otp.hashCode());

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) {
            token = new VerificationToken();
            token.setUser(user);
        }

        token.setOtpHash(otpHash);
        token.setExpiryDate(new Date(System.currentTimeMillis() + OTP_EXPIRATION_MS));
        tokenRepository.save(token);

        try {
            emailService.sendOtpEmail(user.getUsername(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email");
        }

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("OTP sent to your email")
                .build());
    }


    @PostMapping("/reset-password/validate")
    @Operation(summary = "Validate OTP", description = "Validates the OTP for password reset")
    public ResponseEntity<ApiResponse<Void>> validateOtp(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) throw new IllegalArgumentException("No OTP found.");
        if (token.getExpiryDate().before(new Date())) throw new IllegalArgumentException("OTP expired");

        String inputHash = String.valueOf(request.getToken().hashCode());
        if (!inputHash.equals(token.getOtpHash())) throw new IllegalArgumentException("Invalid OTP");

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("OTP is valid")
                .build());
    }


    @PostMapping("/reset-password/reset")
    @Operation(summary = "Reset password", description = "Resets password using a valid OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) throw new IllegalArgumentException("No OTP found.");
        if (token.getExpiryDate().before(new Date())) throw new IllegalArgumentException("OTP expired");

        String inputHash = String.valueOf(request.getToken().hashCode());
        if (!inputHash.equals(token.getOtpHash())) throw new IllegalArgumentException("Invalid OTP");

        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");

        user.setPassword(request.getPassword());
        userRepository.save(user);

        token.setExpiryDate(new Date(System.currentTimeMillis() - 1000)); // expire token
        tokenRepository.save(token);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Password reset successfully")
                .build());
    }
}

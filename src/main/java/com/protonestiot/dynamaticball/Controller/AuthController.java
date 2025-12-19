package com.protonestiot.dynamaticball.Controller;

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

    private static final long OTP_EXPIRATION_MS = 15 * 60 * 1000; // 15 mins

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

        return ResponseEntity.ok(new LoginResponse(jwt, roles));
    }

    @GetMapping("/logout")
    @Operation(summary = "Logout", description = "Client-side JWT token clear (no server state)")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logout successful (client-side token cleared)");
    }


    @PostMapping("/reset-password/request")
    @Operation(summary = "Request OTP", description = "Requests an OTP to be emailed for password reset")
    public ResponseEntity<String> requestOtp(@RequestBody ForgetPassword request) {
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

        return ResponseEntity.ok("OTP sent to your email.");
    }


    @PostMapping("/reset-password/validate")
    @Operation(summary = "Validate OTP", description = "Validates the OTP for password reset")
    public ResponseEntity<String> validateOtp(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) throw new IllegalArgumentException("No OTP found.");
        if (token.getExpiryDate().before(new Date())) throw new IllegalArgumentException("OTP expired");

        String inputHash = String.valueOf(request.getToken().hashCode());
        if (!inputHash.equals(token.getOtpHash())) throw new IllegalArgumentException("Invalid OTP");

        return ResponseEntity.ok("OTP is valid");
    }


    @PostMapping("/reset-password/reset")
    @Operation(summary = "Reset password", description = "Resets password using a valid OTP")
    public ResponseEntity<String> resetPassword(@RequestBody ForgetPassword request) {
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

        return ResponseEntity.ok("Password reset successfully");
    }
}

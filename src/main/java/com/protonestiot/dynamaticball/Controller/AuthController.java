package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.ForgetPassword;
import com.protonestiot.dynamaticball.Dto.LoginRequest;
import com.protonestiot.dynamaticball.Dto.LoginResponse;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Entity.VerificationToken;
import com.protonestiot.dynamaticball.Exception.OtpExpiredException;
import com.protonestiot.dynamaticball.Exception.OtpInvalidException;
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
import java.util.Random; // For OTP generation
import com.protonestiot.dynamaticball.Exception.EmailSendException; // Your custom exception


@RestController
@RequestMapping("/api/v1/auth")
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

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        // Find user by username (case-insensitive)
        User user = userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check password (no encoding as per requirement)
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Load user details for JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // Generate JWT token
        String jwt = jwtUtil.generateToken(userDetails);

        // Get user roles
        List<String> roles = List.of(user.getRole().name());

        // Return response
        return ResponseEntity.ok(new LoginResponse(jwt, roles));
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok()
                .header("Set-Cookie", "jwtToken=; HttpOnly; Path=/; Max-Age=0; SameSite=None; Secure")
                .body("Logged out successfully");
    }

    // ---------------- REQUEST OTP ----------------
    @PostMapping("/reset-password/request")
    public ResponseEntity<?> requestOtp(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        String otpHash = String.valueOf(otp.hashCode());

        // Check existing token
        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) {
            token = new VerificationToken();
            token.setUser(user);
        }

        token.setOtpHash(otpHash);
        token.setExpiryDate(new Date(System.currentTimeMillis() + OTP_EXPIRATION_MS));
        tokenRepository.save(token);

        // Send OTP via email
        try {
            emailService.sendOtpEmail(user.getUsername(), otp);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send OTP email");
        }

        return ResponseEntity.ok().body("OTP sent to your email.");
    }

    // ---------------- VALIDATE OTP ----------------
    @PostMapping("/reset-password/validate")
    public ResponseEntity<?> validateOtp(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) throw new OtpInvalidException("No OTP found.");
        if (token.getExpiryDate().before(new Date())) throw new OtpExpiredException("OTP expired");

        String inputHash = String.valueOf(request.getToken().hashCode());
        if (!inputHash.equals(token.getOtpHash())) throw new OtpInvalidException("Invalid OTP");

        return ResponseEntity.ok().body("OTP is valid");
    }

    // ---------------- RESET PASSWORD ----------------
    @PostMapping("/reset-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) throw new OtpInvalidException("No OTP found.");
        if (token.getExpiryDate().before(new Date())) throw new OtpExpiredException("OTP expired");

        String inputHash = String.valueOf(request.getToken().hashCode());
        if (!inputHash.equals(token.getOtpHash())) throw new OtpInvalidException("Invalid OTP");

        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");

        // Save new password
        user.setPassword(request.getPassword());
        userRepository.save(user);

        token.setExpiryDate(new Date(System.currentTimeMillis() - 1000)); // expire token
        tokenRepository.save(token);

        return ResponseEntity.ok().body("Password reset successfully");
    }
}

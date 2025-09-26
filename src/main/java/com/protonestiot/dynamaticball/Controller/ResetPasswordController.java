package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.ForgetPassword;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Entity.VerificationToken;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import com.protonestiot.dynamaticball.Repository.VerificationTokenRepository;
import com.protonestiot.dynamaticball.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import java.security.SecureRandom;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/reset-password")
public class ResetPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final long OTP_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes

    /** Generate 6-digit OTP **/
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    /** Hash OTP **/
    private String hashOtp(String otp) {
        return passwordEncoder.encode(otp);
    }

    /** Match OTP **/
    private boolean matchOtp(String rawOtp, String hashedOtp) {
        return passwordEncoder.matches(rawOtp, hashedOtp);
    }

    /** 1️ Request OTP **/
    @PostMapping("/request")
    public ResponseEntity<String> requestOtp(@RequestBody ForgetPassword request) throws MessagingException {
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate OTP and hash
        String otp = generateOtp();
        String hashedOtp = hashOtp(otp);

        // Save OTP in DB (delete old one)
        VerificationToken existingToken = tokenRepository.findByUser(user);
        if (existingToken != null) {
            tokenRepository.delete(existingToken);
        }

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setOtpHash(hashedOtp);
        token.setExpiryDate(new Date(System.currentTimeMillis() + OTP_EXPIRATION_MS));
        tokenRepository.save(token);

        // Send OTP email
        emailService.sendOtpEmail(user.getUsername(), otp);

        return ResponseEntity.ok("OTP sent to your email.");
    }

    /** 2️ Validate OTP **/
    @PostMapping("/validate")
    public ResponseEntity<String> validateOtp(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) return ResponseEntity.badRequest().body("No OTP found. Request a new one.");

        if (token.getExpiryDate().before(new Date()))
            return ResponseEntity.badRequest().body("OTP expired");

        if (!matchOtp(request.getToken(), token.getOtpHash()))
            return ResponseEntity.badRequest().body("Invalid OTP");

        return ResponseEntity.ok("OTP is valid");
    }

    /** 3️ Reset Password **/
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) return ResponseEntity.badRequest().body("No OTP found. Request a new one.");

        if (token.getExpiryDate().before(new Date()))
            return ResponseEntity.badRequest().body("OTP expired");
  
        if (!matchOtp(request.getToken(), token.getOtpHash()))
            return ResponseEntity.badRequest().body("Invalid OTP");

        if (!request.getPassword().equals(request.getConfirmPassword()))
            return ResponseEntity.badRequest().body("Passwords do not match");

        // Update password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Delete used OTP
        tokenRepository.delete(token);

        return ResponseEntity.ok("Password reset successfully");
    }
}

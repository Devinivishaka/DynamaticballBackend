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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

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
        User user = userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);
        List<String> roles = List.of(user.getRole().name());
        LoginResponse response = new LoginResponse(jwt, roles);

        return ResponseEntity.ok()
                .header("Set-Cookie", "jwtToken=" + jwt + "; HttpOnly; Path=/; Max-Age=86400; SameSite=None; Secure")
                .body(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok()
                .header("Set-Cookie", "jwtToken=; HttpOnly; Path=/; Max-Age=0; SameSite=None; Secure")
                .body("Logged out successfully");
    }

    // ---------------- PASSWORD RESET ----------------
    @PostMapping("/reset-password/request")
    public ResponseEntity<String> requestOtp(@RequestBody ForgetPassword request) throws MessagingException {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();

        VerificationToken existingToken = tokenRepository.findByUser(user);
        if (existingToken != null) tokenRepository.delete(existingToken);

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setOtpHash(otp); // store plain OTP
        token.setExpiryDate(new Date(System.currentTimeMillis() + OTP_EXPIRATION_MS));
        tokenRepository.save(token);

        emailService.sendOtpEmail(user.getUsername(), otp);
        return ResponseEntity.ok("OTP sent to your email.");
    }

    @PostMapping("/reset-password/validate")
    public ResponseEntity<String> validateOtp(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) return ResponseEntity.badRequest().body("No OTP found.");
        if (token.getExpiryDate().before(new Date())) return ResponseEntity.badRequest().body("OTP expired");
        if (!request.getToken().equals(token.getOtpHash())) return ResponseEntity.badRequest().body("Invalid OTP");

        return ResponseEntity.ok("OTP is valid");
    }

    @PostMapping("/reset-password/reset")
    public ResponseEntity<String> resetPassword(@RequestBody ForgetPassword request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VerificationToken token = tokenRepository.findByUser(user);
        if (token == null) return ResponseEntity.badRequest().body("No OTP found.");
        if (token.getExpiryDate().before(new Date())) return ResponseEntity.badRequest().body("OTP expired");
        if (!request.getToken().equals(token.getOtpHash())) return ResponseEntity.badRequest().body("Invalid OTP");
        if (!request.getPassword().equals(request.getConfirmPassword()))
            return ResponseEntity.badRequest().body("Passwords do not match");

        user.setPassword(request.getPassword());
        userRepository.save(user);
        tokenRepository.delete(token);

        return ResponseEntity.ok("Password reset successfully");
    }
}

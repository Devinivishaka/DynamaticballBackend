package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.LoginRequest;
import com.protonestiot.dynamaticball.Dto.LoginResponse;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import com.protonestiot.dynamaticball.Service.CustomUserDetailsService;
import com.protonestiot.dynamaticball.Service.UserService;
import com.protonestiot.dynamaticball.util.JwtHelper;
import jakarta.validation.Valid;
import org.apache.catalina.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtHelper jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    // Add referee (Only SUPER_ADMIN)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/referees")
    public ResponseEntity<User> addReferee(@RequestBody @Valid User user) {
        return ResponseEntity.ok(userService.addReferee(user));
    }

    // Get all referees
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/referees")
    public ResponseEntity<List<User>> getAllReferees() {
        return ResponseEntity.ok(userService.getAllReferees());
    }

    // Update referee
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/referees/{id}")
    public ResponseEntity<User> updateReferee(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateReferee(id, user));
    }

    // Delete referee
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/referees/{id}")
    public ResponseEntity<?> deleteReferee(@PathVariable Long id) {
        userService.deleteReferee(id);
        return ResponseEntity.ok("Referee deleted successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid credentials");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        // Get the user and their roles
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
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
}

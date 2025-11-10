package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.RefereeResponseDto;
import com.protonestiot.dynamaticball.Dto.UserDto;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(userService.getUsers(page, limit, search));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/referees")
    public ResponseEntity<User> addReferee(@RequestBody @Valid UserDto userDto) {
        return ResponseEntity.ok(userService.addReferee(userDto));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/referees")
    public ResponseEntity<List<RefereeResponseDto>> getAllReferees() {
        return ResponseEntity.ok(userService.getAllRefereesDto());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserByUserId(userId));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/referees/{userId}")
    public ResponseEntity<User> updateReferee(@PathVariable String userId, @RequestBody @Valid UserDto userDto) {
        return ResponseEntity.ok(userService.updateRefereeByUserId(userId, userDto));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/referees/{userId}")
    public ResponseEntity<String> deleteReferee(@PathVariable String userId) {
        userService.deleteRefereeByUserId(userId);
        return ResponseEntity.ok("Referee deleted successfully");
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/{userId}/upload-profile-image")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {
        String blobName = userService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(blobName); // Return blob name to client
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/profile-image/{blobName}")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String blobName) {
        byte[] imageData = userService.getProfileImage(blobName);
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg") // or determine dynamically
                .body(imageData);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @DeleteMapping("/{userId}/delete-profile-image")
    public ResponseEntity<String> deleteProfileImage(@PathVariable String userId) {
        userService.deleteProfileImage(userId);
        return ResponseEntity.ok("Profile image deleted successfully");
    }
}

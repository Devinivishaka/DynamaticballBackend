package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.ApiResponse;
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

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User and Referee management APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    @Operation(summary = "List users", description = "Paginated list of users with optional search")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(userService.getUsers(page, limit, search));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    @Operation(summary = "Add user", description = "Creates a new user")
    public ResponseEntity<ApiResponse<User>> addUser(@RequestBody @Valid UserDto userDto) {
        User user = userService.addUser(userDto);
        return ResponseEntity.ok(ApiResponse.<User>builder()
                .success(true)
                .message("User created successfully")
                .data(user)
                .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/referees")
    @Operation(summary = "List referees", description = "Retrieves all referees")
    public ResponseEntity<ApiResponse<List<RefereeResponseDto>>> getAllReferees() {
        List<RefereeResponseDto> referees = userService.getAllRefereesDto();
        return ResponseEntity.ok(ApiResponse.<List<RefereeResponseDto>>builder()
                .success(true)
                .message("Referees retrieved successfully")
                .data(referees)
                .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves user details by userId")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserByUserId(userId));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/referees/{userId}")
    @Operation(summary = "Update referee", description = "Updates referee data by userId")
    public ResponseEntity<ApiResponse<User>> updateReferee(@PathVariable String userId, @RequestBody @Valid UserDto userDto) {
        User user = userService.updateRefereeByUserId(userId, userDto);
        return ResponseEntity.ok(ApiResponse.<User>builder()
                .success(true)
                .message("Referee updated successfully")
                .data(user)
                .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/referees/{userId}")
    @Operation(summary = "Delete referee", description = "Deletes referee by userId")
    public ResponseEntity<ApiResponse<Void>> deleteReferee(@PathVariable String userId) {
        userService.deleteRefereeByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Referee deleted successfully")
                .build());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/{userId}/upload-profile-image")
    @Operation(summary = "Upload profile image", description = "Uploads profile image to Azure Blob Storage and returns blob name")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {
        String blobName = userService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Profile image uploaded successfully")
                .data(blobName)
                .build());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/profile-image/{blobName}")
    @Operation(summary = "Get profile image", description = "Retrieves profile image binary by blob name")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String blobName) {
        byte[] imageData = userService.getProfileImage(blobName);
        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg") // or determine dynamically
                .body(imageData);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @DeleteMapping("/{userId}/delete-profile-image")
    @Operation(summary = "Delete profile image", description = "Deletes profile image for given userId")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(@PathVariable String userId) {
        userService.deleteProfileImage(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Profile image deleted successfully")
                .build());
    }
}

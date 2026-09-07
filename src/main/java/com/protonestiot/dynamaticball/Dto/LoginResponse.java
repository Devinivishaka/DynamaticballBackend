package com.protonestiot.dynamaticball.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload returned upon successful login")
public class LoginResponse {
    @Schema(description = "Indicates if authentication was successful", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Login successful")
    private String message;

    @Schema(description = "JWT authentication token")
    private String token;

    @Schema(description = "List of assigned roles for the user", example = "[\"REFEREE\"]")
    private List<String> roles;

    @Schema(description = "Public URL to view user's profile image", example = "http://localhost:8080/api/v1/users/profile-image/U_001_uuid_avatar.png")
    private String profileImageUrl;

    public LoginResponse(String jwt, List<String> roles) {
        this(jwt, roles, null);
    }

    public LoginResponse(String jwt, List<String> roles, String profileImageUrl) {
        this.success = true;
        this.message = "Login successful";
        this.token = jwt;
        this.roles = roles;
        this.profileImageUrl = profileImageUrl;
    }
}

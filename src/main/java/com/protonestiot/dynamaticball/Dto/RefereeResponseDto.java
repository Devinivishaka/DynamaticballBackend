package com.protonestiot.dynamaticball.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefereeResponseDto {

    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String username; // email
    private String password; // raw password (visible to Super Admin)
    private String profileImageUrl;
    private String action; // "EDIT/REMOVE" for frontend buttons

    public RefereeResponseDto(Long id, String firstName, String lastName, String username, String password, String action) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.action = action;
    }
}

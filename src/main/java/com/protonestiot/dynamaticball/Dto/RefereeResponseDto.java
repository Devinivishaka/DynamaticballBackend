package com.protonestiot.dynamaticball.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class RefereeResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;   // email
    private String password;   // raw password (visible to Super Admin)
    private String action;     // e.g., "EDIT/REMOVE" for frontend buttons
}

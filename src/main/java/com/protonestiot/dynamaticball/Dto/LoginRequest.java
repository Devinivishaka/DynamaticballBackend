package com.protonestiot.dynamaticball.Dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    // Getters and Setters
    private String username;

    public void setUsername(String username) {
        this.username = username != null ? username.toLowerCase() : null;
    }

    private String password;

}

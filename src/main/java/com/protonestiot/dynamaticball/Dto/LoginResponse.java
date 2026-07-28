package com.protonestiot.dynamaticball.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private List<String> roles;

    public LoginResponse(String jwt, List<String> roles) {
        this.success = true;
        this.message = "Login successful";
        this.token = jwt;
        this.roles = roles;
    }
}

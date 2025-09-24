package com.protonestiot.dynamaticball.Dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgetPassword {
    private String email;
    private String password;
    private String confirmPassword;
    private String otp;


    public String getToken() {
        return otp;
    }
}

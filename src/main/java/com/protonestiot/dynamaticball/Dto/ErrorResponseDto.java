package com.protonestiot.dynamaticball.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseDto {
    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;

}

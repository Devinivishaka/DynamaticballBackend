package com.protonestiot.dynamaticball.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponseDto {
    private boolean success;
    private String message;
    private String playerId;
    private String belt;
    private String rightWristband;
    private String leftWristband;
    private String camera;
    private Long teamId;
    private Long playerRecordId;
}

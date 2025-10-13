package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRequestDto {
    private String playerId;
    private String belt;
    private String rightWristband;
    private String leftWristband;
    private String camera;
    private Long teamId;
    private String teamKey;
}

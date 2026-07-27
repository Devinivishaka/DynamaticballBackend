package com.protonestiot.dynamaticball.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRequestDto {

    @NotBlank(message = "Player ID cannot be empty")
    private String playerId;

    @NotBlank(message = "Belt value cannot be empty")
    private String belt;

    @NotBlank(message = "Right wristband cannot be empty")
    private String rightWristband;

    @NotBlank(message = "Left wristband cannot be empty")
    private String leftWristband;

    @NotBlank(message = "Camera field cannot be empty")
    private String camera;

    @NotNull(message = "Team ID cannot be null")
    private Long teamId;

    private String gameSetupId; // optional field
}

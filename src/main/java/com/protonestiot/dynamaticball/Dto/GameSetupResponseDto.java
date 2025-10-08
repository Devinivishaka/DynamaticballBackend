package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSetupResponseDto {
    private boolean success;
    private String gameSetupId;
    private String message;
}

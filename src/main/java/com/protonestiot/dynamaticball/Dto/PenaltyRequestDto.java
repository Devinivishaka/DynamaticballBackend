package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyRequestDto {
    private String matchId;
    private String eventType;
    private String playerId;
    private String gameTime;
    private String timestamp;
}

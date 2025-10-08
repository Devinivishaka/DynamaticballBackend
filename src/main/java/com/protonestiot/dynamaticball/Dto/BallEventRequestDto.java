package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BallEventRequestDto {
    private String matchId;
    private String eventType;
    private String playerId;
    private String timestamp;
}

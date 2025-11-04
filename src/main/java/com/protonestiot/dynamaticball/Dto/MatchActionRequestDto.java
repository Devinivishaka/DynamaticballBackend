package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchActionRequestDto {
    private String matchId; // matchCode
    private String timestamp;
}

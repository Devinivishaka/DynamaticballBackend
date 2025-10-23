package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameHistoryItemDto {
    private String gameId;
    private String date;
    private TeamDto teamA;
    private TeamDto teamB;
    private String duration;
    private String status;
}

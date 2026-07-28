package com.protonestiot.dynamaticball.Dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchSummaryDto {
    private String matchId;
    private String gameId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String duration;
    private String gameTime;
    private String timestamp;
    private TeamSummaryDto teamA;
    private TeamSummaryDto teamB;
    private String teamAName;
    private String teamBName;
    private String winner;
}



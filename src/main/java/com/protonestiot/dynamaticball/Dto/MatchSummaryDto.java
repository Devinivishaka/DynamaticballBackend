package com.protonestiot.dynamaticball.Dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchSummaryDto {
    private String matchId;               // match.matchCode
    private String gameId;                // match.gameId
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String duration;              // e.g., "45:00" computed from startTime & endTime
    private TeamSummaryDto teamA;
    private TeamSummaryDto teamB;
    private String winner;                // "teamA" / "teamB" / "draw"
}



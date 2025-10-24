package com.protonestiot.dynamaticball.Dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamSummaryDto {
    private String name;                  // team.name
    private String color;                 // team.color
    private int score;                    // from Match.scoreTeamA or scoreTeamB
    private List<PlayerSummaryDto> players;
}



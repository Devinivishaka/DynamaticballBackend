package com.protonestiot.dynamaticball.Dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamSummaryDto {
    private String name;
    private String color;
    private int score;
    private List<PlayerSummaryDto> players;
}



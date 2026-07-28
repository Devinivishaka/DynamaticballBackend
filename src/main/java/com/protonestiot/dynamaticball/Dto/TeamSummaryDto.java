package com.protonestiot.dynamaticball.Dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamSummaryDto {
    private Long teamId;
    private String name;
    private String color;
    private int score;
    private int playerCount;
}



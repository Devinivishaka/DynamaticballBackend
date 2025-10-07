package com.protonestiot.dynamaticball.Dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRequestDto {
    private String teamAName;
    private String teamBName;
    private String teamAColor;
    private String teamBColor;
    private Integer gameTime;
    private Integer playersPerTeam;
    private Integer maxHoldTime;
    private Integer penaltyTime;
    private String selectedBall;
    private String goal1;
    private String goal2;
}


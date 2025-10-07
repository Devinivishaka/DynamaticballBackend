package com.protonestiot.dynamaticball.Dto;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameResponseDto {
    private Long id;
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
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

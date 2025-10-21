package com.protonestiot.dynamaticball.Dto;

import com.protonestiot.dynamaticball.Entity.MatchEvent;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MatchStatusDto {
    private String matchCode;
    private String gameId;
    private String status;
    private Integer scoreTeamA;
    private Integer scoreTeamB;
    private String startTime;
    private String endTime;
    private Long teamAId;
    private Long teamBId;
    private List<MatchEvent> recentEvents;
}
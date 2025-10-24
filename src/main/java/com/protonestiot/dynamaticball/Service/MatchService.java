package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.*;

public interface MatchService {
    GenericResponseDto startMatch(StartMatchRequestDto dto);
    GenericResponseDto changeMatchStatus(MatchActionRequestDto dto, String action);
    GenericResponseDto addScore(ScoreRequestDto dto);
    GenericResponseDto addBallEvent(BallEventRequestDto dto);
    GenericResponseDto halftime(MatchActionRequestDto dto);
    MatchStatusDto getMatchStatus(String matchCode);
    GenericMatchSummaryResponse getMatchSummary(String matchCode);
    GenericMatchTimelineResponse getMatchTimeline(String matchCode);
    GenericPlayerStatsResponse getPlayerStatistics(String matchCode);


}

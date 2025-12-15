package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/start")
    public ResponseEntity<GenericResponseDto> startMatch(@RequestBody StartMatchRequestDto dto) {
        return ResponseEntity.ok(matchService.startMatch(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/pause")
    public ResponseEntity<GenericResponseDto> pause(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "pause"));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/resume")
    public ResponseEntity<GenericResponseDto> resume(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "resume"));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/penalty")
    public ResponseEntity<GenericResponseDto> penalty(@RequestBody PenaltyRequestDto dto) {
        return ResponseEntity.ok(matchService.addPenaltyEvent(dto));
    }


    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/stop")
    public ResponseEntity<GenericResponseDto> stop(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "stop"));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/score")
    public ResponseEntity<GenericResponseDto> addScore(@RequestBody ScoreRequestDto dto) {
        return ResponseEntity.ok(matchService.addScore(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/ball-event")
    public ResponseEntity<GenericResponseDto> ballEvent(@RequestBody BallEventRequestDto dto) {
        return ResponseEntity.ok(matchService.addBallEvent(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/halftime")
    public ResponseEntity<GenericResponseDto> halftime(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.halftime(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/summary")
    public ResponseEntity<GenericMatchSummaryResponse> getMatchSummary(@PathVariable String matchId) {
        return ResponseEntity.ok(matchService.getMatchSummary(matchId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/timeline")
    public GenericMatchTimelineResponse getMatchTimeline(@PathVariable("matchId") String matchId) {
        return matchService.getMatchTimeline(matchId);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/player-stats")
    public GenericPlayerStatsResponse getPlayerStatistics(@PathVariable("matchId") String matchId) {
        return matchService.getPlayerStatistics(matchId);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/recording/start")
    public ResponseEntity<GenericResponseDto> startRecording(@RequestBody StartRecordingRequestDto dto) {
        return ResponseEntity.ok(matchService.startRecording(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/recording/{gameId}/streams")
    public ResponseEntity<StreamsResponseDto> getStreams(@PathVariable String gameId) {
        return ResponseEntity.ok(matchService.getStreams(gameId));
    }
}


package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Match operations: start, pause, resume, penalties, scores, events, recordings, summaries")
public class MatchController {

    private final MatchService matchService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/start")
    @Operation(summary = "Start a match", description = "Starts a match with provided setup and players")
    public ResponseEntity<GenericResponseDto> startMatch(@RequestBody StartMatchRequestDto dto) {
        return ResponseEntity.ok(matchService.startMatch(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/pause")
    @Operation(summary = "Pause a match", description = "Pauses the running match")
    public ResponseEntity<GenericResponseDto> pause(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "pause"));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/resume")
    @Operation(summary = "Resume a match", description = "Resumes a paused match")
    public ResponseEntity<GenericResponseDto> resume(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "resume"));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/penalty")
    @Operation(summary = "Add a penalty", description = "Adds a penalty event to the match")
    public ResponseEntity<GenericResponseDto> penalty(@RequestBody PenaltyRequestDto dto) {
        return ResponseEntity.ok(matchService.addPenaltyEvent(dto));
    }


    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/stop")
    @Operation(summary = "Stop a match", description = "Stops the match and ends recording if active")
    public ResponseEntity<GenericResponseDto> stop(@RequestBody MatchActionRequestDto dto) {
        matchService.stopRecording(dto);
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "stop"));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/score")
    @Operation(summary = "Add score", description = "Adds a score to the match for a team or player")
    public ResponseEntity<GenericResponseDto> addScore(@RequestBody ScoreRequestDto dto) {
        return ResponseEntity.ok(matchService.addScore(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/ball-event")
    @Operation(summary = "Add ball event", description = "Adds a ball event (e.g., possession change) to the match")
    public ResponseEntity<GenericResponseDto> ballEvent(@RequestBody BallEventRequestDto dto) {
        return ResponseEntity.ok(matchService.addBallEvent(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/halftime")
    @Operation(summary = "Halftime", description = "Marks halftime in the match")
    public ResponseEntity<GenericResponseDto> halftime(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.halftime(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/summary")
    @Operation(summary = "Get match summary", description = "Retrieves match summary by matchId")
    public ResponseEntity<GenericMatchSummaryResponse> getMatchSummary(@PathVariable String matchId) {
        return ResponseEntity.ok(matchService.getMatchSummary(matchId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/timeline")
    @Operation(summary = "Get match timeline", description = "Retrieves match timeline events by matchId")
    public GenericMatchTimelineResponse getMatchTimeline(@PathVariable("matchId") String matchId) {
        return matchService.getMatchTimeline(matchId);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/player-stats")
    @Operation(summary = "Get player stats", description = "Retrieves aggregated player statistics by matchId")
    public GenericPlayerStatsResponse getPlayerStatistics(@PathVariable("matchId") String matchId) {
        return matchService.getPlayerStatistics(matchId);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/recording/start")
    @Operation(summary = "Start recordings", description = "Starts camera recordings for a game setup")
    public ResponseEntity<GenericResponseDto> startRecording(@RequestBody StartRecordingRequestDto dto) {
        return ResponseEntity.ok(matchService.startRecording(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/recording/{gameId}/streams")
    @Operation(summary = "Get streams", description = "Gets active streams for a game by gameId")
    public ResponseEntity<StreamsResponseDto> getStreams(@PathVariable @Parameter(description = "Game ID") String gameId) {
        return ResponseEntity.ok(matchService.getStreams(gameId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/videos")
    @Operation(summary = "Get match videos", description = "Retrieves recorded video footages for the match")
    public ResponseEntity<VideosResponseDto> getVideos(@PathVariable String matchId) {
        return ResponseEntity.ok(matchService.getVideos(matchId));
    }
}

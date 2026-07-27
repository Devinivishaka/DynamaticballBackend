package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Service.GameHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.protonestiot.dynamaticball.Service.MatchService;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = "Game History", description = "Game history retrieval APIs")
public class GameHistoryController {

    private final GameHistoryService gameHistoryService;
    private final MatchService matchService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/history")
    @Operation(summary = "Get game history", description = "Retrieves paginated game history with filters")
    public ResponseEntity<GameHistoryResponseDto> getGameHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String gameId
    ) {
        return ResponseEntity.ok(gameHistoryService.getGameHistory(page, limit, dateFrom, dateTo, teamId, gameId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{gameId}/summary")
    @Operation(summary = "Get match summary", description = "Retrieves match summary by gameId")
    public ResponseEntity<GenericMatchSummaryResponse> getMatchSummary(@PathVariable String gameId) {
        return ResponseEntity.ok(matchService.getMatchSummary(gameId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{gameId}/timeline")
    @Operation(summary = "Get match timeline", description = "Retrieves match timeline events by gameId")
    public GenericMatchTimelineResponse getMatchTimeline(@PathVariable("gameId") String gameId) {
        return matchService.getMatchTimeline(gameId);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{gameId}/player-stats")
    @Operation(summary = "Get player stats", description = "Retrieves aggregated player statistics by gameId")
    public GenericPlayerStatsResponse getPlayerStatistics(@PathVariable("gameId") String gameId) {
        return matchService.getPlayerStatistics(gameId);
    }
}

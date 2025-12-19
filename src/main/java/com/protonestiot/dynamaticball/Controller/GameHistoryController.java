package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.GameHistoryResponseDto;
import com.protonestiot.dynamaticball.Service.GameHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = "Game History", description = "Game history retrieval APIs")
public class GameHistoryController {

    private final GameHistoryService gameHistoryService;

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
}

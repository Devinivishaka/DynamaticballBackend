package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Service.MatchService;
import com.protonestiot.dynamaticball.Service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final MatchService matchService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/videos")
    @Operation(summary = "Get match videos", description = "Retrieves recorded video footages for the match")
    public VideosResponseDto getMatchVideos(@PathVariable String matchId) {
        return videoService.getVideos(matchId);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping("/{matchId}/stats")
    @Operation(summary = "Upsert match stats", description = "Stores a stats snapshot for the given matchId")
    public GenericResponseDto upsertMatchStats(@PathVariable String matchId, @RequestBody MatchStatsUpsertRequestDto dto) {
        return matchService.upsertMatchStats(matchId, dto);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/stats")
    @Operation(summary = "Get latest match stats", description = "Retrieves the most recent stats snapshot for the given matchId")
    public MatchStatsResponseDto getLatestMatchStats(@PathVariable String matchId) {
        return matchService.getLatestMatchStats(matchId);
    }
}

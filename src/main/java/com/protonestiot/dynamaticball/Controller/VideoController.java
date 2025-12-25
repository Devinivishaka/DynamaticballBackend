package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.VideosResponseDto;
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

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @GetMapping("/{matchId}/videos")
    @Operation(summary = "Get match videos", description = "Retrieves recorded video footages for the match")
    public VideosResponseDto getMatchVideos(@PathVariable String matchId) {
        return videoService.getVideos(matchId);
    }
}

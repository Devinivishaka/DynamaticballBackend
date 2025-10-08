package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/start")
    public ResponseEntity<GenericResponseDto> startMatch(@RequestBody StartMatchRequestDto dto) {
        return ResponseEntity.ok(matchService.startMatch(dto));
    }

    @PostMapping("/pause")
    public ResponseEntity<GenericResponseDto> pause(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "pause"));
    }

    @PostMapping("/resume")
    public ResponseEntity<GenericResponseDto> resume(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "resume"));
    }

    @PostMapping("/stop")
    public ResponseEntity<GenericResponseDto> stop(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.changeMatchStatus(dto, "stop"));
    }

    @PostMapping("/score")
    public ResponseEntity<GenericResponseDto> addScore(@RequestBody ScoreRequestDto dto) {
        return ResponseEntity.ok(matchService.addScore(dto));
    }

    @PostMapping("/ball-event")
    public ResponseEntity<GenericResponseDto> ballEvent(@RequestBody BallEventRequestDto dto) {
        return ResponseEntity.ok(matchService.addBallEvent(dto));
    }

    @PostMapping("/halftime")
    public ResponseEntity<GenericResponseDto> halftime(@RequestBody MatchActionRequestDto dto) {
        return ResponseEntity.ok(matchService.halftime(dto));
    }
}

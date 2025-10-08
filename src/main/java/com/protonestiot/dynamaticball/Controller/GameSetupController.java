package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.GameSetupRequestDto;
import com.protonestiot.dynamaticball.Dto.GameSetupResponseDto;
import com.protonestiot.dynamaticball.Service.GameSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GameSetupController {

    private final GameSetupService gameSetupService;

    @PostMapping("/game-setup")
    public ResponseEntity<GameSetupResponseDto> saveGameSetup(@RequestBody GameSetupRequestDto request) {
        return ResponseEntity.ok(gameSetupService.saveGameSetup(request));
    }
}

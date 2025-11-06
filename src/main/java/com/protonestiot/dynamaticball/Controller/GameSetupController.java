package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.GameSetupRequestDto;
import com.protonestiot.dynamaticball.Service.GameSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/game-setup")
public class GameSetupController {

    @Autowired
    private GameSetupService gameSetupService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> saveGameSetup(@RequestBody GameSetupRequestDto requestDto) {
        try {
            return ResponseEntity.ok(gameSetupService.saveGameSetup(requestDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "status", 400,
                    "error", "Game Setup Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "status", 500,
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup"
            ));
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{gameSetupId}")
    public ResponseEntity<?> updateGameSetup(@PathVariable String gameSetupId,
                                             @RequestBody GameSetupRequestDto requestDto) {
        try {
            return ResponseEntity.ok(gameSetupService.updateGameSetup(gameSetupId, requestDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "status", 400,
                    "error", "Game Setup Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup/" + gameSetupId
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "status", 500,
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup/" + gameSetupId
            ));
        }
    }



}

package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.GameSetupRequestDto;
import com.protonestiot.dynamaticball.Service.GameSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Service.PlayerService;

@RestController
@RequestMapping("/api/v1/game-setup")
@Tag(name = "Game Setup", description = "Game setup creation and update APIs")
public class GameSetupController {

    @Autowired
    private GameSetupService gameSetupService;

    @Autowired
    private PlayerService playerService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping
    @Operation(summary = "Create game setup", description = "Creates a new game setup")
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

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PutMapping("/{gameSetupId}")
    @Operation(summary = "Update game setup", description = "Updates an existing game setup by ID")
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

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PutMapping("/{gameSetupId}/players/{id}")
    @Operation(summary = "Update player", description = "Updates a player in a specific game setup")
    public ResponseEntity<?> updatePlayerInGameSetup(@PathVariable String gameSetupId,
                                                     @PathVariable Long id,
                                                     @RequestBody PlayerRequestDto requestDto) {
        try {
            return ResponseEntity.ok(playerService.updatePlayerInGameSetup(gameSetupId, id, requestDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "status", 400,
                    "error", "Player Update Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup/" + gameSetupId + "/players/" + id
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "status", 500,
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup/" + gameSetupId + "/players/" + id
            ));
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @DeleteMapping("/{gameSetupId}/players/{id}")
    @Operation(summary = "Delete player", description = "Deletes a player from a specific game setup")
    public ResponseEntity<?> deletePlayerInGameSetup(@PathVariable String gameSetupId,
                                                     @PathVariable Long id) {
        try {
            playerService.deletePlayerInGameSetup(gameSetupId, id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Player deleted successfully."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "status", 400,
                    "error", "Player Deletion Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup/" + gameSetupId + "/players/" + id
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "status", 500,
                    "error", "Internal Server Error",
                    "message", e.getMessage(),
                    "path", "/api/v1/game-setup/" + gameSetupId + "/players/" + id
            ));
        }
    }
}

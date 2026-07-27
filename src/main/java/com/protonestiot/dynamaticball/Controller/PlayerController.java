package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;
import com.protonestiot.dynamaticball.Service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
@Tag(name = "Players", description = "Player management APIs")
public class PlayerController {

    private final PlayerService playerService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping
    @Operation(summary = "Add player", description = "Adds a new player")
    public ResponseEntity<Player> addPlayer(@Valid @RequestBody PlayerRequestDto dto) {
        return ResponseEntity.ok(playerService.addPlayer(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update player", description = "Updates player details by ID")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerRequestDto dto) {
        return ResponseEntity.ok(playerService.updatePlayerById(id, dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete player", description = "Deletes player by ID")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayerById(id);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Player deleted successfully"));
    }
}

package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;
import com.protonestiot.dynamaticball.Service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PostMapping
    public ResponseEntity<Player> addPlayer(@Valid @RequestBody PlayerRequestDto dto) {
        return ResponseEntity.ok(playerService.addPlayer(dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerRequestDto dto) {
        return ResponseEntity.ok(playerService.updatePlayerById(id, dto));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','REFEREE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayerById(id);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Player deleted successfully"));
    }
}

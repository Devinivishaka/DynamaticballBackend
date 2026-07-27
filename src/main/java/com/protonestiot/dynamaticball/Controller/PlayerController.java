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
    @PostMapping("/{gameSetupId}/{teamKey}")
    @Operation(summary = "Add player", description = "Adds a new player to a specific game setup and team")
    public ResponseEntity<Player> addPlayer(@PathVariable String gameSetupId,
                                            @PathVariable String teamKey,
                                            @Valid @RequestBody PlayerRequestDto dto) {
        return ResponseEntity.ok(playerService.addPlayer(gameSetupId, teamKey, dto));
    }

}

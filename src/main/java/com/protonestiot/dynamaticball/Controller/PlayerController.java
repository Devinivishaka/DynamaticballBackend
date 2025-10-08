package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;
import com.protonestiot.dynamaticball.Service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<Player> addPlayer(@RequestBody PlayerRequestDto dto) {
        return ResponseEntity.ok(playerService.addPlayer(dto));
    }

    @PutMapping("/{playerCode}")
    public ResponseEntity<Player> updatePlayer(@PathVariable String playerCode, @RequestBody PlayerRequestDto dto) {
        return ResponseEntity.ok(playerService.updatePlayer(playerCode, dto));
    }

    @DeleteMapping("/{playerCode}")
    public ResponseEntity<?> deletePlayer(@PathVariable String playerCode) {
        playerService.deletePlayer(playerCode);
        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Player deleted successfully"));
    }
}

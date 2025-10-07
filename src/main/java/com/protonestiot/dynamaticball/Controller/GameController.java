package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(@RequestBody GameRequestDto dto) {
        return ResponseEntity.ok(gameService.createGame(dto));
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDto> getGameById(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDto> updateGame(@PathVariable Long id, @RequestBody GameRequestDto dto) {
        return ResponseEntity.ok(gameService.updateGame(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}

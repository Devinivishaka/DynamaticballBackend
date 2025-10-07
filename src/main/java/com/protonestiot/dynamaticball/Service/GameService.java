package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.GameRequestDto;
import com.protonestiot.dynamaticball.Dto.GameResponseDto;
import java.util.List;

public interface GameService {
    GameResponseDto createGame(GameRequestDto dto);
    List<GameResponseDto> getAllGames();
    GameResponseDto getGameById(Long id);
    GameResponseDto updateGame(Long id, GameRequestDto dto);
    void deleteGame(Long id);
}

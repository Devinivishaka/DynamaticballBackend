package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Entity.Game;
import com.protonestiot.dynamaticball.Mapper.GameMapper;
import com.protonestiot.dynamaticball.Repository.GameRepository;
import com.protonestiot.dynamaticball.Service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;

    @Override
    public GameResponseDto createGame(GameRequestDto dto) {
        Game game = gameMapper.toEntity(dto);
        return gameMapper.toDto(gameRepository.save(game));
    }

    @Override
    public List<GameResponseDto> getAllGames() {
        return gameRepository.findAll()
                .stream()
                .map(gameMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public GameResponseDto getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        return gameMapper.toDto(game);
    }

    @Override
    public GameResponseDto updateGame(Long id, GameRequestDto dto) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        game.setTeamAName(dto.getTeamAName());
        game.setTeamBName(dto.getTeamBName());
        game.setTeamAColor(dto.getTeamAColor());
        game.setTeamBColor(dto.getTeamBColor());
        game.setGameTime(dto.getGameTime());
        game.setPlayersPerTeam(dto.getPlayersPerTeam());
        game.setMaxHoldTime(dto.getMaxHoldTime());
        game.setPenaltyTime(dto.getPenaltyTime());
        game.setSelectedBall(dto.getSelectedBall());
        game.setGoal1(dto.getGoal1());
        game.setGoal2(dto.getGoal2());
        return gameMapper.toDto(gameRepository.save(game));
    }

    @Override
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }
}

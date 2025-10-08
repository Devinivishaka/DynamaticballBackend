package com.protonestiot.dynamaticball.Service.Impl;

import com.protonestiot.dynamaticball.Dto.GameSetupRequestDto;
import com.protonestiot.dynamaticball.Dto.GameSetupResponseDto;
import com.protonestiot.dynamaticball.Entity.GameSetup;
import com.protonestiot.dynamaticball.Mapper.GameSetupMapper;
import com.protonestiot.dynamaticball.Repository.GameSetupRepository;
import com.protonestiot.dynamaticball.Service.GameSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameSetupServiceImpl implements GameSetupService {

    private final GameSetupRepository gameSetupRepository;

    @Override
    @Transactional
    public GameSetupResponseDto saveGameSetup(GameSetupRequestDto requestDto) {
        GameSetup entity = GameSetupMapper.toEntity(requestDto);
        // teams and players are cascade saved
        GameSetup saved = gameSetupRepository.save(entity);
        return GameSetupResponseDto.builder()
                .success(true)
                .gameSetupId(saved.getSetupCode())
                .message("Game setup saved successfully")
                .build();
    }
}

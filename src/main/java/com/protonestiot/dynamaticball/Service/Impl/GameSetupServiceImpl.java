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

        //  Generate sequential code
        long count = gameSetupRepository.countBySetupCodeIsNotNull() + 1;
        String setupCode = String.format("GS_%03d", count);
        entity.setSetupCode(setupCode);

        GameSetup saved = gameSetupRepository.save(entity);

        // Fetch team IDs if teams exist
        Long teamAId = null;
        Long teamBId = null;

        if (saved.getTeams() != null && saved.getTeams().size() >= 2) {
            teamAId = saved.getTeams().get(0).getId();
            teamBId = saved.getTeams().get(1).getId();
        }

        return GameSetupResponseDto.builder()
                .success(true)
                .gameSetupId(saved.getSetupCode())
                .teamAId(teamAId)
                .teamBId(teamBId)
                .message("Game setup saved successfully")
                .build();
    }

}

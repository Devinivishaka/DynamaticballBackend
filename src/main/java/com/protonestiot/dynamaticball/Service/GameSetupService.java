package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.GameSetupRequestDto;
import com.protonestiot.dynamaticball.Dto.GameSetupResponseDto;

public interface GameSetupService {
    GameSetupResponseDto saveGameSetup(GameSetupRequestDto requestDto);
    GameSetupResponseDto updateGameSetup(String gameSetupId, GameSetupRequestDto requestDto);
}

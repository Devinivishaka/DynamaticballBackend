package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;

public interface PlayerService {
    com.protonestiot.dynamaticball.Dto.PlayerResponseDto addPlayer(String gameSetupId, PlayerRequestDto dto);
    com.protonestiot.dynamaticball.Dto.PlayerResponseDto updatePlayerInGameSetup(String gameSetupId, Long id, PlayerRequestDto dto);
    void deletePlayerInGameSetup(String gameSetupId, Long id);
}

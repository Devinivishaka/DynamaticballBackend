package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;

public interface PlayerService {
    Player addPlayer(PlayerRequestDto dto);
    Player updatePlayerById(Long id, PlayerRequestDto dto);
    void deletePlayerById(Long id);
}

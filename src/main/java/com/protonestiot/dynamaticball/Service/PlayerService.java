package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;

public interface PlayerService {
    Player addPlayer(PlayerRequestDto dto);
    Player updatePlayer(String playerCode, PlayerRequestDto dto);
    void deletePlayer(String playerCode);
}

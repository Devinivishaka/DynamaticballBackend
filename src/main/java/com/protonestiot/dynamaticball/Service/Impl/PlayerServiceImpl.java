package com.protonestiot.dynamaticball.Service.Impl;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;
import com.protonestiot.dynamaticball.Entity.Team;
import com.protonestiot.dynamaticball.Repository.PlayerRepository;
import com.protonestiot.dynamaticball.Repository.TeamRepository;
import com.protonestiot.dynamaticball.Service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public Player addPlayer(PlayerRequestDto dto) {
        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Player p = Player.builder()
                .playerCode(dto.getPlayerId())
                .belt(dto.getBelt())
                .rightWristband(dto.getRightWristband())
                .leftWristband(dto.getLeftWristband())
                .camera(dto.getCamera())
                .team(team)
                .build();
        return playerRepository.save(p);
    }

    @Override
    @Transactional
    public Player updatePlayer(String playerCode, PlayerRequestDto dto) {
        Player p = playerRepository.findByPlayerCode(playerCode)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        if (dto.getBelt() != null) p.setBelt(dto.getBelt());
        if (dto.getRightWristband() != null) p.setRightWristband(dto.getRightWristband());
        if (dto.getLeftWristband() != null) p.setLeftWristband(dto.getLeftWristband());
        if (dto.getCamera() != null) p.setCamera(dto.getCamera());
        if (dto.getTeamId() != null) {
            Team t = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            p.setTeam(t);
        }
        return playerRepository.save(p);
    }

    @Override
    @Transactional
    public void deletePlayer(String playerCode) {
        playerRepository.deleteByPlayerCode(playerCode);
    }
}

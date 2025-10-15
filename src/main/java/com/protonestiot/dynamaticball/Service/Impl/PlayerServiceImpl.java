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
        if (dto == null) {
            throw new RuntimeException("Player data cannot be null");
        }

        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found: " + dto.getTeamId()));

        Player player = Player.builder()
                .playerCode(dto.getPlayerId())
                .belt(dto.getBelt())
                .rightWristband(dto.getRightWristband())
                .leftWristband(dto.getLeftWristband())
                .camera(dto.getCamera())
                .team(team)
                .build();

        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player updatePlayerById(Long id, PlayerRequestDto dto) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + id));

        if (dto.getBelt() != null) player.setBelt(dto.getBelt());
        if (dto.getRightWristband() != null) player.setRightWristband(dto.getRightWristband());
        if (dto.getLeftWristband() != null) player.setLeftWristband(dto.getLeftWristband());
        if (dto.getCamera() != null) player.setCamera(dto.getCamera());

        if (dto.getTeamId() != null) {
            Team team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found for ID: " + dto.getTeamId()));
            player.setTeam(team);
        }

        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public void deletePlayerById(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new RuntimeException("Player not found with ID: " + id);
        }
        playerRepository.deleteById(id);
    }
}

package com.protonestiot.dynamaticball.Service.Impl;

import com.protonestiot.dynamaticball.Dto.PlayerRequestDto;
import com.protonestiot.dynamaticball.Entity.Player;
import com.protonestiot.dynamaticball.Entity.Team;
import com.protonestiot.dynamaticball.Repository.PlayerRepository;
import com.protonestiot.dynamaticball.Repository.TeamRepository;
import com.protonestiot.dynamaticball.Service.PlayerService;
import com.protonestiot.dynamaticball.Handler.MatchWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final MatchWebSocketHandler matchWebSocketHandler;

    @Override
    @Transactional
    public Player addPlayer(PlayerRequestDto dto) {
        if (dto == null) throw new RuntimeException("Player data cannot be null");

        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found: " + dto.getTeamId()));

        int playersPerTeam = team.getGameSetup().getPlayersPerTeam();
        long currentCount = playerRepository.countByTeam(team);

        if (currentCount >= playersPerTeam) {
            throw new RuntimeException(
                    "Cannot add more players. Team '" + team.getTeamKey() +
                            "' already has the maximum of " + playersPerTeam + " players."
            );
        }

        Player player = Player.builder()
                .playerCode(dto.getPlayerId())
                .belt(dto.getBelt())
                .rightWristband(dto.getRightWristband())
                .leftWristband(dto.getLeftWristband())
                .camera(dto.getCamera())
                .team(team)
                .build();

        Player saved = playerRepository.save(player);

        broadcastPlayerChange("add", saved);

        return saved;
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

        Player saved = playerRepository.save(player);

        broadcastPlayerChange("update", saved);

        return saved;
    }

    @Override
    @Transactional
    public void deletePlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + id));

        playerRepository.deleteById(id);

        broadcastPlayerChange("delete", player);
    }

    // --- Helper method to broadcast WebSocket messages ---
    private void broadcastPlayerChange(String action, Player player) {
        String json = "{ " +
                "\"event\": \"" + action + "\"," +
                "\"playerId\": \"" + player.getPlayerCode() + "\"," +
                "\"teamId\": \"" + player.getTeam().getId() + "\"," +
                "\"belt\": \"" + player.getBelt() + "\"," +
                "\"rightWristband\": \"" + player.getRightWristband() + "\"," +
                "\"leftWristband\": \"" + player.getLeftWristband() + "\"," +
                "\"camera\": \"" + player.getCamera() + "\"" +
                " }";

        matchWebSocketHandler.broadcast(json);
    }
}

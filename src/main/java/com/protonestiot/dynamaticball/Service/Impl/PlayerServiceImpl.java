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
    public com.protonestiot.dynamaticball.Dto.PlayerResponseDto addPlayer(String gameSetupId, PlayerRequestDto dto) {
        if (dto == null) throw new RuntimeException("Player data cannot be null.");
        if (gameSetupId == null || gameSetupId.trim().isEmpty()) throw new RuntimeException("Game setup ID cannot be null.");
        if (dto.getTeamId() == null) throw new RuntimeException("Team ID cannot be null.");

        // Validate required fields
        if (dto.getPlayerId() == null || dto.getPlayerId().trim().isEmpty())
            throw new RuntimeException("Player ID cannot be null or empty.");
        if (dto.getBelt() == null || dto.getBelt().trim().isEmpty())
            throw new RuntimeException("Belt value cannot be null or empty.");
        if (dto.getRightWristband() == null || dto.getRightWristband().trim().isEmpty())
            throw new RuntimeException("Right wristband value cannot be null or empty.");
        if (dto.getLeftWristband() == null || dto.getLeftWristband().trim().isEmpty())
            throw new RuntimeException("Left wristband value cannot be null or empty.");
        if (dto.getCamera() == null || dto.getCamera().trim().isEmpty())
            throw new RuntimeException("Camera value cannot be null or empty.");

        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found for ID: " + dto.getTeamId()));

        if (!team.getGameSetup().getSetupCode().equals(gameSetupId)) {
            throw new RuntimeException("Team does not belong to game setup: " + gameSetupId);
        }


        Long dbGameSetupId = team.getGameSetup().getId();
        boolean existsInGameSetup = playerRepository.existsByPlayerCodeAndTeam_GameSetup_Id(dto.getPlayerId(), dbGameSetupId);
        if (existsInGameSetup) {
            throw new RuntimeException("Player code '" + dto.getPlayerId() + "' already exists in this match.");
        }


        int playersPerTeam = team.getGameSetup().getPlayersPerTeam();
        long currentCount = playerRepository.countByTeam(team);
        if (currentCount >= playersPerTeam) {
            throw new RuntimeException("Cannot add more players. Team '" + team.getTeamKey() +
                    "' already has the maximum of " + playersPerTeam + " players.");
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
        
        return com.protonestiot.dynamaticball.Dto.PlayerResponseDto.builder()
                .success(true)
                .message("Player added successfully.")
                .playerId(saved.getPlayerCode())
                .belt(saved.getBelt())
                .rightWristband(saved.getRightWristband())
                .leftWristband(saved.getLeftWristband())
                .camera(saved.getCamera())
                .teamId(saved.getTeam().getId())
                .build();
    }

    @Override
    @Transactional
    public void deletePlayerInGameSetup(String gameSetupId, Long id) {
        if (gameSetupId == null || gameSetupId.trim().isEmpty()) throw new IllegalArgumentException("Game setup ID cannot be null.");
        if (id == null) throw new IllegalArgumentException("Player ID cannot be null for deletion.");

        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + id));

        if (!player.getTeam().getGameSetup().getSetupCode().equals(gameSetupId)) {
            throw new IllegalArgumentException("Player with ID: " + id + " does not belong to game setup: " + gameSetupId);
        }

        playerRepository.delete(player);
        broadcastPlayerChange("delete", player);
    }

    @Override
    @Transactional
    public com.protonestiot.dynamaticball.Dto.PlayerResponseDto updatePlayerInGameSetup(String gameSetupId, Long id, PlayerRequestDto dto) {
        if (gameSetupId == null || gameSetupId.trim().isEmpty()) throw new IllegalArgumentException("Game setup ID cannot be null.");
        if (id == null) throw new IllegalArgumentException("Player ID cannot be null.");
        if (dto == null) throw new IllegalArgumentException("Update data cannot be null.");

        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + id));

        if (!player.getTeam().getGameSetup().getSetupCode().equals(gameSetupId)) {
            throw new IllegalArgumentException("Player with ID: " + id + " does not belong to game setup: " + gameSetupId);
        }

        Long currentGameSetupId = player.getTeam().getGameSetup().getId();

        if (dto.getPlayerId() != null && !dto.getPlayerId().trim().isEmpty()) {
            boolean existsInGameSetup = playerRepository.existsByPlayerCodeAndTeam_GameSetup_Id(dto.getPlayerId(), currentGameSetupId);

            if (existsInGameSetup && !dto.getPlayerId().equals(player.getPlayerCode())) {
                throw new IllegalArgumentException("Player code '" + dto.getPlayerId() + "' already exists in this match.");
            }

            player.setPlayerCode(dto.getPlayerId());
        }

        if (dto.getBelt() != null && !dto.getBelt().trim().isEmpty())
            player.setBelt(dto.getBelt());
        if (dto.getRightWristband() != null && !dto.getRightWristband().trim().isEmpty())
            player.setRightWristband(dto.getRightWristband());
        if (dto.getLeftWristband() != null && !dto.getLeftWristband().trim().isEmpty())
            player.setLeftWristband(dto.getLeftWristband());
        if (dto.getCamera() != null && !dto.getCamera().trim().isEmpty())
            player.setCamera(dto.getCamera());

        if (dto.getTeamId() != null && !dto.getTeamId().equals(player.getTeam().getId())) {
            Team newTeam = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + dto.getTeamId()));

            int playersPerTeam = newTeam.getGameSetup().getPlayersPerTeam();
            long currentCount = playerRepository.countByTeam(newTeam);

            if (currentCount >= playersPerTeam) {
                throw new IllegalArgumentException("Cannot move player. Team '" + newTeam.getTeamKey() +
                        "' already has the maximum of " + playersPerTeam + " players.");
            }

            Long newGameSetupId = newTeam.getGameSetup().getId();
            boolean existsInNewGameSetup = playerRepository.existsByPlayerCodeAndTeam_GameSetup_Id(player.getPlayerCode(), newGameSetupId);

            if (existsInNewGameSetup && !newTeam.equals(player.getTeam())) {
                throw new IllegalArgumentException("Player code '" + player.getPlayerCode() + "' already exists in this match.");
            }

            player.setTeam(newTeam);
        }

        Player updated = playerRepository.save(player);
        broadcastPlayerChange("update", updated);

        return com.protonestiot.dynamaticball.Dto.PlayerResponseDto.builder()
                .success(true)
                .message("Player updated successfully.")
                .playerId(updated.getPlayerCode())
                .belt(updated.getBelt())
                .rightWristband(updated.getRightWristband())
                .leftWristband(updated.getLeftWristband())
                .camera(updated.getCamera())
                .teamId(updated.getTeam().getId())
                .build();
    }

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

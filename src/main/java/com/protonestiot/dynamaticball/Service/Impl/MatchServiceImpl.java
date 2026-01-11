package com.protonestiot.dynamaticball.Service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Entity.*;
import com.protonestiot.dynamaticball.Handler.MatchWebSocketHandler;
import com.protonestiot.dynamaticball.Repository.*;
import com.protonestiot.dynamaticball.Service.MatchService;
import com.protonestiot.dynamaticball.Service.MediaServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final GameSetupRepository gameSetupRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final TeamRepository teamRepository;
    private final MatchWebSocketHandler matchWebSocketHandler;
    private final MediaServiceClient mediaServiceClient;
    private final MatchStatsSnapshotRepository matchStatsSnapshotRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp);
        } catch (Exception e) {

            try {
                Instant instant = Instant.parse(timestamp);
                return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            } catch (Exception ignored) {
                try {
                    DateTimeFormatter formatter;
                    if (timestamp.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    } else if (timestamp.matches("^\\d{2}:\\d{2}$")) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm");
                    } else {
                        throw new RuntimeException("Invalid time format. Expected 'HH:mm' or 'HH:mm:ss'.");
                    }

                    LocalTime time = LocalTime.parse(timestamp, formatter);
                    return LocalDateTime.of(LocalDate.now(), time);
                } catch (Exception ex) {
                    throw new RuntimeException("Invalid timestamp format. Expected 'HH:mm:ss', 'HH:mm', ISO local datetime, or ISO instant (e.g., 2026-01-03T12:30:00.000Z)");
                }
            }
        }
    }

    private MatchWsEventDto buildMatchWsEvent(Match match, String eventType, LocalDateTime ts, Team team, Player player) {
        MatchWsEventDto.MatchData matchData = MatchWsEventDto.MatchData.builder()
                .status(match.getStatus())
                .scoreTeamA(match.getScoreTeamA())
                .scoreTeamB(match.getScoreTeamB())
                .build();

        MatchWsEventDto.TeamData teamData = team != null ? MatchWsEventDto.TeamData.builder()
                .teamId(team.getId())
                .teamKey(team.getTeamKey())
                .score(team.getTotalScore())
                .build() : null;

        MatchWsEventDto.PlayerData playerData = player != null ? MatchWsEventDto.PlayerData.builder()
                .playerId(player.getPlayerCode())
                .playerScore(player.getScore())
                .penaltySeconds(player.getPenaltyTime() != null ? Integer.parseInt(player.getPenaltyTime()) : null)
                .build() : null;

        return MatchWsEventDto.builder()
                .event(eventType)
                .matchCode(match.getMatchCode())
                .timestamp(ts.toString())
                .match(matchData)
                .team(teamData)
                .player(playerData)
                .build();
    }


    @Override
    @Transactional
    public GenericResponseDto startMatch(StartMatchRequestDto dto) {
        GameSetup gs = gameSetupRepository.findBySetupCode(dto.getGameSetupId())
                .orElseThrow(() -> new RuntimeException("Game setup not found: " + dto.getGameSetupId()));

        long count = matchRepository.count() + 1;
        String matchCode = String.format("M_%03d", count);

        LocalDateTime start = parseTimestamp(dto.getStartTime());

        Long teamAId = null;
        Long teamBId = null;
        for (Team t : gs.getTeams()) {
            if ("teamA".equals(t.getTeamKey())) teamAId = t.getId();
            if ("teamB".equals(t.getTeamKey())) teamBId = t.getId();
        }

        Match match = Match.builder()
                .matchCode(matchCode)
                .gameSetup(gs)
                .startTime(start)
                .status("ACTIVE")
                .scoreTeamA(0)
                .scoreTeamB(0)
                .teamAId(teamAId)
                .teamBId(teamBId)
                .build();

        matchRepository.save(match);

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType("match_start")
                .description("Match started")
                .timestamp(start)
                .build();
        matchEventRepository.save(ev);

        MatchWsEventDto ws = buildMatchWsEvent(match, "match_start", start, null, null);
        matchWebSocketHandler.broadcast(ws);


        return GenericResponseDto.builder()
                .success(true)
                .message("Match started successfully")
                .id(match.getMatchCode())
                .build();
    }

    @Override
    @Transactional
    public GenericResponseDto resumeMatch(MatchActionRequestDto dto) {
        return changeMatchStatus(dto, "resume");
    }

    @Override
    @Transactional
    public GenericResponseDto stopMatch(MatchActionRequestDto dto) {
        return changeMatchStatus(dto, "stop");
    }

    @Override
    @Transactional
    public GenericResponseDto changeMatchStatus(MatchActionRequestDto dto, String action) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found: " + dto.getMatchId()));

        LocalDateTime ts = parseTimestamp(dto.getTimestamp());

        switch (action.toLowerCase()) {
            case "pause" -> {
                if (!"ACTIVE".equals(match.getStatus()))
                    throw new RuntimeException("Cannot pause match. Current status: " + match.getStatus());
                match.setStatus("PAUSED");
            }
            case "resume" -> {
                if (!"PAUSED".equals(match.getStatus()))
                    throw new RuntimeException("Cannot resume match. Current status: " + match.getStatus());
                match.setStatus("ACTIVE");
            }
            case "stop" -> {
                if ("ENDED".equals(match.getStatus()))
                    throw new RuntimeException("Match already ended");
                match.setStatus("ENDED");
                match.setEndTime(ts);
                if (match.getGameId() == null) {
                    long count = matchRepository.countByGameIdIsNotNull() + 1;
                    match.setGameId(String.format("G_%03d", count));
                }
            }
            default -> throw new RuntimeException("Unknown action: " + action);
        }

        matchRepository.save(match);

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType(action.toLowerCase())
                .description(action.substring(0, 1).toUpperCase() + action.substring(1))
                .timestamp(ts)
                .build();
        matchEventRepository.save(ev);

        MatchWsEventDto ws = buildMatchWsEvent(match, action.toLowerCase(), ts, null, null);
        matchWebSocketHandler.broadcast(ws);

        return GenericResponseDto.builder()
                .success(true)
                .message(action.substring(0, 1).toUpperCase() + action.substring(1) + " applied successfully")
                .id(match.getMatchCode())
                .build();
    }

    @Override
    @Transactional
    public GenericResponseDto addScore(ScoreRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found: " + dto.getMatchId()));

        LocalDateTime ts = parseTimestamp(dto.getTimestamp());

        Player player = match.getGameSetup().getTeams().stream()
                .flatMap(team -> team.getPlayers().stream())
                .filter(p -> p.getPlayerCode().equals(dto.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found: " + dto.getPlayerId()));

        player.setScore(player.getScore() + dto.getScore());
        Team team = player.getTeam();
        team.setTotalScore(team.getPlayers().stream().mapToInt(Player::getScore).sum());

        if (match.getTeamAId().equals(team.getId())) {
            match.setScoreTeamA(team.getTotalScore());
        } else if (match.getTeamBId().equals(team.getId())) {
            match.setScoreTeamB(team.getTotalScore());
        }

        matchRepository.save(match);
        teamRepository.save(team);

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType("goal")
                .playerCode(player.getPlayerCode())
                .teamKey(String.valueOf(team.getId()))
                .description("Score +" + dto.getScore() + " by player " + player.getPlayerCode())
                .timestamp(ts)
                .build();
        matchEventRepository.save(ev);

        MatchWsEventDto ws = buildMatchWsEvent(match, "goal", ts, team, player);
        matchWebSocketHandler.broadcast(ws);

        return GenericResponseDto.builder()
                .success(true)
                .message("Score updated for player " + player.getPlayerCode())
                .id(match.getMatchCode())
                .build();
    }


    @Override
    @Transactional
    public GenericResponseDto addBallEvent(BallEventRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        LocalDateTime ts = parseTimestamp(dto.getTimestamp());

        Player player = null;
        Team team = null;
        if (dto.getPlayerId() != null) {
            player = match.getGameSetup().getTeams().stream()
                    .flatMap(t -> t.getPlayers().stream())
                    .filter(p -> p.getPlayerCode().equals(dto.getPlayerId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Player not found: " + dto.getPlayerId()));
            team = player.getTeam();
        }

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType(dto.getEventType())
                .playerCode(dto.getPlayerId())
                .description(dto.getEventType() + (dto.getPlayerId() != null ? " by " + dto.getPlayerId() : ""))
                .timestamp(ts)
                .build();
        matchEventRepository.save(ev);

        MatchWsEventDto ws = buildMatchWsEvent(match, dto.getEventType(), ts, team, player);
        matchWebSocketHandler.broadcast(ws);

        return GenericResponseDto.builder()
                .success(true)
                .message("Event recorded")
                .id(match.getMatchCode())
                .build();
    }
    @Override
    @Transactional
    public GenericResponseDto addPenaltyEvent(PenaltyRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found: " + dto.getMatchId()));

        if (dto.getPlayerId() == null || dto.getPlayerId().isEmpty()) {
            throw new RuntimeException("playerId is required for penalty event");
        }

        LocalDateTime ts = parseTimestamp(dto.getTimestamp());

        Player player = match.getGameSetup().getTeams().stream()
                .flatMap(t -> t.getPlayers().stream())
                .filter(p -> p.getPlayerCode().equals(dto.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found: " + dto.getPlayerId()));

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType("penalty")
                .playerCode(player.getPlayerCode())
                .description("Player " + player.getPlayerCode() +
                        (dto.getPenaltyTime() != null ? " received a " + dto.getPenaltyTime() + " s penalty" : " received a penalty") +
                        (dto.getTimestamp() != null ? " during game time " + dto.getTimestamp() : ""))
                .timestamp(ts)
                .build();

        matchEventRepository.save(ev);

        MatchWsEventDto ws = MatchWsEventDto.builder()
                .event("penalty")
                .matchCode(match.getMatchCode())
                .timestamp(ts.toString())
                .player(MatchWsEventDto.PlayerData.builder()
                        .playerId(player.getPlayerCode())
                        .penaltySeconds(
                                dto.getPenaltyTime() != null
                                        ? Integer.parseInt(dto.getPenaltyTime())
                                        : null
                        )

                        .build())
                .build();

        matchWebSocketHandler.broadcast(ws);


        return GenericResponseDto.builder()
                .success(true)
                .message("Penalty recorded successfully")
                .id(match.getMatchCode())
                .build();
    }

    @Override
    @Transactional
    public GenericResponseDto halftime(MatchActionRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        LocalDateTime ts = parseTimestamp(dto.getTimestamp());

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType("halftime")
                .description("Halftime")
                .timestamp(ts)
                .build();
        matchEventRepository.save(ev);

        MatchWsEventDto ws = buildMatchWsEvent(match, "halftime", ts, null, null);
        matchWebSocketHandler.broadcast(ws);

        return GenericResponseDto.builder()
                .success(true)
                .message("Halftime recorded")
                .id(match.getMatchCode())
                .build();
    }



    public MatchStatusDto getMatchStatus(String matchCode) {
        Match match = matchRepository.findByMatchCode(matchCode)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchCode));

        List<MatchEvent> recentEvents = matchEventRepository.findTop5ByMatchOrderByTimestampDesc(match);

        return MatchStatusDto.builder()
                .matchCode(match.getMatchCode())
                .gameId(match.getGameId())
                .status(match.getStatus())
                .scoreTeamA(match.getScoreTeamA())
                .scoreTeamB(match.getScoreTeamB())
                .startTime(match.getStartTime() != null ? match.getStartTime().toString() : null)
                .endTime(match.getEndTime() != null ? match.getEndTime().toString() : null)
                .teamAId(match.getTeamAId())
                .teamBId(match.getTeamBId())
                .recentEvents(recentEvents)
                .build();
    }



    @Override
    @Transactional(readOnly = true)
    public GenericMatchSummaryResponse getMatchSummary(String matchCode) {
        Match match = matchRepository.findByMatchCode(matchCode)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchCode));

        GameSetup gs = match.getGameSetup();


        Team teamA = gs.getTeams().stream()
                .filter(t -> t.getId().equals(match.getTeamAId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("TeamA not found"));

        TeamSummaryDto teamADto = TeamSummaryDto.builder()
                .name(teamA.getName())
                .color(teamA.getColor())
                .score(match.getScoreTeamA())
                .players(teamA.getPlayers().stream()
                        .map(p -> PlayerSummaryDto.builder()
                                .playerId(p.getPlayerCode())
                                .maxSpeed(0)
                                .penaltyTime("0:00")
                                .ballPossessingTime("0:00")
                                .ballControlInitiations(0)
                                .build())
                        .collect(Collectors.toList()))
                .build();


        Team teamB = gs.getTeams().stream()
                .filter(t -> t.getId().equals(match.getTeamBId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("TeamB not found"));

        TeamSummaryDto teamBDto = TeamSummaryDto.builder()
                .name(teamB.getName())
                .color(teamB.getColor())
                .score(match.getScoreTeamB())
                .players(teamB.getPlayers().stream()
                        .map(p -> PlayerSummaryDto.builder()
                                .playerId(p.getPlayerCode())
                                .maxSpeed(0)
                                .penaltyTime("0:00")
                                .ballPossessingTime("0:00")
                                .ballControlInitiations(0)
                                .build())
                        .collect(Collectors.toList()))
                .build();


        String duration = "00:00";
        if (match.getStartTime() != null && match.getEndTime() != null) {
            Duration dur = Duration.between(match.getStartTime(), match.getEndTime());
            long minutes = dur.toMinutes();
            long seconds = dur.minusMinutes(minutes).getSeconds();
            duration = String.format("%02d:%02d", minutes, seconds);
        }


        String winner;
        if (match.getScoreTeamA() > match.getScoreTeamB()) winner = "teamA";
        else if (match.getScoreTeamB() > match.getScoreTeamA()) winner = "teamB";
        else winner = "draw";

        MatchSummaryDto summary = MatchSummaryDto.builder()
                .matchId(match.getMatchCode())
                .gameId(match.getGameId())
                .startTime(match.getStartTime())
                .endTime(match.getEndTime())
                .duration(duration)
                .teamA(teamADto)
                .teamB(teamBDto)
                .winner(winner)
                .build();

        return GenericMatchSummaryResponse.builder()
                .success(true)
                .data(summary)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GenericMatchTimelineResponse getMatchTimeline(String matchCode) {
        Match match = matchRepository.findByMatchCode(matchCode)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchCode));

        LocalDateTime matchStart = match.getStartTime();

        List<MatchTimelineEventDto> events = match.getEvents().stream()
                .sorted((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()))
                .map(e -> {
                    String time = "00:00";
                    if (matchStart != null && e.getTimestamp() != null) {
                        Duration dur = Duration.between(matchStart, e.getTimestamp());
                        long minutes = dur.toMinutes();
                        long seconds = dur.minusMinutes(minutes).getSeconds();
                        time = String.format("%02d:%02d", minutes, seconds);
                    }

                    return MatchTimelineEventDto.builder()
                            .timestamp(e.getTimestamp() != null ? e.getTimestamp().toString() : null)
                            .time(time)
                            .eventType(e.getEventType())
                            .description(e.getDescription())
                            .playerId(e.getPlayerCode())
                            .teamId(e.getTeamKey())
                            .build();
                })
                .toList(); // Java 16+, else use Collectors.toList()

        MatchTimelineDto timeline = MatchTimelineDto.builder()
                .events(events)
                .build();

        return GenericMatchTimelineResponse.builder()
                .success(true)
                .data(timeline)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GenericPlayerStatsResponse getPlayerStatistics(String matchCode) {
        Match match = matchRepository.findByMatchCode(matchCode)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchCode));

        GameSetup gs = match.getGameSetup();


        Team teamA = gs.getTeams().stream()
                .filter(t -> t.getId().equals(match.getTeamAId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("TeamA not found"));

        List<PlayerStatsDto> teamAStats = teamA.getPlayers().stream()
                .map(p -> PlayerStatsDto.builder()
                        .playerId(p.getPlayerCode())
                        .maxSpeed("0") // placeholder if not available
                        .penaltyTime("0:00")
                        .ballPossessingTime("0:00")
                        .ballControlInitiations("0")
                        .build())
                .toList();


        Team teamB = gs.getTeams().stream()
                .filter(t -> t.getId().equals(match.getTeamBId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("TeamB not found"));

        List<PlayerStatsDto> teamBStats = teamB.getPlayers().stream()
                .map(p -> PlayerStatsDto.builder()
                        .playerId(p.getPlayerCode())
                        .maxSpeed("0")
                        .penaltyTime("0:00")
                        .ballPossessingTime("0:00")
                        .ballControlInitiations("0")
                        .build())
                .toList();

        PlayerStatsResponseDto statsResponse = PlayerStatsResponseDto.builder()
                .teamA(teamAStats)
                .teamB(teamBStats)
                .build();

        return GenericPlayerStatsResponse.builder()
                .success(true)
                .data(statsResponse)
                .build();
    }

    @Override
    @Transactional
    public GenericResponseDto startRecording(StartRecordingRequestDto dto) {
        GameSetup gameSetup = gameSetupRepository.findBySetupCode(dto.getGameSetupId())
                .orElseThrow(() -> new RuntimeException("Game setup not found for gameSetupId: " + dto.getGameSetupId()));

        List<Team> teams = gameSetup.getTeams();
        if (teams == null || teams.isEmpty()) {
            throw new RuntimeException("No teams found for gameSetup: " + gameSetup.getSetupCode());
        }

        List<MediaServiceClient.CameraAssignmentReq> cameras = new ArrayList<>();

        for (Team team : teams) {
            List<Player> players = team.getPlayers();
            if (players != null && !players.isEmpty()) {
                for (Player player : players) {
                    if (player.getCamera() != null && !player.getCamera().isEmpty()) {
                        MediaServiceClient.CameraAssignmentReq cameraReq = new MediaServiceClient.CameraAssignmentReq();
                        cameraReq.setCameraId(player.getCamera());
                        cameraReq.setPlayerId(player.getPlayerCode());
                        cameraReq.setTeam(team.getTeamKey());
                        cameras.add(cameraReq);
                    }
                }
            }
        }

        MediaServiceClient.StartRequest startRequest = new MediaServiceClient.StartRequest();
        startRequest.setMatchId(dto.getGameSetupId());
        startRequest.setCameras(cameras);

        try {
            MediaServiceClient.StartResponse response = mediaServiceClient.startMatch(startRequest);

            return GenericResponseDto.builder()
                    .success(true)
                    .message("Recording started successfully. Status: " + response.getStatus())
                    .id(dto.getGameSetupId())
                    .build();
        } catch (MediaServiceClient.MediaServiceException e) {
            throw new RuntimeException("Failed to start recording: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void stopRecording(MatchActionRequestDto dto) {

        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found for matchCode: " + dto.getMatchId()));

        GameSetup gameSetup = match.getGameSetup();
        String gameId = gameSetup.getSetupCode();

        try {
            MediaServiceClient.StopRequest stopRequest = new MediaServiceClient.StopRequest();
            stopRequest.setMatchId(gameId);

            MediaServiceClient.StopResponse response = mediaServiceClient.stopMatch(stopRequest);

            GenericResponseDto.builder()
                    .success(true)
                    .message("Recording stopped successfully. Status: " + response.getStatus())
                    .id(gameId)
                    .build();
        } catch (MediaServiceClient.MediaServiceException e) {
            throw new RuntimeException("Failed to stop recording: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StreamsResponseDto getStreams(String gameId) {

        GameSetup gameSetup;
        if (gameId.startsWith("M")) {
            Match match = matchRepository.findByMatchCode(gameId)
                    .orElseThrow(() -> new RuntimeException("Match not found for id: " + gameId));

            gameSetup = match.getGameSetup();
        } else {
            gameSetup = gameSetupRepository.findBySetupCode(gameId)
                    .orElseThrow(() -> new RuntimeException("Game setup not found for gameSetupId: " + gameId));
        }

        try {
            MediaServiceClient.StreamsResponse streamsResponse = mediaServiceClient.getStreams(gameSetup.getSetupCode());

            List<StreamsResponseDto.StreamItemDto> streamItems = streamsResponse.getStreams() != null
                    ? streamsResponse.getStreams().stream()
                    .map(item -> StreamsResponseDto.StreamItemDto.builder()
                            .cameraId(item.getCameraId())
                            .playerId(item.getPlayerId())
                            .team(item.getTeam())
                            .manifest(item.getManifest())
                            .token(item.getToken())
                            .build())
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            return StreamsResponseDto.builder()
                    .success(true)
                    .message("Streams retrieved successfully")
                    .matchId(streamsResponse.getMatchId())
                    .streams(streamItems)
                    .build();
        } catch (MediaServiceClient.MediaServiceException e) {
            throw new RuntimeException("Failed to get streams: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public GenericResponseDto upsertMatchStats(String matchId, MatchStatsUpsertRequestDto dto) {
        Match match = matchRepository.findByMatchCode(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));

        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        if (dto.getMatchId() != null && !dto.getMatchId().isBlank() && !matchId.equals(dto.getMatchId())) {
            throw new RuntimeException("matchId in path does not match matchId in body");
        }

        LocalDateTime ts = parseTimestamp(dto.getTimestamp());

        String playerJson;
        String teamJson;
        try {
            playerJson = objectMapper.writeValueAsString(dto.getPlayerStats());
            teamJson = objectMapper.writeValueAsString(dto.getTeamStats());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize stats payload to JSON", e);
        }

        MatchStatsSnapshot snapshot = MatchStatsSnapshot.builder()
                .match(match)
                .timestamp(ts)
                .playerStatsJson(playerJson)
                .teamStatsJson(teamJson)
                .build();

        matchStatsSnapshotRepository.save(snapshot);

        return GenericResponseDto.builder()
                .success(true)
                .message("Match stats stored")
                .id(matchId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MatchStatsResponseDto getLatestMatchStats(String matchId) {
        MatchStatsSnapshot snapshot = matchStatsSnapshotRepository
                .findTopByMatch_MatchCodeOrderByTimestampDesc(matchId)
                .orElseThrow(() -> new RuntimeException("No stats found for match: " + matchId));

        Map<String, MatchStatsUpsertRequestDto.PlayerStatsItemDto> playerStats;
        Map<String, MatchStatsUpsertRequestDto.TeamStatsItemDto> teamStats;

        try {
            playerStats = objectMapper.readValue(
                    snapshot.getPlayerStatsJson(),
                    new TypeReference<Map<String, MatchStatsUpsertRequestDto.PlayerStatsItemDto>>() {}
            );
            teamStats = objectMapper.readValue(
                    snapshot.getTeamStatsJson(),
                    new TypeReference<Map<String, MatchStatsUpsertRequestDto.TeamStatsItemDto>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize stored stats JSON", e);
        }

        return MatchStatsResponseDto.builder()
                .success(true)
                .message("OK")
                .data(
                        MatchStatsResponseDto.Data.builder()
                                .matchId(matchId)
                                .timestamp(snapshot.getTimestamp().toString())
                                .playerStats(playerStats)
                                .teamStats(teamStats)
                                .build()
                )
                .build();
    }
}

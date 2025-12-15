package com.protonestiot.dynamaticball.Service.Impl;

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

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp);
        } catch (Exception e) {
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
                throw new RuntimeException("Invalid timestamp format. Expected 'HH:mm:ss', 'HH:mm', or ISO format (e.g., 2025-11-06T10:30:00)");
            }
        }
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

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";

        matchWebSocketHandler.broadcast(json);


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

        String currentStatus = match.getStatus();

        switch (action.toLowerCase()) {
            case "pause":
                if (!"ACTIVE".equals(currentStatus)) {
                    throw new RuntimeException("Cannot pause match. Current status: " + currentStatus);
                }
                match.setStatus("PAUSED");
                break;

            case "resume":
                if (!"PAUSED".equals(currentStatus)) {
                    throw new RuntimeException("Cannot resume match. Current status: " + currentStatus);
                }
                match.setStatus("ACTIVE");
                break;

            case "stop":
                if ("ENDED".equals(currentStatus)) {
                    throw new RuntimeException("Match is already ended");
                }
                match.setStatus("ENDED");
                match.setEndTime(ts);

                if (match.getGameId() == null) {
                    long count = matchRepository.countByGameIdIsNotNull() + 1;
                    match.setGameId(String.format("G_%03d", count));
                }
                match = matchRepository.save(match);
                break;

            default:
                throw new RuntimeException("Unknown action: " + action);
        }

        match = matchRepository.save(match);

        String eventType = switch (action.toLowerCase()) {
            case "pause" -> "match_paused";
            case "resume" -> "match_resumed";
            case "stop" -> "match_end";
            default -> action;
        };

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType(eventType)
                .description(action.substring(0, 1).toUpperCase() + action.substring(1) + " match")
                .timestamp(ts)
                .build();
        matchEventRepository.save(ev);

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";

        matchWebSocketHandler.broadcast(json);

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


        int newPlayerScore = player.getScore() + dto.getScore();
        player.setScore(newPlayerScore);


        Team team = player.getTeam();
        int teamScore = team.getPlayers().stream().mapToInt(Player::getScore).sum();
        team.setTotalScore(teamScore);

        if (match.getTeamAId().equals(team.getId())) {
            match.setScoreTeamA(teamScore);
        } else if (match.getTeamBId().equals(team.getId())) {
            match.setScoreTeamB(teamScore);
        } else {
            throw new RuntimeException("Player's team does not match any match teams");
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

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";

        matchWebSocketHandler.broadcast(json);

        return GenericResponseDto.builder()
                .success(true)
                .message("Score updated for player " + player.getPlayerCode() + " in team " + team.getId())
                .id(match.getMatchCode())
                .build();
    }


    @Override
    @Transactional
    public GenericResponseDto addBallEvent(BallEventRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        LocalDateTime ts = parseTimestamp(dto.getTimestamp());

        if ("possession_change".equals(dto.getEventType())) {
            if (dto.getPlayerId() == null || dto.getPlayerId().isEmpty()) {
                throw new RuntimeException("playerId is required for possession_change event");
            }
        }

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType(dto.getEventType())
                .playerCode(dto.getPlayerId())
                .description(dto.getEventType() + (dto.getPlayerId() != null && !dto.getPlayerId().isEmpty() ? " by " + dto.getPlayerId() : ""))
                .timestamp(ts)
                .build();

        matchEventRepository.save(ev);

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";

        matchWebSocketHandler.broadcast(json);

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

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";
        matchWebSocketHandler.broadcast(json);

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

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";

        matchWebSocketHandler.broadcast(json);


        return GenericResponseDto.builder().success(true).message("Halftime recorded").id(match.getMatchCode()).build();
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
}

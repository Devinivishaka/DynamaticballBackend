package com.protonestiot.dynamaticball.Service.Impl;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Entity.*;
import com.protonestiot.dynamaticball.Handler.MatchWebSocketHandler;
import com.protonestiot.dynamaticball.Repository.*;
import com.protonestiot.dynamaticball.Service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeParseException;
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

    private LocalDateTime parseOrNow(String iso) {
        if (iso == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(iso);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }



    @Override
    @Transactional
    public GenericResponseDto startMatch(StartMatchRequestDto dto) {
        GameSetup gs = gameSetupRepository.findBySetupCode(dto.getGameSetupId())
                .orElseThrow(() -> new RuntimeException("Game setup not found: " + dto.getGameSetupId()));

        long count = matchRepository.count() + 1;
        String matchCode = String.format("M_%03d", count);

        LocalDateTime start = parseOrNow(dto.getStartTime());

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
    public GenericResponseDto changeMatchStatus(MatchActionRequestDto dto, String action) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found: " + dto.getMatchId()));
        LocalDateTime ts = parseOrNow(dto.getTimestamp());

        MatchEvent ev = null;

        switch (action.toLowerCase()) {
            case "pause":
                match.setStatus("PAUSED");
                matchRepository.save(match);
                ev = MatchEvent.builder()
                        .match(match)
                        .eventType("pause")
                        .description("Match paused")
                        .timestamp(ts)
                        .build();
                matchEventRepository.save(ev);
                break;
            case "resume":
                match.setStatus("ACTIVE");
                matchRepository.save(match);
                ev = MatchEvent.builder()
                        .match(match)
                        .eventType("resume")
                        .description("Match resumed")
                        .timestamp(ts)
                        .build();
                matchEventRepository.save(ev);
                break;
            case "stop":
                match.setStatus("ENDED");
                match.setEndTime(ts);
                match = matchRepository.save(match);

                if (match.getGameId() == null) {
                    long count = matchRepository.countByGameIdIsNotNull() + 1;
                    match.setGameId(String.format("G_%03d", count));
                    match = matchRepository.save(match);
                }

                ev = MatchEvent.builder()
                        .match(match)
                        .eventType("match_end")
                        .description("Match ended")
                        .timestamp(ts)
                        .build();
                matchEventRepository.save(ev);
                break;
            default:
                throw new RuntimeException("Unknown action: " + action);
        }

        if (ev != null) {
            String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                    "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                    "\"description\": \"" + ev.getDescription() + "\" }";

            matchWebSocketHandler.broadcast(json);

        }

        return GenericResponseDto.builder().success(true).message("Action applied: " + action).id(match.getMatchCode()).build();
    }

    @Override
    @Transactional
    public GenericResponseDto addScore(ScoreRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found: " + dto.getMatchId()));
        LocalDateTime ts = parseOrNow(dto.getTimestamp());

        Long teamId = Long.parseLong(dto.getTeamId());

        if (match.getTeamAId().equals(teamId)) {
            match.setScoreTeamA(match.getScoreTeamA() + dto.getScore());
        } else if (match.getTeamBId().equals(teamId)) {
            match.setScoreTeamB(match.getScoreTeamB() + dto.getScore());
        } else {
            throw new RuntimeException("Invalid teamId: " + teamId + ". Must match teamAId or teamBId in this match.");
        }

        matchRepository.save(match);

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType("goal")
                .playerCode(dto.getPlayerId())
                .teamKey(String.valueOf(teamId))
                .description("Score +" + dto.getScore() + " to teamId " + teamId)
                .timestamp(ts)
                .build();

        matchEventRepository.save(ev);

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";

        matchWebSocketHandler.broadcast(json);


        return GenericResponseDto.builder()
                .success(true)
                .message("Score updated for teamId " + teamId)
                .id(match.getMatchCode())
                .build();
    }

    @Override
    @Transactional
    public GenericResponseDto addBallEvent(BallEventRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        LocalDateTime ts = parseOrNow(dto.getTimestamp());

        MatchEvent ev = MatchEvent.builder()
                .match(match)
                .eventType(dto.getEventType())
                .playerCode(dto.getPlayerId())
                .description(dto.getEventType() + (dto.getPlayerId() != null ? " by " + dto.getPlayerId() : ""))
                .timestamp(ts)
                .build();
        matchEventRepository.save(ev);

        String json = "{ \"event\": \"" + ev.getEventType() + "\", " +
                "\"matchCode\": \"" + match.getMatchCode() + "\", " +
                "\"description\": \"" + ev.getDescription() + "\" }";

        matchWebSocketHandler.broadcast(json);


        return GenericResponseDto.builder().success(true).message("Event recorded").id(match.getMatchCode()).build();
    }

    @Override
    @Transactional
    public GenericResponseDto halftime(MatchActionRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        LocalDateTime ts = parseOrNow(dto.getTimestamp());

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

    // New method to get latest match status
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

        // Map team A
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
                                .maxSpeed(0)             // fill if available
                                .penaltyTime("0:00")     // fill if available
                                .ballPossessingTime("0:00") // fill if available
                                .ballControlInitiations(0) // fill if available
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // Map team B
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

        // Compute duration
        String duration = "00:00";
        if (match.getStartTime() != null && match.getEndTime() != null) {
            Duration dur = Duration.between(match.getStartTime(), match.getEndTime());
            long minutes = dur.toMinutes();
            long seconds = dur.minusMinutes(minutes).getSeconds();
            duration = String.format("%02d:%02d", minutes, seconds);
        }

        // Determine winner
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

        // Map Team A players
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

        // Map Team B players
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


}
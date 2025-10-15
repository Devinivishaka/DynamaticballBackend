package com.protonestiot.dynamaticball.Service.Impl;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Entity.*;
import com.protonestiot.dynamaticball.Repository.*;
import com.protonestiot.dynamaticball.Service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final GameSetupRepository gameSetupRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final TeamRepository teamRepository;

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

        //  Sequential match code
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

        switch (action.toLowerCase()) {
            case "pause":
                match.setStatus("PAUSED");
                matchRepository.save(match);
                matchEventRepository.save(MatchEvent.builder()
                        .match(match)
                        .eventType("pause")
                        .description("Match paused")
                        .timestamp(ts)
                        .build());
                break;
            case "resume":
                match.setStatus("ACTIVE");
                matchRepository.save(match);
                matchEventRepository.save(MatchEvent.builder()
                        .match(match)
                        .eventType("resume")
                        .description("Match resumed")
                        .timestamp(ts)
                        .build());
                break;

            case "stop":
                match.setStatus("ENDED");
                match.setEndTime(ts);
                match = matchRepository.save(match);

                if (match.getGameId() == null) {
                    long count = matchRepository.countByGameIdIsNotNull() + 1; // ✅ count existing games
                    match.setGameId(String.format("G_%03d", count));
                    match = matchRepository.save(match);
                }

                matchEventRepository.save(MatchEvent.builder()
                        .match(match)
                        .eventType("match_end")
                        .description("Match ended")
                        .timestamp(ts)
                        .build());
                break;


            default:
                throw new RuntimeException("Unknown action: " + action);
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

        return GenericResponseDto.builder().success(true).message("Event recorded").id(match.getMatchCode()).build();
    }

    @Override
    @Transactional
    public GenericResponseDto halftime(MatchActionRequestDto dto) {
        Match match = matchRepository.findByMatchCode(dto.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        LocalDateTime ts = parseOrNow(dto.getTimestamp());
        matchEventRepository.save(MatchEvent.builder()
                .match(match)
                .eventType("halftime")
                .description("Halftime")
                .timestamp(ts)
                .build());
        return GenericResponseDto.builder().success(true).message("Halftime recorded").id(match.getMatchCode()).build();
    }
}

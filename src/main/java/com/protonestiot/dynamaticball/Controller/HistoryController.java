package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Entity.Match;
import com.protonestiot.dynamaticball.Entity.MatchEvent;
import com.protonestiot.dynamaticball.Repository.MatchEventRepository;
import com.protonestiot.dynamaticball.Repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HistoryController {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;

    // GET /api/v1/games/history?page=1&limit=10
    @GetMapping("/games/history")
    public ResponseEntity<?> getGameHistory(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(Math.max(page-1,0), limit, Sort.by("startTime").descending());
        Page<Match> p = matchRepository.findAll(pageable);
        Map<String,Object> out = new HashMap<>();
        out.put("success", true);
        out.put("data", Map.of(
                "games", p.getContent().stream().map(m -> Map.of(
                        "matchId", m.getMatchCode(),
                        "gameSetupId", m.getGameSetup() != null ? m.getGameSetup().getSetupCode() : null,
                        "startTime", m.getStartTime(),
                        "endTime", m.getEndTime(),
                        "duration", m.getStartTime()!=null && m.getEndTime()!=null ? Duration.between(m.getStartTime(), m.getEndTime()).toString() : null,
                        "status", m.getStatus(),
                        "scoreTeamA", m.getScoreTeamA(),
                        "scoreTeamB", m.getScoreTeamB()
                )).collect(Collectors.toList()),
                "pagination", Map.of(
                        "currentPage", p.getNumber()+1,
                        "totalPages", p.getTotalPages(),
                        "totalItems", p.getTotalElements(),
                        "itemsPerPage", p.getSize()
                )
        ));
        return ResponseEntity.ok(out);
    }

    // GET /api/v1/matches/{matchId}/timeline
    @GetMapping("/matches/{matchId}/timeline")
    public ResponseEntity<?> getTimeline(@PathVariable String matchId) {
        Match match = matchRepository.findByMatchCode(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        List<MatchEvent> events = matchEventRepository.findByMatchOrderByTimestampAsc(match);
        List<Map<String,Object>> evts = events.stream().map(e -> {
            Map<String,Object> m = new HashMap<>();
            m.put("timestamp", e.getTimestamp());
            long seconds = 0;
            if (match.getStartTime()!=null && e.getTimestamp()!=null) {
                seconds = Duration.between(match.getStartTime(), e.getTimestamp()).getSeconds();
            }
            long mm = seconds/60;
            long ss = seconds%60;
            m.put("time", String.format("%02d:%02d", mm, ss));
            m.put("eventType", e.getEventType());
            m.put("description", e.getDescription());
            m.put("playerId", e.getPlayerCode());
            m.put("teamId", e.getTeamKey());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("events", evts)));
    }

    // GET /api/v1/matches/{matchId}/summary (basic)
    @GetMapping("/matches/{matchId}/summary")
    public ResponseEntity<?> getSummary(@PathVariable String matchId) {
        Match match = matchRepository.findByMatchCode(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        Map<String,Object> out = new HashMap<>();
        out.put("matchId", match.getMatchCode());
        out.put("gameSetupId", match.getGameSetup()!=null ? match.getGameSetup().getSetupCode() : null);
        out.put("startTime", match.getStartTime());
        out.put("endTime", match.getEndTime());
        out.put("duration", match.getStartTime()!=null && match.getEndTime()!=null ? Duration.between(match.getStartTime(), match.getEndTime()).toString() : null);
        out.put("teams", Map.of(
                "teamA", Map.of("teamId", match.getTeamAId(), "score", match.getScoreTeamA()),
                "teamB", Map.of("teamId", match.getTeamBId(), "score", match.getScoreTeamB())
        ));
        out.put("winner", match.getScoreTeamA() > match.getScoreTeamB() ? "teamA" : (match.getScoreTeamB() > match.getScoreTeamA() ? "teamB" : "draw"));
        return ResponseEntity.ok(Map.of("success", true, "data", out));
    }
}

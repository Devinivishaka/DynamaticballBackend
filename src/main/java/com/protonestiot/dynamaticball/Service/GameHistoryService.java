package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Entity.Match;
import com.protonestiot.dynamaticball.Repository.MatchRepository;
import com.protonestiot.dynamaticball.Repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public GameHistoryResponseDto getGameHistory(int page, int limit, String date, String teamName, String gameId) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "endTime"));


        List<Specification<Match>> specs = new ArrayList<>();

        specs.add((root, query, cb) -> cb.equal(root.get("status"), "ENDED"));

        if (date != null && !date.isBlank()) {
            specs.add((root, query, cb) -> cb.like(root.get("endTime").as(String.class), "%" + date + "%"));
        }

        if (teamName != null && !teamName.isBlank()) {
            List<Long> matchingTeamIds = teamRepository.findByNameContainingIgnoreCase(teamName).stream().map(com.protonestiot.dynamaticball.Entity.Team::getId).toList();
            if (matchingTeamIds.isEmpty()) {
                specs.add((root, query, cb) -> cb.disjunction());
            } else {
                specs.add((root, query, cb) -> cb.or(
                        root.get("teamAId").in(matchingTeamIds),
                        root.get("teamBId").in(matchingTeamIds)
                ));
            }
        }

        if (gameId != null && !gameId.isBlank()) {
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("gameId")), "%" + gameId.toLowerCase() + "%"));
        }


        Specification<Match> finalSpec = Specification.allOf(specs);

        Page<Match> matchPage = matchRepository.findAll(finalSpec, pageable);

        List<GameHistoryItemDto> gameList = matchPage.getContent().stream().map(match -> {
            TeamDto teamA = TeamDto.builder()
                    .name(teamRepository.findById(match.getTeamAId())
                            .map(t -> t.getName() != null ? t.getName() : t.getTeamKey())
                            .orElse("Unknown"))
                    .score(match.getScoreTeamA())
                    .build();

            TeamDto teamB = TeamDto.builder()
                    .name(teamRepository.findById(match.getTeamBId())
                            .map(t -> t.getName() != null ? t.getName() : t.getTeamKey())
                            .orElse("Unknown"))
                    .score(match.getScoreTeamB())
                    .build();

            return GameHistoryItemDto.builder()
                    .gameId(match.getGameId())
                    .date(match.getEndTime() != null ? match.getEndTime().toLocalDate().toString() : null)
                    .teamA(teamA)
                    .teamB(teamB)
                    .duration(calculateDuration(match.getStartTime(), match.getEndTime()))
                    .status("completed")
                    .build();
        }).toList();

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(matchPage.getNumber() + 1)
                .totalPages(matchPage.getTotalPages())
                .totalItems(matchPage.getTotalElements())
                .itemsPerPage(matchPage.getSize())
                .build();

        GameHistoryDataDto data = GameHistoryDataDto.builder()
                .games(gameList)
                .pagination(pagination)
                .build();

        return GameHistoryResponseDto.builder()
                .success(true)
                .data(data)
                .build();
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "00:00:00";
        Duration d = Duration.between(start, end).abs();
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

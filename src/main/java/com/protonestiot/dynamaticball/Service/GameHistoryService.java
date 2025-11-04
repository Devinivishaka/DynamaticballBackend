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

    public GameHistoryResponseDto getGameHistory(int page, int limit, String dateFrom, String dateTo, Long teamId, String gameId) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "endTime"));


        List<Specification<Match>> specs = new ArrayList<>();

        specs.add((root, query, cb) -> cb.equal(root.get("status"), "ENDED"));

        if (dateFrom != null) {
            LocalDate from = LocalDate.parse(dateFrom);
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endTime"), from.atStartOfDay()));
        }

        if (dateTo != null) {
            LocalDate to = LocalDate.parse(dateTo);
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), to.plusDays(1).atStartOfDay()));
        }

        if (teamId != null) {
            specs.add((root, query, cb) -> cb.or(
                    cb.equal(root.get("teamAId"), teamId),
                    cb.equal(root.get("teamBId"), teamId)
            ));
        }

        if (gameId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("gameId"), gameId));
        }


        Specification<Match> finalSpec = Specification.allOf(specs);

        Page<Match> matchPage = matchRepository.findAll(finalSpec, pageable);

        List<GameHistoryItemDto> gameList = matchPage.getContent().stream().map(match -> {
            TeamDto teamA = TeamDto.builder()
                    .name(teamRepository.findById(match.getTeamAId())
                            .map(t -> t.getTeamKey())
                            .orElse("Unknown"))
                    .score(match.getScoreTeamA())
                    .build();

            TeamDto teamB = TeamDto.builder()
                    .name(teamRepository.findById(match.getTeamBId())
                            .map(t -> t.getTeamKey())
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
        if (start == null || end == null) return "00:00";
        Duration d = Duration.between(start, end);
        long minutes = d.toMinutes();
        long seconds = d.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }
}

package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Match;
import com.protonestiot.dynamaticball.Entity.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
    List<MatchEvent> findByMatchOrderByTimestampAsc(Match match);

    List<MatchEvent> findTop5ByMatchOrderByTimestampDesc(Match match);
}

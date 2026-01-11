package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.MatchStatsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchStatsSnapshotRepository extends JpaRepository<MatchStatsSnapshot, Long> {

    Optional<MatchStatsSnapshot> findTopByMatch_MatchCodeOrderByTimestampDesc(String matchCode);
}


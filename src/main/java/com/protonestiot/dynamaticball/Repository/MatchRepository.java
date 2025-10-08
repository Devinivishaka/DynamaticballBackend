package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchCode(String matchCode);
}

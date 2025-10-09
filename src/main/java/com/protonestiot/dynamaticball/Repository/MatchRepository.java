package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {
    Optional<Match> findByMatchCode(String matchCode);
    Optional<Match> findByGameId(String gameId); // useful if client searches by G_ id

    long countByGameIdIsNotNull();
}

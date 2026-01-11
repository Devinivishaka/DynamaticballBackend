package com.protonestiot.dynamaticball.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "match_stats_snapshots")
public class MatchStatsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnore
    private Match match;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * JSON string representation of player stats keyed by playerId.
     * Stored as LONGTEXT for MySQL compatibility.
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String playerStatsJson;

    /**
     * JSON string representation of team stats keyed by team key (teamA/teamB).
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String teamStatsJson;
}


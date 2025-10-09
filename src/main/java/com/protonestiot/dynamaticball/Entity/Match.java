package com.protonestiot.dynamaticball.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matchCode;

    // nullable, set when match is ended
    @Column(unique = true)
    private String gameId; // e.g. G_001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_setup_id")
    private GameSetup gameSetup;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // NOT_STARTED, ACTIVE, PAUSED, ENDED
    private String status;

    // snapshot scores
    private int scoreTeamA;
    private int scoreTeamB;

    // store team ids (optional snapshot)
    private Long teamAId;
    private Long teamBId;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchEvent> events = new ArrayList<>();

    // helper - generate a G_ formatted id based on DB id (only call after entity has an id)
    public void ensureGameId() {
        if (this.gameId == null && this.id != null) {
            this.gameId = String.format("G_%03d", this.id);
        }
    }
}

package com.protonestiot.dynamaticball.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @Column(unique = true)
    private String gameId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_setup_id")
    @JsonIgnore
    private GameSetup gameSetup;

    private LocalDateTime startTime;
    private LocalDateTime endTime;


    private String status;


    private int scoreTeamA;
    private int scoreTeamB;


    private Long teamAId;
    private Long teamBId;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchEvent> events = new ArrayList<>();


    public void ensureGameId() {
        if (this.gameId == null && this.id != null) {
            this.gameId = String.format("G_%03d", this.id);
        }
    }
}

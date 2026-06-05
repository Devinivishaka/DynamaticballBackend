package com.protonestiot.dynamaticball.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "game_setup")
public class GameSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String setupCode;

    private int gameTime;
    private int playersPerTeam;
    private int maxHoldTime;
    private int penaltyTime;

    private String selectedBall;
    private String goal1;
    private String goal2;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gameSetup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Team> teams = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

    }


    @PostPersist
    public void generateSetupCode() {
        if (this.setupCode == null && this.id != null) {
            this.setupCode = String.format("GS_%03d", this.id);
        }
    }

    // Add matches one-to-many relation (bidirectional)
    @OneToMany(mappedBy = "gameSetup", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<Match> matches = new ArrayList<>();

    // Helper to keep both sides in sync
    public void addMatch(Match match) {
        if (match == null) return;
        if (!this.matches.contains(match)) {
            this.matches.add(match);
            match.setGameSetup(this);
        }
    }

    public void removeMatch(Match match) {
        if (match == null) return;
        if (this.matches.remove(match)) {
            match.setGameSetup(null);
        }
    }
}

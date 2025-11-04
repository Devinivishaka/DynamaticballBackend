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
}

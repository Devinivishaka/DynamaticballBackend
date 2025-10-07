package com.protonestiot.dynamaticball.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameSetupId; // optional link to setup if used later

    private String teamAName;
    private String teamBName;
    private String teamAColor;
    private String teamBColor;

    private Integer gameTime; // in minutes
    private Integer playersPerTeam;
    private Integer maxHoldTime; // seconds
    private Integer penaltyTime; // seconds

    private String selectedBall;
    private String goal1;
    private String goal2;

    private String status; // e.g. "created", "in_progress", "completed"

    private LocalDateTime createdDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @PrePersist
    public void onCreate() {
        createdDate = LocalDateTime.now();
        status = "created";
    }
}

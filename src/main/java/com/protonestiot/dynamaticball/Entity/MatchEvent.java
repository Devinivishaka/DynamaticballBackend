package com.protonestiot.dynamaticball.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "match_events")
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    private String eventType; // match_start, goal, possession_change, halftime, pause, resume, stop
    private String description;

    private String playerCode; // optional
    private String teamKey; // "teamA" or "teamB" (optional)

    private LocalDateTime timestamp;
}

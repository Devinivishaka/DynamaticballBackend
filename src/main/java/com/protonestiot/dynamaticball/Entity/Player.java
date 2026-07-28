package com.protonestiot.dynamaticball.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String playerCode;

    private String belt;
    private String rightWristband;
    private String leftWristband;
    private String camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("players")
    private Team team;

    @Column(nullable = false)
    @Builder.Default
    private int score = 0; // added score

    @Column(name = "penalty_time")
    private String penaltyTime; // format: "0:00" or seconds as string

    @Column(name = "max_speed")
    private Double maxSpeed;

    @Column(name = "ball_possessing_time")
    private String ballPossessingTime;

    @Column(name = "ball_control_initiations")
    private Integer ballControlInitiations;

}

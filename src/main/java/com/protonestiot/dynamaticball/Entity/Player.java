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

    // internal PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // business player id from device/UI (e.g., "001")
    @Column(nullable = false)
    private String playerCode;

    private String belt;
    private String rightWristband;
    private String leftWristband;
    private String camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}

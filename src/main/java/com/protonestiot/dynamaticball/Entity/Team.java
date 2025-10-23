package com.protonestiot.dynamaticball.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "teamA" or "teamB" (business key)
    private String teamKey;

    private String name;
    private String color; // PURPLE/PINK
    private String goal; // GOAL_1 etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_setup_id")
    @JsonIgnore
    private GameSetup gameSetup;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Player> players = new ArrayList<>();
}

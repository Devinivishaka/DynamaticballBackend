package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MatchTimelineEventDto {
    private String timestamp; // ISO string
    private String time;      // "mm:ss" from match start
    private String eventType;
    private String description;
    private String playerId;  // optional
    private String teamId;    // optional, "teamA" or "teamB"
}

package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MatchTimelineEventDto {
    private String timestamp;
    private String gameTime;
    private String eventType;
    private String description;
    private String playerId;
    private String teamId;
}

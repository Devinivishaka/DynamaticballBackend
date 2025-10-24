package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MatchTimelineDto {
    private List<MatchTimelineEventDto> events;
}

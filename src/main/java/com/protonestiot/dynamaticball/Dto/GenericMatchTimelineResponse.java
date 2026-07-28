package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericMatchTimelineResponse {
    private boolean success;
    private String message;
    private MatchTimelineDto data;
}

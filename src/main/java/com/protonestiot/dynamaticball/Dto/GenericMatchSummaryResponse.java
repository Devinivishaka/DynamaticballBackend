package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericMatchSummaryResponse {
    private boolean success;
    private MatchSummaryDto data;
}


package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GenericPlayerStatsResponse {
    private boolean success;
    private PlayerStatsResponseDto data;
}

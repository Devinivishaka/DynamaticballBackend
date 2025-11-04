package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartMatchRequestDto {
    private String gameSetupId;
    private String startTime;
}

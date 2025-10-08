package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartMatchRequestDto {
    private String gameSetupId; // setupCode like GS_xxx
    private String startTime; // optional ISO string
}

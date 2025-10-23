package com.protonestiot.dynamaticball.Dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameHistoryDataDto {
    private List<GameHistoryItemDto> games;
    private PaginationDto pagination;
}

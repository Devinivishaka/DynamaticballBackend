package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationDto {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int itemsPerPage;
}

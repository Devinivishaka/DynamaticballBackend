package com.protonestiot.dynamaticball.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamsResponseDto {
    private boolean success;
    private String message;
    private String matchId;
    private List<StreamItemDto> streams;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StreamItemDto {
        private String cameraId;
        private String playerId;
        private String team;
        private String manifest;
        private String token;
    }
}


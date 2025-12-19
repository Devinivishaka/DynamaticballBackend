package com.protonestiot.dynamaticball.Dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideosResponseDto {
    private boolean success;
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        private List<VideoItem> videos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoItem {
        private String videoId;
        private String playerId;
        private String startTime; // ISO-8601 string
        private String endTime;   // ISO-8601 string
        private String duration;  // e.g., "45:00"
        private String filePath;
        private String thumbnailPath;
    }
}


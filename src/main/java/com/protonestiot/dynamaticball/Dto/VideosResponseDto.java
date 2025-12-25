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
        private String matchId;
        private List<PlayerVideos> players;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerVideos {
        private String playerId;
        private List<VideoItem> videos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoItem {
        private String videoId;
        private String startTime;
        private String endTime;
        private long durationSeconds;
        private String videoUrl;
        private String thumbnailUrl;
    }
}

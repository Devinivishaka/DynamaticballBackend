package com.protonestiot.dynamaticball.Service.Impl;

import com.protonestiot.dynamaticball.Dto.VideosResponseDto;
import com.protonestiot.dynamaticball.Entity.*;
import com.protonestiot.dynamaticball.Repository.MatchRepository;
import com.protonestiot.dynamaticball.Service.AzureBlobService;
import com.protonestiot.dynamaticball.Service.VideoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final MatchRepository matchRepository;
    private final AzureBlobService azureBlobService;

    @Override
    @Transactional
    public VideosResponseDto getVideos(String matchId) {

        Match match = matchRepository.findByMatchCode(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));

        LocalDateTime start = match.getStartTime();
        LocalDateTime end = match.getEndTime() != null ? match.getEndTime() : LocalDateTime.now();

        long durationSeconds = Duration.between(start, end).getSeconds();

        GameSetup setup = match.getGameSetup();
        if (setup == null) throw new RuntimeException("Game setup missing");

        Map<String, List<VideosResponseDto.VideoItem>> map = new HashMap<>();

        for (Team team : setup.getTeams()) {
            for (Player player : team.getPlayers()) {
                String playerId = player.getPlayerCode();
                String base = matchId + "/player_" + playerId;

                String videoUrl = azureBlobService.getVideoUrl(base + ".m3u8");
                if (videoUrl == null) continue;

                String thumbnailUrl = azureBlobService.getThumbnailUrl(base + ".jpg");

                VideosResponseDto.VideoItem item =
                        VideosResponseDto.VideoItem.builder()
                                .videoId(matchId + "_" + playerId)
                                .startTime(start.toString())
                                .endTime(end.toString())
                                .durationSeconds(durationSeconds)
                                .videoUrl(videoUrl)
                                .thumbnailUrl(thumbnailUrl)
                                .build();

                map.computeIfAbsent(playerId, k -> new ArrayList<>()).add(item);
            }
        }

        List<VideosResponseDto.PlayerVideos> players = new ArrayList<>();
        map.forEach((playerId, videos) ->
                players.add(
                        VideosResponseDto.PlayerVideos.builder()
                                .playerId(playerId)
                                .videos(videos)
                                .build()
                )
        );

        return VideosResponseDto.builder()
                .success(true)
                .data(
                        VideosResponseDto.Data.builder()
                                .matchId(matchId)
                                .players(players)
                                .build()
                )
                .build();
    }
}

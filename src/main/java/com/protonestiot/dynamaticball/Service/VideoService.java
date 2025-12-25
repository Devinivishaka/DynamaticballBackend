package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.VideosResponseDto;

public interface VideoService {
    VideosResponseDto getVideos(String matchId);
}

package com.protonestiot.dynamaticball.Config;

import com.protonestiot.dynamaticball.Service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MatchService matchService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();

        if (destination != null && destination.startsWith("/topic/match/")) {
            String matchCode = destination.substring("/topic/match/".length());
            var status = matchService.getMatchStatus(matchCode);
            messagingTemplate.convertAndSend("/topic/match/" + matchCode, status);
        }

    }
}
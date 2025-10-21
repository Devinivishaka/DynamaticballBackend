package com.protonestiot.dynamaticball.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topic-based real-time updates
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Native WebSocket
        registry.addEndpoint("/ws-match")
                .setAllowedOriginPatterns("*");

        // Optional SockJS fallback (if needed for browsers)
        registry.addEndpoint("/ws-match")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

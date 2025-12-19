package com.protonestiot.dynamaticball.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class MatchWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WS connected: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcast(Object message) {
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    String json;
                    if (message instanceof String s && s.trim().startsWith("{")) {
                        // ensure JSON string passes through
                        json = s;
                    } else {
                        json = mapper.writeValueAsString(message);
                    }
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void broadcastSimple(String event, String matchCode, String description) {
        ObjectNode node = mapper.createObjectNode();
        node.put("event", event);
        node.put("matchCode", matchCode);
        node.put("description", description);
        broadcast(node);
    }
}

package com.minimaya.websocket;

import com.minimaya.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles binary Protobuf frames on the geometry Data Plane.
 * Raw vertex/index byte arrays are NEVER deserialized here — they are
 * routed to CasService or forwarded to other sessions opaquely.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeometryWebSocketHandler extends BinaryWebSocketHandler {

    private final JwtService jwtService;

    // sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        final String token = extractToken(session);
        if (token == null || !jwtService.isTokenValid(token)) {
            closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessions.put(session.getId(), session);
        log.info("WS connected: session={} user={}", session.getId(),
                 jwtService.extractUserId(token));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // message.getPayload() is a raw binary Protobuf SceneMessage.
        // Parse the envelope type only (not geometry payloads) to route correctly.
        // Heavy geometry byte[] fields are passed through as opaque blobs.
        final byte[] payload = message.getPayload().array();
        log.debug("WS binary frame: session={} bytes={}", session.getId(), payload.length);

        // TODO: parse SceneMessage envelope, extract type, route:
        //   SCENE_FULL/SCENE_DELTA -> persist to CAS + broadcast to other sessions
        //   ACK                    -> handle client acknowledgement
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WS disconnected: session={} status={}", session.getId(), status);
    }

    public void broadcastToProject(String projectId, byte[] protoPayload) {
        sessions.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new BinaryMessage(protoPayload));
                }
            } catch (Exception e) {
                log.warn("Failed to send to session {}: {}", s.getId(), e.getMessage());
            }
        });
    }

    private String extractToken(WebSocketSession session) {
        final var uri = session.getUri();
        if (uri == null) return null;
        final String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try { session.close(status); } catch (Exception ignored) {}
    }
}

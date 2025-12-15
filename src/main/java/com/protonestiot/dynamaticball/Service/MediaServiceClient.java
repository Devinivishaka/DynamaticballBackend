package com.protonestiot.dynamaticball.Service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Client service for the Media Service described by the provided OpenAPI spec.
 * Uses Spring WebClient to call the remote API. The API key header `x-api-key` will
 * be added automatically when `mediamtx.server.api-key` property or `MEDIAMTX_SERVER_API_KEY` env var is set.
 */
@Service
public class MediaServiceClient {

    private final WebClient webClient;
    private final String apiKey;

    public MediaServiceClient(
            @Value("${streaming.server.url:http://localhost:8000}") String baseUrl,
            @Value("${streaming.server.api-key:}") String apiKey
    ) {
        this.apiKey = (apiKey == null || apiKey.isBlank()) ? null : apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // --- API methods ---

    public StartResponse startMatch(StartRequest request) throws MediaServiceException {
        try {
            return webClient.post()
                    .uri("/start")
                    .headers(this::addApiKeyIfPresent)
                    .body(Mono.just(request), StartRequest.class)
                    .retrieve()
                    .bodyToMono(StartResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw toMediaServiceException(e);
        }
    }

    public StopResponse stopMatch(StopRequest request) throws MediaServiceException {
        try {
            return webClient.post()
                    .uri("/stop")
                    .headers(this::addApiKeyIfPresent)
                    .body(Mono.just(request), StopRequest.class)
                    .retrieve()
                    .bodyToMono(StopResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw toMediaServiceException(e);
        }
    }

    public StreamsResponse getStreams(String matchId) throws MediaServiceException {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/streams/{matchId}").build(matchId))
                    .headers(this::addApiKeyIfPresent)
                    .retrieve()
                    .bodyToMono(StreamsResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw toMediaServiceException(e);
        }
    }

    public TokenValidationResponse validateToken(MediaMTXAuthReq req) throws MediaServiceException {
        try {
            return webClient.post()
                    .uri("/validate-token")
                    .body(Mono.just(req), MediaMTXAuthReq.class)
                    .retrieve()
                    .bodyToMono(TokenValidationResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw toMediaServiceException(e);
        }
    }

    public HealthResponse health() throws MediaServiceException {
        try {
            return webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(HealthResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw toMediaServiceException(e);
        }
    }

    // --- helpers ---

    private void addApiKeyIfPresent(HttpHeaders headers) {
        if (this.apiKey != null) {
            headers.set("x-api-key", this.apiKey);
        }
    }

    private MediaServiceException toMediaServiceException(WebClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        return new MediaServiceException(status, body, e);
    }

    // --- custom exception ---
    @Getter
    public static class MediaServiceException extends Exception {
        private final int status;
        private final String responseBody;

        public MediaServiceException(int status, String responseBody, Throwable cause) {
            super("MediaService error: " + status + " -> " + responseBody, cause);
            this.status = status;
            this.responseBody = responseBody;
        }

    }

    // --- DTOs (minimal representation matching the OpenAPI) ---

    @Setter
    @Getter
    public static class CameraAssignmentReq {
        private String cameraId;
        private String playerId;
        private String team;
        private String sourceUrl;

        public CameraAssignmentReq() {}

    }

    @Setter
    @Getter
    public static class StartRequest {
        private String matchId;
        private List<CameraAssignmentReq> cameras = Collections.emptyList();

        public StartRequest() {}

    }

    @Setter
    @Getter
    public static class StartResponse {
        private String status;
        private String matchId;

        public StartResponse() {}

    }

    @Setter
    @Getter
    public static class StopRequest {
        private String matchId;

        public StopRequest() {}

    }

    @Setter
    @Getter
    public static class StopResponse {
        private String status;
        private String matchId;
        private String message;

        public StopResponse() {}

    }

    @Setter
    @Getter
    public static class StreamItem {
        private String cameraId;
        private String playerId;
        private String team;
        private String manifest;
        private String token;

        public StreamItem() {}

    }

    @Setter
    @Getter
    public static class StreamsResponse {
        private String matchId;
        private List<StreamItem> streams = Collections.emptyList();

        public StreamsResponse() {}

    }

    @Setter
    @Getter
    public static class MediaMTXAuthReq {
        private String user;
        private String password;
        private String token;
        private String ip;
        private String action;
        private String path;
        private String protocol;
        private String id;
        private String query;

        public MediaMTXAuthReq() {}
    }

    @Setter
    @Getter
    public static class TokenValidationResponse {
        private boolean valid;
        private String matchId;
        private String action;

        public TokenValidationResponse() {}
    }

    @Setter
    @Getter
    public static class HealthResponse {
        private String status;

        public HealthResponse() {}

    }

    public static class Error {
        private String detail;

        public Error() {}

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }
}

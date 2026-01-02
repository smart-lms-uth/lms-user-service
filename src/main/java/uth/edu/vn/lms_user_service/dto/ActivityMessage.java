package uth.edu.vn.lms_user_service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Message DTO for RabbitMQ Activity Queue
 */
public record ActivityMessage(
    Long userId,
    String sessionId,
    String activityType,
    String action,
    String pageUrl,
    String pageTitle,
    String elementId,
    String elementText,
    String apiEndpoint,
    String httpMethod,
    Integer responseStatus,
    Long responseTimeMs,
    Map<String, Object> metadata,
    String ipAddress,
    String userAgent,
    String deviceType,
    String browser,
    String os,
    Integer screenWidth,
    Integer screenHeight,
    Instant timestamp,
    Long durationMs
) implements Serializable {

    public static ActivityMessage from(Long userId, ActivityRequest request, String ipAddress, String userAgent) {
        Map<String, Object> metadataMap = null;
        if (request.metadata() != null) {
            try {
                metadataMap = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(request.metadata(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
            } catch (Exception e) {
                metadataMap = Map.of("raw", request.metadata());
            }
        }

        return new ActivityMessage(
            userId,
            request.sessionId(),
            request.activityType() != null ? request.activityType().name() : null,
            request.action(),
            request.pageUrl(),
            request.pageTitle(),
            request.elementId(),
            request.elementText(),
            request.apiEndpoint(),
            request.httpMethod(),
            request.responseStatus(),
            request.responseTimeMs(),
            metadataMap,
            ipAddress,
            userAgent,
            request.deviceType(),
            request.browser(),
            request.os(),
            request.screenWidth(),
            request.screenHeight(),
            request.timestamp() != null ? request.timestamp().toInstant(java.time.ZoneOffset.UTC) : Instant.now(),
            request.durationMs()
        );
    }
}

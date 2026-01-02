package uth.edu.vn.lms_user_service.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uth.edu.vn.lms_user_service.document.ActivityLog;
import uth.edu.vn.lms_user_service.entity.UserActivity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * DTO for activity response
 * Supports both MongoDB document and legacy JPA entity
 */
public record ActivityResponse(
    String id,
    Long userId,
    String sessionId,
    String activityType,
    String action,
    String pageUrl,
    String pageTitle,
    String metadata,
    LocalDateTime timestamp,
    Long durationMs,
    String ipAddress,
    String userAgent
) {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    /**
     * Create from MongoDB ActivityLog document
     */
    public static ActivityResponse fromDocument(ActivityLog log) {
        return new ActivityResponse(
            log.getId(),
            log.getUserId(),
            log.getSessionId(),
            log.getActivityType(),
            log.getAction(),
            log.getPageUrl(),
            log.getPageTitle(),
            mapToJson(log.getMetadata()),
            log.getTimestamp() != null 
                ? LocalDateTime.ofInstant(log.getTimestamp(), VIETNAM_ZONE)
                : null,
            log.getDurationMs(),
            log.getIpAddress(),
            log.getUserAgent()
        );
    }
    
    /**
     * Create from JPA UserActivity entity (legacy support)
     */
    public static ActivityResponse fromEntity(UserActivity activity) {
        return new ActivityResponse(
            activity.getId() != null ? activity.getId().toString() : null,
            activity.getUser() != null ? activity.getUser().getId() : null,
            activity.getSessionId(),
            activity.getActivityType() != null ? activity.getActivityType().name() : null,
            activity.getAction(),
            activity.getPageUrl(),
            activity.getPageTitle(),
            activity.getMetadata(),
            activity.getTimestamp(),
            activity.getDurationMs(),
            activity.getIpAddress(),
            activity.getUserAgent()
        );
    }
    
    /**
     * Create for async queue response (no ID yet)
     */
    public static ActivityResponse pending(Long userId, String sessionId, String activityType, 
            String action, String pageUrl, String pageTitle, LocalDateTime timestamp) {
        return new ActivityResponse(
            null, // ID not available until consumed from queue
            userId,
            sessionId,
            activityType,
            action,
            pageUrl,
            pageTitle,
            null,
            timestamp,
            null,
            null,
            null
        );
    }
    
    /**
     * Convert Map to JSON string
     */
    private static String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return map.toString();
        }
    }
}

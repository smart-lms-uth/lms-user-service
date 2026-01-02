package uth.edu.vn.lms_user_service.dto;

import uth.edu.vn.lms_user_service.entity.ActivityType;

import java.time.LocalDateTime;

/**
 * DTO for receiving activity data from frontend
 */
public record ActivityRequest(
    String sessionId,
    ActivityType activityType,
    String action,
    String pageUrl,
    String pageTitle,
    String elementId,
    String elementText,
    String apiEndpoint,
    String httpMethod,
    Integer responseStatus,
    Long responseTimeMs,
    String metadata,
    String deviceType,
    String browser,
    String os,
    Integer screenWidth,
    Integer screenHeight,
    Long durationMs,
    LocalDateTime timestamp
) {}

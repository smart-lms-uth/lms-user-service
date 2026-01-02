package uth.edu.vn.lms_user_service.dto;

import java.util.Map;

/**
 * DTO for activity statistics
 */
public record ActivityStatsResponse(
    Long totalActivities,
    Long uniqueUsers,
    Long uniqueSessions,
    Double avgSessionDurationMinutes,
    Map<String, Long> activityByType,
    Map<String, Long> topPages,
    Map<Integer, Long> hourlyDistribution
) {}

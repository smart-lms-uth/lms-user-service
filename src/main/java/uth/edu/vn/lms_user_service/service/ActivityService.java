package uth.edu.vn.lms_user_service.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uth.edu.vn.lms_user_service.document.ActivityLog;
import uth.edu.vn.lms_user_service.dto.ActivityMessage;
import uth.edu.vn.lms_user_service.dto.ActivityRequest;
import uth.edu.vn.lms_user_service.dto.ActivityResponse;
import uth.edu.vn.lms_user_service.dto.ActivityStatsResponse;
import uth.edu.vn.lms_user_service.messaging.ActivityProducer;
import uth.edu.vn.lms_user_service.repository.ActivityLogRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Activity Service - Refactored for async logging via RabbitMQ + MongoDB
 * 
 * Write Path: HTTP Request -> ActivityProducer -> RabbitMQ -> ActivityConsumer -> MongoDB
 * Read Path: MongoDB (direct query)
 */
@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    
    private final ActivityProducer activityProducer;
    private final ActivityLogRepository activityLogRepository;

    public ActivityService(ActivityProducer activityProducer, ActivityLogRepository activityLogRepository) {
        this.activityProducer = activityProducer;
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Log a single activity (async via RabbitMQ)
     */
    public ActivityResponse logActivity(Long userId, ActivityRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest != null ? getClientIp(httpRequest) : null;
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        
        // Create message and send to RabbitMQ
        ActivityMessage message = ActivityMessage.from(userId, request, ipAddress, userAgent);
        activityProducer.sendActivity(message);
        
        log.debug("Queued activity: {} for user: {}", request.activityType(), userId);
        
        // Return response immediately (non-blocking)
        return ActivityResponse.pending(
            userId,
            request.sessionId(),
            request.activityType() != null ? request.activityType().name() : null,
            request.action(),
            request.pageUrl(),
            request.pageTitle(),
            request.timestamp() != null ? request.timestamp() : LocalDateTime.now(VIETNAM_ZONE)
        );
    }

    /**
     * Log batch activities (async via RabbitMQ)
     */
    public List<ActivityResponse> logActivities(Long userId, List<ActivityRequest> requests, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest != null ? getClientIp(httpRequest) : null;
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        
        List<ActivityMessage> messages = requests.stream()
            .map(request -> ActivityMessage.from(userId, request, ipAddress, userAgent))
            .toList();
        
        activityProducer.sendActivities(messages);
        
        log.debug("Queued {} activities for user: {}", requests.size(), userId);
        
        return requests.stream()
            .map(request -> ActivityResponse.pending(
                userId,
                request.sessionId(),
                request.activityType() != null ? request.activityType().name() : null,
                request.action(),
                request.pageUrl(),
                request.pageTitle(),
                request.timestamp() != null ? request.timestamp() : LocalDateTime.now(VIETNAM_ZONE)
            ))
            .toList();
    }

    /**
     * Get user activities with pagination (from MongoDB)
     */
    public Page<ActivityResponse> getUserActivities(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100)); // Max 100 per page
        return activityLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
            .map(ActivityResponse::fromDocument);
    }

    /**
     * Get activities by session (from MongoDB)
     */
    public List<ActivityResponse> getSessionActivities(String sessionId) {
        return activityLogRepository.findBySessionIdOrderByTimestampAsc(sessionId).stream()
            .map(ActivityResponse::fromDocument)
            .collect(Collectors.toList());
    }

    /**
     * Get activity statistics for a time period (from MongoDB)
     */
    public ActivityStatsResponse getStats(LocalDateTime start, LocalDateTime end) {
        Instant startInstant = start.toInstant(ZoneOffset.UTC);
        Instant endInstant = end.toInstant(ZoneOffset.UTC);
        
        // Get all activities in range for aggregation
        List<ActivityLog> activities = activityLogRepository.findByTimestampBetween(startInstant, endInstant);
        
        // Total activities
        long totalActivities = activities.size();
        
        // Unique users (DAU)
        long uniqueUsers = activities.stream()
            .map(ActivityLog::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        
        // Unique sessions
        long uniqueSessions = activities.stream()
            .map(ActivityLog::getSessionId)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        
        // Activity by type
        Map<String, Long> activityByType = activities.stream()
            .filter(a -> a.getActivityType() != null)
            .collect(Collectors.groupingBy(
                ActivityLog::getActivityType,
                LinkedHashMap::new,
                Collectors.counting()
            ));
        
        // Top pages
        Map<String, Long> topPages = activities.stream()
            .filter(a -> "PAGE_VIEW".equals(a.getActivityType()) && a.getPageUrl() != null)
            .collect(Collectors.groupingBy(
                ActivityLog::getPageUrl,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        // Hourly distribution
        Map<Integer, Long> hourlyDistribution = activities.stream()
            .filter(a -> a.getTimestamp() != null)
            .collect(Collectors.groupingBy(
                a -> a.getTimestamp().atZone(VIETNAM_ZONE).getHour(),
                LinkedHashMap::new,
                Collectors.counting()
            ));
        
        // Average session duration
        double avgSessionDuration = calculateAvgSessionDuration(activities);
        
        return new ActivityStatsResponse(
            totalActivities,
            uniqueUsers,
            uniqueSessions,
            avgSessionDuration,
            activityByType,
            topPages,
            hourlyDistribution
        );
    }

    private double calculateAvgSessionDuration(List<ActivityLog> activities) {
        Map<String, List<ActivityLog>> sessions = activities.stream()
            .filter(a -> a.getSessionId() != null && a.getTimestamp() != null)
            .collect(Collectors.groupingBy(ActivityLog::getSessionId));
        
        if (sessions.isEmpty()) return 0.0;
        
        double totalMinutes = 0;
        int count = 0;
        
        for (var sessionActivities : sessions.values()) {
            var sorted = sessionActivities.stream()
                .sorted(Comparator.comparing(ActivityLog::getTimestamp))
                .toList();
            
            if (sorted.size() >= 2) {
                Instant sessionStart = sorted.getFirst().getTimestamp();
                Instant sessionEnd = sorted.getLast().getTimestamp();
                Duration duration = Duration.between(sessionStart, sessionEnd);
                totalMinutes += duration.toMinutes();
                count++;
            }
        }
        
        return count > 0 ? totalMinutes / count : 0.0;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Clean up old activities (for scheduled cleanup)
     */
    public void cleanupOldActivities(int daysToKeep) {
        Instant cutoff = Instant.now().minus(Duration.ofDays(daysToKeep));
        activityLogRepository.deleteByTimestampBefore(cutoff);
        log.info("Cleaned up activities older than {} days", daysToKeep);
    }
}

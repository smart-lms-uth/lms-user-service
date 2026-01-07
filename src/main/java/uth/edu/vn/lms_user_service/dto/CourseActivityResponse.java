package uth.edu.vn.lms_user_service.dto;

import uth.edu.vn.lms_user_service.document.ActivityLog;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * DTO for course-specific activity response with Vietnamese formatted titles
 */
public record CourseActivityResponse(
    String id,
    Long userId,
    String activityType,
    String action,
    String formattedTitle,  // Vietnamese formatted title e.g. "Truy cập bài học X vào lúc 10:30"
    String resourceType,    // COURSE, SECTION, MODULE, QUIZ, ASSIGNMENT, VIDEO
    String resourceName,    // Name of the resource accessed
    Long resourceId,        // ID of the resource
    String pageUrl,
    LocalDateTime timestamp,
    String timestampFormatted,
    Long durationMs,
    Map<String, Object> metadata
) {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy", new Locale("vi", "VN"));

    /**
     * Create from MongoDB ActivityLog document
     */
    public static CourseActivityResponse fromDocument(ActivityLog log) {
        LocalDateTime localTime = log.getTimestamp() != null 
            ? LocalDateTime.ofInstant(log.getTimestamp(), VIETNAM_ZONE)
            : null;
        
        String resourceType = extractResourceType(log.getActivityType());
        String resourceName = extractResourceName(log.getMetadata(), log.getPageTitle());
        Long resourceId = extractResourceId(log.getMetadata());
        
        String formattedTitle = formatActivityTitle(
            log.getActivityType(), 
            resourceName, 
            localTime
        );
        
        String timestampStr = localTime != null ? localTime.format(TIME_FORMATTER) : null;
        
        return new CourseActivityResponse(
            log.getId(),
            log.getUserId(),
            log.getActivityType(),
            log.getAction(),
            formattedTitle,
            resourceType,
            resourceName,
            resourceId,
            log.getPageUrl(),
            localTime,
            timestampStr,
            log.getDurationMs(),
            log.getMetadata()
        );
    }

    /**
     * Extract resource type from activity type
     */
    private static String extractResourceType(String activityType) {
        if (activityType == null) return "OTHER";
        
        return switch (activityType) {
            case "COURSE_VIEW", "COURSE_ENROLL", "COURSE_UNENROLL", "COURSE_COMPLETE" -> "COURSE";
            case "SECTION_VIEW", "SECTION_COMPLETE" -> "SECTION";
            case "MODULE_VIEW", "MODULE_COMPLETE" -> "MODULE";
            case "QUIZ_VIEW", "QUIZ_START", "QUIZ_ANSWER", "QUIZ_SUBMIT", "QUIZ_RESULT_VIEW" -> "QUIZ";
            case "ASSIGNMENT_VIEW", "ASSIGNMENT_START", "ASSIGNMENT_SUBMIT", "ASSIGNMENT_GRADE_VIEW" -> "ASSIGNMENT";
            case "VIDEO_PLAY", "VIDEO_PAUSE", "VIDEO_COMPLETE", "VIDEO_SEEK" -> "VIDEO";
            case "DOCUMENT_VIEW", "DOCUMENT_DOWNLOAD" -> "DOCUMENT";
            case "DISCUSSION_VIEW", "DISCUSSION_POST", "DISCUSSION_REPLY" -> "DISCUSSION";
            default -> "OTHER";
        };
    }

    /**
     * Extract resource name from metadata or page title
     */
    private static String extractResourceName(Map<String, Object> metadata, String pageTitle) {
        if (metadata != null) {
            // Try to get specific name from metadata
            if (metadata.containsKey("courseName")) return (String) metadata.get("courseName");
            if (metadata.containsKey("sectionName")) return (String) metadata.get("sectionName");
            if (metadata.containsKey("moduleName")) return (String) metadata.get("moduleName");
            if (metadata.containsKey("quizName")) return (String) metadata.get("quizName");
            if (metadata.containsKey("assignmentName")) return (String) metadata.get("assignmentName");
            if (metadata.containsKey("videoTitle")) return (String) metadata.get("videoTitle");
            if (metadata.containsKey("resourceName")) return (String) metadata.get("resourceName");
        }
        return pageTitle != null ? pageTitle : "Không xác định";
    }

    /**
     * Extract resource ID from metadata
     */
    private static Long extractResourceId(Map<String, Object> metadata) {
        if (metadata == null) return null;
        
        Object id = metadata.get("courseId");
        if (id == null) id = metadata.get("sectionId");
        if (id == null) id = metadata.get("moduleId");
        if (id == null) id = metadata.get("quizId");
        if (id == null) id = metadata.get("assignmentId");
        if (id == null) id = metadata.get("resourceId");
        
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        if (id instanceof String) {
            try {
                return Long.parseLong((String) id);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Format activity title in Vietnamese
     */
    private static String formatActivityTitle(String activityType, String resourceName, LocalDateTime timestamp) {
        if (activityType == null) {
            return "Hoạt động không xác định";
        }
        
        String timeStr = timestamp != null ? " vào lúc " + timestamp.format(TIME_FORMATTER) : "";
        
        return switch (activityType) {
            // Course activities
            case "COURSE_VIEW" -> "Truy cập khóa học \"" + resourceName + "\"" + timeStr;
            case "COURSE_ENROLL" -> "Đăng ký khóa học \"" + resourceName + "\"" + timeStr;
            case "COURSE_UNENROLL" -> "Hủy đăng ký khóa học \"" + resourceName + "\"" + timeStr;
            case "COURSE_COMPLETE" -> "Hoàn thành khóa học \"" + resourceName + "\"" + timeStr;
            
            // Section activities
            case "SECTION_VIEW" -> "Xem chương \"" + resourceName + "\"" + timeStr;
            case "SECTION_COMPLETE" -> "Hoàn thành chương \"" + resourceName + "\"" + timeStr;
            
            // Module activities
            case "MODULE_VIEW" -> "Xem bài học \"" + resourceName + "\"" + timeStr;
            case "MODULE_COMPLETE" -> "Hoàn thành bài học \"" + resourceName + "\"" + timeStr;
            
            // Quiz activities
            case "QUIZ_VIEW" -> "Xem bài kiểm tra \"" + resourceName + "\"" + timeStr;
            case "QUIZ_START" -> "Bắt đầu làm bài kiểm tra \"" + resourceName + "\"" + timeStr;
            case "QUIZ_ANSWER" -> "Trả lời câu hỏi trong \"" + resourceName + "\"" + timeStr;
            case "QUIZ_SUBMIT" -> "Nộp bài kiểm tra \"" + resourceName + "\"" + timeStr;
            case "QUIZ_RESULT_VIEW" -> "Xem kết quả bài kiểm tra \"" + resourceName + "\"" + timeStr;
            
            // Assignment activities
            case "ASSIGNMENT_VIEW" -> "Xem bài tập \"" + resourceName + "\"" + timeStr;
            case "ASSIGNMENT_START" -> "Bắt đầu làm bài tập \"" + resourceName + "\"" + timeStr;
            case "ASSIGNMENT_SUBMIT" -> "Nộp bài tập \"" + resourceName + "\"" + timeStr;
            case "ASSIGNMENT_GRADE_VIEW" -> "Xem điểm bài tập \"" + resourceName + "\"" + timeStr;
            
            // Video activities
            case "VIDEO_PLAY" -> "Xem video \"" + resourceName + "\"" + timeStr;
            case "VIDEO_PAUSE" -> "Tạm dừng video \"" + resourceName + "\"" + timeStr;
            case "VIDEO_COMPLETE" -> "Hoàn thành xem video \"" + resourceName + "\"" + timeStr;
            case "VIDEO_SEEK" -> "Tua video \"" + resourceName + "\"" + timeStr;
            
            // Document activities
            case "DOCUMENT_VIEW" -> "Xem tài liệu \"" + resourceName + "\"" + timeStr;
            case "DOCUMENT_DOWNLOAD" -> "Tải tài liệu \"" + resourceName + "\"" + timeStr;
            
            // Discussion activities
            case "DISCUSSION_VIEW" -> "Xem thảo luận \"" + resourceName + "\"" + timeStr;
            case "DISCUSSION_POST" -> "Đăng bài thảo luận trong \"" + resourceName + "\"" + timeStr;
            case "DISCUSSION_REPLY" -> "Trả lời thảo luận trong \"" + resourceName + "\"" + timeStr;
            
            // Login activities
            case "LOGIN" -> "Đăng nhập hệ thống" + timeStr;
            case "LOGOUT" -> "Đăng xuất hệ thống" + timeStr;
            
            // Generic page view
            case "PAGE_VIEW" -> "Truy cập trang \"" + resourceName + "\"" + timeStr;
            
            default -> "Hoạt động: " + activityType + " - " + resourceName + timeStr;
        };
    }
}

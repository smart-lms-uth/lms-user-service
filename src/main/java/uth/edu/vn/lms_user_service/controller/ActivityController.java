package uth.edu.vn.lms_user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.lms_user_service.dto.ActivityRequest;
import uth.edu.vn.lms_user_service.dto.ActivityResponse;
import uth.edu.vn.lms_user_service.dto.ActivityStatsResponse;
import uth.edu.vn.lms_user_service.dto.ApiResponse;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.service.ActivityService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@Tag(name = "User Activities", description = "APIs for tracking and analyzing user activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Log a single activity
     */
    @PostMapping
    @Operation(summary = "Log user activity")
    public ResponseEntity<ApiResponse<ActivityResponse>> logActivity(
            Authentication authentication,
            @RequestBody ActivityRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            userId = ((User) authentication.getPrincipal()).getId();
        }
        
        ActivityResponse response = activityService.logActivity(userId, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Activity logged", response));
    }

    /**
     * Log batch activities (buffered from frontend)
     */
    @PostMapping("/batch")
    @Operation(summary = "Log multiple activities at once")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> logActivities(
            Authentication authentication,
            @RequestBody List<ActivityRequest> requests,
            HttpServletRequest httpRequest) {
        
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            userId = ((User) authentication.getPrincipal()).getId();
        }
        
        List<ActivityResponse> responses = activityService.logActivities(userId, requests, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Activities logged", responses));
    }

    /**
     * Get current user's activities
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user's activities")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getMyActivities(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = (User) authentication.getPrincipal();
        Page<ActivityResponse> activities = activityService.getUserActivities(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Activities retrieved", activities));
    }

    /**
     * Get activities by session ID
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get activities for a specific session")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getSessionActivities(
            @PathVariable String sessionId) {
        
        List<ActivityResponse> activities = activityService.getSessionActivities(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Session activities retrieved", activities));
    }

    /**
     * Get activity statistics (Admin only)
     */
    @GetMapping("/stats")
    @Operation(summary = "Get activity statistics for a time period")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ActivityStatsResponse>> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        ActivityStatsResponse stats = activityService.getStats(start, end);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));
    }

    /**
     * Get user activities by user ID (Admin only)
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get activities for a specific user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Page<ActivityResponse>>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ActivityResponse> activities = activityService.getUserActivities(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("User activities retrieved", activities));
    }
}

package uth.edu.vn.lms_user_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uth.edu.vn.lms_user_service.dto.ActivityRequest;
import uth.edu.vn.lms_user_service.dto.ActivityResponse;
import uth.edu.vn.lms_user_service.dto.ActivityStatsResponse;
import uth.edu.vn.lms_user_service.dto.CourseActivityResponse;
import uth.edu.vn.lms_user_service.entity.ActivityType;

import java.util.List;

public interface IActivityService {
    
    void logSystemActivity(Long userId, ActivityType activityType, String action, String metadata);
    
    void logUserActivity(Long userId, ActivityRequest request, String ipAddress, String userAgent);
    
    void logBatchActivities(Long userId, List<ActivityRequest> requests, String ipAddress, String userAgent);
    
    Page<ActivityResponse> getUserActivities(Long userId, Pageable pageable);
    
    Page<CourseActivityResponse> getCourseActivities(Long userId, Pageable pageable);
    
    ActivityStatsResponse getActivityStats(Long userId);
}

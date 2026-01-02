package uth.edu.vn.lms_user_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.lms_user_service.entity.ActivityType;
import uth.edu.vn.lms_user_service.entity.UserActivity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    // Find by user
    Page<UserActivity> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    List<UserActivity> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    // Find by session
    List<UserActivity> findBySessionIdOrderByTimestampAsc(String sessionId);
    
    // Find by activity type
    Page<UserActivity> findByActivityTypeOrderByTimestampDesc(ActivityType activityType, Pageable pageable);
    
    List<UserActivity> findByActivityTypeAndTimestampBetween(ActivityType activityType, LocalDateTime start, LocalDateTime end);
    
    // Count by type for statistics
    @Query("SELECT a.activityType, COUNT(a) FROM UserActivity a WHERE a.timestamp BETWEEN :start AND :end GROUP BY a.activityType")
    List<Object[]> countByActivityTypeAndTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Count by user for statistics
    @Query("SELECT a.user.id, COUNT(a) FROM UserActivity a WHERE a.timestamp BETWEEN :start AND :end GROUP BY a.user.id ORDER BY COUNT(a) DESC")
    List<Object[]> countByUserAndTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    
    // Get page view stats
    @Query("SELECT a.pageUrl, COUNT(a) FROM UserActivity a WHERE a.activityType = 'PAGE_VIEW' AND a.timestamp BETWEEN :start AND :end GROUP BY a.pageUrl ORDER BY COUNT(a) DESC")
    List<Object[]> getPageViewStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    
    // Count unique users (DAU - Daily Active Users)
    @Query("SELECT COUNT(DISTINCT a.user.id) FROM UserActivity a WHERE a.timestamp BETWEEN :start AND :end")
    Long countDistinctUsersByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Count unique sessions
    @Query("SELECT COUNT(DISTINCT a.sessionId) FROM UserActivity a WHERE a.timestamp BETWEEN :start AND :end")
    Long countDistinctSessionsByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Count total activities in period
    @Query("SELECT COUNT(a) FROM UserActivity a WHERE a.timestamp BETWEEN :start AND :end")
    Long countByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Average session duration
    @Query("SELECT a.sessionId, MIN(a.timestamp), MAX(a.timestamp) FROM UserActivity a WHERE a.timestamp BETWEEN :start AND :end GROUP BY a.sessionId")
    List<Object[]> getSessionDurations(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Get hourly activity distribution
    @Query(value = "SELECT EXTRACT(HOUR FROM timestamp) as hour, COUNT(*) FROM user_activities WHERE timestamp BETWEEN :start AND :end GROUP BY EXTRACT(HOUR FROM timestamp) ORDER BY hour", nativeQuery = true)
    List<Object[]> getHourlyDistribution(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Delete old activities (for cleanup)
    void deleteByTimestampBefore(LocalDateTime before);
}

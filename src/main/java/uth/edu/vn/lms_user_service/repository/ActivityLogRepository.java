package uth.edu.vn.lms_user_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uth.edu.vn.lms_user_service.document.ActivityLog;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB Repository for Activity Logs
 */
@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {

    // Find by user with pagination
    Page<ActivityLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    // Find by session
    List<ActivityLog> findBySessionIdOrderByTimestampAsc(String sessionId);

    // Find by activity type
    Page<ActivityLog> findByActivityTypeOrderByTimestampDesc(String activityType, Pageable pageable);

    // Find activities in time range
    List<ActivityLog> findByTimestampBetween(Instant start, Instant end);

    // Find by user in time range
    List<ActivityLog> findByUserIdAndTimestampBetween(Long userId, Instant start, Instant end);

    // Count by activity type in time range
    @Query(value = "{ 'timestamp': { $gte: ?0, $lte: ?1 } }", count = true)
    Long countByTimestampBetween(Instant start, Instant end);

    // Count distinct users in time range
    @Query(value = "{ 'timestamp': { $gte: ?0, $lte: ?1 } }", count = true)
    Long countDistinctUserIdByTimestampBetween(Instant start, Instant end);

    // Count distinct sessions in time range
    @Query(value = "{ 'timestamp': { $gte: ?0, $lte: ?1 } }", count = true)
    Long countDistinctSessionIdByTimestampBetween(Instant start, Instant end);

    // Delete old activities (for cleanup job)
    void deleteByTimestampBefore(Instant before);

    // Find by user and courseId in metadata (for course-specific activities)
    // courseId is stored as String in metadata from frontend
    @Query("{ 'userId': ?0, 'metadata.courseId': ?1 }")
    List<ActivityLog> findByUserIdAndCourseId(Long userId, String courseId);

    // Find by user and courseId with pagination
    @Query("{ 'userId': ?0, 'metadata.courseId': ?1 }")
    Page<ActivityLog> findByUserIdAndCourseIdOrderByTimestampDesc(Long userId, String courseId, Pageable pageable);

    // Find course-related activities by user
    @Query("{ 'userId': ?0, 'metadata.courseId': ?1, 'activityType': { $in: ?2 } }")
    Page<ActivityLog> findByUserIdAndCourseIdAndActivityTypeIn(Long userId, String courseId, List<String> activityTypes, Pageable pageable);

    // Find all activities for a course (ordered by timestamp desc for getting latest per user)
    @Query("{ 'metadata.courseId': ?0 }")
    List<ActivityLog> findByCourseIdOrderByTimestampDesc(String courseId);
}

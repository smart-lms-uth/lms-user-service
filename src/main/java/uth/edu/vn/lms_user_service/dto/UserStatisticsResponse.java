package uth.edu.vn.lms_user_service.dto;

public record UserStatisticsResponse(
    long totalUsers,
    long adminCount,
    long teacherCount,
    long studentCount,
    long enabledCount,
    long disabledCount
) {}

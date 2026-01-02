package uth.edu.vn.lms_user_service.entity;

/**
 * Enum định nghĩa các loại hoạt động của người dùng
 */
public enum ActivityType {
    // Authentication
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_RESET,
    
    // Navigation
    PAGE_VIEW,
    PAGE_LEAVE,
    
    // User Interactions
    BUTTON_CLICK,
    LINK_CLICK,
    FORM_SUBMIT,
    FORM_ERROR,
    
    // API Calls
    API_REQUEST,
    API_ERROR,
    
    // Profile
    PROFILE_VIEW,
    PROFILE_UPDATE,
    AVATAR_UPLOAD,
    
    // Search & Filter
    SEARCH,
    FILTER,
    SORT,
    
    // Content
    CONTENT_VIEW,
    CONTENT_DOWNLOAD,
    CONTENT_UPLOAD,
    
    // Session
    SESSION_START,
    SESSION_END,
    SESSION_TIMEOUT,
    
    // Errors
    ERROR,
    CRASH,
    
    // Course - Khóa học
    COURSE_VIEW,
    COURSE_ENROLL,
    COURSE_UNENROLL,
    COURSE_COMPLETE,
    COURSE_PROGRESS,
    
    // Assignment - Bài tập
    ASSIGNMENT_VIEW,
    ASSIGNMENT_START,
    ASSIGNMENT_SUBMIT,
    ASSIGNMENT_GRADE_VIEW,
    
    // Quiz - Bài kiểm tra
    QUIZ_VIEW,
    QUIZ_START,
    QUIZ_ANSWER,
    QUIZ_SUBMIT,
    QUIZ_RESULT_VIEW,
    
    // Video - Video học tập
    VIDEO_PLAY,
    VIDEO_PAUSE,
    VIDEO_COMPLETE,
    VIDEO_SEEK,
    
    // Discussion - Thảo luận
    DISCUSSION_VIEW,
    DISCUSSION_POST,
    DISCUSSION_REPLY,
    
    // Notification - Thông báo
    NOTIFICATION_VIEW,
    NOTIFICATION_CLICK,
    
    // Custom
    CUSTOM
}

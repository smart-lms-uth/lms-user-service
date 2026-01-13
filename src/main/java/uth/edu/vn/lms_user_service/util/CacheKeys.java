package uth.edu.vn.lms_user_service.util;

public final class CacheKeys {

    private CacheKeys() {}

    public static final String USER_PROFILE_PREFIX = "user:profile:";
    
    public static String userProfile(Long userId) {
        return USER_PROFILE_PREFIX + userId;
    }
}

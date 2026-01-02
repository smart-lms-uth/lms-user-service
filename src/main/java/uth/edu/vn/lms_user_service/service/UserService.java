package uth.edu.vn.lms_user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.lms_user_service.dto.UpdateProfileRequest;
import uth.edu.vn.lms_user_service.dto.UserResponse;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.exception.ResourceNotFoundException;
import uth.edu.vn.lms_user_service.repository.UserRepository;

import java.time.Duration;

/**
 * User Service with Cache-Aside Pattern
 * Read: Check Cache -> Miss -> Read DB -> Set Cache
 * Write: Update DB -> Invalidate Cache
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String USER_CACHE_KEY_PREFIX = "user:profile:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get user profile with Cache-Aside pattern
     * 1. Check Redis cache
     * 2. If hit, return cached data
     * 3. If miss, query PostgreSQL -> cache result -> return
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        
        // Try to get from cache
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof UserResponse cachedResponse) {
                log.debug("Cache HIT for user: {}", userId);
                return cachedResponse;
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed for user {}: {}", userId, e.getMessage());
            // Continue to database on cache failure
        }
        
        log.debug("Cache MISS for user: {}", userId);
        
        // Cache miss - fetch from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        UserResponse response = UserResponse.fromUser(user);
        
        // Cache the result
        try {
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
            log.debug("Cached user profile: {}", userId);
        } catch (Exception e) {
            log.warn("Redis cache write failed for user {}: {}", userId, e.getMessage());
            // Continue without caching on failure
        }
        
        return response;
    }

    /**
     * Update user profile and invalidate cache
     */
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        updateUserFields(user, request);
        User savedUser = userRepository.save(user);
        
        // Invalidate cache
        invalidateUserCache(userId);
        
        return UserResponse.fromUser(savedUser);
    }

    /**
     * Complete profile setup and invalidate cache
     */
    public UserResponse completeProfileSetup(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        updateUserFields(user, request);
        user.setProfileCompleted(true);
        User savedUser = userRepository.save(user);
        
        // Invalidate cache
        invalidateUserCache(userId);
        
        return UserResponse.fromUser(savedUser);
    }

    @Transactional(readOnly = true)
    public boolean isProfileComplete(Long userId) {
        // This is a simple query, no caching needed
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return user.isProfileCompleted();
    }

    /**
     * Invalidate user cache on any write operation
     */
    private void invalidateUserCache(Long userId) {
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Invalidated cache for user: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate cache for user {}: {}", userId, e.getMessage());
            // Continue on cache failure - data consistency is maintained
        }
    }

    private void updateUserFields(User user, UpdateProfileRequest request) {
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.dateOfBirth() != null && !request.dateOfBirth().isBlank()) {
            user.setDateOfBirth(java.time.LocalDate.parse(request.dateOfBirth()));
        }
        if (request.address() != null) {
            user.setAddress(request.address());
        }
    }
}

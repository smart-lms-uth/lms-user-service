package uth.edu.vn.lms_user_service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.lms_user_service.dto.UpdateProfileRequest;
import uth.edu.vn.lms_user_service.dto.UserResponse;
import uth.edu.vn.lms_user_service.entity.ActivityType;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.exception.ApiException;
import uth.edu.vn.lms_user_service.exception.ResourceNotFoundException;
import uth.edu.vn.lms_user_service.repository.UserRepository;
import uth.edu.vn.lms_user_service.util.CacheKeys;
import uth.edu.vn.lms_user_service.util.ErrorMessages;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements IUserService {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ActivityService activityService;

    public UserService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate, 
                       PasswordEncoder passwordEncoder, ActivityService activityService) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.activityService = activityService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        String cacheKey = CacheKeys.userProfile(userId);
        
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof UserResponse cachedResponse) {
                return cachedResponse;
            }
        } catch (Exception ignored) {
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        UserResponse response = UserResponse.fromUser(user);
        
        try {
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
        } catch (Exception ignored) {
        }
        
        return response;
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));

        updateUserFields(user, request);
        User savedUser = userRepository.save(user);
        invalidateUserCache(userId);
        
        return UserResponse.fromUser(savedUser);
    }

    @Override
    public UserResponse completeProfileSetup(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));

        updateUserFields(user, request);
        user.setProfileCompleted(true);
        User savedUser = userRepository.save(user);
        invalidateUserCache(userId);
        
        return UserResponse.fromUser(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProfileComplete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        return user.isProfileCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        return user.getPassword() != null && !user.getPassword().isBlank();
    }

    @Override
    public void setPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            throw ApiException.badRequest(ErrorMessages.PASSWORD_ALREADY_SET);
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        invalidateUserCache(userId);
        
        activityService.logSystemActivity(userId, ActivityType.PASSWORD_SET, "set-password", 
            String.format("{\"authProvider\":\"%s\"}", user.getAuthProvider()));
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw ApiException.badRequest(ErrorMessages.NO_PASSWORD_SET);
        }
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw ApiException.badRequest(ErrorMessages.INCORRECT_PASSWORD);
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        invalidateUserCache(userId);
        
        activityService.logSystemActivity(userId, ActivityType.PASSWORD_CHANGE, "change-password", null);
    }

    private void invalidateUserCache(Long userId) {
        String cacheKey = CacheKeys.userProfile(userId);
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception ignored) {
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

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByIds(List<Long> userIds) {
        return userRepository.findByIdIn(userIds).stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllTeachers() {
        return userRepository.findActiveUsersByRole(Role.TEACHER).stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllStudents() {
        return userRepository.findActiveUsersByRole(Role.STUDENT).stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        invalidateUserCache(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getAvatarUrl(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.userNotFound(userId)));
        return user.getAvatarUrl();
    }
}

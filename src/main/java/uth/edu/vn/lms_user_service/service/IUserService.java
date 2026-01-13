package uth.edu.vn.lms_user_service.service;

import uth.edu.vn.lms_user_service.dto.UpdateProfileRequest;
import uth.edu.vn.lms_user_service.dto.UserResponse;

import java.util.List;

public interface IUserService {
    
    UserResponse getProfile(Long userId);
    
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    
    UserResponse completeProfileSetup(Long userId, UpdateProfileRequest request);
    
    boolean isProfileComplete(Long userId);
    
    boolean hasPassword(Long userId);
    
    void setPassword(Long userId, String newPassword);
    
    void changePassword(Long userId, String currentPassword, String newPassword);
    
    UserResponse getUserById(Long userId);
    
    List<UserResponse> getUsersByIds(List<Long> userIds);
    
    List<UserResponse> getAllTeachers();
    
    List<UserResponse> getAllStudents();

    void updateAvatar(Long userId, String avatarUrl);

    String getAvatarUrl(Long userId);
}

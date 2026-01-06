package uth.edu.vn.lms_user_service.dto;

import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;

import java.time.LocalDateTime;

/**
 * DTO cho Admin xem thông tin user
 */
public record AdminUserDTO(
    Long id,
    String username,
    String email,
    String fullName,
    String phoneNumber,
    String avatarUrl,
    Role role,
    String authProvider,
    boolean enabled,
    boolean emailVerified,
    boolean profileCompleted,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt
) {
    public static AdminUserDTO fromUser(User user) {
        return new AdminUserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getAvatarUrl(),
            user.getRole(),
            user.getAuthProvider() != null ? user.getAuthProvider().name() : null,
            user.isEnabled(),
            user.isEmailVerified(),
            user.isProfileCompleted(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }
}

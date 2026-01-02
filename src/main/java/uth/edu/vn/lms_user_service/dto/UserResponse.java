package uth.edu.vn.lms_user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import uth.edu.vn.lms_user_service.entity.AuthProvider;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for authenticated user data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    Long id,
    String username,
    String email,
    String fullName,
    String phoneNumber,
    String avatarUrl,
    Role role,
    AuthProvider authProvider,
    boolean active,
    boolean emailVerified,
    boolean profileCompleted,
    String bio,
    LocalDate dateOfBirth,
    String address,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getAvatarUrl(),
            user.getRole(),
            user.getAuthProvider(),
            user.isEnabled(),
            user.isEmailVerified(),
            user.isProfileCompleted(),
            user.getBio(),
            user.getDateOfBirth(),
            user.getAddress(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

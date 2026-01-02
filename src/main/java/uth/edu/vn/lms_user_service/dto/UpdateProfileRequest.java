package uth.edu.vn.lms_user_service.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user profile
 */
public record UpdateProfileRequest(
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName,

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    String phoneNumber,

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    String avatarUrl,
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    String bio,
    
    String dateOfBirth,
    
    String address
) {}

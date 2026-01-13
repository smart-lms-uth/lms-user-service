package uth.edu.vn.lms_user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import uth.edu.vn.lms_user_service.entity.Role;

public record AdminUpdateUserRequest(
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    String fullName,

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    String phoneNumber,

    Role role,

    Boolean enabled,

    Boolean emailVerified
) {}

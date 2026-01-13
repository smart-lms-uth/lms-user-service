package uth.edu.vn.lms_user_service.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
    @NotBlank(message = "Token is required")
    String token
) {}

package uth.edu.vn.lms_user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Token is required")
    String token,

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()\\-_=+\\[\\]{}|;:'\",.<>/\\\\]).{8,}$",
        message = "Password must contain uppercase, lowercase, digit, and special character"
    )
    String newPassword,

    @NotBlank(message = "Confirm password is required")
    String confirmPassword
) {
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}

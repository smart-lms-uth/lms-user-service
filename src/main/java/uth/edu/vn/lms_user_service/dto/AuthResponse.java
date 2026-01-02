package uth.edu.vn.lms_user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Response DTO for authentication (login)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String refreshToken,
    UserInfo user
) {
    public AuthResponse(String accessToken, long expiresIn, UserInfo user) {
        this(accessToken, "Bearer", expiresIn, null, user);
    }

    public record UserInfo(
        Long id,
        String username,
        String email,
        String fullName,
        List<String> roles
    ) {}
}

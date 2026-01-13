package uth.edu.vn.lms_user_service.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uth.edu.vn.lms_user_service.security.JwtUtil;

@Component
public class JwtHealthIndicator implements HealthIndicator {

    private final JwtUtil jwtUtil;

    public JwtHealthIndicator(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Health health() {
        try {
            // Verify JWT configuration is valid
            if (jwtUtil.getAccessTokenExpiration() > 0 && jwtUtil.getRefreshTokenExpiration() > 0) {
                return Health.up()
                        .withDetail("accessTokenTTL", jwtUtil.getAccessTokenExpiration() + "ms")
                        .withDetail("refreshTokenTTL", jwtUtil.getRefreshTokenExpiration() + "ms")
                        .build();
            }
            return Health.down()
                    .withDetail("error", "Invalid JWT configuration")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

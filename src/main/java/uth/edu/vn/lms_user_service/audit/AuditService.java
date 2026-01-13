package uth.edu.vn.lms_user_service.audit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuditService {

    private static final String AUDIT_KEY_PREFIX = "audit:";
    private static final long AUDIT_TTL_DAYS = 90;

    private final RedisTemplate<String, Object> redisTemplate;

    public AuditService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Async
    public void logLogin(Long userId, String username, String ip, String userAgent, boolean success) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "LOGIN");
        auditData.put("userId", userId);
        auditData.put("username", username);
        auditData.put("ip", ip);
        auditData.put("userAgent", userAgent);
        auditData.put("success", success);
        auditData.put("timestamp", Instant.now().toString());

        saveAudit("login", auditData);
    }

    @Async
    public void logLogout(Long userId, String username) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "LOGOUT");
        auditData.put("userId", userId);
        auditData.put("username", username);
        auditData.put("timestamp", Instant.now().toString());

        saveAudit("logout", auditData);
    }

    @Async
    public void logPasswordChange(Long userId, String username, String ip) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "PASSWORD_CHANGE");
        auditData.put("userId", userId);
        auditData.put("username", username);
        auditData.put("ip", ip);
        auditData.put("timestamp", Instant.now().toString());

        saveAudit("security", auditData);
    }

    @Async
    public void logAccountLocked(Long userId, String username, String reason) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "ACCOUNT_LOCKED");
        auditData.put("userId", userId);
        auditData.put("username", username);
        auditData.put("reason", reason);
        auditData.put("timestamp", Instant.now().toString());

        saveAudit("security", auditData);
    }

    @Async
    public void logAdminAction(Long adminId, String action, String targetEntity, Long targetId, String details) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "ADMIN_ACTION");
        auditData.put("adminId", adminId);
        auditData.put("action", action);
        auditData.put("targetEntity", targetEntity);
        auditData.put("targetId", targetId);
        auditData.put("details", details);
        auditData.put("timestamp", Instant.now().toString());

        saveAudit("admin", auditData);
    }

    private void saveAudit(String category, Map<String, Object> data) {
        String key = AUDIT_KEY_PREFIX + category + ":" + System.currentTimeMillis();
        redisTemplate.opsForHash().putAll(key, data);
        redisTemplate.expire(key, AUDIT_TTL_DAYS, TimeUnit.DAYS);
    }
}

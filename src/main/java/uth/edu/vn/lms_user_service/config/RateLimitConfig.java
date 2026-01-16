package uth.edu.vn.lms_user_service.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int LOGIN_LIMIT = 10000;
    private static final Duration LOGIN_DURATION = Duration.ofMinutes(1);

    private static final int API_LIMIT = 100000;
    private static final Duration API_DURATION = Duration.ofMinutes(1);

    private static final int REGISTER_LIMIT = 10000;
    private static final Duration REGISTER_DURATION = Duration.ofHours(1);

    public Bucket resolveLoginBucket(String ip) {
        return buckets.computeIfAbsent("login:" + ip, k -> createBucket(LOGIN_LIMIT, LOGIN_DURATION));
    }

    public Bucket resolveApiBucket(String userId) {
        return buckets.computeIfAbsent("api:" + userId, k -> createBucket(API_LIMIT, API_DURATION));
    }

    public Bucket resolveRegisterBucket(String ip) {
        return buckets.computeIfAbsent("register:" + ip, k -> createBucket(REGISTER_LIMIT, REGISTER_DURATION));
    }

    private Bucket createBucket(int limit, Duration duration) {
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, duration));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    public void clearBucket(String key) {
        buckets.remove(key);
    }
}

package uth.edu.vn.lms_user_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Uses default Spring async executor
    // For production, consider customizing with ThreadPoolTaskExecutor
}

package uth.edu.vn.lms_user_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uth.edu.vn.lms_user_service.document.ActivityLog;
import uth.edu.vn.lms_user_service.dto.ActivityMessage;
import uth.edu.vn.lms_user_service.repository.ActivityLogRepository;

@Component
public class ActivityConsumer {

    private final ActivityLogRepository activityLogRepository;

    public ActivityConsumer(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.activity}")
    public void handleActivity(ActivityMessage message) {
        var activityLog = ActivityLog.builder()
            .userId(message.userId())
            .sessionId(message.sessionId())
            .activityType(message.activityType())
            .action(message.action())
            .pageUrl(message.pageUrl())
            .pageTitle(message.pageTitle())
            .elementId(message.elementId())
            .elementText(message.elementText())
            .apiEndpoint(message.apiEndpoint())
            .httpMethod(message.httpMethod())
            .responseStatus(message.responseStatus())
            .responseTimeMs(message.responseTimeMs())
            .metadata(message.metadata())
            .ipAddress(message.ipAddress())
            .userAgent(message.userAgent())
            .deviceType(message.deviceType())
            .browser(message.browser())
            .os(message.os())
            .screenWidth(message.screenWidth())
            .screenHeight(message.screenHeight())
            .timestamp(message.timestamp())
            .durationMs(message.durationMs())
            .build();

        activityLogRepository.save(activityLog);
    }
}

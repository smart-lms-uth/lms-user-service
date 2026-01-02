package uth.edu.vn.lms_user_service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uth.edu.vn.lms_user_service.dto.ActivityMessage;

/**
 * RabbitMQ Producer for Activity Logs
 * Sends activity messages to queue for async processing
 */
@Service
public class ActivityProducer {

    private static final Logger log = LoggerFactory.getLogger(ActivityProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.activity}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.activity}")
    private String routingKey;

    public ActivityProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send activity message to queue
     */
    public void sendActivity(ActivityMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.debug("Sent activity to queue: {} for user: {}", 
                message.activityType(), message.userId());
        } catch (Exception e) {
            log.error("Failed to send activity to queue: {}", e.getMessage(), e);
            // In production, consider fallback to direct save or dead letter queue
        }
    }

    /**
     * Send batch activities to queue
     */
    public void sendActivities(Iterable<ActivityMessage> messages) {
        messages.forEach(this::sendActivity);
    }
}

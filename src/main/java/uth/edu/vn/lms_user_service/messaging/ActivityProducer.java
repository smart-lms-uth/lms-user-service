package uth.edu.vn.lms_user_service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uth.edu.vn.lms_user_service.dto.ActivityMessage;

@Service
public class ActivityProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.activity}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.activity}")
    private String routingKey;

    public ActivityProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendActivity(ActivityMessage message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void sendActivities(Iterable<ActivityMessage> messages) {
        messages.forEach(this::sendActivity);
    }
}

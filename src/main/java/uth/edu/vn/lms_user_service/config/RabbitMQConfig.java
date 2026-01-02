package uth.edu.vn.lms_user_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Async Activity Logging
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queue.activity}")
    private String activityQueue;

    @Value("${app.rabbitmq.exchange.activity}")
    private String activityExchange;

    @Value("${app.rabbitmq.routing-key.activity}")
    private String activityRoutingKey;

    // Queue for activity logs
    @Bean
    public Queue activityQueue() {
        return QueueBuilder.durable(activityQueue)
            .withArgument("x-message-ttl", 86400000) // 24 hours TTL
            .build();
    }

    // Direct exchange for activity
    @Bean
    public DirectExchange activityExchange() {
        return new DirectExchange(activityExchange);
    }

    // Binding queue to exchange
    @Bean
    public Binding activityBinding(Queue activityQueue, DirectExchange activityExchange) {
        return BindingBuilder.bind(activityQueue)
            .to(activityExchange)
            .with(activityRoutingKey);
    }

    // JSON Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Getters for values
    public String getActivityQueue() {
        return activityQueue;
    }

    public String getActivityExchange() {
        return activityExchange;
    }

    public String getActivityRoutingKey() {
        return activityRoutingKey;
    }
}

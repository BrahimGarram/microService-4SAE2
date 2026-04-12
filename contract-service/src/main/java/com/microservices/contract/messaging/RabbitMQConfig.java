package com.microservices.contract.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for contract-service.
 * Declares the exchange, queue and binding for contract events.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CONTRACT_EXCHANGE   = "contract.exchange";
    public static final String CONTRACT_QUEUE      = "contract.created.queue";
    public static final String CONTRACT_ROUTING_KEY = "contract.created";

    @Bean
    public TopicExchange contractExchange() {
        return new TopicExchange(CONTRACT_EXCHANGE);
    }

    @Bean
    public Queue contractCreatedQueue() {
        return QueueBuilder.durable(CONTRACT_QUEUE).build();
    }

    @Bean
    public Binding contractBinding(Queue contractCreatedQueue, TopicExchange contractExchange) {
        return BindingBuilder.bind(contractCreatedQueue)
                .to(contractExchange)
                .with(CONTRACT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

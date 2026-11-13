package com.station.api.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "station.direct.exchange";
    public static final String TELEMETRIE_QUEUE = "station.telemetrie.queue";
    public static final String ALERTE_QUEUE = "station.alertes.queue";

    public static final String TELEMETRIE_ROUTING_KEY = "station.telemetrie.key";
    public static final String ALERTE_ROUTING_KEY = "station.alerte.key";

    @Bean
    public DirectExchange stationExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue telemetrieQueue() {
        return QueueBuilder.durable(TELEMETRIE_QUEUE).build();
    }

    @Bean
    public Queue alerteQueue() {
        return QueueBuilder.durable(ALERTE_QUEUE).build();
    }

    @Bean
    public Binding telemetrieBinding(Queue telemetrieQueue, DirectExchange stationExchange) {
        return BindingBuilder.bind(telemetrieQueue).to(stationExchange).with(TELEMETRIE_ROUTING_KEY);
    }

    @Bean
    public Binding alerteBinding(Queue alerteQueue, DirectExchange stationExchange) {
        return BindingBuilder.bind(alerteQueue).to(stationExchange).with(ALERTE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(mapper);
    }
}

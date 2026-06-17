package com.minimaya.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${minimaya.rabbitmq.render-exchange}")
    private String renderExchange;

    @Value("${minimaya.rabbitmq.render-jobs-queue}")
    private String renderJobsQueue;

    @Value("${minimaya.rabbitmq.render-results-queue}")
    private String renderResultsQueue;

    @Value("${minimaya.rabbitmq.preview-frames-queue}")
    private String previewFramesQueue;

    @Bean
    public TopicExchange renderExchange() {
        return new TopicExchange(renderExchange, true, false);
    }

    @Bean
    public Queue renderJobsQueue() {
        return QueueBuilder.durable(renderJobsQueue).build();
    }

    @Bean
    public Queue renderResultsQueue() {
        return QueueBuilder.durable(renderResultsQueue).build();
    }

    @Bean
    public Queue previewFramesQueue() {
        return QueueBuilder.durable(previewFramesQueue).build();
    }

    @Bean
    public Binding renderJobsBinding() {
        return BindingBuilder.bind(renderJobsQueue()).to(renderExchange()).with("render.jobs.#");
    }

    @Bean
    public Binding renderResultsBinding() {
        return BindingBuilder.bind(renderResultsQueue()).to(renderExchange()).with("render.results.#");
    }

    @Bean
    public Binding previewFramesBinding() {
        return BindingBuilder.bind(previewFramesQueue()).to(renderExchange()).with("render.previews.#");
    }

    @Bean
    public MessageConverter messageConverter() {
        // Raw binary Protobuf frames — no JSON conversion.
        return new SimpleMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

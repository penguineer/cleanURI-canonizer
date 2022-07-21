package com.penguineering.cleanuri.canonizer.amqp;

import com.penguineering.cleanuri.canonizer.tasks.ErrorResult;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitProperty;

@RabbitClient
public interface ErrorEmitter {
    @RabbitProperty(name = "contentType", value = "application/json")
    void send(@RabbitProperty("correlationId") String correlationId,
              @Binding String replyTo,
              ErrorResult error);
}

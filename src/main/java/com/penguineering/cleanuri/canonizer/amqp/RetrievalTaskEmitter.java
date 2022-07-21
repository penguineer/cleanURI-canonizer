package com.penguineering.cleanuri.canonizer.amqp;

import com.penguineering.cleanuri.canonizer.tasks.ReductionTask;
import com.penguineering.cleanuri.canonizer.tasks.RetrievalTask;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitProperty;

@RabbitClient
public interface RetrievalTaskEmitter {
    @Binding("${canonizer.retrieval-task-queue}")
    @RabbitProperty(name = "contentType", value = "application/json")
    void send(@RabbitProperty("correlationId") String correlationId,
              @RabbitProperty("replyTo") String replyTo,
              RetrievalTask task);
}

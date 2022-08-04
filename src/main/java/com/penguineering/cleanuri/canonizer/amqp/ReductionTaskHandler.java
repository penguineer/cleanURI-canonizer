package com.penguineering.cleanuri.canonizer.amqp;

import com.penguineering.cleanuri.canonizer.processors.Canonizer;
import com.penguineering.cleanuri.common.amqp.ExtractionTaskEmitter;
import com.penguineering.cleanuri.common.message.ExtractionTask;
import io.micronaut.context.annotation.Property;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.annotation.RabbitProperty;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RabbitListener
public class ReductionTaskHandler {
    @Property(name = "canonizer.retrieval-task-queue")
    String taskQueue;

    @Inject
    ExtractionTaskEmitter emitter;

    @Inject
    List<Canonizer> canonizers;

    @Queue("${canonizer.reduction-task-queue}")
    public void receive(@RabbitProperty("correlationId") String correlationId,
                        @RabbitProperty("replyTo") String replyTo,
                        final ExtractionTask task,
                        RabbitAcknowledgement acknowledgement) {
        final ExtractionTask.Builder taskBuilder = ExtractionTask.Builder.copy(task);

        try {
            Optional<Canonizer> canonizer = canonizers.stream()
                    .filter(c -> c.isSuitable(task.getRequest().getURI()))
                    .findAny();

            if (canonizer.isPresent()) {
                    final URI canonized = canonizer.get().canonize(task.getRequest().getURI());
                    taskBuilder.setCanonizedURI(canonized);
            } else {
                taskBuilder.addError("Could not find a matching canonizer!");
            }
        } catch (IllegalArgumentException e) {
            taskBuilder.addError(e.getMessage());
        }

        final ExtractionTask resultTask = taskBuilder.instance();
        emitter.send(
                resultTask.getErrors().isEmpty() ? taskQueue : replyTo,
                correlationId, replyTo, resultTask);

        acknowledgement.ack();
    }
}

package com.penguineering.cleanuri.canonizer.amqp;

import com.penguineering.cleanuri.canonizer.processors.Canonizer;
import com.penguineering.cleanuri.canonizer.tasks.ErrorResult;
import com.penguineering.cleanuri.canonizer.tasks.ReductionTask;
import com.penguineering.cleanuri.canonizer.tasks.RetrievalTask;
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
    @Inject
    RetrievalTaskEmitter retrievalTaskEmitter;

    @Inject
    ErrorEmitter errorEmitter;

    @Inject
    List<Canonizer> canonizers;

    @Queue("${canonizer.reduction-task-queue}")
    public void receive(@RabbitProperty("correlationId") String correlationId,
                        @RabbitProperty("replyTo") String replyTo,
                        final ReductionTask reductionTask,
                        RabbitAcknowledgement acknowledgement) {
        final RetrievalTask.Builder retrievalTaskBuilder = RetrievalTask.Builder
                .fromReductionTask(reductionTask);

        Optional<Canonizer> canonizer = canonizers.stream()
                .filter(c -> c.isSuitable(reductionTask.getUri()))
                .findAny();

        if (canonizer.isPresent()) {
            final URI reduced = canonizer.get().canonize(reductionTask.getUri());
            retrievalTaskBuilder.setReducedURI(reduced);
            retrievalTaskEmitter.send(correlationId, replyTo, retrievalTaskBuilder.instance());
        } else {
            errorEmitter.send(correlationId, replyTo, new ErrorResult(
                    404, "Could not find a matching canonizer!"
            ));
        }

        acknowledgement.ack();
    }
}

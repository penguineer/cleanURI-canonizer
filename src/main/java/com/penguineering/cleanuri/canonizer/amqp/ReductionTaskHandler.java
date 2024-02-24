package com.penguineering.cleanuri.canonizer.amqp;

import com.penguineering.cleanuri.common.amqp.ExtractionTaskEmitter;
import com.penguineering.cleanuri.common.message.ExtractionTask;
import com.penguineering.cleanuri.site.Canonizer;
import com.penguineering.cleanuri.site.Site;
import io.micronaut.context.annotation.Property;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.annotation.RabbitProperty;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;

@RabbitListener
public class ReductionTaskHandler {
    @Property(name = "canonizer.extractor-task-rk")
    String extractorTaskRK;

    @Inject
    ExtractionTaskEmitter emitter;

    @Inject
    List<Site> sites;

    @Queue("${canonizer.reduction-task-queue}")
    public void receive(@RabbitProperty("correlationId") String correlationId,
                        @RabbitProperty("replyTo") String replyTo,
                        final ExtractionTask task,
                        RabbitAcknowledgement acknowledgement) {
        final ExtractionTask.Builder taskBuilder = ExtractionTask.Builder.copy(task);
        final URI requestedURI = task.getRequest().getURI();

        try {
            sites.stream()
                    .filter(c -> c.canProcessURI(requestedURI))
                    .findAny()
                    .flatMap(s -> s.newCanonizer(requestedURI))
                    .map(c -> (Canonizer) c.withExceptionHandler((l, e) -> {
                        // add error if level is warning or higher
                        if (l.intValue() <= Level.WARNING.intValue())
                            taskBuilder.addError(e.getMessage());
                    }))
                    .flatMap(com.penguineering.cleanuri.site.Canonizer::canonize)
                    .ifPresentOrElse(
                            taskBuilder::setCanonizedURI,
                            () -> taskBuilder.addError("Could not find a matching canonizer!"));
        } catch (IllegalArgumentException e) {
            taskBuilder.addError(e.getMessage());
        }

        final ExtractionTask resultTask = taskBuilder.instance();
        emitter.send(
                resultTask.getErrors().isEmpty() ? extractorTaskRK : replyTo,
                correlationId, replyTo, resultTask);

        acknowledgement.ack();
    }
}

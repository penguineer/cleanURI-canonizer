package com.penguineering.cleanuri.canonizer.tasks;


import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Introspected
public class RetrievalTask {
    public static class Builder {
        public static Builder fromReductionTask(ReductionTask task) {
            return new Builder().copyMeta(task.getMeta()).setURI(task.getUri());
        }

        private URI reduced_uri;
        private URI uri;
        private Set<ReductionTask.Meta> meta = null;

        private Builder() {
        }

        public Builder setReducedURI(URI reduced_uri) {
            this.reduced_uri = reduced_uri;
            return this;
        }

        public Builder setURI(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder addMeta(ReductionTask.Meta meta) {
            if (this.meta == null)
                this.meta = new HashSet<>();
            this.meta.add(meta);
            return this;
        }

        private Builder copyMeta(Set<ReductionTask.Meta> meta) {
            this.meta = new HashSet<>();
            this.meta.addAll(meta);
            return this;
        }

        public RetrievalTask instance() {
            final RetrievalTask instance = new RetrievalTask(uri, reduced_uri, meta);
            this.meta = null;
            return instance;
        }
    }

    private final URI reduced_uri;
    private final URI uri;
    private final Set<ReductionTask.Meta> meta;

    @Internal
    RetrievalTask(URI uri, URI reduced_uri, Set<ReductionTask.Meta> meta) {
        this.uri = uri;
        this.reduced_uri = reduced_uri;
        this.meta = meta;
    }

    public URI getUri() {
        return uri;
    }

    public URI getReduced_uri() {
        return reduced_uri;
    }

    public Set<ReductionTask.Meta> getMeta() {
        return meta;
    }
}

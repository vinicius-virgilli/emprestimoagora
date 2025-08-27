package org.viniciusvirgilli.config;

import io.smallrye.context.api.ManagedExecutorConfig;
import io.smallrye.context.api.NamedInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

@ApplicationScoped
public class ManagedExecutorConfiguration {

    @Produces
    @ApplicationScoped
    @ManagedExecutorConfig(propagated = ThreadContext.ALL_REMAINING)
    @NamedInstance("MyExecutor")
    public ManagedExecutor createMyExecutor() {
        return ManagedExecutor.builder()
                .propagated(ThreadContext.ALL_REMAINING)
                .build();
    }
}
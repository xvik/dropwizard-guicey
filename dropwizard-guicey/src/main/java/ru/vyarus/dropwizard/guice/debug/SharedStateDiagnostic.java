package ru.vyarus.dropwizard.guice.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Prints shared state usage during application startup. Shows:
 * <ul>
 *  <li>What objects stored in state
 *  <li>Who accessed stored objects (preserving access order)
 *  <li>Misses (requesting not yet available values)
 *  <li>Never set, but requested objects (including never called state value listeners).
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 20.03.2025
 */
public class SharedStateDiagnostic extends UniqueGuiceyLifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(SharedStateDiagnostic.class);

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        logger.info("Shared configuration state usage: \n{}", event.getSharedState().getAccessReport());
    }
}

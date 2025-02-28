package ru.vyarus.dropwizard.guice.test.jupiter.setup.log.support;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class DManaged implements Managed {
    private final Logger logger = LoggerFactory.getLogger(DManaged.class);

    @Override
    public void start() throws Exception {
        logger.trace("Managed started");
    }

    @Override
    public void stop() throws Exception {
        logger.trace("Managed stopped");
    }
}

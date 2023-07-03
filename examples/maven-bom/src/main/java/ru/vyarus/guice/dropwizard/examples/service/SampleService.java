package ru.vyarus.guice.dropwizard.examples.service;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Rusakov
 * @since 03.07.2023
 */
public class SampleService implements Managed {

    private final Logger logger = LoggerFactory.getLogger(SampleService.class);

    @Override
    public void start() throws Exception {
        logger.info("Starting some resource");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Shutting down some resource");
    }
}

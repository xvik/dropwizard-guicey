package ru.vyarus.dropwizard.guice.examples.service;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 05.02.2017
 */
@Singleton
public class SampleBootstrap implements Managed {
    private final Logger logger = LoggerFactory.getLogger(SampleBootstrap.class);

    @Override
    public void start() throws Exception {
        logger.info("Starting some resource");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Shutting down some resource");
    }
}

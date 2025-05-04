package ru.vyarus.dropwizard.guice.test.log.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class GBundle1 implements GuiceyBundle {
    private final Logger logger = LoggerFactory.getLogger(GBundle1.class);

    @Override
    public void initialize(GuiceyBootstrap bootstrap) {
        logger.trace("Bundle initialized");
    }

    @Override
    public void run(GuiceyEnvironment environment) throws Exception {
        logger.trace("Bundle started");
    }
}

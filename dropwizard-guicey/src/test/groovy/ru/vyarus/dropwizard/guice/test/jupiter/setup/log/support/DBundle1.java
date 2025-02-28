package ru.vyarus.dropwizard.guice.test.jupiter.setup.log.support;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class DBundle1 implements ConfiguredBundle<Configuration> {
    private final Logger logger = LoggerFactory.getLogger(DBundle1.class);

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        logger.trace("Bundle initialized");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        logger.trace("Bundle started");
    }
}

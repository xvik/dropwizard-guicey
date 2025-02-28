package ru.vyarus.dropwizard.guice.test.jupiter.setup.log.support;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 27.02.2025
 */
public class LogRecordsApp extends Application<Configuration> {
    private final Logger logger = LoggerFactory.getLogger(LogRecordsApp.class);

    public LogRecordsApp() {
        logger.trace("Constructor");
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        logger.trace("Before init");
        bootstrap.addBundle(new DBundleBefore());
        bootstrap.addBundle(GuiceBundle.builder()
                .bundles(new GBundle1())
                .dropwizardBundles(new DBundle1())
                .modules(new GModule())
                .enableAutoConfig().build());
        bootstrap.addBundle(new DBundleAfter());
        logger.trace("After init");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        logger.trace("Run");
    }
}

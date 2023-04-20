package ru.vyarus.guicey.admin

import ch.qos.logback.classic.Level
import io.dropwizard.logging.BootstrapLogging
import io.dropwizard.logging.LoggingUtil
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 22.10.2019
 */
class AbstractTest extends Specification {
    static {
        BootstrapLogging.bootstrap(Level.DEBUG); // bootstrap set threshold filter!
        LoggingUtil.getLoggerContext().getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).setLevel(Level.WARN);
        LoggingUtil.getLoggerContext().getLogger("ru.vyarus.dropwizard.guice").setLevel(Level.INFO);
    }

    void cleanupSpec() {
        // some tests are intentionally failing so be sure to remove stale applications
        SharedConfigurationState.clear()
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }
}

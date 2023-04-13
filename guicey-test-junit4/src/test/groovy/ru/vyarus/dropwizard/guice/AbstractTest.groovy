package ru.vyarus.dropwizard.guice

import ch.qos.logback.classic.Level
import com.codahale.metrics.health.HealthCheckRegistry
import io.dropwizard.jersey.setup.JerseyEnvironment
import io.dropwizard.jetty.MutableServletContextHandler
import io.dropwizard.jetty.setup.ServletEnvironment
import io.dropwizard.lifecycle.setup.LifecycleEnvironment
import io.dropwizard.logging.common.BootstrapLogging
import io.dropwizard.logging.common.LoggingUtil
import io.dropwizard.core.setup.AdminEnvironment
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.EnableHook
import spock.lang.Specification

import jakarta.servlet.FilterRegistration
import jakarta.servlet.ServletRegistration

/**
 * Base class for tests.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
abstract class AbstractTest extends Specification {

    static {
        BootstrapLogging.bootstrap(Level.DEBUG); // bootstrap set threshold filter!
        LoggingUtil.getLoggerContext().getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).setLevel(Level.WARN);
        LoggingUtil.getLoggerContext().getLogger("ru.vyarus.dropwizard.guice").setLevel(Level.INFO);
    }

    // common guicey extra extensions used for all tests
    @EnableHook
    public static GuiceyConfigurationHook TEST_HOOK = { it.bundles(new HK2DebugBundle(), new GuiceRestrictedConfigBundle()) }

    void cleanupSpec() {
        // some tests are intentionally failing so be sure to remove stale applications
        SharedConfigurationState.clear()
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    Environment mockEnvironment() {
        def environment = Mock(Environment)
        environment.jersey() >> Mock(JerseyEnvironment)
        environment.servlets() >> Mock(ServletEnvironment)
        environment.servlets().addFilter(*_) >> Mock(FilterRegistration.Dynamic)
        environment.servlets().addServlet(*_) >> Mock(ServletRegistration.Dynamic)
        environment.getApplicationContext() >> Mock(MutableServletContextHandler)
        environment.admin() >> Mock(AdminEnvironment)
        environment.admin().addFilter(*_) >> Mock(FilterRegistration.Dynamic)
        environment.admin().addServlet(*_) >> Mock(ServletRegistration.Dynamic)
        environment.getAdminContext() >> Mock(MutableServletContextHandler)
        environment.lifecycle() >> Mock(LifecycleEnvironment)
        environment.healthChecks() >> Mock(HealthCheckRegistry)
        return environment
    }
}

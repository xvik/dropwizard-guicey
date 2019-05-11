package ru.vyarus.dropwizard.guice

import com.codahale.metrics.health.HealthCheckRegistry
import io.dropwizard.jersey.setup.JerseyEnvironment
import io.dropwizard.jetty.MutableServletContextHandler
import io.dropwizard.jetty.setup.ServletEnvironment
import io.dropwizard.lifecycle.setup.LifecycleEnvironment
import io.dropwizard.setup.AdminEnvironment
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyConfiguration
import spock.lang.Specification

import javax.servlet.FilterRegistration
import javax.servlet.ServletRegistration

/**
 * Base class for tests.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
@UseGuiceyConfiguration(GuiceyTestHook)
abstract class AbstractTest extends Specification {

    void cleanupSpec() {
        // some tests are intentionally failing so be sure to remove stale applications
        InjectorLookup.clear()
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

    // common guicey extra extensions used for all tests
    static class GuiceyTestHook implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {
            builder.bundles(new GuiceRestrictedConfigBundle())
        }
    }
}

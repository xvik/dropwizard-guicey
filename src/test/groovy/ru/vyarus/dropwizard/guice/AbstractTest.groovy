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
import ru.vyarus.dropwizard.guice.support.util.GuiceRestrictedConfigBundle
import spock.lang.Specification

import javax.servlet.FilterRegistration
import javax.servlet.ServletRegistration

/**
 * Base class for tests.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
abstract class AbstractTest extends Specification {

    static {
        // setupSpec is too late - app already launched
        PropertyBundleLookup.enableBundles(HK2DebugBundle, GuiceRestrictedConfigBundle)
    }

    void setupSpec() {
        assert System.getProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    void cleanupSpec() {
        InjectorLookup.clear()
        // recover system property after test
        PropertyBundleLookup.enableBundles(HK2DebugBundle, GuiceRestrictedConfigBundle)
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

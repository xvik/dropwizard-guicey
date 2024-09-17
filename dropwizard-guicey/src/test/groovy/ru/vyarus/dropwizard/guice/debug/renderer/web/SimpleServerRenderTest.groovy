package ru.vyarus.dropwizard.guice.debug.renderer.web

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 25.10.2019
 */
@TestDropwizardApp(value = App,
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/simple-server.yml',
        configOverride = ["server.applicationContextPath: /app/"])
class SimpleServerRenderTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Environment environment
    @Inject
    GuiceyConfigurationInfo info
    WebMappingsRenderer renderer

    void setup() {
        renderer = new WebMappingsRenderer(environment, info)
    }

    def "Check complete render"() {

        expect:
        render(new MappingsConfig()
                .showMainContext()
                .showAdminContext()
                .showDropwizardMappings()) == """

    MAIN /app
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                                     [REQUEST]       io.dropwizard.jersey.filter.AllowedMethodsFilter-11111111
    ├── filter     /*                   async   ZipExceptionHandlingServletFilter (io.dropwizard.jetty)                              [REQUEST]       io.dropwizard.jetty.ZipExceptionHandlingServletFilter-11111111
    ├── filter     /*                   async   ThreadNameFilter             (i.d.servlets)                                          [REQUEST]       io.dropwizard.servlets.ThreadNameFilter-11111111
    ├── servlet    /rest/*              async   JerseyServletContainer       (i.d.jersey.setup)                                                      jersey
    └── servlet    /                    async   Default404Servlet            (o.e.j.e.s.ServletHandler)                                              org.eclipse.jetty.ee10.servlet.ServletHandler\$Default404Servlet-11111111


    ADMIN /admin
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                                     [REQUEST]       io.dropwizard.jersey.filter.AllowedMethodsFilter-11111111
    ├── servlet    /tasks/*             async   TaskServlet                  (i.d.servlets.tasks)                                                    tasks
    ├── servlet    /*                   async   AdminServlet                 (i.d.metrics.servlets)                                                  io.dropwizard.metrics.servlets.AdminServlet-11111111
    └── servlet    /                    async   Default404Servlet            (o.e.j.e.s.ServletHandler)                                              org.eclipse.jetty.ee10.servlet.ServletHandler\$Default404Servlet-11111111
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .printWebMappings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(MappingsConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
                .replaceAll('-[^\\s]{5,8}([\\s]+)', "-11111111\$1")
    }
}

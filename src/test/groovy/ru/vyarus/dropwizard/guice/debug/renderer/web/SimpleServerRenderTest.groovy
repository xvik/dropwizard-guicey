package ru.vyarus.dropwizard.guice.debug.renderer.web

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.ConfigOverride
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 25.10.2019
 */
@UseDropwizardApp(value = App,
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/simple-server.yml',
        configOverride = @ConfigOverride(key = "server.applicationContextPath", value = "/app/"))
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
    ├── filter     /*                   async   ThreadNameFilter             (i.d.servlets)                                          [REQUEST]       io.dropwizard.servlets.ThreadNameFilter-11111111
    ├── servlet    /rest/*              async   JerseyServletContainer       (i.d.jersey.setup)                                                      io.dropwizard.jersey.setup.JerseyServletContainer-11111111
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)                                                org.eclipse.jetty.servlet.ServletHandler\$Default404Servlet-11111111


    ADMIN /admin
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                                     [REQUEST]       io.dropwizard.jersey.filter.AllowedMethodsFilter-11111111
    ├── servlet    /tasks/*             async   TaskServlet                  (i.d.servlets.tasks)                                                    tasks
    ├── servlet    /*                   async   AdminServlet                 (c.c.metrics.servlets)                                                  com.codahale.metrics.servlets.AdminServlet-11111111
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)                                                org.eclipse.jetty.servlet.ServletHandler\$Default404Servlet-11111111
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
                .replaceAll('-[^\\s]{7,8}([\\s]+)', "-11111111\$1")
    }
}

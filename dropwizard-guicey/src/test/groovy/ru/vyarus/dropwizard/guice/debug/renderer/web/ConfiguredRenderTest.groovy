package ru.vyarus.dropwizard.guice.debug.renderer.web

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.web.support.UserServletsBundle
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 24.10.2019
 */
@TestDropwizardApp(App)
class ConfiguredRenderTest extends Specification {

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
                .showGuiceMappings()
                .showDropwizardMappings()) == """

    MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [ERROR]         .custommapping
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .async
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .both
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .main
    ├── filter     /2/*                         --"--
    ├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                                    [REQUEST]       Guice Filter
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                                     [REQUEST]       io.dropwizard.jersey.filter.AllowedMethodsFilter-11111111
    ├── filter     /*                   async   ZipExceptionHandlingServletFilter (io.dropwizard.jetty)                              [REQUEST]       io.dropwizard.jetty.ZipExceptionHandlingServletFilter-11111111
    ├── filter     /*                   async   ThreadNameFilter             (i.d.servlets)                                          [REQUEST]       io.dropwizard.servlets.ThreadNameFilter-11111111
    │
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    target
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .targetservlet
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .both
    ├── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .async
    ├── servlet    /*                   async   JerseyServletContainer       (i.d.jersey.setup)                                                      jersey
    └── servlet    /                    async   Default404Servlet            (o.e.j.e.s.ServletHandler)                                              org.eclipse.jetty.ee10.servlet.ServletHandler\$Default404Servlet-11111111


    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .both
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .admin
    ├── filter     /2/*                         --"--
    ├── filter     /*                   async   AdminGuiceFilter             (r.v.d.g.m.i.internal)                                  [REQUEST]       Guice Filter
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                                     [REQUEST]       io.dropwizard.jersey.filter.AllowedMethodsFilter-11111111
    ├── servlet    /tasks/*             async   TaskServlet                  (i.d.servlets.tasks)                                                    tasks
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .admin
    ├── servlet    /baradmin                    --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .both
    ├── servlet    /*                   async   AdminServlet                 (i.d.metrics.servlets)                                                  io.dropwizard.metrics.servlets.AdminServlet-11111111
    └── servlet    /                    async   Default404Servlet            (o.e.j.e.s.ServletHandler)                                              org.eclipse.jetty.ee10.servlet.ServletHandler\$Default404Servlet-11111111
""" as String;
    }

    def "Check user only mappings render"() {

        expect:
        render(new MappingsConfig()
                .showMainContext()
                .showAdminContext()) == """

    MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [ERROR]         .custommapping
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .async
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .both
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .main
    ├── filter     /2/*                         --"--
    │
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    target
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .targetservlet
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .both
    └── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .async


    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .both
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .admin
    ├── filter     /2/*                         --"--
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .admin
    ├── servlet    /baradmin                    --"--
    └── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .both
""" as String;
    }

    def "Check admin only render"() {

        expect:
        render(new MappingsConfig()
                .showAdminContext()) == """

    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .both
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)                    [REQUEST]       .admin
    ├── filter     /2/*                         --"--
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .admin
    ├── servlet    /baradmin                    --"--
    └── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)                                    .both
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new UserServletsBundle())
                    .printWebMappings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(MappingsConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
                .replaceAll('-[^\\s]{5,8}([\\s$]+)', "-11111111\$1")
    }
}

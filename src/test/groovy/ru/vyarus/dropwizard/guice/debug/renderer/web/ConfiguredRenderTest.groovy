package ru.vyarus.dropwizard.guice.debug.renderer.web

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.web.support.UserServletsBundle
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 24.10.2019
 */
@UseDropwizardApp(App)
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
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [ERROR]
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--
    ├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                          [REQUEST]
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                           [REQUEST]
    ├── filter     /*                   async   ThreadNameFilter             (i.d.servlets)                                [REQUEST]
    │
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)
    ├── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)
    ├── servlet    /*                   async   JerseyServletContainer       (i.d.jersey.setup)
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)


    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--
    ├── filter     /*                   async   AdminGuiceFilter             (r.v.d.g.m.i.internal)                        [REQUEST]
    ├── filter     /*                   async   AllowedMethodsFilter         (i.d.jersey.filter)                           [REQUEST]
    ├── servlet    /tasks/*             async   TaskServlet                  (i.d.servlets.tasks)
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)
    ├── servlet    /baradmin                    --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)
    ├── servlet    /*                   async   AdminServlet                 (c.c.metrics.servlets)
    └── servlet    /                    async   Default404Servlet            (o.e.j.s.ServletHandler)
""" as String;
    }

    def "Check user only mappings render"() {

        expect:
        render(new MappingsConfig()
                .showMainContext()
                .showAdminContext()) == """

    MAIN /
    ├── filter     /custom/*                    CustomMappingFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [ERROR]
    ├── filter     /async/*             async   AsyncFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         MainFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--
    │
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)
    └── servlet    /async               async   AsyncServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)


    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)
    ├── servlet    /baradmin                    --"--
    └── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)
""" as String;
    }

    def "Check admin only render"() {

        expect:
        render(new MappingsConfig()
                .showAdminContext()) == """

    ADMIN /
    ├── filter     /both/*                      BothFilter                   (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /1/*                         AdminFilter                  (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    ├── filter     /2/*                         --"--
    ├── servlet    /fooadmin                    AdminServlet                 (r.v.d.g.d.r.w.s.UserServletsBundle)
    ├── servlet    /baradmin                    --"--
    └── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)
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
    }
}

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
class UserServletsRenderTest extends Specification {

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

    def "Check user servlets and filters render"() {

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

    def "Check disabled servlets render"() {

        when: "disabling servlet"
        def servlet = environment.getApplicationContext().getServletHandler().getServlet("target")
        servlet.setEnabled(false)
        servlet.stop()
        environment.getApplicationContext().getServletHandler().getServlet(".both").stop()

        then: "marker shown"
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
    ├── servlet    /foo                         MainServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle) *DISABLED STOPPED
    │   └── filter                                  TargetServletFilter          (r.v.d.g.d.r.w.s.UserServletsBundle)          [REQUEST]
    │
    ├── servlet    /bar                         --"--
    ├── servlet    /both                        BothServlet                  (r.v.d.g.d.r.w.s.UserServletsBundle)      STOPPED
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

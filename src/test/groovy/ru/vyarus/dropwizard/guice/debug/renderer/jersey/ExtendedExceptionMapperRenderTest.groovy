package ru.vyarus.dropwizard.guice.debug.renderer.jersey

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.glassfish.jersey.internal.inject.InjectionManager
import org.glassfish.jersey.spi.ExtendedExceptionMapper
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfig
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfigRenderer
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.core.Response
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 27.10.2019
 */
@TestDropwizardApp(App)
class ExtendedExceptionMapperRenderTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    InjectionManager manager
    JerseyConfigRenderer renderer

    void setup() {
        renderer = new JerseyConfigRenderer(manager, true)
    }

    def "Check extended exception mapper render"() {

        expect:
        render(new JerseyConfig().showExceptionMappers()) == """

    Exception mappers
        Throwable                      ExceptionMapperBinder\$1      (i.d.core.setup)
        EofException                   EarlyEofExceptionMapper      (i.d.jersey.errors)
        EmptyOptionalException         EmptyOptionalExceptionMapper (i.d.jersey.optional)
        IOException                    ExtMapper                    (r.v.d.g.d.r.j.ExtendedExceptionMapperRenderTest) *extended
        IllegalStateException          IllegalStateExceptionMapper  (i.d.jersey.errors)
        JerseyViolationException       JerseyViolationExceptionMapper (i.d.j.validation)
        JsonProcessingException        JsonProcessingExceptionMapper (i.d.jersey.jackson)
        ValidationException            ValidationExceptionMapper    (o.g.j.s.v.internal)
""" as String;
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(ExtMapper)
                    .printJerseyConfig()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Provider
    static class ExtMapper implements ExtendedExceptionMapper<IOException> {
        @Override
        boolean isMappable(IOException exception) {
            return false
        }

        @Override
        Response toResponse(IOException exception) {
            return null
        }
    }


    String render(JerseyConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}

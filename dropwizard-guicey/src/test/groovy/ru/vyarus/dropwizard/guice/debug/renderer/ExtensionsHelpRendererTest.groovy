package ru.vyarus.dropwizard.guice.debug.renderer

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.debug.report.extensions.ExtensionsHelpRenderer
import ru.vyarus.dropwizard.guice.diagnostic.BaseDiagnosticTest
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InstallersResolvedEvent
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 12.12.2022
 */
@TestGuiceyApp(App)
class ExtensionsHelpRendererTest extends BaseDiagnosticTest {

    @Inject
    Bootstrap bootstrap

    def "Check extensions help render"() {

        expect:
        new ExtensionsHelpRenderer((bootstrap.getApplication() as App).installers)
                .renderReport(null).replaceAll("\r", "").replaceAll(" +\n", "\n") == """

    lifecycle            (r.v.d.g.m.i.f.LifeCycleInstaller)
        implements LifeCycle

    managed              (r.v.d.g.m.i.feature.ManagedInstaller)
        implements Managed

    jerseyfeature        (r.v.d.g.m.i.f.j.JerseyFeatureInstaller)
        implements Feature

    jerseyprovider       (r.v.d.g.m.i.f.j.p.JerseyProviderInstaller)
        @Provider on class
        implements ExceptionMapper
        implements ParamConverterProvider
        implements ContextResolver
        implements MessageBodyReader
        implements MessageBodyWriter
        implements ReaderInterceptor
        implements WriterInterceptor
        implements ContainerRequestFilter
        implements ContainerResponseFilter
        implements DynamicFeature
        implements ValueParamProvider
        implements InjectionResolver
        implements ApplicationEventListener
        implements ModelProcessor

    resource             (r.v.d.g.m.i.f.j.ResourceInstaller)
        @Path on class
        @Path on implemented interface

    eagersingleton       (r.v.d.g.m.i.f.e.EagerSingletonInstaller)
        @EagerSingleton on class

    healthcheck          (r.v.d.g.m.i.f.h.HealthCheckInstaller)
        extends NamedHealthCheck

    task                 (r.v.d.g.m.i.feature.TaskInstaller)
        extends Task

    plugin               (r.v.d.g.m.i.f.plugin.PluginInstaller)
        @Plugin on class
        custom annotation on class, annotated with @Plugin

    webservlet           (r.v.d.g.m.i.f.w.WebServletInstaller)
        extends HttpServlet + @WebServlet

    webfilter            (r.v.d.g.m.i.f.web.WebFilterInstaller)
        implements Filter + @WebFilter

    weblistener          (r.v.d.g.m.i.f.w.l.WebListenerInstaller)
        implements EventListener + @WebListener

    custom               (r.v.d.g.d.r.ExtensionsHelpRendererTest\$CustomInstaller)
        sign 1
        sign 2

    custom2              (r.v.d.g.d.r.ExtensionsHelpRendererTest\$CustomInstaller2)
        <no information provided>
"""
    }

    static class App extends Application<Configuration> {

        List<FeatureInstaller> installers

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .installers(CustomInstaller, CustomInstaller2)
                    .listen(new GuiceyLifecycleAdapter() {
                        @Override
                        protected void installersResolved(InstallersResolvedEvent event) {
                            installers = event.installers
                        }
                    })
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class CustomInstaller implements FeatureInstaller {
        @Override
        boolean matches(Class<?> type) {
            return false
        }

        @Override
        void report() {
        }

        @Override
        List<String> getRecognizableSigns() {
            return Arrays.asList("sign 1", "sign 2")
        }
    }

    static class CustomInstaller2 implements FeatureInstaller {
        @Override
        boolean matches(Class<?> type) {
            return false
        }

        @Override
        void report() {
        }
    }
}

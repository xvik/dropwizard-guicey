package ru.vyarus.dropwizard.guice.debug.renderer.guice


import com.google.inject.Injector
import com.google.inject.TypeLiteral
import com.google.inject.spi.ProvisionListener
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
@UseDropwizardApp(App)
class GuiceRendererCasesTest extends Specification {

    static {
        System.clearProperty(PropertyBundleLookup.BUNDLES_PROPERTY)
    }

    @Inject
    Injector injector
    GuiceBindingsRenderer renderer

    void setup() {
        renderer = new GuiceBindingsRenderer(injector)
    }

    def "Check custom render"() {

        expect:
        render(new GuiceConfig()
                .hideGuiceBindings()
                .hideGuiceyBindings()) == """

    1 MODULES with 3 bindings
    │
    └── CasesModule                  (r.v.d.g.d.r.g.support)
        ├── <typelistener>                        CustomTypeListener                              at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:19)
        ├── <provisionlistener>                   CustomProvisionListener                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:26)
        ├── <aop>                                 CustomAop                                       at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:33)
        ├── untargetted          [@Singleton]     AopedService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:36) *AOP
        ├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:37) *OVERRIDDEN
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:38) *OVERRIDDEN


    1 OVERRIDING MODULES with 2 bindings
    │
    └── OverrideModule               (r.v.d.g.d.r.g.support)
        ├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:14) *OVERRIDE
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:15) *OVERRIDE


    1 UNDECLARED bindings
    └── JitService                   (r.v.d.g.d.r.g.GuiceRendererCasesTest)


    BINDING CHAINS
    └── BindService  --[linked]-->  OverrideService
""" as String;
    }


    def "Check all render"() {

        expect:
        render(new GuiceConfig()) == """

    6 MODULES with 87 bindings
    │
    ├── CasesModule                  (r.v.d.g.d.r.g.support)
    │   ├── <typelistener>                        CustomTypeListener                              at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:19)
    │   ├── <provisionlistener>                   CustomProvisionListener                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:26)
    │   ├── <aop>                                 CustomAop                                       at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:33)
    │   ├── untargetted          [@Singleton]     AopedService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:36) *AOP
    │   ├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:37) *OVERRIDDEN
    │   └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:38) *OVERRIDDEN
    │
    └── GuiceBootstrapModule         (r.v.d.guice.module)
        ├── <scope>              [@Prototype]     -                                               at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:51)
        ├── instance             [@Singleton]     Options                                         at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:57)
        ├── instance             [@Singleton]     ConfigurationInfo                               at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:60)
        ├── instance             [@Singleton]     StatsInfo                                       at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:61)
        ├── instance             [@Singleton]     OptionsInfo                                     at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:62)
        ├── untargetted          [@Singleton]     GuiceyConfigurationInfo                         at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.configure(GuiceBootstrapModule.java:63)
        ├── instance             [@Singleton]     Bootstrap                                       at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.bindEnvironment(GuiceBootstrapModule.java:71)
        ├── instance             [@Singleton]     Environment                                     at ru.vyarus.dropwizard.guice.module.GuiceBootstrapModule.bindEnvironment(GuiceBootstrapModule.java:72)
        │
        ├── InstallerModule              (r.v.d.g.m.installer)
        │   └── instance             [@Singleton]     ExtensionsHolder                                at ru.vyarus.dropwizard.guice.module.installer.InstallerModule.configure(InstallerModule.java:34)
        │
        ├── Jersey2Module                (r.v.d.g.m.jersey)
        │   ├── providerinstance     [@Prototype]     InjectionManager                                at ru.vyarus.dropwizard.guice.module.jersey.Jersey2Module.configure(Jersey2Module.java:58)
        │   │
        │   └── GuiceBindingsModule          (r.v.d.g.m.jersey.hk2)
        │       ├── providerinstance     [@Prototype]     Application                                     at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@Prototype]     MultivaluedParameterExtractorProvider           at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@Prototype]     Providers                                       at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@RequestScoped] AsyncContext                                    at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@RequestScoped] ContainerRequest                                at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@RequestScoped] HttpHeaders                                     at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@RequestScoped] Request                                         at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@RequestScoped] ResourceInfo                                    at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       ├── providerinstance     [@RequestScoped] SecurityContext                                 at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │       └── providerinstance     [@RequestScoped] UriInfo                                         at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:179)
        │
        └── ConfigBindingModule          (r.v.d.g.m.yaml.bind)
            ├── instance             [@Singleton]     ConfigurationTree                               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.configure(ConfigBindingModule.java:45)
            ├── instance             [@Singleton]     Configuration                                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindRootTypes(ConfigBindingModule.java:63)
            ├── instance             [@Singleton]     @Config Configuration                           at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindRootTypes(ConfigBindingModule.java:65)
            ├── instance             [@Singleton]     @Config GzipHandlerFactory                      at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:79)
            ├── instance             [@Singleton]     @Config LoggingFactory                          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:79)
            ├── instance             [@Singleton]     @Config MetricsFactory                          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:79)
            ├── instance             [@Singleton]     @Config RequestLogFactory<Object>               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:79)
            ├── instance             [@Singleton]     @Config ServerFactory                           at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:79)
            ├── instance             [@Singleton]     @Config ServerPushFilterFactory                 at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:79)
            ├── instance             [@Singleton]     @Config("logging") LoggingFactory               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("logging.appenders") List<AppenderFactory<ILoggingEvent>>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("logging.level") String                 at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("logging.loggers") Map<String, JsonNode>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("metrics") MetricsFactory               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("metrics.frequency") Duration           at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("metrics.reportOnStop") Boolean         at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("metrics.reporters") List<ReporterFactory>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server") ServerFactory                 at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.adminConnectors") List<ConnectorFactory>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.adminContextPath") String       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.adminMaxThreads") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.adminMinThreads") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.allowedMethods") Set<String>    at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.applicationConnectors") List<ConnectorFactory>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.applicationContextPath") String   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.detailedJsonProcessingExceptionMapper") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.dumpAfterStart") Boolean        at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.dumpBeforeStop") Boolean        at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.enableThreadNameFilter") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip") GzipHandlerFactory       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip.bufferSize") DataSize      at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip.deflateCompressionLevel") Integer   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip.enabled") Boolean          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip.excludedUserAgentPatterns") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip.gzipCompatibleInflation") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip.minimumEntitySize") DataSize   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.gzip.syncFlush") Boolean        at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.idleThreadTimeout") Duration    at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.maxQueuedRequests") Integer     at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.maxThreads") Integer            at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.minThreads") Integer            at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.registerDefaultExceptionMappers") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.requestLog") RequestLogFactory<Object>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.requestLog.appenders") List<AppenderFactory<IAccessEvent>>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.rootPath") Optional<String>     at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.serverPush") ServerPushFilterFactory   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.serverPush.associatePeriod") Duration   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.serverPush.enabled") Boolean    at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.serverPush.maxAssociations") Integer   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── instance             [@Singleton]     @Config("server.shutdownGracePeriod") Duration   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.gid") Integer                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.group") String                  at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.compressedMimeTypes") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.excludedMimeTypes") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.excludedPaths") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.includedMethods") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.includedPaths") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.nofileHardLimit") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.nofileSoftLimit") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.serverPush.refererHosts") List<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.serverPush.refererPorts") List<Integer>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.startsAsRoot") Boolean          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.uid") Integer                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            ├── providerinstance     [@Prototype]     @Config("server.umask") String                  at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)
            └── providerinstance     [@Prototype]     @Config("server.user") String                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:92)


    1 OVERRIDING MODULES with 2 bindings
    │
    └── OverrideModule               (r.v.d.g.d.r.g.support)
        ├── linkedkey            [@Prototype]     BindService                                     at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:14) *OVERRIDE
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:15) *OVERRIDE


    1 UNDECLARED bindings
    └── JitService                   (r.v.d.g.d.r.g.GuiceRendererCasesTest)


    BINDING CHAINS
    └── BindService  --[linked]-->  OverrideService
""" as String;
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new CasesModule())
                    .modulesOverride(new OverrideModule())
                    .printGuiceBindings()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    String render(GuiceConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    static class CustomTypeListener implements TypeListener {
        @Override
        def <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {

        }
    }

    static class CustomProvisionListener implements ProvisionListener {
        @Override
        def <T> void onProvision(ProvisionInvocation<T> provision) {

        }
    }

    static class CustomAop implements MethodInterceptor {
        @Override
        Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.proceed()
        }
    }

    @Singleton
    static class AopedService {
        @Inject
        JitService service
    }

    @Singleton
    static class JitService {}

    static interface BindService {}

    static interface BindService2 {}

    static class OverriddenService implements BindService {}

    static class OverrideService implements BindService {}
}

package ru.vyarus.dropwizard.guice.debug.renderer.guice

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule
import ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
@TestDropwizardApp(App)
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
        ├── linkedkey            [@Prototype]     BindService --> OverriddenService               at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:37) *OVERRIDDEN
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:38) *OVERRIDDEN


    1 OVERRIDING MODULES with 2 bindings
    │
    └── OverrideModule               (r.v.d.g.d.r.g.support)
        ├── linkedkey            [@Prototype]     BindService --> OverrideService                 at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:16) *OVERRIDE
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:17) *OVERRIDE


    1 UNDECLARED bindings
    └── JitService                   (r.v.d.g.d.r.g.s.exts)


    BINDING CHAINS
    └── BindService  --[linked]-->  OverrideService
""" as String;
    }

    def "Check no yaml bindings render"() {

        expect:
        render(new GuiceConfig()
                .hideGuiceBindings()
                .hideYamlBindings()) == """

    6 MODULES with 22 bindings
    │
    ├── CasesModule                  (r.v.d.g.d.r.g.support)
    │   ├── <typelistener>                        CustomTypeListener                              at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:19)
    │   ├── <provisionlistener>                   CustomProvisionListener                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:26)
    │   ├── <aop>                                 CustomAop                                       at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:33)
    │   ├── untargetted          [@Singleton]     AopedService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:36) *AOP
    │   ├── linkedkey            [@Prototype]     BindService --> OverriddenService               at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:37) *OVERRIDDEN
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
        │   └── instance             [@Singleton]     ExtensionsHolder                                at ru.vyarus.dropwizard.guice.module.installer.InstallerModule.configure(InstallerModule.java:30)
        │
        └── Jersey2Module                (r.v.d.g.m.jersey)
            ├── providerinstance     [@Prototype]     InjectionManager                                at ru.vyarus.dropwizard.guice.module.jersey.Jersey2Module.configure(Jersey2Module.java:59)
            ├── GuiceWebModule               (r.v.d.g.m.jersey)         *WEB
            │
            └── GuiceBindingsModule          (r.v.d.g.m.jersey.hk2)
                ├── providerinstance     [@Prototype]     Application                                     at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@Prototype]     MultivaluedParameterExtractorProvider           at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@Prototype]     Providers                                       at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@RequestScoped] AsyncContext                                    at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@RequestScoped] ContainerRequest                                at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@RequestScoped] HttpHeaders                                     at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@RequestScoped] Request                                         at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@RequestScoped] ResourceInfo                                    at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                ├── providerinstance     [@RequestScoped] SecurityContext                                 at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
                └── providerinstance     [@RequestScoped] UriInfo                                         at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)


    1 OVERRIDING MODULES with 2 bindings
    │
    └── OverrideModule               (r.v.d.g.d.r.g.support)
        ├── linkedkey            [@Prototype]     BindService --> OverrideService                 at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:16) *OVERRIDE
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:17) *OVERRIDE


    1 UNDECLARED bindings
    └── JitService                   (r.v.d.g.d.r.g.s.exts)


    BINDING CHAINS
    └── BindService  --[linked]-->  OverrideService
""" as String;
    }


    def "Check all render"() {

        expect:
        render(new GuiceConfig()) == """

    8 MODULES with 111 bindings
    │
    ├── CasesModule                  (r.v.d.g.d.r.g.support)
    │   ├── <typelistener>                        CustomTypeListener                              at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:19)
    │   ├── <provisionlistener>                   CustomProvisionListener                         at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:26)
    │   ├── <aop>                                 CustomAop                                       at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:33)
    │   ├── untargetted          [@Singleton]     AopedService                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:36) *AOP
    │   ├── linkedkey            [@Prototype]     BindService --> OverriddenService               at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.CasesModule.configure(CasesModule.java:37) *OVERRIDDEN
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
        │   └── instance             [@Singleton]     ExtensionsHolder                                at ru.vyarus.dropwizard.guice.module.installer.InstallerModule.configure(InstallerModule.java:30)
        │
        ├── Jersey2Module                (r.v.d.g.m.jersey)
        │   ├── providerinstance     [@Prototype]     InjectionManager                                at ru.vyarus.dropwizard.guice.module.jersey.Jersey2Module.configure(Jersey2Module.java:59)
        │   │
        │   ├── GuiceWebModule               (r.v.d.g.m.jersey)         *WEB
        │   │   │
        │   │   └── InternalServletModule        (c.g.inject.servlet)
        │   │       ├── <scope>              [@RequestScoped] -                                               at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:84)
        │   │       ├── <scope>              [@SessionScoped] -                                               at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:85)
        │   │       ├── linkedkey            [@Prototype]     ServletRequest --> HttpServletRequest           at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:86)
        │   │       ├── linkedkey            [@Prototype]     ServletResponse --> HttpServletResponse         at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:87)
        │   │       ├── untargetted          [@Singleton]     ManagedFilterPipeline                           at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:94)
        │   │       ├── untargetted          [@Singleton]     ManagedServletPipeline                          at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:95)
        │   │       ├── linkedkey            [@Singleton]     FilterPipeline --> ManagedFilterPipeline        at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:96)
        │   │       ├── providerkey          [@Prototype]     ServletContext                                  at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:98)
        │   │       ├── untargetted          [@Singleton]     BackwardsCompatibleServletContextProvider       at com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:99)
        │   │       ├── providerinstance     [@Singleton]     @ScopingOnly GuiceFilter                        at com.google.inject.servlet.InternalServletModule.provideScopingOnlyGuiceFilter(InternalServletModule.java:106)
        │   │       ├── providerinstance     [@RequestScoped] HttpServletRequest                              at com.google.inject.servlet.InternalServletModule.provideHttpServletRequest(InternalServletModule.java:112)
        │   │       ├── providerinstance     [@RequestScoped] HttpServletResponse                             at com.google.inject.servlet.InternalServletModule.provideHttpServletResponse(InternalServletModule.java:118)
        │   │       ├── providerinstance     [@Prototype]     HttpSession                                     at com.google.inject.servlet.InternalServletModule.provideHttpSession(InternalServletModule.java:123)
        │   │       └── providerinstance     [@RequestScoped] @RequestParameters Map<String, String[]>        at com.google.inject.servlet.InternalServletModule.provideRequestParameters(InternalServletModule.java:131)
        │   │
        │   └── GuiceBindingsModule          (r.v.d.g.m.jersey.hk2)
        │       ├── providerinstance     [@Prototype]     Application                                     at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@Prototype]     MultivaluedParameterExtractorProvider           at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@Prototype]     Providers                                       at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@RequestScoped] AsyncContext                                    at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@RequestScoped] ContainerRequest                                at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@RequestScoped] HttpHeaders                                     at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@RequestScoped] Request                                         at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@RequestScoped] ResourceInfo                                    at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       ├── providerinstance     [@RequestScoped] SecurityContext                                 at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │       └── providerinstance     [@RequestScoped] UriInfo                                         at ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent(JerseyBinding.java:190)
        │
        └── ConfigBindingModule          (r.v.d.g.m.yaml.bind)
            ├── instance             [@Singleton]     ConfigurationTree                               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.configure(ConfigBindingModule.java:45)
            ├── instance             [@Singleton]     Configuration                                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindRootTypes(ConfigBindingModule.java:63)
            ├── instance             [@Singleton]     @Config Configuration                           at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindRootTypes(ConfigBindingModule.java:65)
            ├── instance             [@Singleton]     @Config AdminFactory                            at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config GzipHandlerFactory                      at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config HealthCheckConfiguration                at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config LoggingFactory                          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config MetricsFactory                          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config RequestLogFactory<Object>               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config ServerFactory                           at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config ServerPushFilterFactory                 at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config TaskConfiguration                       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindUniqueSubConfigurations(ConfigBindingModule.java:78)
            ├── instance             [@Singleton]     @Config("admin") AdminFactory                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("admin.healthChecks") HealthCheckConfiguration   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("admin.healthChecks.maxThreads") Integer   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("admin.healthChecks.minThreads") Integer   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("admin.healthChecks.servletEnabled") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("admin.healthChecks.workQueueSize") Integer   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("admin.tasks") TaskConfiguration        at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("admin.tasks.printStackTraceOnError") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("health") Optional<HealthFactory>       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("logging") LoggingFactory               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("logging.appenders") List<AppenderFactory<ILoggingEvent>>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("logging.level") String                 at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("logging.loggers") Map<String, JsonNode>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("metrics") MetricsFactory               at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("metrics.frequency") Duration           at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("metrics.reportOnStop") Boolean         at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("metrics.reporters") List<ReporterFactory>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server") ServerFactory                 at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.adminConnectors") List<ConnectorFactory>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.adminContextPath") String       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.adminMaxThreads") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.adminMinThreads") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.allowedMethods") Set<String>    at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.applicationConnectors") List<ConnectorFactory>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.applicationContextPath") String   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.detailedJsonProcessingExceptionMapper") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.dumpAfterStart") Boolean        at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.dumpBeforeStop") Boolean        at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.enableThreadNameFilter") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip") GzipHandlerFactory       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip.bufferSize") DataSize      at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip.deflateCompressionLevel") Integer   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip.enabled") Boolean          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip.excludedUserAgentPatterns") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip.gzipCompatibleInflation") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip.minimumEntitySize") DataSize   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.gzip.syncFlush") Boolean        at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.idleThreadTimeout") Duration    at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.maxQueuedRequests") Integer     at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.maxThreads") Integer            at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.minThreads") Integer            at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.registerDefaultExceptionMappers") Boolean   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.requestLog") RequestLogFactory<Object>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.requestLog.appenders") List<AppenderFactory<IAccessEvent>>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.rootPath") Optional<String>     at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.serverPush") ServerPushFilterFactory   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.serverPush.associatePeriod") Duration   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.serverPush.enabled") Boolean    at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.serverPush.maxAssociations") Integer   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── instance             [@Singleton]     @Config("server.shutdownGracePeriod") Duration   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.gid") Integer                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.group") String                  at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.compressedMimeTypes") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.excludedMimeTypes") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.excludedPaths") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.includedMethods") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.gzip.includedPaths") Set<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.nofileHardLimit") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.nofileSoftLimit") Integer       at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.serverPush.refererHosts") List<String>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.serverPush.refererPorts") List<Integer>   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.startsAsRoot") Boolean          at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.uid") Integer                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            ├── providerinstance     [@Prototype]     @Config("server.umask") String                  at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)
            └── providerinstance     [@Prototype]     @Config("server.user") String                   at ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule.bindValuePaths(ConfigBindingModule.java:90)


    1 OVERRIDING MODULES with 2 bindings
    │
    └── OverrideModule               (r.v.d.g.d.r.g.support)
        ├── linkedkey            [@Prototype]     BindService --> OverrideService                 at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:16) *OVERRIDE
        └── instance             [@Singleton]     BindService2                                    at ru.vyarus.dropwizard.guice.debug.renderer.guice.support.OverrideModule.configure(OverrideModule.java:17) *OVERRIDE


    1 UNDECLARED bindings
    └── JitService                   (r.v.d.g.d.r.g.s.exts)


    BINDING CHAINS
    ├── BindService  --[linked]-->  OverrideService
    ├── FilterPipeline  --[linked]-->  ManagedFilterPipeline
    ├── ServletRequest  --[linked]-->  HttpServletRequest  --[provided]-->  @Provides com.google.inject.servlet.InternalServletModule.provideHttpServletRequest(InternalServletModule.java:112)
    └── ServletResponse  --[linked]-->  HttpServletResponse  --[provided]-->  @Provides com.google.inject.servlet.InternalServletModule.provideHttpServletResponse(InternalServletModule.java:118)
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
}

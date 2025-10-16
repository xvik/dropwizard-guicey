package ru.vyarus.dropwizard.guice.lifecycle

import com.google.inject.Binder
import com.google.inject.Module
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.ConfiguredBundle
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopConfig
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfig
import ru.vyarus.dropwizard.guice.debug.report.option.OptionsConfig
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueModule
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyFeatureInstaller
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.module.lifecycle.event.*
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.*
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.*
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.*
import ru.vyarus.dropwizard.guice.support.feature.DummyPlugin1
import ru.vyarus.dropwizard.guice.support.feature.DummyTask
import ru.vyarus.dropwizard.guice.support.util.BindModule
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 23.04.2018
 */
@TestDropwizardApp(value = App, hooks = XConf, useDefaultExtensions = false)
class EventsConsistencyTest extends AbstractTest {

    def "Check events consistency"() {

        expect: "all events called except shutdown"
        Listener.called.size() == GuiceyLifecycle.values().size() - 2

        and: "order correct"
        Listener.called == Arrays.asList(GuiceyLifecycle.values() - GuiceyLifecycle.ApplicationShutdown - GuiceyLifecycle.ApplicationStopped)
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new Listener(),
                            // to call all methods in adapter and make coverage happy
                            new GuiceyLifecycleAdapter())
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.support.feature")
                    .bundles(new GBundle(), new GBundle())
                    .dropwizardBundles(new DBundle(), new DBundle(), new DBundleDisabled())
                    .modules(new XMod(), new YMod(), new YMod(), new BindModule(DummyTask))
                    .disableBundles(LookupBundle)
                    .disableModules(XMod, InnerModule)
                    .disableInstallers(JerseyFeatureInstaller)
                    .disableExtensions(DummyPlugin1, HK2DebugFeature, BindEx)
                    .disableDropwizardBundles(DBundleDisabled)
                    .searchCommands()
                    .printLifecyclePhasesDetailed()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class DBundle implements ConfiguredBundle {
        @Override
        public boolean equals(final Object obj) {
            // only one debug module instance allowed
            return obj != null && getClass().equals(obj.getClass());
        }
    }

    static class DBundleDisabled implements ConfiguredBundle {}

    static class XMod implements Module {
        @Override
        void configure(Binder binder) {
        }
    }

    static class YMod extends UniqueModule {
        @Override
        protected void configure() {
            install(new InnerModule());
            bind(BindEx)
        }
    }

    static class InnerModule implements Module {
        @Override
        void configure(Binder binder) {
            // need any binding to detect module removal
            binder.bind(DummyTask)
        }
    }

    @Path("/")
    static class BindEx {}

    static class XConf implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {
            PropertyBundleLookup.enableBundles(LookupBundle.class)
        }
    }

    static class LookupBundle implements GuiceyBundle {
        @Override
        void initialize(GuiceyBootstrap bootstrap) {

        }
    }

    static class GBundle extends UniqueGuiceyBundle {}

    static class Listener extends GuiceyLifecycleAdapter {

        static List<GuiceyLifecycle> called = new ArrayList<>()

        @Override
        protected void configurationHooksProcessed(ConfigurationHooksProcessedEvent event) {
            baseChecks(event)
            assert event.hooks.size() == 2
            assert event.hooks[0].toString().contains(AbstractTest.simpleName)
            assert event.hooks[1] instanceof XConf
        }

        @Override
        protected void beforeInit(BeforeInitEvent event) {
            confChecks(event)
        }

        @Override
        protected void dropwizardBundlesInitialized(DropwizardBundlesInitializedEvent event) {
            confChecks(event)
            assert event.getBundles().size() == 1
            assert event.getBundles()[0].class == DBundle
            assert event.getDisabled().size() == 1
            assert event.getIgnored().size() == 1
        }

        @Override
        protected void lookupBundlesResolved(BundlesFromLookupResolvedEvent event) {
            confChecks(event)
            assert event.getBundles().size() == 1
            assert event.getBundles()[0].class == LookupBundle
        }

        @Override
        protected void bundlesResolved(BundlesResolvedEvent event) {
            confChecks(event)
            // dw and lookup bundles are disabled
            assert event.getBundles().size() == 4
            assert event.getDisabled().size() == 1
            assert event.getIgnored().size() == 1
        }

        @Override
        protected void bundlesInitialized(BundlesInitializedEvent event) {
            confChecks(event)
            assert event.getBundles().size() == 5
            assert event.getDisabled().size() == 1
            assert event.getIgnored().size() == 1
        }

        @Override
        protected void commandsResolved(CommandsResolvedEvent event) {
            confChecks(event)
            assert event.getBootstrap() != null
            assert event.getCommands().size() == 2
            assert event.getBootstrap().getCommands().containsAll(event.getCommands())
        }

        @Override
        protected void installersResolved(InstallersResolvedEvent event) {
            confChecks(event)
            assert event.installers.size() == 11
            assert event.disabled.size() == 1
        }

        @Override
        protected void classpathExtensionsResolved(ClasspathExtensionsResolvedEvent event) {
            confChecks(event)
            assert event.extensions.size() == 15
        }

        @Override
        protected void initialized(InitializedEvent event) {
            confChecks(event)
            assert event.getBootstrap() != null
        }

        @Override
        protected void beforeRun(BeforeRunEvent event) {
            runChecks(event)
        }

        @Override
        protected void bundlesStarted(BundlesStartedEvent event) {
            runChecks(event)
            assert event.getBundles().size() == 5
        }

        @Override
        protected void manualExtensionsValidated(ManualExtensionsValidatedEvent event) {
            confChecks(event)
            assert event.extensions.size() == 1
            assert event.validated.size() == 0
        }

        @Override
        protected void modulesAnalyzed(ModulesAnalyzedEvent event) {
            runChecks(event)
            assert event.getAnalyzedModules().size() == 4
            assert event.getExtensions().size() == 2
            assert event.getTransitiveModulesRemoved().size() == 1
            assert event.getBindingsRemoved().size() == 1
        }

        @Override
        protected void extensionsResolved(ExtensionsResolvedEvent event) {
            runChecks(event)
            assert event.extensions.size() == 14
            assert event.disabled.size() == 3
        }

        @Override
        protected void injectorCreation(InjectorCreationEvent event) {
            runChecks(event)
            assert event.modules.size() == 5
            assert event.overridingModules.isEmpty()
            assert event.disabled.size() == 2
            assert event.ignored.size() == 1
        }

        @Override
        protected void extensionsInstalledBy(ExtensionsInstalledByEvent event) {
            injectorChecks(event)
            assert event.getInstaller() != null
            assert !event.getInstalled().isEmpty()
        }

        @Override
        protected void extensionsInstalled(ExtensionsInstalledEvent event) {
            injectorChecks(event)
            assert !event.getExtensions().isEmpty()
        }

        @Override
        protected void applicationRun(ApplicationRunEvent event) {
            injectorChecks(event)
        }

        @Override
        protected void jerseyConfiguration(JerseyConfigurationEvent event) {
            jerseyCheck(event)
        }

        @Override
        protected void jerseyExtensionsInstalledBy(JerseyExtensionsInstalledByEvent event) {
            jerseyCheck(event)
            assert event.getInstaller() != null
            assert !event.getInstalled().isEmpty()
        }

        @Override
        protected void jerseyExtensionsInstalled(JerseyExtensionsInstalledEvent event) {
            jerseyCheck(event)
            assert !event.getExtensions().isEmpty()
        }

        @Override
        protected void applicationStarting(ApplicationStartingEvent event) {
            injectorChecks(event)
        }

        @Override
        protected void applicationStarted(ApplicationStartedEvent event) {
            jerseyCheck(event)
            assert event.jettyStarted
            assert event.jerseyStarted
            assert event.renderJerseyConfig(new JerseyConfig()) != null
        }

        @Override
        protected void applicationShutdown(ApplicationShutdownEvent event) {
            jerseyCheck(event)
            assert event.jettyStarted
            assert event.jerseyStarted
        }

        @Override
        protected void applicationStopped(ApplicationStoppedEvent event) {
            jerseyCheck(event)
        }

        private void baseChecks(GuiceyLifecycleEvent event) {
            assert event != null
            assert event.options != null
            assert event.sharedState != null
            if (!called.contains(event.getType())) {
                called.add(event.getType())
            }
        }

        private void confChecks(ConfigurationPhaseEvent event) {
            baseChecks(event)
            assert event.getBootstrap() != null
        }

        private void runChecks(RunPhaseEvent event) {
            confChecks(event)
            assert event.getEnvironment() != null
            assert event.getConfiguration() != null
            assert event.getConfigurationTree() != null
            assert event.renderConfigurationBindings(new BindingsConfig().showNullValues()) != null
        }

        private void injectorChecks(InjectorPhaseEvent event) {
            runChecks(event)
            assert event.injector != null
            assert event.configurationInfo != null
            assert event.reportRenderer != null
            assert event.reportRenderer.renderStats(false) != null
            assert event.reportRenderer.renderOptions(new OptionsConfig()) != null
            assert event.reportRenderer.renderConfigurationSummary(new DiagnosticConfig()) != null
            assert event.reportRenderer.renderConfigurationTree(new ContextTreeConfig()) != null
            assert event.reportRenderer.renderGuiceBindings(new GuiceConfig()) != null
            assert event.reportRenderer.renderGuiceAop(new GuiceAopConfig()) != null
            assert event.reportRenderer.renderWebMappings(new MappingsConfig()) != null
        }

        private void jerseyCheck(JerseyPhaseEvent event) {
            injectorChecks(event)
            assert event.getInjectionManager() != null
            assert event.jerseyStarted
        }
    }
}

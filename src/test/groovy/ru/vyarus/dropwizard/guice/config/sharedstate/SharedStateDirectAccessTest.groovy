package ru.vyarus.dropwizard.guice.config.sharedstate

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.DropwizardBundlesInitializedEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InitializedEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.BeforeRunEvent
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 15.10.2021
 */
@UseGuiceyApp(value = App, hooks = Hook)
class SharedStateDirectAccessTest extends Specification {

    def "Check shared state direct availability"() {

        expect:
        noState()
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().listen(new Listener()).build())
            assert SharedConfigurationState.getStartupInstance()
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            noState()
        }
    }

    static class Listener extends GuiceyLifecycleAdapter {

        @Override
        protected void configurationHooksProcessed(ConfigurationHooksProcessedEvent event) {
            def state = SharedConfigurationState.getStartupInstance()
            assert state
            noBootstrap(state)
        }

        @Override
        protected void dropwizardBundlesInitialized(DropwizardBundlesInitializedEvent event) {
            def state = SharedConfigurationState.getStartupInstance()
            assert state
            assert state.bootstrap.get()
            noEnvironment(state)
        }

        @Override
        protected void initialized(InitializedEvent event) {
            def state = SharedConfigurationState.getStartupInstance()
            assert state
            assert state.bootstrap.get()
            noEnvironment(state)
        }

        @Override
        protected void beforeRun(BeforeRunEvent event) {
            def state = SharedConfigurationState.getStartupInstance()
            assert state
            assert state.bootstrap.get()
            assert state.environment.get()
            assert state.configuration.get()
            assert state.configurationTree.get()
            noInjector(state)
        }

        @Override
        protected void applicationRun(ApplicationRunEvent event) {
            def state = SharedConfigurationState.getStartupInstance()
            assert state
            assert state.bootstrap.get()
            assert state.environment.get()
            noInjector(state)
        }

        @Override
        protected void applicationStarted(ApplicationStartedEvent event) {
            noState()
        }
    }

    static class Hook implements GuiceyConfigurationHook {

        @Override
        void configure(GuiceBundle.Builder builder) {
            assert SharedConfigurationState.getStartupInstance() != null

             builder.withSharedState({
                 noBootstrap(it)
             })
        }
    }

    private static void noBootstrap(SharedConfigurationState state) {
        try {
            state.bootstrap.get()
            assert false
        } catch (Exception ex) {
        }
    }

    private static void noEnvironment(SharedConfigurationState state) {
        try {
            state.environment.get()
            assert false
        } catch (Exception ex) {
        }
    }

    private static void noInjector(SharedConfigurationState state) {
        try {
            state.injector.get()
            assert false
        } catch (Exception ex) {
        }
    }

    private static boolean noState() {
        try {
            SharedConfigurationState.getStartupInstance()
            assert false
        } catch (Exception ex) {
        }
        return true
    }
}

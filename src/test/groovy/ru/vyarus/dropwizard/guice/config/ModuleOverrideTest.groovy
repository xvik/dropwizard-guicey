package ru.vyarus.dropwizard.guice.config

import com.google.inject.AbstractModule
import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 10.04.2018
 */
@UseGuiceyApp(App)
class ModuleOverrideTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check bindings override"() {

        expect: "correct registration"
        info.getModules().containsAll(NormalModule, OverridingModule)

        info.getNormalModules().contains(NormalModule)
        !info.getNormalModules().contains(OverridingModule)

        info.getOverridingModules().contains(OverridingModule)
        !info.getOverridingModules().contains(NormalModule)

        and: "service binding overridden"
        injector.getInstance(SomeService) instanceof OverridingService
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new NormalModule())
                    .modulesOverride(new OverridingModule())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class NormalModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(SomeService).to(NormalService)
        }
    }

    static interface SomeService {}

    static class NormalService implements SomeService {}

    static class OverridingModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(SomeService).to(OverridingService)
        }
    }

    static class OverridingService implements SomeService {}
}

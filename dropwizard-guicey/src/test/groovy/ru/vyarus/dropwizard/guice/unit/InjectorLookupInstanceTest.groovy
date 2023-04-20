package ru.vyarus.dropwizard.guice.unit

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.ConfiguredBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 22.10.2019
 */
@TestGuiceyApp(App)
class InjectorLookupInstanceTest extends AbstractTest {

    @Inject
    Environment environment
    @Inject
    Bootstrap bootstrap

    def "Check injection access"() {

        expect: "injector lookup works"
        InjectorLookup.getInjector(bootstrap.getApplication()).get() != null
        InjectorLookup.getInjector(environment).get() != null
        InjectorLookup.getInstance(bootstrap.getApplication(), Configuration.class).get() != null
        InjectorLookup.getInstance(environment, Configuration.class).get() != null
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(new ConfiguredBundle<Configuration>() {
                @Override
                void run(Configuration configuration, Environment environment) throws Exception {
                    // too early!
                    assert !InjectorLookup.getInjector(environment).isPresent()
                }
            })
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}

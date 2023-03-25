package ru.vyarus.dropwizard.guice.guiceconfig

import com.google.inject.*
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 03.09.2019
 */
@TestGuiceyApp(App)
class BindingDisableTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check bindings exclusion"() {
        when: "application started"
        then: "extensions found and disabled"
        info.getExtensionsDisabled() as Set == [Res1, Res2, Res3, Res4, Res5] as Set
        info.getInfo(Res1).registered
        info.getInfo(Res2).registered
        info.getInfo(Res3).registered
        info.getInfo(Res4).registered
        info.getInfo(Res5).registered

        then: "bindings removed"
        injector.getExistingBinding(Key.get(Res1)) == null
        injector.getExistingBinding(Key.get(Res2)) == null
        injector.getExistingBinding(Key.get(Res3)) == null
        injector.getExistingBinding(Key.get(Res4)) == null
        injector.getExistingBinding(Key.get(Res5)) == null
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new BndModule())
                    .disableExtensions(Res1, Res2, Res3, Res4, Res5)
                    .printDiagnosticInfo()
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class BndModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Res1)
            bind(Res2).to(Res2Impl)
            bind(Res3).toInstance(new Res3Impl())
            bind(Res4).toProvider(Res4Provider)
            bind(Res5).toProvider(new Res5Provider())
        }
    }

    @Path("/1")
    static class Res1 {}

    @Path("/2")
    static class Res2 {}

    static class Res2Impl extends Res2 {}

    @Path("/3")
    static class Res3 {}

    static class Res3Impl extends Res3 {}

    @Path("/4")
    static class Res4 {}

    static class Res4Provider implements Provider<Res4> {
        @Override
        Res4 get() {
            return new Res4() {};
        }
    }

    @Path("/4")
    static class Res5 {}

    static class Res5Provider implements Provider<Res5> {
        @Override
        Res5 get() {
            return new Res5() {};
        }
    }
}

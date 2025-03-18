package ru.vyarus.dropwizard.guice.config.sharedstate

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.12.2019
 */
@TestGuiceyApp(App)
class SharedStateAccessInModuleTest extends AbstractTest {

    @Inject
    Environment environment

    def "Check shared state access within module"() {

        expect: "module modified state"
        SharedConfigurationState.lookup(environment, ModState).get()["module"] == "i was here"
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
            // init sample value in shared state
                    .withSharedState({ it.put(ModState, new ModState()) })
                    .modules(new Mod())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Mod extends DropwizardAwareModule {

        @Override
        protected void configure() {
            assert !sharedState(List).isPresent()
            assert sharedState(ModState).isPresent()
            def val = sharedStateOrFail(ModState, "No mod initialized")
            assert val instanceof Map

            val.put("module", "i was here")


            shareState(List, Collections.singletonList("fafa"))
            assert sharedState(List).get()[0] == "fafa"


            sharedState(List, { ["ff"] }).iterator().next() == "ff"
            sharedState(Queue, { ["tt"] }).iterator().next() == "tt"
        }
    }

    static class ModState extends HashMap<String, String> {
    }
}

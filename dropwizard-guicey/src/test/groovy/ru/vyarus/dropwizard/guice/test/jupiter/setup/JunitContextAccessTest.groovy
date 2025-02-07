package ru.vyarus.dropwizard.guice.test.jupiter.setup

import com.google.inject.AbstractModule
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import jakarta.inject.Inject
import org.junit.jupiter.api.extension.ExtensionContext
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2025
 */
@TestGuiceyApp(App)
class JunitContextAccessTest extends AbstractTest {

    @EnableSetup
    static TestEnvironmentSetup setup = new TestEnvironmentSetup() {
        @Override
        Object setup(TestExtension extension) {
            extension.hooks(new GuiceyConfigurationHook() {
                @Override
                void configure(GuiceBundle.Builder builder) {
                    builder.modules(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(ExtensionContext).toInstance(extension.getJunitContext())
                        }
                    })
                }
            })
            return null
        }
    }

    @Inject
    ExtensionContext context

    def "Check junit context accessible"() {

        expect: "context provided"
        context != null
        context.getRequiredTestClass() == JunitContextAccessTest
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}

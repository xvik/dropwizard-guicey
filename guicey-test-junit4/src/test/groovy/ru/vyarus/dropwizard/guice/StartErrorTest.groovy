package ru.vyarus.dropwizard.guice

import com.google.inject.AbstractModule
import com.google.inject.name.Named
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.junit.Rule
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyCommand
import ru.vyarus.dropwizard.guice.test.StartupErrorRule

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov 
 * @since 24.11.2015
 */
class StartErrorTest extends AbstractTest {

    @Rule
    StartupErrorRule rule = StartupErrorRule.create()

    def "Check application exit on injector error"() {

        when:
        new ErrorApplication().main(['server', 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml'] as String[])

        then: 'guice exception thrown'
        thrown(rule.indicatorExceptionType)
        // java 9 and above use quotes in annotations (@com.google.inject.name.Named(value="unknown")) while previous versions did not
        rule.error.replace('"', '').contains(
                "[Guice/JitDisabled]: Explicit bindings are required and String annotated with @Named")
    }

    static class ErrorApplication extends Application<TestConfiguration> {
        static void main(String[] args) {
            new ErrorApplication().run(args)
        }

        @Override
        void initialize(Bootstrap<TestConfiguration> bootstrap) {
            TEST_HOOK.register()
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new ErrorModule())
                    .build()
            );
            bootstrap.addCommand(new DummyCommand(bootstrap.getApplication()))
        }

        @Override
        void run(TestConfiguration configuration, Environment environment) throws Exception {
        }
    }

    static class ErrorModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ErrorService).asEagerSingleton()
        }
    }

    static class ErrorService {

        @Inject
        ErrorService(@Named('unknown') String unknown) {
        }
    }
}
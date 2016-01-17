package ru.vyarus.dropwizard.guice

import com.google.inject.AbstractModule
import com.google.inject.CreationException
import com.google.inject.name.Named
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.DummyCommand

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov 
 * @since 24.11.2015
 */
class StartErrorTest extends AbstractTest {

    def "Check application exit on injector error"() {

        when:
        new ErrorApplication().main(['server', 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml'] as String[])

        then: 'guice exception thrown'
        thrown(CreationException)
    }

    static class ErrorApplication extends Application<TestConfiguration> {
        public static void main(String[] args) {
            new ErrorApplication().run(args)
        }

        @Override
        void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.<TestConfiguration> builder()
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
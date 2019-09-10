package ru.vyarus.dropwizard.guice.lifecycle

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 10.09.2019
 */
@UseGuiceyApp(App)
class DuplicateListenersIgnoredTest extends AbstractTest {

    def "Check listeneres deduplicates"() {

        expect:
        SimpleListener.calls == 2
        CustomListener.calls == 1
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            SimpleListener listener = new SimpleListener()
            CustomListener listener2 = new CustomListener()
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(listener, listener, new SimpleListener())
                    .listen(listener2, listener2, new CustomListener())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static class SimpleListener extends GuiceyLifecycleAdapter {
        static int calls

        @Override
        protected void applicationRun(ApplicationRunEvent event) {
            calls++
        }
    }

    static class CustomListener extends GuiceyLifecycleAdapter {
        static int calls

        @Override
        protected void applicationRun(ApplicationRunEvent event) {
            calls++
        }

        @Override
        boolean equals(Object obj) {
            return obj instanceof CustomListener
        }

        @Override
        int hashCode() {
            return CustomListener.hashCode()
        }
    }
}

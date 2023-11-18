package ru.vyarus.dropwizard.guice.test.reuse

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.DropwizardTestSupport
import org.junit.platform.engine.TestExecutionResult
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent
import ru.vyarus.dropwizard.guice.test.TestSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Requires
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 26.12.2022
 */
class ReusableAppSpockTest extends Specification {
    static Boolean ACTIVE = false

    public static List<String> actions = new ArrayList<>();

    def "Check reusable app support"() {

        when:
        ACTIVE = true
        def runner = new EmbeddedSpecRunner()
        // do not rethrow exception - all errors will remain in holder
        runner.throwFailure = false

        def res = runner.runClasses(Arrays.asList(Test1, Test2))
        res.allEvents().failed().stream()
        // exceptions appended to events log
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get()
                    err.printStackTrace()
                    actions.add("Error: (" + err.getClass().getSimpleName() + ") " + err.getMessage())
                })
//                    .containerEvents()
//                    .assertStatistics(stats -> stats.failed(0).aborted(0));

        then:
        actions == ["started", "stopped"]
        App.cnt == 1

        cleanup:
        ACTIVE = false
    }

    @TestGuiceyApp(value = App, reuseApplication = true)
    abstract static class Base extends Specification {
    }

    @Requires({ ACTIVE })
    static class Test1 extends Base {

        @Inject Injector injector

        def "Test"(DropwizardTestSupport support) {

            expect:
            injector != null
            support == TestSupport.getContext()
        }
    }

    @Requires({ ACTIVE })
    static class Test2 extends Base {

        @Inject Injector injector

        def "Test"(DropwizardTestSupport support) {

            expect:
            injector != null
            // checking that shared support object is THE SAME
            support == TestSupport.getContext()
        }
    }

    static class App extends Application<Configuration> {

        public static int cnt;

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            cnt++
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new GuiceyLifecycleAdapter() {
                        @Override
                        protected void applicationStarted(ApplicationStartedEvent event) {
                            actions.add("started")
                        }

                        @Override
                        protected void applicationStopped(ApplicationStoppedEvent event) {
                            actions.add("stopped")
                        }
                    })
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}

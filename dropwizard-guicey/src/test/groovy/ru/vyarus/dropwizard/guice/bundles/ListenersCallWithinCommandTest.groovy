package ru.vyarus.dropwizard.guice.bundles

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.cli.EnvironmentCommand
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.lifecycle.Managed
import net.sourceforge.argparse4j.inf.Namespace
import org.eclipse.jetty.util.component.LifeCycle
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 18.09.2019
 */
class ListenersCallWithinCommandTest extends Specification {

    def "Check lifecycle under command"() {

        when: "run command"
        new App().run(['test'] as String[])
        then: "listener not called"
        Listener.called.isEmpty()
        and: "managed not called"
        !Mng.started
        !Mng.stopped

        when: "run application normally"
        TestSupport.runWebApp(App, null)
        then: "listener called"
        Listener.called == ['starting', 'started', 'stopping', 'stopped']
        and: "managed called"
        Mng.started
        Mng.stopped
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addCommand(new Command(this))
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.lifecycle().manage(new Mng())
            environment.lifecycle().addEventListener(new Listener())
        }
    }

    static class Command extends EnvironmentCommand<Configuration> {

        Command(Application<Configuration> application) {
            super(application, 'test', 'fdfd')
        }

        @Override
        protected void run(Environment environment, Namespace namespace, Configuration configuration) throws Exception {

        }
    }

    static class Mng implements Managed {
        static boolean started
        static boolean stopped

        @Override
        void start() throws Exception {
            started = true
        }

        @Override
        void stop() throws Exception {
            stopped = true
        }
    }

    static class Listener implements LifeCycle.Listener {
        static List<String> called = []

        @Override
        void lifeCycleStarted(LifeCycle event) {
            called << 'started'
        }

        @Override
        void lifeCycleStarting(LifeCycle event) {
            called << 'starting'
        }

        @Override
        void lifeCycleStopped(LifeCycle event) {
            called << 'stopped'
        }

        @Override
        void lifeCycleStopping(LifeCycle event) {
            called << 'stopping'
        }
    }
}

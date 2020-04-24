package ru.vyarus.dropwizard.guice.bundles

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.cli.EnvironmentCommand
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import net.sourceforge.argparse4j.inf.Namespace
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
class EnvironmentListenersWuthCommandTest extends Specification {

    def "Check lifecycle under command"() {

        when: "run command"
        new App().run(['test'] as String[])
        then: "listener not called"
        !App.lifecycleStarted
        App.guiceyStarted
        !App.serverStarted


        when: "run application normally"
        def rule = new DropwizardAppRule<>(App)
        rule.before()
        rule.after()
        then: "listener called"
        App.lifecycleStarted
        App.guiceyStarted
        App.serverStarted
    }

    static class App extends Application<Configuration> {
        static boolean lifecycleStarted
        static boolean guiceyStarted
        static boolean serverStarted

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addCommand(new Command(this))
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new GuiceyBundle() {
                        @Override
                        void run(GuiceyEnvironment environment) throws Exception {
                            environment
                                    .listenJetty(new AbstractLifeCycle.AbstractLifeCycleListener() {
                                        @Override
                                        void lifeCycleStarted(LifeCycle event) {
                                            lifecycleStarted = true
                                        }
                                    })
                                    .onGuiceyStartup({ a, b, c -> guiceyStarted = true })
                                    .onApplicationStartup({ serverStarted = true })
                        }
                    })
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
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
}

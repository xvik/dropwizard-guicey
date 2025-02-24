package ru.vyarus.dropwizard.guice.bundles

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.cli.EnvironmentCommand
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import net.sourceforge.argparse4j.inf.Namespace
import org.eclipse.jetty.util.component.LifeCycle
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment
import ru.vyarus.dropwizard.guice.test.TestSupport
import ru.vyarus.dropwizard.guice.test.util.RunResult
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 28.09.2019
 */
class EnvironmentListenersWuthCommandTest extends Specification {

    def "Check lifecycle under command"() {

        when: "run command"
        App app = new App()
                app.run(['test'] as String[])
        then: "listener not called"
        !app.lifecycleStarted
        app.guiceyStarted
        !app.serverStarted
        !app.appShutdown


        when: "run application normally"
        RunResult res = TestSupport.runWebApp(App)
        then: "listener called"
        App app1 = res.application
        app1.lifecycleStarted
        app1.guiceyStarted
        app1.serverStarted
        app1.appShutdown
    }

    static class App extends Application<Configuration> {
        boolean lifecycleStarted
        boolean guiceyStarted
        boolean serverStarted
        boolean appShutdown

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addCommand(new Command(this))
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new GuiceyBundle() {
                        @Override
                        void run(GuiceyEnvironment environment) throws Exception {
                            environment
                                    .listenJetty(new LifeCycle.Listener() {
                                        @Override
                                        void lifeCycleStarted(LifeCycle event) {
                                            lifecycleStarted = true
                                        }
                                    })
                                    .onGuiceyStartup({ a, b, c -> guiceyStarted = true })
                                    .onApplicationStartup({ serverStarted = true })
                                    .onApplicationShutdown {appShutdown = true}
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

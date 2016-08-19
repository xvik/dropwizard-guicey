package ru.vyarus.dropwizard.guice.web

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.web.session.SessionListener

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.DenySessionListenersWithoutSession

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
class SessionListenerTest extends AbstractTest {

    def "Check application startup without sessions"() {

        when: "starting app without session configured"
        def rule = new DropwizardAppRule(NSApp)
        rule.before()
        then: "listeners were not installed - warning printed"
        true

        cleanup:
        rule.after()

    }

    def "Check application startup fail without sessions"() {

        when: "starting app without session configured"
        def rule = new DropwizardAppRule(NSFailApp, 'src/test/resources/ru/vyarus/dropwizard/guice/simple-server.yml')
        rule.before()
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message == 'Can\'t register session listeners for application context because sessions support is not enabled: SessionListener'
    }

    def "Check session listener installation"() {

        when: "starting app with session configured"
        def rule = new DropwizardAppRule(SApp)
        rule.before()
        then: "listener installation ok"
        true

        cleanup:
        rule.after()

    }

    static class NSApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useWebInstallers()
                    .extensions(SessionListener)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }

    }

    static class NSFailApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useWebInstallers()
                    .extensions(SessionListener)
                    .option(DenySessionListenersWithoutSession, true)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.lifecycle().addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
                @Override
                void lifeCycleStopping(LifeCycle event) {
                    (event as AbstractLifeCycle).stopTimeout = 0
                }
            })
        }

    }

    static class SApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useWebInstallers()
                    .extensions(SessionListener)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.servlets().setSessionHandler(new SessionHandler())
        }
    }
}

package ru.vyarus.dropwizard.guice.web

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.eclipse.jetty.server.session.SessionHandler
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.support.web.session.SessionListener
import ru.vyarus.dropwizard.guice.test.TestSupport

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.DenySessionListenersWithoutSession

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
class SessionListenerTest extends AbstractTest {

    def "Check application startup without sessions"() {

        when: "starting app without session configured"
        TestSupport.runWebApp(NSApp)
        then: "listeners were not installed - warning printed"
        true
    }

    def "Check application startup fail without sessions"() {

        when: "starting app without session configured"
        TestSupport.runWebApp(NSFailApp, 'src/test/resources/ru/vyarus/dropwizard/guice/simple-server.yml')
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message == 'Can\'t register session listeners for application context because sessions support is not enabled: SessionListener'
    }

    def "Check session listener installation"() {

        when: "starting app with session configured"
        TestSupport.runWebApp(SApp)
        then: "listener installation ok"
        true
    }

    static class NSApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
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
                    .extensions(SessionListener)
                    .option(DenySessionListenersWithoutSession, true)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }

    }

    static class SApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SessionListener)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.servlets().setSessionHandler(new SessionHandler())
        }
    }
}

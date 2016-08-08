package ru.vyarus.dropwizard.guice.web

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.util.MultiException
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.WebInstallersBundle
import ru.vyarus.dropwizard.guice.support.web.session.SessionListener

/**
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
class SessionListenerTest extends AbstractTest {

    def "Check application startup without sessions"() {

        when: "starting app without session configured"
        def rule = new DropwizardAppRule(FSApp)
        rule.before()
        then: "listeners were not installed - warning printed"
        true

        cleanup:
        rule.after()

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

    static class FSApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new WebInstallersBundle())
                    .extensions(SessionListener)
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
                    .bundles(new WebInstallersBundle())
                    .extensions(SessionListener)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.servlets().setSessionHandler(new SessionHandler())
        }
    }
}

package ru.vyarus.dropwizard.guice.config

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.context.bootstrap.BootstrapProxyFactory
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 28.07.2019
 */
@TestGuiceyApp(App)
class BootstrapProxyTest extends AbstractTest {

    @Inject
    Bootstrap bootstrap

    def "Check bootstrap proxy correctness"() {

        setup: "prepare proxy"
        def proxy = BootstrapProxyFactory.create(bootstrap, null)

        expect: "all methods except addBundle are bypassed"
        proxy.getApplication() == bootstrap.getApplication()
        proxy.getCommands() == bootstrap.getCommands()
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addCommand(new Command())
            bootstrap.addBundle(GuiceBundle.builder()
                    .build()
            );
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    static class Command extends io.dropwizard.core.cli.Command {

        Command() {
            super("sample", "")
        }

        @Override
        void configure(Subparser subparser) {

        }

        @Override
        void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {

        }
    }

}

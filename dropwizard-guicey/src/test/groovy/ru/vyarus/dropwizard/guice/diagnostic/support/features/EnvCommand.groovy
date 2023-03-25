package ru.vyarus.dropwizard.guice.diagnostic.support.features

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.cli.EnvironmentCommand
import io.dropwizard.core.setup.Environment
import net.sourceforge.argparse4j.inf.Namespace

/**
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
class EnvCommand extends EnvironmentCommand<Configuration> {

    EnvCommand(Application<Configuration> application) {
        super(application, "Sample", "----")
    }

    @Override
    protected void run(Environment environment, Namespace namespace, Configuration configuration) throws Exception {

    }
}

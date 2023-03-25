package ru.vyarus.dropwizard.guice.support.feature

import com.google.inject.Inject
import io.dropwizard.core.Application
import io.dropwizard.core.cli.EnvironmentCommand
import io.dropwizard.core.setup.Environment
import net.sourceforge.argparse4j.inf.Namespace
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
class DummyCommand extends EnvironmentCommand<TestConfiguration> {

    @Inject
    DummyService service

    DummyCommand(Application app) {
        super(app, "sample", "sample command")
    }

    @Override
    protected void run(Environment environment, Namespace namespace, TestConfiguration configuration) throws Exception {
        println "I'm alive! ${service.hey()}"
    }
}

package ru.vyarus.dropwizard.guice.support.feature

import com.google.inject.Inject
import io.dropwizard.cli.ConfiguredCommand
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
class DummyCommand extends ConfiguredCommand<TestConfiguration> {

    @Inject
    DummyService service

    DummyCommand() {
        super("sample", "sample command")
    }

    @Override
    protected void run(Bootstrap<TestConfiguration> bootstrap, Namespace namespace, TestConfiguration configuration) throws Exception {
    }
}

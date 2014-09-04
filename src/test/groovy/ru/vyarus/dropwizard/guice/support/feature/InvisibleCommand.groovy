package ru.vyarus.dropwizard.guice.support.feature

import com.google.inject.Inject
import io.dropwizard.cli.ConfiguredCommand
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * Checks that invisible annotation works for commands.
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@InvisibleForScanner
class InvisibleCommand extends ConfiguredCommand<TestConfiguration> {

    @Inject
    DummyService service

    InvisibleCommand() {
        super("sample", "sample command")
    }

    @Override
    protected void run(Bootstrap<TestConfiguration> bootstrap, Namespace namespace, TestConfiguration configuration) throws Exception {
    }
}

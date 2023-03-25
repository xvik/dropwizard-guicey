package ru.vyarus.dropwizard.guice.support.feature

import com.google.inject.Inject
import io.dropwizard.core.cli.ConfiguredCommand
import io.dropwizard.core.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import ru.vyarus.dropwizard.guice.support.TestConfiguration

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class NonInjactableCommand extends ConfiguredCommand<TestConfiguration> {

    static NonInjactableCommand instance

    // it's here just to show that injection on simple commands not performed (only environment commands)
    @Inject
    DummyService service

    NonInjactableCommand() {
        super("nonguice", "Command without guice injection")
        instance = this
    }

    @Override
    protected void run(Bootstrap<TestConfiguration> bootstrap, Namespace namespace, TestConfiguration configuration) throws Exception {

    }
}

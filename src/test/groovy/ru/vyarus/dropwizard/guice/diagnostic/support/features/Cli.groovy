package ru.vyarus.dropwizard.guice.diagnostic.support.features

import io.dropwizard.core.cli.Command
import io.dropwizard.core.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser

/**
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
class Cli extends Command {

    Cli() {
        super("Cli", "------")
    }

    @Override
    void configure(Subparser subparser) {
    }

    @Override
    void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    }
}

package ru.vyarus.dropwizard.guice.support.badcmd

import io.dropwizard.cli.Command
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class BadCommand extends Command{

    BadCommand(String name, String description) {
        super(name, description)
    }

    @Override
    void configure(Subparser subparser) {

    }

    @Override
    void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {

    }
}

package ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.InitPhaseEvent;

import java.util.List;

/**
 * Called after guice bundle initialization ({@link ru.vyarus.dropwizard.guice.GuiceBundle#initialize(Bootstrap)}.
 * <p>
 * If commands scan is enabled, then provides all found and registered commands.
 * <p>
 * Dropwizard {@link Bootstrap} object could be configured at that point (dropwizard initialization phase is ongoing).
 * <p>
 * Note: dropwizard bundles, registered after {@link ru.vyarus.dropwizard.guice.GuiceBundle} will be initialized
 * after this point.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public class InitializationEvent extends InitPhaseEvent {

    private final List<Command> commands;

    public InitializationEvent(final Options options, final Bootstrap bootstrap, final List<Command> installed) {
        super(GuiceyLifecycle.Initialization, options, bootstrap);
        commands = installed;
    }

    /**
     * @return list of registere commands or empty list if nothing found or commands scan wis not enabled
     */
    public List<Command> getCommands() {
        return commands;
    }
}

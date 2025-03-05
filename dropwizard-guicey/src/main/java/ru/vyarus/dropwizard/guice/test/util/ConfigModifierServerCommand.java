package ru.vyarus.dropwizard.guice.test.util;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.ServerCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;

/**
 * Dropwizard {@link io.dropwizard.core.cli.ServerCommand} with configuration modifiers support.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 04.03.2025
 */
public class ConfigModifierServerCommand<C extends Configuration> extends ServerCommand<C> {

    private final List<ConfigModifier<C>> modifiers;

    public ConfigModifierServerCommand(final Application<C> application, final List<ConfigModifier<C>> modifiers) {
        super(application);
        this.modifiers = modifiers;
    }

    @Override
    protected void run(final Bootstrap<C> bootstrap,
                       final Namespace namespace,
                       final C configuration) throws Exception {
        // at this point only logging configuration performed
        ConfigOverrideUtils.runModifiers(configuration, modifiers);
        super.run(bootstrap, namespace, configuration);
    }
}

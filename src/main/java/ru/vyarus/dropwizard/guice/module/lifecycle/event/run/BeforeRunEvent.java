package ru.vyarus.dropwizard.guice.module.lifecycle.event.run;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.RunPhaseEvent;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

/**
 * Meta event. Called just before guice bundle processin gin run phase. Convenient point before main guicey logic.
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2018
 */
public class BeforeRunEvent extends RunPhaseEvent {

    public BeforeRunEvent(final GuiceyLifecycle type,
                          final Options options,
                          final Bootstrap bootstrap,
                          final Configuration configuration,
                          final ConfigurationTree configurationTree,
                          final Environment environment) {
        super(type, options, bootstrap, configuration, configurationTree, environment);
    }
}

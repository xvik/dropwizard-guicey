package ru.vyarus.dropwizard.guice.injector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ru.vyarus.dropwizard.guice.injector.InjectorFactory} that simply delegates
 * to {@link com.google.inject.Guice#createInjector(com.google.inject.Stage, com.google.inject.Module...)}.
 *
 * @author Nicholas Pace
 * @since Dec 26, 2014
 */
@SuppressFBWarnings("DM_EXIT")
public class DefaultInjectorFactory implements InjectorFactory {

    private final Logger logger = LoggerFactory.getLogger(DefaultInjectorFactory.class);

    @Override
    public Injector createInjector(final Stage stage, final Iterable<? extends Module> modules) {
        Injector injector = null;

        try {
            injector = Guice.createInjector(stage, modules);
        } catch (Exception e) {
            logger.error("Failed to create guice injector", e);
            System.exit(1);
        }

        return injector;
    }
}

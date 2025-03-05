package ru.vyarus.dropwizard.guice.test;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;

import java.util.List;

/**
 * Lightweight variation of server command for testing purposes.
 * Handles managed objects lifecycle.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 23.10.2014
 */
public class TestCommand<C extends Configuration> extends EnvironmentCommand<C> {

    private final Logger logger = LoggerFactory.getLogger(TestCommand.class);
    private final Class<C> configurationClass;
    private final boolean simulateManaged;
    private final List<ConfigModifier<C>> modifiers;
    private ContainerLifeCycle container;

    public TestCommand(final Application<C> application) {
        this(application, true);
    }

    public TestCommand(final Application<C> application, final boolean simulateManaged) {
        this(application, simulateManaged, null);
    }

    public TestCommand(final Application<C> application, final boolean simulateManaged,
                       final List<ConfigModifier<C>> modifiers) {
        super(application, "guicey-test", "Specific command to run guice context without jetty server");
        cleanupAsynchronously();
        configurationClass = application.getConfigurationClass();
        this.simulateManaged = simulateManaged;
        this.modifiers = modifiers;
    }

    @Override
    protected void run(final Bootstrap<C> bootstrap,
                       final Namespace namespace,
                       final C configuration) throws Exception {
        // at this point only logging configuration performed
        if (modifiers != null) {
            ConfigOverrideUtils.runModifiers(configuration, modifiers);
        }
        super.run(bootstrap, namespace, configuration);
    }

    @Override
    protected void run(final Environment environment, final Namespace namespace,
                       final C configuration) throws Exception {
        // simulating managed objects lifecycle support
        // if managed lifecycle is not required, just prevent such objects registration, but
        // preserve simulation itself as guicey application events rely on it
        container = simulateManaged ? new ContainerLifeCycle() : new NoManagedContainerLifeCycle();
        environment.lifecycle().attach(container);
        container.start();
        if (!simulateManaged) {
            logger.info("NOTE: Managed lifecycle support disabled!");
        }
    }

    public void stop() {
        if (container != null) {
            try {
                container.stop();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to stop managed objects container", e);
            }
            container.destroy();
        }
        cleanup();
    }

    @Override
    protected Class<C> getConfigurationClass() {
        return configurationClass;
    }

    /**
     * Custom container lifecycle with additional objects ignorance. It is important to presence lifecycle
     * itsef due to {@link #addEventListener(java.util.EventListener)}, used for application start/stop detection
     * (and some reports).
     */
    public static class NoManagedContainerLifeCycle extends ContainerLifeCycle {

        @Override
        public boolean addBean(final Object o) {
            // ignore registrations (for Managed and LifeCycle objects)
            return false;
        }
    }
}

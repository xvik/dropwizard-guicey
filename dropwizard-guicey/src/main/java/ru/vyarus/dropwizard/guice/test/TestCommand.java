package ru.vyarus.dropwizard.guice.test;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private ContainerLifeCycle container;

    public TestCommand(final Application<C> application) {
        this(application, true);
    }

    public TestCommand(final Application<C> application, final boolean simulateManaged) {
        super(application, "guicey-test", "Specific command to run guice context without jetty server");
        cleanupAsynchronously();
        configurationClass = application.getConfigurationClass();
        this.simulateManaged = simulateManaged;
    }

    @Override
    protected void run(final Environment environment, final Namespace namespace,
                       final C configuration) throws Exception {
        // simulating managed objects lifecycle support
        if (simulateManaged) {
            container = new ContainerLifeCycle();
            environment.lifecycle().attach(container);
            container.start();
        } else {
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
}

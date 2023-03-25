package ru.vyarus.dropwizard.guice.test;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

/**
 * Lightweight variation of server command for testing purposes.
 * Handles managed objects lifecycle.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 23.10.2014
 */
public class TestCommand<C extends Configuration> extends EnvironmentCommand<C> {

    private final Class<C> configurationClass;
    private ContainerLifeCycle container;

    public TestCommand(final Application<C> application) {
        super(application, "guicey-test", "Specific command to run guice context without jetty server");
        cleanupAsynchronously();
        configurationClass = application.getConfigurationClass();
    }

    @Override
    protected void run(final Environment environment, final Namespace namespace,
                       final C configuration) throws Exception {
        // simulating managed objects lifecycle support
        container = new ContainerLifeCycle();
        environment.lifecycle().attach(container);
        container.start();
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

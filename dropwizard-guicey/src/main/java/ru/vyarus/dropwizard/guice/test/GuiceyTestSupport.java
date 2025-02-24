package ru.vyarus.dropwizard.guice.test;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.Command;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;

import jakarta.annotation.Nullable;
import java.util.function.Function;


/**
 * An alternative to {@link DropwizardTestSupport} which does not run jetty (web part) allowing to test only guice
 * context. Internally, {@link TestCommand} used instead of {@link io.dropwizard.core.cli.ServerCommand}.
 * <p>
 * Supposed to be used in cases when application startup fail must be tested:
 * {@code new GuiceyTestSupport(MyApp.class, (String) null).before()}.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 03.02.2022
 */
public class GuiceyTestSupport<C extends Configuration> extends DropwizardTestSupport<C> {

    public GuiceyTestSupport(final Class<? extends Application<C>> applicationClass,
                             final @Nullable String configPath,
                             final ConfigOverride... configOverrides) {
        this(applicationClass, configPath, (String) null, configOverrides);
    }

    public GuiceyTestSupport(final Class<? extends Application<C>> applicationClass,
                             final @Nullable String configPath,
                             final @Nullable ConfigurationSourceProvider configSourceProvider,
                             final ConfigOverride... configOverrides) {
        this(applicationClass, configPath, configSourceProvider, null, configOverrides);
    }

    public GuiceyTestSupport(final Class<? extends Application<C>> applicationClass,
                             final @Nullable String configPath,
                             final @Nullable ConfigurationSourceProvider configSourceProvider,
                             final @Nullable String customPropertyPrefix,
                             final ConfigOverride... configOverrides) {
        super(applicationClass, configPath, configSourceProvider, customPropertyPrefix,
                new CmdProvider<>(), configOverrides);
    }

    public GuiceyTestSupport(final Class<? extends Application<C>> applicationClass,
                             final @Nullable String configPath,
                             final @Nullable String customPropertyPrefix,
                             final ConfigOverride... configOverrides) {
        super(applicationClass, configPath, customPropertyPrefix, new CmdProvider<>(), configOverrides);
    }

    public GuiceyTestSupport(final Class<? extends Application<C>> applicationClass,
                             final C configuration) {
        super(applicationClass, configuration, new CmdProvider<>());
    }

    /**
     * By default, guicey simulates {@link io.dropwizard.lifecycle.Managed} (and
     * {@link org.eclipse.jetty.util.component.LifeCycle}) lifecycles. It might be not required for tests with
     * mocks.
     */
    public GuiceyTestSupport<C> disableManagedLifecycle() {
        ((CmdProvider<C>) this.commandInstantiator).disableManagedSimulation();
        return this;
    }

    /**
     * Normally, {@link #before()} and {@link #after()} methods are called separately. This method is a shortcut
     * mostly for errors testing when {@link #before()} assumed to fail to make sure {@link #after()} will be called
     * in any case: {@code testSupport.run(null)}.
     *
     * @param callback callback (may be null)
     * @param <T>      result type
     * @return callback result
     * @throws Exception any appeared exception
     */
    public <T> T run(final @Nullable TestSupport.RunCallback<T> callback) throws Exception {
        return TestSupport.run(this, callback);
    }

    /**
     * Shortcut for accessing guice beans.
     *
     * @param type target bean type
     * @param <T>  bean type
     * @return bean instance
     */
    public <T> T getBean(final Class<T> type) {
        return TestSupport.getBean(this, type);
    }

    /**
     * Shortcut for accessing guice beans.
     *
     * @param key binding key
     * @param <T> bean type
     * @return bean instance
     */
    public <T> T getBean(final Key<T> key) {
        return TestSupport.getBean(this, key);
    }

    @Override
    public void after() {
        super.after();
        final TestCommand<C> cmd = ((CmdProvider<C>) commandInstantiator).command;
        if (cmd != null) {
            cmd.stop();
        }
    }

    @SuppressWarnings("checkstyle:VisibilityModifier")
    static class CmdProvider<C extends Configuration> implements Function<Application<C>, Command> {

        public TestCommand<C> command;
        private boolean simulateManaged = true;

        public void disableManagedSimulation() {
            Preconditions.checkState(command == null, "Command already initialized");
            simulateManaged = false;
        }

        @Override
        public Command apply(final Application<C> application) {
            Preconditions.checkState(command == null, "Command already created");
            command = new TestCommand<>(application, simulateManaged);
            return command;
        }
    }
}

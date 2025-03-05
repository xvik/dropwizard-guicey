package ru.vyarus.dropwizard.guice.test;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.cli.Command;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;

import javax.annotation.Nullable;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;
import ru.vyarus.dropwizard.guice.test.util.RunResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
     * By default, guicey simulates jetty lifecycle to support for {@link io.dropwizard.lifecycle.Managed} and
     * {@link org.eclipse.jetty.util.component.LifeCycle} objects.
     * <p>
     * It might be required in test to avoid starting managed objects (especially all managed in application) because
     * important (for test) services replaced with mocks (and no need to wait for the rest of the application).
     *
     * @return test support instance for chained calls
     */
    public GuiceyTestSupport<C> disableManagedLifecycle() {
        ((CmdProvider<C>) this.commandInstantiator).disableManagedSimulation();
        return this;
    }

    /**
     * Register configuration modifiers.
     *
     * @param modifiers configuration modifiers
     * @return support object instance for chained calls
     */
    @SafeVarargs
    public final GuiceyTestSupport<C> configModifiers(final ConfigModifier<C>... modifiers) {
        return configModifiers(Arrays.asList(modifiers));
    }

    /**
     * Register configuration modifiers.
     *
     * @param modifiers configuration modifiers
     * @return support object instance for chained calls
     */
    public GuiceyTestSupport<C> configModifiers(final List<ConfigModifier<C>> modifiers) {
        ((CmdProvider<C>) this.commandInstantiator).configModifiers(modifiers);
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
     * Normally, {@link #before()} and {@link #after()} methods are called separately. This method is a shortcut
     * mostly for errors testing when {@link #before()} assumed to fail to make sure {@link #after()} will be called
     * in any case: {@code testSupport.run(null)}.
     *
     * @return execution result (with all required objects for verification)
     * @throws Exception any appeared exception
     */
    public RunResult<C> run() throws Exception {
        return TestSupport.run(this);
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
        private final List<ConfigModifier<C>> modifiers = new ArrayList<>();

        public void disableManagedSimulation() {
            Preconditions.checkState(command == null, "Command already initialized");
            simulateManaged = false;
        }

        public void configModifiers(final List<ConfigModifier<C>> modifiers) {
            Preconditions.checkState(command == null, "Command already initialized");
            this.modifiers.addAll(modifiers);
        }

        @Override
        public Command apply(final Application<C> application) {
            Preconditions.checkState(command == null, "Command already created");
            command = new TestCommand<>(application, simulateManaged, modifiers);
            return command;
        }
    }
}

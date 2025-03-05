package ru.vyarus.dropwizard.guice.test.cmd;

import com.google.common.base.MoreObjects;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import ru.vyarus.dropwizard.guice.test.builder.BaseBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link ru.vyarus.dropwizard.guice.test.cmd.CommandTestSupport}. Provides almost the same methods
 * as application support builder ({@link ru.vyarus.dropwizard.guice.test.builder.TestSupportBuilder}).
 * <p>
 * Use {@link ru.vyarus.dropwizard.guice.test.TestSupport#buildCommandRunner(Class)} for builder creation.
 * <p>
 * Builder is not supposed to be used for multiple runs: registered hooks will be applied only once. This limitation
 * is not possible to avoid because builder could be used for support objects creation, which are not aware of
 * hooks. So hooks could be registered globally only in time of addition to the builder.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 20.11.2023
 */
public class CommandRunBuilder<C extends Configuration> extends BaseBuilder<C, CommandRunBuilder<C>> {

    private String[] inputs;
    private final List<CommandListener<C>> listeners = new ArrayList<>();

    public CommandRunBuilder(final Class<? extends Application<C>> app) {
        super(app);
    }

    /**
     * The amount af answers should not be less than provided answers count.
     *
     * @param inputs answers for command console questions
     * @return builder instance for chained calls
     */
    public CommandRunBuilder<C> consoleInputs(final String... inputs) {
        this.inputs = inputs;
        return this;
    }

    /**
     * Simple listener to setup and cleanup something before and after command execution.
     *
     * @param listener listener
     * @return builder instance for chained calls
     */
    public CommandRunBuilder<C> listen(final CommandListener<C> listener) {
        this.listeners.add(listener);
        return this;
    }

    /**
     * Shortcut for {@code run("server")} to start application. Should be used to test application startup errors.
     * Error would be thrown if the application started successfully (to avoid frozen test).
     *
     * @return execution result
     */
    public CommandResult<C> runApp() {
        return run("server");
    }

    /**
     * Execute dropwizard command. Could be used to execute any command. The only difference with the usual usage
     * is that configuration file should not be declared (as second argument). Config file could be specified -
     * it would not lead to error (if the config file path was not declared in builder also).
     * <p>
     * Execution never throws an exception! Any appeared exception would be returned inside an unsuccessful result.
     * <p>
     * As it is not possible to run any callback in time of command execution - all runtime objects are provided
     * inside the result for inspection (some objects could be null, depending on a command type).
     * <p>
     * All command output would be available in the result. Also, output is streamed to console (to indicate the
     * exact app froze point, if command hangs). Error stream is also available separately to simplify error
     * check.
     *
     * @param args command execution arguments (without configuration file)
     * @return command execution result
     */
    public CommandResult<C> run(final String... args) {
        for (CommandListener<C> listener : listeners) {
            listener.setup(args);
        }

        final CommandResult<C> result = inputs == null ? build().run(args)
                : build().run(inputs, args);

        for (CommandListener<C> listener : listeners) {
            listener.cleanup(result);
        }
        return result;
    }

    private CommandTestSupport<C> build() {
        final CommandTestSupport<C> support;
        if (configObject != null) {
            if (configPath != null || !configOverrides.isEmpty() || configSourceProvider != null) {
                throw new IllegalStateException("Configuration object can't be used together with yaml configuration");
            }
            support = new CommandTestSupport<>(app, configObject);
        } else {
            final String prefix = MoreObjects.firstNonNull(propertyPrefix, "dw.");
            support = new CommandTestSupport<>(app, configPath, configSourceProvider,
                    prefix, prepareOverrides(prefix));
        }
        support.configModifiers(modifiers);

        return support;
    }

    /**
     * Command execution listener. Could be used to apply some additional initialization and clean for
     * command execution.
     *
     * @param <C> configuration type
     */
    public interface CommandListener<C extends Configuration> {

        /**
         * Called before command execution.
         *
         * @param args run arguments (without added configuration file) - exactly as specified in test
         */
        default void setup(final String... args) {
            // empty
        }

        /**
         * Called after command execution (even if execution fails).
         *
         * @param result command execution result
         */
        default void cleanup(final CommandResult<C> result) {
            // empty
        }
    }
}

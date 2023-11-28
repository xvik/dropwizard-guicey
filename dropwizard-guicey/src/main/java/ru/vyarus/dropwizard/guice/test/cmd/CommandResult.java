package ru.vyarus.dropwizard.guice.test.cmd;

import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.annotation.Nullable;

/**
 * Dropwizard command execution result. Note that command execution never throws exceptions: instead, all thrown
 * exceptions are intercepted and provided as the unsuccessful execution result.
 * <p>
 * Depending on executed command type, some object might be null (injector would be available only for environment
 * commands).
 * <p>
 * Output contains both out and err streams (to simulate console view, when both streams are shown in console).
 * Error output is also collected separately for validation (all these messages are present in common output too).
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 20.11.2023
 */
public class CommandResult<C extends Configuration> {
    private final boolean success;
    private final Throwable exception;
    private final String output;
    private final String errorOutput;
    private final Command command;
    private final Application<C> application;
    private final Bootstrap<C> bootstrap;
    private final C configuration;
    private final Environment environment;
    private final Injector injector;

    @SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:ParameterNumber"})
    public CommandResult(final boolean success,
                         final @Nullable Throwable exception,
                         final String output,
                         final String errorOutput,
                         final @Nullable Command command,
                         final Application<C> app,
                         final Bootstrap<C> bootstrap,
                         final @Nullable C configuration,
                         final @Nullable Environment environment,
                         final @Nullable Injector injector) {
        this.success = success;
        this.exception = exception;
        this.output = output;
        this.errorOutput = errorOutput;
        this.command = command;
        this.application = app;
        this.bootstrap = bootstrap;
        this.configuration = configuration;
        this.environment = environment;
        this.injector = injector;
    }

    /**
     * @return true for successful execution, false is exception was thrown
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return exception, thrown during command execution (or null for successful execution)
     */
    @Nullable
    public Throwable getException() {
        return exception;
    }

    /**
     * Note that default command
     * {@link Command#onError(io.dropwizard.cli.Cli, net.sourceforge.argparse4j.inf.Namespace, Throwable)}
     * implementation simply prints error stack trace to console, so output would contain this trace.
     *
     * @return console output (together with error stream to get output exactly as it would be in console) or
     * empty string if no output
     * @see #getErrorOutput() for error output only
     */
    public String getOutput() {
        return output;
    }


    /**
     * Note that default command
     * {@link Command#onError(io.dropwizard.cli.Cli, net.sourceforge.argparse4j.inf.Namespace, Throwable)}
     * implementation simply prints error stack trace to console, so output would contain this trace.
     *
     * @return error output or empty string
     */
    public String getErrorOutput() {
        return errorOutput;
    }

    /**
     * Could be null only if incorrect command name was specified (in this case help message would be shown
     * by dropwizard instead of execution). In all other cases command should not be null (it is searched manually
     * in the bootstrap object before execution).
     *
     * @return command instance, used for execution
     */
    @Nullable
    public Command getCommand() {
        return command;
    }

    /**
     * @return application instance used for execution
     */
    public Application<C> getApplication() {
        return application;
    }

    /**
     * @return bootstrap object used for execution
     */
    public Bootstrap<C> getBootstrap() {
        return bootstrap;
    }

    /**
     * Could be null for command without configuration and in case of configuration parse errors.
     *
     * @return configuration instance used or null
     */
    @Nullable
    public C getConfiguration() {
        return configuration;
    }

    /**
     * @return environment instance or null (for non-environment commands or startup error)
     */
    @Nullable
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Note: injector created only for {@link io.dropwizard.cli.EnvironmentCommand}.
     *
     * @return injector instance or null (for non-environment commands or due to injector startup errors)
     */
    @Nullable
    public Injector getInjector() {
        return injector;
    }
}

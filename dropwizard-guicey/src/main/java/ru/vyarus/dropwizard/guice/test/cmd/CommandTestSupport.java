package ru.vyarus.dropwizard.guice.test.cmd;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.cli.CheckCommand;
import io.dropwizard.core.cli.Cli;
import io.dropwizard.core.cli.Command;
import io.dropwizard.core.cli.ServerCommand;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.logging.common.LoggingUtil;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.POJOConfigurationFactory;
import io.dropwizard.util.JarLocation;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.io.EchoStream;
import ru.vyarus.dropwizard.guice.test.util.io.SystemInMock;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Test helper for running (any) commands. The class is almost similar to
 * {@link io.dropwizard.testing.DropwizardTestSupport}, but duffers in a way command is executed: this class
 * use {@link io.dropwizard.core.cli.Cli} which selects exactly the same command as in real use. Also, command
 * execution is a one-shot operation and so all validations could be performed only after command execution
 * (and not in the middle, as with usual application tests). That's why the resulting object contains all
 * objects used during execution - there is no other way to access them.
 * <p>
 * Supposed to be used through builder: {@link ru.vyarus.dropwizard.guice.test.TestSupport#buildCommandRunner(Class)}.
 * <p>
 * All types of dropwizard commands are supported, but depending on the command type, some objects in result would be
 * null. Note that guicey could only be used with {@link io.dropwizard.core.cli.EnvironmentCommand} - for other
 * commands it would be simply ignored (because dropwizard would not call bundle's run method).
 * <p>
 * Configurations support is the same as in dropwizard support: config object or configuration file with config
 * overrides might be used. When a configuration file is used, it would be automatically added to called command
 * (as a second argument).
 * <p>
 * System in, err and out streams are overridden. To test commands with used input, input strings must be declared
 * before command run. The resulting object would contain complete command output.
 * <p>
 * Execution never throws an error: in case of exception, it would be provided inside the resulting object.
 * <p>
 * Class is also suitable for application server startup errors check: instead of system exit, it will provide
 * exception in the resulted object. If exception does not appear during startup, test would be failed to prevent
 * infinite run (indicating unexpected successful run).
 * <p>
 * Command tests can't be executed in parallel (due to system io overrides)! For junit 5 use
 * {@code @Execution(SAME_THREAD)} on test class to prevent concurrent execution.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 20.11.2023
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyFields",
        "checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
public class CommandTestSupport<C extends Configuration> {
    protected final Class<? extends Application<C>> applicationClass;
    protected final String configPath;
    protected final ConfigurationSourceProvider configSourceProvider;
    protected final Set<ConfigOverride> configOverrides;
    protected final List<ConfigModifier<C>> modifiers = new ArrayList<>();
    protected final String customPropertyPrefix;

    /**
     * Flag that indicates whether instance was constructed with an explicit
     * Configuration object or not; handling of the two cases differ.
     * Needed because state of {@link #configuration} changes during lifecycle.
     */
    protected final boolean explicitConfig;
    protected C configuration;
    protected Environment environment;
    protected Injector injector;
    protected Application<C> application;
    protected Bootstrap<C> bootstrap;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;

    // print all output to real console (to track execution and visually see hangs)
    private final EchoStream stdOut = new EchoStream(originalOut);
    // err stream is merged in output and collected separately, just in case
    private final EchoStream stdErr = new EchoStream(stdOut);
    private final SystemInMock stdIn = new SystemInMock();
    private Cli cli;

    public CommandTestSupport(final Class<? extends Application<C>> applicationClass, final C configuration) {
        this.applicationClass = applicationClass;
        this.configPath = "";
        this.configSourceProvider = null;
        this.configOverrides = Collections.emptySet();
        this.customPropertyPrefix = null;
        this.configuration = configuration;
        this.explicitConfig = true;
    }

    public CommandTestSupport(final Class<? extends Application<C>> applicationClass,
                              final @Nullable String configPath,
                              final @Nullable ConfigurationSourceProvider configSourceProvider,
                              final @Nullable String customPropertyPrefix,
                              final ConfigOverride... configOverrides) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;
        this.configSourceProvider = configSourceProvider;
        this.configOverrides = Optional.ofNullable(configOverrides)
                .map(Set::of)
                .orElse(Set.of());
        this.customPropertyPrefix = customPropertyPrefix;
        this.explicitConfig = false;
    }

    /**
     * Register configuration modifiers.
     *
     * @param modifier configuration modifiers
     * @return command support instance for chained calls
     * @throws java.lang.IllegalStateException if called after application startup
     */
    @SafeVarargs
    public final CommandTestSupport<C> configModifiers(final ConfigModifier<C>... modifier) {
        return configModifiers(Arrays.asList(modifier));
    }

    /**
     * Register configuration modifiers.
     *
     * @param modifiers configuration modifiers
     * @return command support instance for chained calls
     * @throws java.lang.IllegalStateException if called after application startup
     */
    public CommandTestSupport<C> configModifiers(final List<ConfigModifier<C>> modifiers) {
        Preconditions.checkState(application == null, "Application is already created");
        this.modifiers.addAll(modifiers);
        return this;
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
        return run(null, args);
    }

    /**
     * Run for commands requiring console user input (in all other aspects is the same as {@link #run(String...)}).
     * <p>
     * Error would be thrown if provided responses are not enough (on the first input request, not covered by mock
     * data).
     *
     * @param input user input (should be the same (or more) then application would ask
     * @param args  command run arguments
     * @return command execution result
     */
    public CommandResult<C> run(final @Nullable String[] input, final String... args) {
        if (input != null) {
            stdIn.provideText(input);
        }

        final String[] params = insertConfigFile(args);
        // visually separate command output to simplify test output reading
        originalOut.println("\n\n" + applicationClass.getSimpleName() + " COMMAND: "
                + String.join(" ", params) + (input == null ? ""
                : (" (with " + input.length + " inputs)")));
        originalOut.println("-------------------------------------------------------------------------------------\n");

        Throwable err = null;
        try {
            // fail in case of successful server startup (this should be used ONLY to test startup fails)
            before(params.length > 0 && "server".equals(params[0]));
        } catch (Exception ex) {
            err = ex;
        }

        final Application<C> app = application;
        // if command would not be found, dropwizard would fail on run so no need to throw error here
        Command run = null;
        if (params.length > 0) {
            final String name = params[0];
            for (Command cmd : bootstrap.getCommands()) {
                if (cmd.getName().equals(name)) {
                    run = cmd;
                    break;
                }
            }
        }

        if (err == null) {
            // never throw error
            err = cli.run(params).orElse(null);
        }

        reset();
        return new CommandResult<>(err == null, err, stdOut.toString(), stdErr.toString(),
                run, app, bootstrap, configuration, environment, injector);
    }

    protected void before(final boolean preventServerStart) throws Exception {
        applyConfigOverrides();

        // Redirect stdout and stderr to our byte streams
        System.setOut(new PrintStream(stdOut, false, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(stdErr, false, StandardCharsets.UTF_8));
        System.setIn(stdIn);

        application = InstanceUtils.create(applicationClass);
        bootstrap = new Bootstrap<>(application);
        initializeBootstrap(preventServerStart);

        // Build what'll run the command and interpret arguments
        cli = new Cli(new DummyJarLocation(), bootstrap, System.out, System.err);
    }

    protected void reset() {
        resetConfigOverrides();
        // Don't leak logging appenders into other test cases
        if (configuration != null) {
            configuration.getLoggingFactory().reset();
        } else {
            LoggingUtil.getLoggerContext().getLogger(Logger.ROOT_LOGGER_NAME).detachAndStopAllAppenders();
        }
        application = null;

        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
    }

    private void applyConfigOverrides() {
        configOverrides.forEach(ConfigOverride::addToSystemProperties);
    }

    private void resetConfigOverrides() {
        configOverrides.forEach(ConfigOverride::removeFromSystemProperties);
    }

    private void initializeBootstrap(final boolean preventServerStart) {
        // register default command (see io.dropwizard.core.Application.addDefaultCommands)
        bootstrap.addCommand(new ServerCommand<>(application));
        bootstrap.addCommand(new CheckCommand<>(application));

        application.initialize(bootstrap);
        // important to put it after all other bundles to be able to resolve injector
        bootstrap.addBundle(new ConfiguredBundle<>() {
            @Override
            public void run(final C configuration, final Environment environment) {
                CommandTestSupport.this.environment = environment;
                // it would be impossible to resolve injector reference after shutdown
                CommandTestSupport.this.injector = InjectorLookup.getInjector(environment).orElse(null);

                if (preventServerStart) {
                    environment.lifecycle().addServerLifecycleListener(server -> {
                        throw new IllegalStateException(
                                "Application was expected to fail on startup, but successfully started instead");
                    });
                }
            }
        });

        if (configSourceProvider != null) {
            bootstrap.setConfigurationSourceProvider(configSourceProvider);
        }

        if (explicitConfig) {
            // pojo factory does nothing - it's ok to run modifiers here
            ConfigOverrideUtils.runModifiers(configuration, modifiers);
            bootstrap.setConfigurationFactoryFactory((klass, validator, objectMapper, propertyPrefix) ->
                    new POJOConfigurationFactory<>(configuration));
        } else if (customPropertyPrefix != null) {
            final String prefix = customPropertyPrefix;
            bootstrap.setConfigurationFactoryFactory((klass, validator, objectMapper, propertyPrefix) ->
                    new ConfigInterceptor<>(new YamlConfigurationFactory<>(klass, validator, objectMapper, prefix),
                            // the only way to intercept command instance in case of ConfiguredCommand
                            c -> {
                                ConfigOverrideUtils.runModifiers(c, modifiers);
                                this.configuration = c;
                            }));
        }
    }

    private String[] insertConfigFile(final String... args) {
        String[] params = args;
        if (configPath != null && args.length > 0) {
            params = new String[args.length + 1];
            params[0] = args[0];
            // have to include config path into command, becuase there is no wasy to embed it directly into
            // namespace as in DropwizardTestSupport
            params[1] = configPath;
            if (args.length > 1) {
                System.arraycopy(args, 2, params, 1, args.length);
            }
        }
        return params;
    }

    /**
     * The only way to intercept configuration in all cases is to wrap the configuration factory
     * (because bundle run method would be called only for environment commands).
     *
     * @param <C> configuration type
     */
    private static class ConfigInterceptor<C extends Configuration> implements ConfigurationFactory<C> {
        private final ConfigurationFactory<C> realFactory;
        private final Consumer<C> action;

        ConfigInterceptor(final ConfigurationFactory<C> realFactory, final Consumer<C> action) {
            this.realFactory = realFactory;
            this.action = action;
        }

        @Override
        public C build(final ConfigurationSourceProvider provider, final String path)
                throws IOException, ConfigurationException {
            final C res = realFactory.build(provider, path);
            action.accept(res);
            return res;
        }

        @Override
        public C build() throws IOException, ConfigurationException {
            final C res = realFactory.build();
            action.accept(res);
            return res;
        }
    }

    /**
     * Fake jar locator. It is required only to correctly print version and jar name in help, but for
     * command tests it is not important.
     */
    private static class DummyJarLocation extends JarLocation {

        DummyJarLocation() {
            super(CommandTestSupport.class);
        }

        @Override
        public Optional<String> getVersion() {
            return Optional.of("1.0.0");
        }
    }
}

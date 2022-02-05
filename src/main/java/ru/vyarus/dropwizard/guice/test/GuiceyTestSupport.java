package ru.vyarus.dropwizard.guice.test;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.jetbrains.annotations.Nullable;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;

import java.util.concurrent.Callable;
import java.util.function.Function;


/**
 * An alternative to {@link DropwizardTestSupport} which does not run jetty (web part) allowing to test only guice
 * context. Internally, {@link TestCommand} used instead of {@link io.dropwizard.cli.ServerCommand}.
 * <p>
 * Supposed to be used in cases when application startup fail must be tested:
 * {@link new GuiceyTestSupport(MyApp.class).before()}.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 03.02.2022
 */
public class GuiceyTestSupport<C extends Configuration> extends DropwizardTestSupport<C> {

    private static final ThreadLocal<TestCommand> COMMAND = new ThreadLocal<>();

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
     * Void execution. Mostly used to detect startup errors. Same as {@code #execute(null)}.
     *
     * @throws Exception any appeared exception
     */
    public void execute() throws Exception {
        execute((Callable<Void>) null);
    }

    /**
     * Normally, {@link #before()} and {@link #after()} methods are called separately. This method is a shortcut
     * mostly for errors testing when {@link #before()} assumed to fail to make sure {@link #after()} will be called
     * in any case: {@code testSupport.execute(null)}.
     *
     * @param callback callback (may be null)
     * @throws Exception any appeared exception
     */
    public void execute(final Runnable callback) throws Exception {
        execute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                callback.run();
                return null;
            }
        });
    }

    /**
     * Normally, {@link #before()} and {@link #after()} methods are called separately. This method is a shortcut
     * mostly for errors testing when {@link #before()} assumed to fail to make sure {@link #after()} will be called
     * in any case: {@code testSupport.execute(null)}.
     *
     * @param callback callback (may be null)
     * @throws Exception any appeared exception
     */
    public <T> T execute(final Callable<T> callback) throws Exception {
        before();
        try {
            return callback != null ? callback.call() : null;
        } finally {
            after();
        }
    }

    /**
     * Shortcut for accessing guice beans.
     *
     * @param type target bean type
     * @param <T>  bean type
     * @return bean instance
     */
    public <T> T getBean(final Class<T> type) {
        return getBean(Key.get(type));
    }

    /**
     * Shortcut for accessing guice beans.
     *
     * @param key binding key
     * @param <T> bean type
     * @return bean instance
     */
    public <T> T getBean(final Key<T> key) {
        return InjectorLookup.getInjector(getApplication()).get().getInstance(key);
    }

    @Override
    public void after() {
        super.after();
        final TestCommand cmd = COMMAND.get();
        if (cmd != null) {
            COMMAND.remove();
            cmd.stop();
        }
    }

    static class CmdProvider<C extends Configuration> implements Function<Application<C>, Command> {

        @Override
        public Command apply(final Application<C> application) {
            Preconditions.checkState(GuiceyTestSupport.COMMAND.get() == null,
                    "Command already bound in thread");
            final TestCommand<C> cmd = new TestCommand<>(application);
            GuiceyTestSupport.COMMAND.set(cmd);
            return cmd;
        }
    }
}

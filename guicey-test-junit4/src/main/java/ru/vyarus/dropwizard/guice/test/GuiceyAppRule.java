package ru.vyarus.dropwizard.guice.test;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.rules.ExternalResource;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;

import jakarta.annotation.Nullable;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * A JUnit rule for starting and stopping your guice application at the start and end of a test class.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).</p>
 * <p>Based on {@link io.dropwizard.testing.junit.DropwizardAppRule}, but doesn't start jetty and as a consequence
 * jersey and guice web modules not initialized. Emulates managed objects lifecycle.</p>
 * <p>Suppose to be used for testing internal services business logic as lightweight alternative for
 * dropwizard rule.</p>
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp for junit 5 alterantive
 * @since 23.10.2014
 */
public class GuiceyAppRule<C extends Configuration> extends ExternalResource {

    private final Class<? extends Application<C>> applicationClass;
    private final String configPath;
    private final List<ConfigOverride> configOverrides;

    private C configuration;
    private Application<C> application;
    private Environment environment;
    private TestCommand<C> command;

    public GuiceyAppRule(final Class<? extends Application<C>> applicationClass,
                         @Nullable final String configPath,
                         final ConfigOverride... configOverrides) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;
        this.configOverrides = Arrays.asList(configOverrides);
    }

    public C getConfiguration() {
        return configuration;
    }

    @SuppressWarnings("unchecked")
    public <A extends Application<C>> A getApplication() {
        return (A) application;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Injector getInjector() {
        return InjectorLookup.getInjector(application).get();
    }

    public <T> T getBean(final Class<T> type) {
        return getInjector().getInstance(type);
    }

    protected Application<C> newApplication() {
        try {
            return InstanceUtils.create(applicationClass);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate application", e);
        }
    }

    @Override
    protected void before() throws Throwable {
        for (ConfigOverride configOverride : configOverrides) {
            configOverride.addToSystemProperties();
        }
        startIfRequired();
    }

    @Override
    protected void after() {
        resetConfigOverrides();
        command.stop();
        command = null;
    }

    private void startIfRequired() {
        if (command != null) {
            return;
        }

        try {
            application = newApplication();

            final Bootstrap<C> bootstrap = new Bootstrap<C>(application) {
                @Override
                public void run(final C configuration, final Environment environment) throws Exception {
                    GuiceyAppRule.this.configuration = configuration;
                    GuiceyAppRule.this.environment = environment;
                    super.run(configuration, environment);
                }
            };

            application.initialize(bootstrap);

            startCommand(bootstrap);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to start test environment", e);
        }
    }

    private void startCommand(final Bootstrap<C> bootstrap) throws Exception {
        command = new TestCommand<>(application);

        final ImmutableMap.Builder<String, Object> file = ImmutableMap.builder();
        if (!Strings.isNullOrEmpty(configPath)) {
            file.put("file", configPath);
        }
        final Namespace namespace = new Namespace(file.build());

        command.run(bootstrap, namespace);
    }

    private void resetConfigOverrides() {
        for (final Enumeration<?> props = System.getProperties().propertyNames(); props.hasMoreElements();) {
            final String keyString = (String) props.nextElement();
            if (keyString.startsWith("dw.")) {
                System.clearProperty(keyString);
            }
        }
    }
}

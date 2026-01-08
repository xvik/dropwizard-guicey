package ru.vyarus.dropwizard.guice.injector.lookup;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Provider;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility for delayed access to guice beans. Useful when provider must be created before injector creation
 * and possibly before even environment or application become available. This is a more flexible alternative to
 * direct {@link ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup} usage.
 * <p>
 * For example:
 * {@code Provider<MyBean> provider = GuiceBeanProvider.provide(MyBean.class).forApp(application)} or, when
 * application is not available,
 * {@code GuiceBeanProvider.provide(MyBean.class).forApp(() -> something.getApplication())}.
 * The same for environment:
 * {@code GuiceBeanProvider.provide(MyBean.class).forEnv(() -> something.getEnvironment())}
 * (different method name for environment is required because of java generics erasure).
 * <p>
 * Generified or qualified beans are also supported:
 * {@code GuiceBeanProvider.provide(new TypeLiteral<>(MyBean<String>){}).qualified(MyAnn.class).forEnv(environment)}.
 * <p>
 * Normally, in case when guice injector is not available exception would be thrown.
 * There is a special {@link ru.vyarus.dropwizard.guice.injector.lookup.GuiceBeanProvider.Builder#nullWhenNoInjector()}
 * mode to return null instead of throwing error. This could be useful in case when bean is optional and provider could
 * be used with or without started guicey.
 *
 * @param <T> target bean type
 * @author Vyacheslav Rusakov
 * @since 05.01.2026
 */
public final class GuiceBeanProvider<T> implements Provider<T> {

    private final Key<T> key;
    private final Supplier<Environment> environment;
    private final Supplier<Application<?>> application;
    private final boolean optional;

    /**
     * Create provider.
     *
     * @param key         target bean key (may include qualifier)
     * @param environment environment supplier or null
     * @param application application supplier or null
     * @param optional    true to ignore all errors and return null value
     */
    private GuiceBeanProvider(final Key<T> key,
                              final @Nullable Supplier<Environment> environment,
                              final @Nullable Supplier<Application<?>> application,
                              final boolean optional) {
        this.key = Preconditions.checkNotNull(key, "Key required");
        this.environment = environment;
        this.application = application;
        this.optional = optional;
        Preconditions.checkState(environment != null || application != null, "Application or environment required");
    }

    /**
     * Start provider creation by declaring required bean type. If required, qualifier could be specified on
     * the next step. Provider creation is completed after application or environment source specification.
     *
     * @param type bean type
     * @param <T>  bean type
     * @return provider builder
     */
    public static <T> Builder<T> provide(final Class<T> type) {
        return new Builder<>(type);
    }

    /**
     * Start provider creation by declaring required bean type. If required, qualifier could be specified on
     * the next step. Provider creation is completed after application or environment source specification.
     * <p>
     * Type literal is useful for generified types declaration: {@code new TypeLiteral<MyBean<String>>(){}}.
     * If you already have a {@link java.lang.reflect.Type} object then wrap it into literal:
     * {@code TypeLiteral.get(type)}.
     *
     * @param type bean type
     * @param <T>  bean type
     * @return provider builder
     */
    public static <T> Builder<T> provide(final TypeLiteral<T> type) {
        return new Builder<>(type.getType());
    }


    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        final Injector injector = environment != null ? getInjector(environment.get())
                : getInjector(application.get());

        // extra case support: Injector itself requested
        final boolean isInjectorRequested = key.getTypeLiteral().getRawType().equals(Injector.class)
                && key.getAnnotation() == null && key.getAnnotationType() == null;
        if (injector == null || isInjectorRequested) {
            // optional case (no injector) or injector request
            return (T) injector;
        }
        return injector.getInstance(key);
    }

    private Injector getInjector(final Application<?> app) {
        if (optional && app == null) {
            return null;
        }
        Preconditions.checkNotNull(app, "Application required");
        final Optional<Injector> injector = InjectorLookup.getInjector(app);
        return optional && injector.isEmpty() ? null : injector.orElseThrow(() ->
                new IllegalStateException("Injector is not available for provided application "
                        + "or wrong application instance provided"));
    }

    private Injector getInjector(final Environment env) {
        if (optional && env == null) {
            return null;
        }
        Preconditions.checkNotNull(env, "Environment required");
        final Optional<Injector> injector = InjectorLookup.getInjector(env);
        return optional && injector.isEmpty() ? null : injector.orElseThrow(() ->
                new IllegalStateException("Injector is not available for provided environment"));
    }


    /**
     * Provider builder.
     *
     * @param <T> target type
     */
    public static class Builder<T> {
        private Key<T> key;
        private boolean optional;

        /**
         * Create builder.
         *
         * @param type target bean type
         */
        @SuppressWarnings("unchecked")
        public Builder(final Type type) {
            this.key = (Key<T>) Key.get(type);
        }

        /**
         * Specify bean qualifier annotation.
         *
         * @param annotation qualifier annotation
         * @return builder instance
         */
        public Builder<T> qualified(final Class<? extends Annotation> annotation) {
            checkQualifier();
            key = key.withAnnotation(annotation);
            return this;
        }

        /**
         * Specify bean qualifier string (for {@code @Named("string")} qualifier).
         *
         * @param name qualifier name
         * @return builder instance
         */
        public Builder<T> qualified(final String name) {
            return qualified(Names.named(name));
        }

        /**
         * Specify bean qualifier annotation instance. Not often used: the best example is guice {@code @Named}
         * annotations: {@code Names.named("name")} - annotation instance.
         *
         * @param annotation qualifier annotation instance
         * @return builder instance
         */
        public Builder<T> qualified(final Annotation annotation) {
            checkQualifier();
            key = key.withAnnotation(annotation);
            return this;
        }

        /**
         * Used for cases when provider could be used inside application (with started injector) and outside of it
         * (e.g. some unit test, or utility that should work without started application (bean is optional)).
         * <p>
         * Null returned if provided application/environment is null or injector could not be resolved. If injector
         * is resolved, but bean is not registered - error would be thrown (otherwise it would hide potential errors).
         *
         * @return builder instance
         */
        public Builder<T> nullWhenNoInjector() {
            optional = true;
            return this;
        }

        /**
         * Build provider using application instance. If application is not available, use provider instead.
         *
         * @param application application instance.
         * @return provider instance
         */
        public Provider<T> forApp(final Application<?> application) {
            return forApp(() -> application);
        }

        /**
         * Build provider using application instance provider.
         *
         * @param application application provider
         * @return provider instance
         */
        public Provider<T> forApp(final Supplier<Application<?>> application) {
            return new GuiceBeanProvider<>(key, null, application, optional);
        }

        /**
         * Build provider using environment instance. If environment is not available, use provider instead.
         *
         * @param environment environment instance
         * @return provider instance
         */
        public Provider<T> forEnv(final Environment environment) {
            return forEnv(() -> environment);
        }

        /**
         * Build provider using environment provider.
         *
         * @param environment environment provider
         * @return provider instance
         */
        public Provider<T> forEnv(final Supplier<Environment> environment) {
            return new GuiceBeanProvider<>(key, environment, null, optional);
        }

        private void checkQualifier() {
            Preconditions.checkState(key.getAnnotation() == null && key.getAnnotationType() == null,
                    "Qualification already applied");
        }
    }
}

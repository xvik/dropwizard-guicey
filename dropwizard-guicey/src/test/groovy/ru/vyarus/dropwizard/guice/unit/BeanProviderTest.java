package ru.vyarus.dropwizard.guice.unit;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jetty.MutableServletContextHandler;
import jakarta.inject.Provider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.injector.lookup.GuiceBeanProvider;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Vyacheslav Rusakov
 * @since 06.01.2026
 */
public class BeanProviderTest {

    @Test
    void testProvider() throws Exception {
        TestSupport.runCoreApp(App.class, injector -> {
            final App app = TestSupport.getContext().getApplication();
            final Environment env = TestSupport.getContext().getEnvironment();

            final Provider<Service> simple = GuiceBeanProvider
                    .provide(Service.class).forApp(app);
            final Provider<Service> simpleEnv = GuiceBeanProvider
                    .provide(Service.class).forEnv(env);
            final Provider<Service> qual = GuiceBeanProvider
                    .provide(Service.class).qualified(Qual.class).forApp(app);
            final Provider<Service> q = GuiceBeanProvider
                    .provide(Service.class).qualified("q").forApp(app);
            final Provider<Service<String>> generic = GuiceBeanProvider
                    .provide(new TypeLiteral<Service<String>>(){}).forApp(app);

            Assertions.assertThat(simple.get().getName()).isEqualTo("simple");
            Assertions.assertThat(simpleEnv.get().getName()).isEqualTo("simple");
            Assertions.assertThat(qual.get().getName()).isEqualTo("qual");
            Assertions.assertThat(q.get().getName()).isEqualTo("q");
            Assertions.assertThat(generic.get().getName()).isEqualTo("generic");

            // WHEN not existing bean
            Provider<Service<Integer>> unknown = GuiceBeanProvider
                    .provide(new TypeLiteral<Service<Integer>>(){}).forApp(app);

            Assertions.assertThatThrownBy(unknown::get)
                    .hasMessageContaining("[Guice/MissingConstructor]: No injectable constructor for type BeanProviderTest$Service<Integer>.");

            // WHEN optional
            unknown = GuiceBeanProvider
                    .provide(new TypeLiteral<Service<Integer>>(){}).nullWhenNoInjector().forApp(app);

            Assertions.assertThatThrownBy(unknown::get)
                    .hasMessageContaining("[Guice/MissingConstructor]: No injectable constructor for type BeanProviderTest$Service<Integer>.");

            return null;
        });
    }

    @Test
    void testNoInjectorError() {
        // WHEN no injector by app
        Provider<Service> simple = GuiceBeanProvider
                .provide(Service.class).forApp(new App());

        Assertions.assertThatThrownBy(simple::get)
                .hasMessageContaining("Injector is not available for provided application or wrong application instance provided");

        // WHEN no injector by env
        final Environment mock = Mockito.mock(Environment.class);
        Mockito.when(mock.getApplicationContext()).thenReturn(Mockito.mock(MutableServletContextHandler.class));
        simple = GuiceBeanProvider
                .provide(Service.class).forEnv(mock);

        Assertions.assertThatThrownBy(simple::get)
                .hasMessageContaining("Injector is not available for provided environment");
    }

    @Test
    void testOptional() {
        // WHEN no injector by app
        Provider<Service> simple = GuiceBeanProvider
                .provide(Service.class).nullWhenNoInjector().forApp(new App());

        Assertions.assertThat(simple.get()).isNull();

        // WHEN no injector by env
        final Environment mock = Mockito.mock(Environment.class);
        Mockito.when(mock.getApplicationContext()).thenReturn(Mockito.mock(MutableServletContextHandler.class));
        simple = GuiceBeanProvider
                .provide(Service.class).nullWhenNoInjector().forEnv(mock);

        Assertions.assertThat(simple.get()).isNull();

    }

    @Test
    void testNullAppAndEnvForOptional() {
        // WHEN no injector by app
        Provider<Service> simple = GuiceBeanProvider
                .provide(Service.class).nullWhenNoInjector().forApp((Application<?>) null);

        Assertions.assertThat(simple.get()).isNull();

        // WHEN no injector by env
        simple = GuiceBeanProvider
                .provide(Service.class).nullWhenNoInjector().forEnv((Environment) null);

        Assertions.assertThat(simple.get()).isNull();
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .modules(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(Service.class).toInstance(new Service("simple"));
                            bind(Key.get(Service.class).withAnnotation(Qual.class)).toInstance(new Service("qual"));
                            bind(Key.get(Service.class).withAnnotation(Names.named("q"))).toInstance(new Service("q"));
                            bind(Key.get(new TypeLiteral<Service<String>>() {})).toInstance(new Service("generic"));
                        }
                    })
                    .build();
        }
    }

    public static class Service<T> {
        private String name;

        public Service(String name) {
            this.name = name;
        }

        public String getName() {return name;}
    }

    @Retention(RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @BindingAnnotation
    public @interface Qual {}
}

package ru.vyarus.dropwizard.guice.cases.analysis;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2021
 */
public class AnnModuleApp extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .modules(binder -> {
                    binder.bind(String.class).annotatedWith(DefaultName.class).toInstance("John");
                    binder.install(binder2 -> binder2.bind(Double.class).annotatedWith(DefaultName.class).toInstance(1.0));
                        },
                        new Module1())
                .printGuiceBindings()
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    public static @interface DefaultName {
    }

    public static class Module1 extends AbstractModule {
        @Override
        protected void configure() {
            install(binder -> binder.bind(Integer.class).annotatedWith(DefaultName.class).toInstance(12));
        }
    }
}

package ru.vyarus.dropwizard.guice.examples;

import com.google.inject.matcher.Matchers;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.guice.validator.ValidationModule;

import javax.validation.Validator;
import javax.ws.rs.Path;

/**
 * Guice-validator integration sample application.
 *
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
public class GValApplication extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {

        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .modules(
                        // register validation module, but with exclusion for rest resources (which are guice beans)
                        // because dropwizard already applies validation support there
                        new ValidationModule(bootstrap.getValidatorFactory())
                                .targetClasses(Matchers.not(Matchers.annotatedWith(Path.class)))
                                .targetMethods(Matchers.not(Matchers.annotatedWith(Path.class)))
                )
                .build());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        // substitute dropwizard validator with guice-aware validator in order to use custom
        // validators in resources
        environment.setValidator(InjectorLookup.getInjector(this).get().getInstance(Validator.class));
    }
}

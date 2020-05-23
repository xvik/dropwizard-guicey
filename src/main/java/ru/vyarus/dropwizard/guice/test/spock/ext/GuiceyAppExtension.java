package ru.vyarus.dropwizard.guice.test.spock.ext;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.TestCommand;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp;

/**
 * Spock extension for guice-only lightweight tests.
 *
 * @author Vyacheslav Rusakov
 * @since 02.01.2015
 */
public class GuiceyAppExtension extends AbstractAppExtension<UseGuiceyApp> {

    @Override
    protected GuiceyInterceptor.EnvironmentSupport buildSupport(final UseGuiceyApp annotation, final Class<?> test) {
        return new GuiceyTestEnvironment(annotation, test);
    }

    @Override
    protected Class<? extends GuiceyConfigurationHook>[] getHooks(final UseGuiceyApp annotation) {
        return annotation.hooks();
    }

    private class GuiceyTestEnvironment extends GuiceyInterceptor.AbstractEnvironmentSupport {

        private final UseGuiceyApp annotation;
        private TestCommand command;

        GuiceyTestEnvironment(final UseGuiceyApp annotation, final Class<?> test) {
            super(test);
            this.annotation = annotation;
        }

        @Override
        protected DropwizardTestSupport build() {
            return create(annotation.value(),
                    annotation.config(),
                    convertOverrides(annotation.configOverride()));
        }

        @Override
        public void after() {
            // root call still important to cleanup properties
            super.after();
            if (command != null) {
                command.stop();
            }
        }

        @SuppressWarnings({"unchecked", "checkstyle:Indentation"})
        private <C extends Configuration> DropwizardTestSupport<C> create(
                final Class<? extends Application> app,
                final String configPath,
                final ConfigOverride... overrides) {
            return new DropwizardTestSupport<C>((Class<? extends Application<C>>) app,
                    configPath,
                    (String) null,
                    application -> {
                        command = new TestCommand<>(application);
                        return command;
                    },
                    overrides);
        }
    }
}

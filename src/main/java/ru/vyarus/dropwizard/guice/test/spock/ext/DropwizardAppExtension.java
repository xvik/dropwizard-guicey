package ru.vyarus.dropwizard.guice.test.spock.ext;

import io.dropwizard.testing.DropwizardTestSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp;

/**
 * Spock extension for starting complete dropwizard app.
 *
 * @author Vyacheslav Rusakov
 * @since 03.01.2015
 */
public class DropwizardAppExtension extends AbstractAppExtension<UseDropwizardApp> {

    @Override
    @SuppressWarnings("unchecked")
    protected GuiceyInterceptor.EnvironmentSupport buildSupport(final UseDropwizardApp annotation) {
        return new GuiceyInterceptor.AbstractEnvironmentSupport() {
            @Override
            protected DropwizardTestSupport build() {
                return new DropwizardTestSupport(annotation.value(),
                        annotation.config(),
                        convertOverrides(annotation.configOverride()));
            }
        };
    }

    @Override
    protected Class<? extends GuiceyConfigurationHook>[] getHooks(final UseDropwizardApp annotation) {
        return annotation.hooks();
    }
}

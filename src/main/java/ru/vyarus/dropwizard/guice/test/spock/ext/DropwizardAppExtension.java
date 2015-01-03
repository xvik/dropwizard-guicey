package ru.vyarus.dropwizard.guice.test.spock.ext;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.ExternalResource;
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp;

/**
 * Spock extension for {@link io.dropwizard.testing.junit.DropwizardAppRule}.
 *
 * @author Vyacheslav Rusakov
 * @since 03.01.2015
 */
public class DropwizardAppExtension extends AbstractAppExtension<UseDropwizardApp> {

    @Override
    protected GuiceyInterceptor.ResourceFactory buildResourceFactory(final UseDropwizardApp annotation) {
        return new GuiceyInterceptor.ResourceFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public ExternalResource newResource() {
                return new DropwizardAppRule(annotation.value(), annotation.config(),
                        convertOverrides(annotation.configOverride()));
            }
        };
    }
}

package ru.vyarus.dropwizard.guice.test.spock.ext;

import org.junit.rules.ExternalResource;
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule;
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp;

/**
 * Spock extension for {@link ru.vyarus.dropwizard.guice.test.GuiceyAppRule}.
 *
 * @author Vyacheslav Rusakov
 * @since 02.01.2015
 */
public class GuiceyAppExtension extends AbstractAppExtension<UseGuiceyApp> {

    @Override
    protected GuiceyInterceptor.ResourceFactory buildResourceFactory(final UseGuiceyApp annotation) {
        return new GuiceyInterceptor.ResourceFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public ExternalResource newResource() {
                return new GuiceyAppRule(annotation.value(), annotation.config(),
                        convertOverrides(annotation.configOverride()));
            }
        };
    }
}

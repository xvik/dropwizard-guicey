package ru.vyarus.dropwizard.guice.test.spock.ext;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
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
    protected GuiceyInterceptor.ExternalRuleAdapter buildResourceFactory(final UseGuiceyApp annotation) {
        return new GuiceyInterceptor.ExternalRuleAdapter() {
            private GuiceyAppRule rule;

            @Override
            @SuppressWarnings("unchecked")
            public ExternalResource newResource() {
                Preconditions.checkState(rule == null, "External resource creation could be called once.");
                rule = new GuiceyAppRule(annotation.value(), annotation.config(),
                        convertOverrides(annotation.configOverride()));
                return rule;
            }

            @Override
            public Injector getInjector() {
                Preconditions.checkState(rule != null, "External resource not created.");
                return rule.getInjector();
            }
        };
    }
}

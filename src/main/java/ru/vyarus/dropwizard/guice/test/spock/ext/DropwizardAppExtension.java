package ru.vyarus.dropwizard.guice.test.spock.ext;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.ExternalResource;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.support.conf.GuiceyConfigurator;
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp;

/**
 * Spock extension for {@link io.dropwizard.testing.junit.DropwizardAppRule}.
 *
 * @author Vyacheslav Rusakov
 * @since 03.01.2015
 */
public class DropwizardAppExtension extends AbstractAppExtension<UseDropwizardApp> {

    @Override
    protected GuiceyInterceptor.ExternalRuleAdapter buildResourceFactory(final UseDropwizardApp annotation) {
        return new GuiceyInterceptor.ExternalRuleAdapter() {
            private DropwizardAppRule rule;

            @Override
            @SuppressWarnings("unchecked")
            public ExternalResource newResource() {
                Preconditions.checkState(rule == null, "External resource creation could be called once.");
                rule = new DropwizardAppRule(annotation.value(), annotation.config(),
                        convertOverrides(annotation.configOverride()));
                return rule;
            }

            @Override
            public Injector getInjector() {
                Preconditions.checkState(rule != null, "External resource not created.");
                return InjectorLookup.getInjector(rule.getApplication()).get();
            }
        };
    }

    @Override
    protected Class<? extends GuiceyConfigurator>[] getConfigurators(final UseDropwizardApp annotation) {
        return annotation.configurators();
    }
}

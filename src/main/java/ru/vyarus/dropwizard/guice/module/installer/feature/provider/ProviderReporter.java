package ru.vyarus.dropwizard.guice.module.installer.feature.provider;

import com.google.common.collect.Lists;
import com.sun.jersey.spi.inject.InjectableProvider;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

import static java.lang.String.format;

/**
 * Special reporter to build detail providers report.
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
public class ProviderReporter extends Reporter {
    private static final String EXCEPTION = TAB + "%-10s (%s)";
    private static final String INJECTABLE = TAB + "@%s %s (%s)";

    private final List<String> providers = Lists.newArrayList();
    private final List<String> injectables = Lists.newArrayList();
    private final List<String> exceptions = Lists.newArrayList();

    public ProviderReporter() {
        super(JerseyProviderInstaller.class, "providers = ");
    }

    public ProviderReporter provider(final Class<?> provider) {
        if (InjectableProvider.class.isAssignableFrom(provider)) {
            logInjectable(provider);
        } else if (ExceptionMapper.class.isAssignableFrom(provider)) {
            logException(provider);
        } else {
            providers.add(format(TAB + "(%s)", provider.getName()));
        }
        return this;
    }

    @Override
    public void report() {
        if (!exceptions.isEmpty()) {
            line("Exception mappers");
            printAll(exceptions);
            if (!injectables.isEmpty() || !providers.isEmpty()) {
                emptyLine();
            }
        }
        if (!injectables.isEmpty()) {
            line("Injectable providers");
            printAll(injectables);
            if (!providers.isEmpty()) {
                emptyLine();
            }
        }
        if (!providers.isEmpty()) {
            line("Other");
            printAll(providers);
        }
        super.report();
    }

    private void logInjectable(final Class<?> provider) {
        final Class[] params = FeatureUtils.getInterfaceGenerics(provider, InjectableProvider.class);
        injectables.add(format(INJECTABLE,
                params[0].getSimpleName(), params[1].getSimpleName(), provider.getName()));
    }

    private void logException(final Class<?> provider) {
        final Class[] params = FeatureUtils.getInterfaceGenerics(provider, ExceptionMapper.class);
        exceptions.add(format(EXCEPTION, params[0].getSimpleName(), provider.getName()));
    }

    private void printAll(final List<String> lines) {
        for (String line : lines) {
            line(line);
        }
    }
}

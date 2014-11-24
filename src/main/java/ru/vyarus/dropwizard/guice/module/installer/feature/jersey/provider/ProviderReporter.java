package ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider;

import com.google.common.collect.Lists;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.java.generics.resolver.GenericsResolver;

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
    private static final String SIMPLE_FORMAT = TAB + "%-10s (%s)";
    private static final String INJECTION_FORMAT = TAB + "@%-10s (%s)";
    private static final String HK_MANAGED = " *HK managed";

    private final List<String> providers = Lists.newArrayList();
    private final List<String> injectionResolvers = Lists.newArrayList();
    private final List<String> factories = Lists.newArrayList();
    private final List<String> exceptions = Lists.newArrayList();

    public ProviderReporter() {
        super(JerseyProviderInstaller.class, "providers = ");
    }

    public ProviderReporter provider(final Class<?> provider, final boolean isHkManaged, final boolean isLazy) {
        if (InjectionResolver.class.isAssignableFrom(provider)) {
            injectionResolvers.add(logSimple(provider, InjectionResolver.class, INJECTION_FORMAT)
                    + hkManaged(isHkManaged) + lazy(isLazy));
        } else if (ExceptionMapper.class.isAssignableFrom(provider)) {
            exceptions.add(logSimple(provider, ExceptionMapper.class, SIMPLE_FORMAT)
                    + hkManaged(isHkManaged) + lazy(isLazy));
        } else if (Factory.class.isAssignableFrom(provider)) {
            factories.add(logSimple(provider, Factory.class, SIMPLE_FORMAT)
                    + hkManaged(isHkManaged) + lazy(isLazy));
        } else {
            providers.add(format(TAB + "(%s)", provider.getName())
                    + hkManaged(isHkManaged) + lazy(isLazy));
        }
        return this;
    }

    @Override
    public void report() {
        reportGroup("Exception mappers", exceptions);
        reportGroup("Injection resolvers", injectionResolvers);
        reportGroup("Factories", factories);
        reportGroup("Other", providers);
        super.report();
    }

    private String logSimple(final Class<?> provider, final Class<?> target, final String format) {
        final String param = GenericsResolver.resolve(provider).type(target).genericAsString(0);
        return format(format, param, provider.getName());
    }

    private String hkManaged(final boolean isHkManaged) {
        return isHkManaged ? HK_MANAGED : "";
    }

    private void printAll(final List<String> lines) {
        for (String line : lines) {
            line(line);
        }
    }

    private void reportGroup(final String title, final List<String> items) {
        if (!items.isEmpty()) {
            separate();
            line(title);
            printAll(items);
        }
    }
}

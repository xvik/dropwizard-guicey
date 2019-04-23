package ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.GenericsContext;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.ext.*;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * Special reporter to build detail providers report.
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
public class ProviderReporter extends Reporter {
    private static final String SIMPLE_FORMAT = TAB + "(%s)";
    private static final String SINGLE_GENERIC_FORMAT = TAB + "%-10s (%s)";
    private static final String DOUBLE_GENERICS_FORMAT = TAB + "%-10s -> %-10s (%s)";
    private static final String INJECTION_FORMAT = TAB + "@%-10s (%s)";
    private static final String HK_MANAGED = " *HK managed";

    private static final Map<Class, ExtDescriptor> DESCRIPTORS = ImmutableMap.<Class, ExtDescriptor>builder()
            .put(Supplier.class, new ExtDescriptor("Suppliers", SINGLE_GENERIC_FORMAT, 1))
            .put(Function.class, new ExtDescriptor("Functions", DOUBLE_GENERICS_FORMAT, 2))
            .put(ExceptionMapper.class, new ExtDescriptor("Exception mappers", SINGLE_GENERIC_FORMAT, 1))
            .put(ParamConverterProvider.class, new ExtDescriptor("Param converters", SIMPLE_FORMAT, 0))
            .put(ContextResolver.class, new ExtDescriptor("Context resolvers", SINGLE_GENERIC_FORMAT, 1))
            .put(MessageBodyReader.class, new ExtDescriptor("Message body readers", SINGLE_GENERIC_FORMAT, 1))
            .put(MessageBodyWriter.class, new ExtDescriptor("Message body writers", SINGLE_GENERIC_FORMAT, 1))
            .put(ReaderInterceptor.class, new ExtDescriptor("Reader interceptors", SIMPLE_FORMAT, 0))
            .put(WriterInterceptor.class, new ExtDescriptor("Writer interceptors", SIMPLE_FORMAT, 0))
            .put(ContainerRequestFilter.class, new ExtDescriptor("Container request filters", SIMPLE_FORMAT, 0))
            .put(ContainerResponseFilter.class, new ExtDescriptor("Container response filters", SIMPLE_FORMAT, 0))
            .put(DynamicFeature.class, new ExtDescriptor("Dynamic features", SIMPLE_FORMAT, 0))
            .put(ValueParamProvider.class, new ExtDescriptor("Value factory providers", SIMPLE_FORMAT, 0))
            .put(InjectionResolver.class, new ExtDescriptor("Injection resolvers", INJECTION_FORMAT, 1))
            .put(ApplicationEventListener.class, new ExtDescriptor("Application event listeners", SIMPLE_FORMAT, 0))
            .build();

    private final Multimap<Class, String> prerender = HashMultimap.create();

    public ProviderReporter() {
        super(JerseyProviderInstaller.class, "providers = ");
    }

    @SuppressWarnings("unchecked")
    public ProviderReporter provider(final Class<?> provider, final boolean isHkManaged, final boolean isLazy) {
        boolean recognized = false;
        for (Map.Entry<Class, ExtDescriptor> entry : DESCRIPTORS.entrySet()) {
            final Class ext = entry.getKey();
            if (ext.isAssignableFrom(provider)) {
                recognized = true;
                prerender.put(ext, renderLine(ext, provider, entry.getValue(), isHkManaged, isLazy));
            }
        }
        if (!recognized) {
            prerender.put(Object.class, format(SIMPLE_FORMAT, provider.getName())
                    + hkManaged(isHkManaged) + lazy(isLazy));
        }
        return this;
    }

    @Override
    public void report() {
        for (Class cls : prerender.keySet()) {
            final ExtDescriptor desc = DESCRIPTORS.get(cls);
            reportGroup(desc != null ? desc.name : "Other", prerender.get(cls));
        }
        super.report();
    }

    private String renderLine(final Class ext, final Class provider, final ExtDescriptor desc,
                              final boolean isHkManaged, final boolean isLazy) {
        final Object[] params = new Object[1 + desc.generics];
        int pos = 0;
        final GenericsContext generics = GenericsResolver.resolve(provider).type(ext);
        while (pos < desc.generics) {
            params[pos] = generics.genericAsString(pos++);
        }
        params[pos] = provider.getName();
        return format(desc.format, params) + hkManaged(isHkManaged) + lazy(isLazy);
    }

    private String hkManaged(final boolean isHkManaged) {
        return isHkManaged ? HK_MANAGED : "";
    }

    private void printAll(final Collection<String> lines) {
        for (String line : lines) {
            line(line);
        }
    }

    private void reportGroup(final String title, final Collection<String> items) {
        if (!items.isEmpty()) {
            separate();
            line(title);
            printAll(items);
        }
    }

    /**
     * Extension point descriptor.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    private static class ExtDescriptor {
        public String name;
        public String format;
        public int generics;

        ExtDescriptor(final String name, final String format, final int generics) {
            this.name = name;
            this.format = format;
            this.generics = generics;
        }
    }
}

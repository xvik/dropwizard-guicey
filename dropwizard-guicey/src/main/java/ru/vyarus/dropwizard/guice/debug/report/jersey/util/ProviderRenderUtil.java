package ru.vyarus.dropwizard.guice.debug.report.jersey.util;

import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.GenericsContext;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.ext.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Jersey providers recognition and render utility.
 *
 * @author Vyacheslav Rusakov
 * @since 26.10.2019
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class ProviderRenderUtil {
    private static final String SIMPLE_FORMAT = "%s";
    private static final String SINGLE_GENERIC_FORMAT = "%-30s %s";
    private static final String DOUBLE_GENERICS_FORMAT = "%-15s -> %-15s %s";
    private static final String INJECTION_FORMAT = "@%-30s %s";

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
            .put(InjectionResolver.class, new ExtDescriptor("Injection resolvers", INJECTION_FORMAT, 1))
            .put(ValueParamProvider.class, new ExtDescriptor("Param value providers", SIMPLE_FORMAT, 0))
            .put(ApplicationEventListener.class, new ExtDescriptor("Application event listeners", SIMPLE_FORMAT, 0))
            .put(ModelProcessor.class, new ExtDescriptor("Model processors", SIMPLE_FORMAT, 0))
            .build();

    private ProviderRenderUtil() {
    }

    /**
     * Detects known provider types in provided class.
     *
     * @param provider provider class for detection
     * @return list of detected types or list with {@link Object} class to identify unknown type.
     */
    @SuppressWarnings("unchecked")
    public static List<Class> detectProviderTypes(final Class<?> provider) {
        final List<Class> res = new ArrayList<>();
        boolean recognized = false;
        for (Map.Entry<Class, ExtDescriptor> entry : DESCRIPTORS.entrySet()) {
            final Class ext = entry.getKey();
            if (ext.isAssignableFrom(provider)) {
                recognized = true;
                res.add(ext);
            }
        }
        if (!recognized) {
            res.add(Object.class);
        }
        return res;
    }

    /**
     * Render provider as exact extension type. Original provider may implement multiple extension types,
     * but here we render only as exact type only.
     *
     * @param ext         extension type (affects render format)
     * @param provider    provider class
     * @param isHkManaged true if extension is managed with HK2
     * @param isLazy      true if extension is annotated with
     *                    {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding}
     * @return rendered provider line
     */
    public static String render(final Class<?> ext,
                                final Class provider,
                                final boolean isHkManaged,
                                final boolean isLazy) {
        if (ext.equals(MessageBodyWriter.class) || ext.equals(MessageBodyReader.class)) {
            return renderMessageReaderWriter(ext, provider, isHkManaged, isLazy);
        }
        return DESCRIPTORS.containsKey(ext)
                ? renderLine(ext, provider, DESCRIPTORS.get(ext), isHkManaged, isLazy)
                : renderUnknown(provider, isHkManaged, isLazy);
    }

    /**
     * In fuw cases it is possible to get more information using provider instance. So this report
     * will be a bit more detailed comparing to {@link #render(Class, Class, boolean, boolean)}.
     *
     * @param ext         extension type (affects render format)
     * @param instance    provider instance
     * @param isHkManaged true if extension is managed with HK2
     * @param isLazy      true if extension is annotated with
     *                    {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding}
     * @return rendered provider line
     */
    public static String render(final Class<?> ext,
                                final Object instance,
                                final boolean isHkManaged,
                                final boolean isLazy) {
        final Class<?> type = instance.getClass();
        String res = null;
        if (ParamInjectionResolver.class.isAssignableFrom(type)) {
            res = renderParamInjectionResolver((ParamInjectionResolver) instance, isHkManaged, isLazy);
        } else if (ext.equals(InjectionResolver.class)) {
            res = renderInjectionResolver((InjectionResolver) instance, isHkManaged, isLazy);
        }
        return res == null ? render(ext, type, isHkManaged, isLazy) : res;
    }

    /**
     * @param ext provider extension type
     * @return name of extension type group (e.g. "Exception mappers") or "Other" if it is not a known extension type
     */
    public static String getTypeName(final Class<?> ext) {
        final ExtDescriptor desc = DESCRIPTORS.get(ext);
        return desc != null ? desc.name : "Other";
    }

    private static String renderUnknown(final Class<?> provider, final boolean isHkManaged, final boolean isLazy) {
        return String.format(SIMPLE_FORMAT, RenderUtils
                .renderClassLine(provider, collectMarkers(Object.class, provider, isHkManaged, isLazy)));
    }

    @SuppressWarnings({"checkstyle:NPathComplexity", "PMD.NPathComplexity"})
    private static List<String> collectMarkers(final Class<?> ext,
                                               final Class<?> provider,
                                               final boolean isHkManaged,
                                               final boolean isLazy) {
        final List<String> markers = new ArrayList<>();
        if (isHkManaged) {
            markers.add("jersey managed");
        }
        if (isLazy) {
            markers.add("lazy");
        }
        if (ExceptionMapper.class.equals(ext) && ExtendedExceptionMapper.class.isAssignableFrom(provider)) {
            markers.add("extended");
        }
        final Annotation filter = FeatureUtils.getAnnotatedAnnotation(provider, NameBinding.class);
        if (filter != null) {
            markers.add("only @" + filter.annotationType().getSimpleName());
        }
        return markers;
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static String renderLine(final Class ext,
                                     final Class provider,
                                     final ExtDescriptor desc,
                                     final boolean isHkManaged,
                                     final boolean isLazy) {
        final Object[] params = new Object[1 + desc.generics];
        int pos = 0;
        final GenericsContext generics = GenericsResolver.resolve(provider).type(ext);
        while (pos < desc.generics) {
            params[pos] = generics.genericAsString(pos++);
        }
        params[pos] = RenderUtils.renderClassLine(provider, collectMarkers(ext, provider, isHkManaged, isLazy));
        // special case for message body readers and writers to identify collection mappers
        if ("Object".equals(params[0]) && isAbstractCollectionJaxbProvider(provider)) {
            params[0] = "T[], Collection<T>";
        }
        return String.format(desc.format, params);
    }

    // AbstractCollectionJaxbProvider located inside (org.glassfish.jersey.media:jersey-media-jaxb:2.36)
    // artifact, not present by default (but it is a transitive dependency for dropwizard-testing),
    // and so can't be checked with isAssignableFrom
    private static boolean isAbstractCollectionJaxbProvider(final Class provider) {
        return (MessageBodyReader.class.isAssignableFrom(provider)
                || MessageBodyWriter.class.isAssignableFrom(provider))
                && GenericsResolver.resolve(provider).getGenericsInfo()
                .getComposingTypes().stream()
                .map(Class::getName)
                .anyMatch("org.glassfish.jersey.jaxb.internal.AbstractCollectionJaxbProvider"::equals);
    }

    private static String renderMessageReaderWriter(final Class<?> ext,
                                                    final Class provider,
                                                    final boolean isHkManaged,
                                                    final boolean isLazy) {
        String media = "";
        if (MessageBodyReader.class.equals(ext)) {
            final Consumes consume = FeatureUtils.getAnnotation(provider, Consumes.class);
            if (consume != null) {
                media = Arrays.toString(consume.value());
            }
        }
        if (MessageBodyWriter.class.equals(ext)) {
            final Produces produce = FeatureUtils.getAnnotation(provider, Produces.class);
            if (produce != null) {
                media = Arrays.toString(produce.value());
            }
        }
        final String res = renderLine(ext, provider, DESCRIPTORS.get(ext), isHkManaged, isLazy);
        return String.format("%-100s %s", res, media);
    }

    private static String renderParamInjectionResolver(final ParamInjectionResolver instance,
                                                       final boolean hkManaged,
                                                       final boolean lazy) {
        try {
            final Field field = ParamInjectionResolver.class.getDeclaredField("valueParamProvider");
            field.setAccessible(true);
            final ValueParamProvider param = (ValueParamProvider) field.get(instance);
            final Class<? extends ParamInjectionResolver> type = instance.getClass();
            return String.format("@%-30s %s using %s %s",
                    instance.getAnnotation().getSimpleName(),
                    RenderUtils.renderClassLine(type),
                    param.getClass().getSimpleName(),
                    RenderUtils.markers(collectMarkers(InjectionResolver.class, type, hkManaged, lazy)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to access provider field", e);
        }
    }

    private static String renderInjectionResolver(final InjectionResolver instance,
                                                  final boolean hkManaged,
                                                  final boolean lazy) {
        final Class<? extends InjectionResolver> type = instance.getClass();
        return String.format(INJECTION_FORMAT, instance.getAnnotation().getSimpleName(),
                RenderUtils.renderClassLine(type,
                        collectMarkers(InjectionResolver.class, type, hkManaged, lazy)));
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

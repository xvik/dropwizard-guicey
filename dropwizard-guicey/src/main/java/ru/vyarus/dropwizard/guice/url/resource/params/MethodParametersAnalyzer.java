package ru.vyarus.dropwizard.guice.url.resource.params;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import org.glassfish.jersey.model.Parameter;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.model.param.BeanParameterSource;
import ru.vyarus.dropwizard.guice.url.model.param.DeclarationSource;
import ru.vyarus.dropwizard.guice.url.model.param.ParameterSource;
import ru.vyarus.dropwizard.guice.url.util.MultipartParamsSupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Supplement for {@link ru.vyarus.dropwizard.guice.url.resource.ResourceAnalyzer}: analyze method parameter
 * declaration and fills {@link ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo}.
 *
 * @author Vyacheslav Rusakov
 * @since 17.12.2025
 */
public final class MethodParametersAnalyzer {

    private MethodParametersAnalyzer() {
    }

    /**
     * Recognize resource method parameters (query params, path params, entity, etc.).
     * Use jersey api for parameters recognition to reduce potential inconsistencies.
     * <p>
     * Only analyze parameters with non-null value provided (null means parameter must not be used).
     *
     * @param resource  resource class
     * @param method    analyzed method
     * @param args      provided arguments
     * @param info      method info aggregation object
     * @param multipart multipart fields aggregation object
     */
    public static void analyze(final Class<?> resource, final Method method, final Object[] args,
                               final ResourceMethodInfo info, final Multimap<String, Object> multipart) {
        // use jersey parameters parsing mechanism
        final List<Parameter> methodParams = org.glassfish.jersey.server.model.Parameter
                .create(resource, method.getDeclaringClass(), method, resource.isAnnotationPresent(Encoded.class));
        final Context methodContext = new Context(resource, method);
        for (int i = 0; i < methodParams.size(); i++) {
            final Object value = args[i];
            final Parameter param = methodParams.get(i);
            // ignore parameters without user-provided value (nothing to process)
            if (value != null) {
                analyzeParam(param, methodContext.paramContext(i, value), info, multipart);
            }
        }
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private static void analyzeParam(final Parameter param, final Context context,
                                     final ResourceMethodInfo info, final Multimap<String, Object> multipart) {
        // list because multipart declarations may span multiple parameters
        final List<ParameterSource> source = switch (param.getSource()) {
            case PATH -> handlePathParam(param, context, info);
            case QUERY -> handleQueryParam(param, context, info);
            case HEADER -> handleHeaderParam(param, context, info);
            case MATRIX -> handleMatrixParam(param, context, info);
            case COOKIE -> handleCookieParam(param, context, info);
            case FORM -> handleFormParam(param, context, info);
            case ENTITY -> handleEntityParam(param, context, info);
            case BEAN_PARAM -> handleBeanParam(param, context, info, multipart);
            case UNKNOWN -> handleUnknown(param, context, info, multipart);
            default -> throw new IllegalArgumentException("Unsupported parameter type: " + param.getSource()
                    + " (" + param + ")");
        };
        // no source name could be for BeanParam (no need to store bean param itself)
        if (source != null) {
            // storing source to be able to use registered parameter converters
            source.forEach(info::addParameterSource);
        }
    }

    private static List<ParameterSource> handlePathParam(final Parameter param, final Context context,
                                                         final ResourceMethodInfo info) {
        // path segments could be used for matrix params declaration in the middle
        if (!(context.value instanceof PathSegment)) {
            info.getPathParams().put(param.getSourceName(), context.value);
            return Collections.singletonList(source(param, context));
        }
        return null;
    }

    private static List<ParameterSource> handleQueryParam(final Parameter param, final Context context,
                                                          final ResourceMethodInfo info) {
        info.getQueryParams().put(param.getSourceName(), context.value);
        return Collections.singletonList(source(param, context));
    }

    private static List<ParameterSource> handleHeaderParam(final Parameter param, final Context context,
                                                           final ResourceMethodInfo info) {
        info.getHeaderParams().put(param.getSourceName(), context.value);
        return Collections.singletonList(source(param, context));
    }

    private static List<ParameterSource> handleMatrixParam(final Parameter param, final Context context,
                                                           final ResourceMethodInfo info) {
        info.getMatrixParams().put(param.getSourceName(), context.value);
        return Collections.singletonList(source(param, context));
    }

    private static List<ParameterSource> handleCookieParam(final Parameter param, final Context context,
                                                           final ResourceMethodInfo info) {
        info.getCookieParams().put(param.getSourceName(), context.value);
        return Collections.singletonList(source(param, context));
    }

    private static List<ParameterSource> handleFormParam(final Parameter param, final Context context,
                                                         final ResourceMethodInfo info) {
        info.getFormParams().put(param.getSourceName(), context.value);
        return Collections.singletonList(source(param, context));
    }

    private static List<ParameterSource> handleEntityParam(final Parameter param, final Context context,
                                                           final ResourceMethodInfo info) {
        // assume it is a method body (e.g. POST entity).
        // The logic: if used provided not null value then it should be used in request
        Preconditions.checkState(info.getEntity() == null, "Multiple entity arguments detected: \n\t%s\n\t%s",
                info.getEntity(), context.value);
        info.setEntity(context.value);
        return Collections.singletonList(source(param, context));
    }

    private static List<ParameterSource> handleBeanParam(final Parameter param,
                                                         final Context context,
                                                         final ResourceMethodInfo info,
                                                         final Multimap<String, Object> multipart) {
        final org.glassfish.jersey.server.model.Parameter.BeanParameter bean =
                (org.glassfish.jersey.server.model.Parameter.BeanParameter) param;

        final Field[] fields = bean.getRawType().getDeclaredFields();
        final Iterator<org.glassfish.jersey.server.model.Parameter> paramIt = bean.getParameters().iterator();
        for (Field field : fields) {
            if (field.getAnnotations().length == 0) {
                continue;
            }
            final Parameter fieldParameter = paramIt.next();
            verify(fieldParameter, field);
            field.setAccessible(true);
            final Object fieldValue;
            try {
                fieldValue = field.get(context.value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to introspect @BeanParam", e);
            }
            if (fieldValue != null) {
                // bean class remembered because it might be a generic
                analyzeParam(fieldParameter, context.fieldContext(bean.getRawType(), field, fieldValue),
                        info, multipart);
            }
        }
        // no source for bean itself
        return null;
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private static List<ParameterSource> handleUnknown(final Parameter param, final Context context,
                                                       final ResourceMethodInfo info,
                                                       final Multimap<String, Object> multipart) {
        // most likely, unknown parameter would be a multipart declaration
        final boolean formData = "FormDataParam".equals(param.getSourceAnnotation().annotationType().getSimpleName());
        final Object value = context.value;
        // for multipart or Multimap values storing only declaration source (to track source only)
        if (formData) {
            if (value instanceof Collection<?>) {
                ((Collection<?>) value).forEach(o -> multipart.put(param.getSourceName(), o));
            } else {
                multipart.put(param.getSourceName(), value);
            }
            return Collections.singletonList(declarationSource(param, context, param.getSourceName()));
        } else if ("org.glassfish.jersey.media.multipart.FormDataMultiPart".equals(param.getRawType().getName())) {
            final List<ParameterSource> sources = new ArrayList<>();
            // case: method parameter accepts entire multipart (without direct fields mapping)
            MultipartParamsSupport.configureFromMultipart(multipart, value).forEach(
                    (name, val) -> sources.add(declarationSource(param, context, name)));
            return sources.isEmpty() ? null : sources;
        } else if (value instanceof MultivaluedMap<?, ?> map) {
            final List<ParameterSource> sources = new ArrayList<>();
            // this could be urlencoded parameters
            map.forEach((o, objects) -> {
                final String name = String.valueOf(o);
                info.getFormParams().put(name, objects.size() == 1 ? objects.get(0) : objects);
                        sources.add(declarationSource(param, context, name));
                    }
            );
            return sources.isEmpty() ? null : sources;
        } else {
            // edge case: if POST/PUT entity declared as parameter with validation annotation (NotNull) - it would
            // not be detected as ENTITY, but as UNKNOWN.
            Preconditions.checkState(info.getEntity() == null, "Multiple entity arguments detected: \n\t%s\n\t%s",
                    info.getEntity(), value);
            info.setEntity(value);

            return Collections.singletonList(sourceOverride(param, context, Parameter.Source.ENTITY));
        }
    }

    // make sure correct parameter selected, just in case
    private static void verify(final Parameter parameter, final Field field) {
        for (Annotation ann : field.getAnnotations()) {
            // make sure all field annotations present in parameter declaration
            Preconditions.checkState(parameter.getAnnotation(ann.annotationType()).equals(ann),
                    "Incorrect parameter selected for bean field %s: %s", field, parameter);
        }
    }

    // for multipart parameters or urlencoded Multimap
    private static ParameterSource declarationSource(final Parameter parameter, final Context context,
                                                     final String name) {
        final Parameter subst = CustomParameter.overrideSource(
                (org.glassfish.jersey.server.model.Parameter) parameter, Parameter.Source.FORM, name);
        return new DeclarationSource(subst, context.value, context.resource, context.method,
                context.position, name);
    }

    private static ParameterSource sourceOverride(final Parameter parameter, final Context context,
                                                  final Parameter.Source source) {
        // note: this also substitutes parameter NAME which is wrong!
        final Parameter subst = CustomParameter.overrideSource(
                (org.glassfish.jersey.server.model.Parameter) parameter, source, null);
        return source(subst, context);
    }

    private static ParameterSource source(final Parameter parameter, final Context context) {
        // original value preserved to be able to track value changes (e.g. multipart value is always processed)
        if (context.field != null) {
            return new BeanParameterSource(parameter, context.value, context.resource, context.method,
                    context.position, context.beanClass, context.field);
        } else {
            return new ParameterSource(parameter, context.value, context.resource, context.method, context.position);
        }
    }

    private static final class Context {
        private final Class<?> resource;
        private final Method method;
        private final int position;
        private final Class<?> beanClass;
        private final Field field;
        private final Object value;

        private Context(final Class<?> resource, final Method method) {
            this(resource, method, 0, null, null, null);
        }

        private Context(final Class<?> resource, final Method method,
                        final int position, final Class<?> beanClass, final Field field, final Object value) {
            this.resource = resource;
            this.method = method;
            this.position = position;
            this.beanClass = beanClass;
            this.field = field;
            this.value = value;
        }

        public Context paramContext(final int position, final Object value) {
            return new Context(resource, method, position, null, null, value);
        }

        public Context fieldContext(final Class<?> beanClass, final Field field, final Object value) {
            return new Context(resource, method, position, beanClass, field, value);
        }

        @Override
        public String toString() {
            String res = resource.getSimpleName() + "." + method.getName() + " ([" + position + "] "
                    + method.getParameterTypes()[position].getSimpleName() + ")";
            if (field != null) {
                res += " " + beanClass.getSimpleName() + "." + field.getName();
            }
            return res;
        }
    }
}

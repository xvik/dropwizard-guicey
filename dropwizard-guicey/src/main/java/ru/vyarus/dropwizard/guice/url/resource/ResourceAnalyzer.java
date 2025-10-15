package ru.vyarus.dropwizard.guice.url.resource;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.url.model.MethodCall;
import ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo;
import ru.vyarus.dropwizard.guice.url.util.Caller;
import ru.vyarus.dropwizard.guice.url.util.MultipartParamsSupport;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility to search jersey annotations on resource class. Jersey assumes that all annotations are declared on
 * the same method! This could be a superclass method or implemented interface.
 *
 * @author Vyacheslav Rusakov
 * @since 25.09.2025
 */
@SuppressWarnings({"PMD.GodClass", "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
public final class ResourceAnalyzer {

    private ResourceAnalyzer() {
    }

    /**
     * Check that provided method belongs to resource class hierarchy (it might be superclass method or interface
     * method).
     *
     * @param resource resource class
     * @param method   target method to verify
     */
    public static void validateResourceMethod(final Class<?> resource, final Method method) {
        Preconditions.checkState(method.getDeclaringClass().isAssignableFrom(resource),
                "Method '%s' does not belong to resource '%s'",
                TypeToStringUtils.toStringMethod(method, null), resource.getSimpleName());
    }

    /**
     * Search for {@link Path} annotation on resource class or it's superclasses or interfaces.
     * <p>
     * The result is normalized: will always start with slash and will never end with slash
     * (simpler for concatenation).
     *
     * @param resource resource class to analyze
     * @return path annotation value
     * @throws IllegalStateException if path annotation is not found on a resource or any of it's super classes and
     *                               interfaces
     */
    public static String getResourcePath(final Class<?> resource) {
        return PathUtils.normalizeAbsolutePath(findAnnotatedResource(resource).getAnnotation(Path.class).value());
    }

    /**
     * Search for the actual annotated method (could be superclass or interface) and read the configured path.
     * <p>
     * The result is normalized: will always start with slash and will never end with slash
     * (simpler for concatenation).
     *
     * @param method resource method
     * @return value of {@link jakarta.ws.rs.Path} annotation on metho
     * @throws java.lang.IllegalStateException if annotation not found
     */
    public static String getMethodPath(final Method method) {
        final Path path = method.getAnnotation(Path.class);
        if (path == null) {
            // method might miss path annotation if the path is the same as resource path
            return "/";
        }
        return PathUtils.normalizeAbsolutePath(findAnnotatedMethod(method).getAnnotation(Path.class).value());
    }

    /**
     * Search for resource method {@code @Path} annotation. If multiple methods selected, will select method
     * without arguments. If there is no matching no-args method found throws exception (about multiple methods found).
     *
     * @param resource resource class
     * @param method   method name
     * @return resource method {@code @Path} annotation value
     * @throws java.lang.IllegalStateException if method with annotations not found
     */
    public static String getMethodPath(final Class<?> resource, final String method) {
        final Method target = findMethod(resource, method);
        return getMethodPath(target);
    }

    /**
     * Search for the actual resource annotations source (might be superclass of interface).
     *
     * @param resource resource class
     * @return resource class or its superclass or interface where {@link jakarta.ws.rs.Path} annotation declared
     */
    public static Class<?> findAnnotatedResource(final Class<?> resource) {
        return getAnnotatedResource(resource).orElseThrow(() -> new IllegalStateException(String.format(
                "@Path annotation was not found on resource %s or any of it's super classes and interfaces",
                resource.getSimpleName()
        )));
    }

    /**
     * Shortcut for {@code class.getMethod(Par1.class, Par2.class)}.
     *
     * @param resource   resource class
     * @param methodName method name
     * @param parameters method parameter types
     * @return method instance
     * @throws java.lang.IllegalStateException when method not found
     */
    public static Method findMethod(final Class<?> resource, final String methodName, final Class<?>... parameters) {
        final Method method;
        try {
            method = resource.getMethod(methodName, parameters);
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("Method '%s(%s)' not found in class '%s'",
                    methodName,
                    Arrays.stream(parameters).map(Class::getSimpleName).collect(Collectors.joining(", ")),
                    resource.getSimpleName()), ex);
        }
        return method;
    }

    /**
     * Search for method with provided name in resource. If multiple methods selected, will select method
     * without arguments. If there is no matching no-args method found throws exception (about multiple methods found).
     * This no-args behavior is required to comply with arguments-based search (it would be otherwise impossible
     * to search for no-args method).
     * <p>
     * Returned method might be not the resource method, but a super method from a subclass or interface,
     * where jersey annotations declared.
     *
     * @param resource resource clas
     * @param method   method name
     * @return resource method with jersey annotations
     * @throws java.lang.IllegalStateException if the method is not found or multiple methods found
     */
    public static Method findMethod(final Class<?> resource, final String method) {
        final Method[] methods = resource.getMethods();
        final List<Method> found = new ArrayList<>();
        for (final Method m : methods) {
            if (method.equals(m.getName()) && !m.isSynthetic()) {
                found.add(m);
            }
        }
        Preconditions.checkState(!found.isEmpty(), "Method '%s' not found in class '%s'",
                method, resource.getSimpleName());
        if (found.size() > 1) {
            // search for no-args method
            final Method single = found.stream().filter(method1 -> method1.getParameterCount() == 0).findAny()
                    .orElseThrow(() ->
                            new IllegalStateException(String.format(
                                    "Method with name '%s' is not unique in class '%s': %s",
                                    method, resource.getSimpleName(), found.stream()
                                            .map(method1 -> TypeToStringUtils.toStringMethod(method1, null))
                                            .collect(Collectors.joining(", "))))
                    );
            found.clear();
            found.add(single);
        }

        return findAnnotatedMethod(found.get(0));
    }

    /**
     * Search method, annotated with {@link jakarta.ws.rs.Path}. Jersey supports declaring annotations in
     * superclass or on interface, so the resource method may miss actual annotations.
     *
     * @param method method to search annotated for
     * @return annotated method
     * @throws java.lang.IllegalStateException if method with annotations not found
     */
    public static Method findAnnotatedMethod(final Method method) {
        Method res = null;
        // searching for annotated method (could be Path or method annotation)
        if (!isJerseyAnnotated(method)) {
            // try to search in superclasses and interfaces (for declaring class!)
            for (Class<?> type : GenericsResolver.resolve(method.getDeclaringClass())
                    .getGenericsInfo().getComposingTypes()) {
                for (Method cand : type.getDeclaredMethods()) {
                    // searching same method, but annotated
                    if (cand.getName().equals(method.getName())
                            && cand.getParameterTypes().length == method.getParameterTypes().length
                            // not count possible type differences
                            && Arrays.equals(cand.getParameterTypes(), method.getParameterTypes())
                            && isJerseyAnnotated(cand)) {
                        res = cand;
                        break;
                    }
                }
                if (res != null) {
                    break;
                }
            }
            Preconditions.checkState(res != null, "Annotated method %s was not found in class hierarchy of %s",
                    TypeToStringUtils.toStringMethod(method, null), method.getDeclaringClass().getSimpleName());
        } else {
            res = method;
        }
        return res;
    }

    /**
     * Check if provided method is annotated with jersey annotations. Http methods must have http method
     * annotation (like {@link jakarta.ws.rs.GET}), but may lack {@link jakarta.ws.rs.Path} annotation.
     * Sub-resource lookup method must have {@link Path annotation}. So target method must be checked to contain
     * one of possible annotations.
     * <p>
     * Note that it is impossible to have Path and http method annotation on different methods (jersey requirement).
     *
     * @param method method to check
     * @return true if method contains jersey annotations
     */
    public static boolean isJerseyAnnotated(final Method method) {
        return method.getAnnotation(Path.class) != null || getHttpMethod(method).isPresent();
    }

    /**
     * Resolve http method by searching for method annotations like {@link jakarta.ws.rs.GET} or
     * {@link jakarta.ws.rs.POST}.
     * <p>
     * WARNING: does not search the correct annotated method (see
     * {@link #findAnnotatedMethod(java.lang.reflect.Method)}) - assumed correct method was already found.
     *
     * @param method method to read annotations on
     * @return http method string (from method annotations)
     * @throws java.lang.IllegalStateException if annotation not found
     */
    public static String findHttpMethod(final Method method) {
        return getHttpMethod(method).orElseThrow(() -> new IllegalStateException(String.format(
                "Http method type annotation was not found on resource method: %s",
                TypeToStringUtils.toStringMethod(method, null)
        )));
    }

    /**
     * Search http annotations on method (like {@link jakarta.ws.rs.GET}).
     *
     * @param method method to find annotation on
     * @return http method or null
     */
    public static Optional<String> getHttpMethod(final Method method) {
        String httpMethod = null;
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().isAnnotationPresent(HttpMethod.class)) {
                httpMethod = ann.annotationType().getAnnotation(HttpMethod.class).value();
            }
        }
        return Optional.ofNullable(httpMethod);
    }

    /**
     * Use resource method call to collect data, required to call this rest method (like method path, query or path
     * params). Method associate provided method arguments with data required for call (like pre-declared
     * query or path params).
     * <p>
     * Provided consumer must call one resource method with any arguments:
     * {@code analyzeMethod(Res.class, Res mock -> mock.someMethod(12, "foo", null))}
     * where resource method is:
     * {@code Response someMethod(@QueryParam("p1") int p1, @PathParam("pp") String pp, @Context UriInfo info}.
     * From the call above we can:
     * <ul>
     *     <li>find {@link java.nio.file.Path} method annotation to get the path</li>
     *     <li>find http method annotation</li>
     *     <li>record required query param value: p1=12</li>
     *     <li>record required path param value: pp="foo"</li>
     * </ul>
     * <p>
     * Sub method calls assumed to be a sub-resource calls like {@code resource.subResource(args).method(args)}.
     * For sub-resource calls, returned info will contain root resource as base resource, but resource method
     * path will include all paths from locator methods (class-level {@link jakarta.ws.rs.Path} annotation is
     * ignored for sub-resources!).
     *
     * @param resource resource class
     * @param consumer consumer calling exactly one resource method
     * @param <T>      resource type
     * @return method analysis info
     */
    public static <T> ResourceMethodInfo analyzeMethodCall(final Class<T> resource, final Caller<T> consumer) {
        final List<MethodCall> methods = ResourceMethodLookup.getMethodCalls(resource, consumer);
        Preconditions.checkState(methods.size() == 1, "Only one resource method call is required, but %s recorded",
                methods.size());
        return analyzeMethodCall(methods.get(0));
    }

    /**
     * Analyze resource method, using method call arguments. Recognize query params, path params, headers, cookies,
     * etc. from method parameter annotations for non-null arguments.
     * <p>
     * Sub method calls assumed to be a sub-resource calls like {@code resource.subResource(args).method(args)}.
     * For sub-resource calls, returned info will contain root resource as base resource, but resource method
     * path will include all paths from locator methods (class-level {@link jakarta.ws.rs.Path} annotation is
     * ignored for sub-resources!).
     * <p>
     * Method call could be intercepted with
     * {@link ResourceMethodLookup#getMethodCalls(Class, ru.vyarus.dropwizard.guice.url.util.Caller)}.
     *
     * @param call intercepted method call
     * @return analysis result
     */
    public static ResourceMethodInfo analyzeMethodCall(final MethodCall call) {
        final List<ResourceMethodInfo> infos = new ArrayList<>();
        MethodCall current = call;
        while (current != null) {
            infos.add(analyzeSingleCall(current));
            current = current.getSubCall();
        }

        // simple case
        if (infos.size() == 1) {
            return infos.get(0);
        }

        // sub resource call

        // creating a result based on the last call (actual sub-resource method call)
        final ResourceMethodInfo last = infos.get(infos.size() - 1);
        final ResourceMethodInfo first = infos.get(0);
        // full path starting from the first resource (including all methods and sub resources)
        final List<String> resourcePath = new ArrayList<>();
        final List<Class<?>> subResources = new ArrayList<>();
        for (ResourceMethodInfo info : infos) {
            subResources.add(info.getResource());
            // only method path counted!
            resourcePath.add(info.getPath());
        }

        // consider sub resources as part of the method call (method call relative to the root resource)
        final ResourceMethodInfo res = new ResourceMethodInfo(first.getResource(),
                // root resource path
                first.getResourcePath(),
                last.getMethod(),
                // only lookup methods path is counted + actual resource method
                PathUtils.path(resourcePath.toArray(new String[0])),
                last.getHttpMethod(),
                // sub resource classes, excluding root resource
                subResources.subList(1, subResources.size()),
                infos);

        // apply data from left to right (right data should override left)
        infos.forEach(res::apply);

        return res;
    }

    /**
     * Analyze method call, ignoring sub calls {@link MethodCall#getSubCall()}.
     * See {@link #analyzeMethodCall(MethodCall)} for complete analysis.
     *
     * @param call method call
     * @return single call analysis info
     */
    public static ResourceMethodInfo analyzeSingleCall(final MethodCall call) {
        // jersey restriction: all annotations MUST be on the same method!
        // the resource might not contain annotation in case of sub-resource
        final Class<?> annotatedResource = getAnnotatedResource(call.getResource()).orElse(null);
        final Method annotated = findAnnotatedMethod(call.getMethod());
        final String path = getMethodPath(annotated);
        // sub-resource locator may lack http method annotation
        final String httpMethod = getHttpMethod(annotated).orElse(null);
        final ResourceMethodInfo info = new ResourceMethodInfo(
                MoreObjects.firstNonNull(annotatedResource, call.getResource()),
                annotatedResource == null ? "/" : getResourcePath(annotatedResource),
                annotated, path, httpMethod);

        detectMediaTypes(info, info.getResource(), annotated);

        // analyze parameters
        final Multimap<String, Object> multipart = ArrayListMultimap.create();
        for (int i = 0; i < call.getArgs().length; i++) {
            final Object arg = call.getArgs()[i];
            handle(annotated.getParameterAnnotations()[i], arg, info, multipart);
        }
        if (!multipart.isEmpty()) {
            // multipart params often doubled (stream with metadata) so need to process all at once
            MultipartParamsSupport.processFormParams(info.getFormParams(), multipart);
        }
        return info;
    }

    private static Optional<Class<?>> getAnnotatedResource(final Class<?> resource) {
        Path ann = resource.getAnnotation(Path.class);
        Class<?> res = resource;

        if (ann == null) {
            // try to search in superclasses and interfaces
            for (Class<?> type : GenericsResolver.resolve(resource).getGenericsInfo().getComposingTypes()) {
                ann = type.getAnnotation(Path.class);
                if (ann != null) {
                    res = type;
                    break;
                }
            }
        }
        return Optional.ofNullable(ann == null ? null : res);
    }

    private static void detectMediaTypes(final ResourceMethodInfo info,
                                         final Class<?> resource,
                                         final Method method) {
        Consumes consumes = method.getAnnotation(Consumes.class);
        Produces produces = method.getAnnotation(Produces.class);

        if (consumes == null) {
            consumes = resource.getAnnotation(Consumes.class);
        }
        if (produces == null) {
            produces = resource.getAnnotation(Produces.class);
        }

        if (consumes != null) {
            Collections.addAll(info.getConsumes(), consumes.value());
            // clear default
            info.getConsumes().remove("*/*");
        }
        if (produces != null) {
            Collections.addAll(info.getProduces(), produces.value());
            // clear default
            info.getProduces().remove("*/*");
        }
    }

    @SuppressWarnings({"unchecked", "PMD.NcssCount", "PMD.CognitiveComplexity", "PMD.CyclomaticComplexity",
            "checkstyle:CyclomaticComplexity", "checkstyle:JavaNCSS", "checkstyle:ExecutableStatementCount"})
    private static void handle(final Annotation[] annotations, final Object value, final ResourceMethodInfo info,
                               final Multimap<String, Object> multipart) {
        boolean recognized = false;
        if (value != null) {
            for (Annotation ann : annotations) {
                if (ann.annotationType().equals(Context.class)) {
                    // avoid handling special parameters
                    return;
                } else if (ann.annotationType().equals(PathParam.class)) {
                    final PathParam param = (PathParam) ann;
                    // path segments could be used for matrix params declaration in the middle
                    if (!(value instanceof PathSegment)) {
                        info.getPathParams().put(param.value(), value);
                    }
                    recognized = true;
                } else if (ann.annotationType().equals(QueryParam.class)) {
                    final QueryParam param = (QueryParam) ann;
                    info.getQueryParams().put(param.value(), value);
                    recognized = true;
                } else if (ann.annotationType().equals(HeaderParam.class)) {
                    final HeaderParam param = (HeaderParam) ann;
                    info.getHeaderParams().put(param.value(), value);
                    recognized = true;
                } else if (ann.annotationType().equals(MatrixParam.class)) {
                    final MatrixParam param = (MatrixParam) ann;
                    info.getMatrixParams().put(param.value(), value);
                    recognized = true;
                } else if (ann.annotationType().equals(FormParam.class)) {
                    final FormParam param = (FormParam) ann;
                    info.getFormParams().put(param.value(), value);
                    recognized = true;
                } else if (ann.annotationType().equals(CookieParam.class)) {
                    final CookieParam param = (CookieParam) ann;
                    info.getCookieParams().put(param.value(), String.valueOf(value));
                    recognized = true;
                } else if ("FormDataParam".equals(ann.annotationType().getSimpleName())) {
                    // collected for delayed processing
                    final String paramName = MultipartParamsSupport.getParamName(ann);
                    if (value instanceof Collection) {
                        ((Collection<?>) value).forEach(o -> multipart.put(paramName, o));
                    } else {
                        multipart.put(paramName, value);
                    }
                    recognized = true;
                } else if ("org.glassfish.jersey.media.multipart.FormDataMultiPart".equals(
                        value.getClass().getName())) {
                    // case: method parameter accept entire multipart (without direct fields mapping)
                    MultipartParamsSupport.configureFromMultipart(multipart, value);
                    recognized = true;
                } else if (ann.annotationType().equals(BeanParam.class)) {
                    handleBean(value, info, multipart);
                    recognized = true;
                }
            }
            // this could be urlencoded parameters
            if (value instanceof MultivaluedMap) {
                final MultivaluedMap<Object, Object> map = (MultivaluedMap<Object, Object>) value;
                map.forEach((o, objects) ->
                        info.getFormParams().put(String.valueOf(o), objects.size() == 1 ? objects.get(0) : objects)
                );
                recognized = true;
            }
            if (!recognized) {
                // assume it is a method body (e.g. POST entity).
                // The logic: if used provided not null value then it should be used in request
                Preconditions.checkState(info.getEntity() == null, "Multiple entity arguments detected: \n\t%s\n\t%s",
                        info.getEntity(), value);
                info.setEntity(value);
            }
        }
    }

    private static void handleBean(final Object bean, final ResourceMethodInfo info,
                                   final Multimap<String, Object> multipart) {
        final Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            final Object value;
            try {
                value = field.get(bean);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to introspect @BeanParam", e);
            }
            if (value != null) {
                handle(field.getAnnotations(), value, info, multipart);
            }
        }
    }
}

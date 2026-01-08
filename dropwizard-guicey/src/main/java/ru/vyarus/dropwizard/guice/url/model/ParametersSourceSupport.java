package ru.vyarus.dropwizard.guice.url.model;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.glassfish.jersey.model.Parameter;
import org.jspecify.annotations.Nullable;
import ru.vyarus.dropwizard.guice.url.model.param.ParameterSource;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Jersey parameters-related storage for resource method analysis
 * {@link ru.vyarus.dropwizard.guice.url.model.ResourceMethodInfo}.
 * <p>
 * Jersey {@link org.glassfish.jersey.model.Parameter} creation mechanism is used to later apply registered
 * {@link jakarta.ws.rs.ext.ParamConverter} (for toString). An additional wrapper {@link ParameterSource} is used
 * because jersey parameter does not store bean param fields. Also, in case of sub resources, parameters from
 * multiple methods could be stored - source object contains source resource class and method to simplify
 * differentiation.
 * <p>
 * Multiple form parameters might be declared in a single argument (using map or generic multipart entity).
 * In such cases each parameter would have separate source (pointing to the same argument).
 * <p>
 * It might not seem logical to store parameters separately from actual values, but it is an additional data
 * layer, used only for conversion - otherwise it would be more complicated to traverse parameters.
 * Also, in case of multipart, it is a not one to one mapping as often several multipart parameters used
 * for handling (metadata and stream).
 * <p>
 * Overall, parameters represent declaration source for detected parameters. To avoid complications, it is separated
 * into its own layer.
 *
 * @author Vyacheslav Rusakov
 * @since 21.12.2025
 */
public class ParametersSourceSupport {

    /**
     * Parameter declaration source. Key consists of parameterSource_parameterName (because names from different
     * sources potentially could collide).
     * Multimap used because multipart declarations support multiple sources.
     */
    protected final Multimap<String, ParameterSource> parameterSources = LinkedHashMultimap.create();

    /**
     * Most parameters represented with a single source, except some multipart declarations.
     * Often 2 arguments used for a single parameter: content disposition and input stream and so two sources would
     * be stored for a single field.
     *
     * @param source source type (path param, query param etc.)
     * @param name   parameter name
     * @return parameter source declaration (first declaration if multiple sources registered)
     */
    @Nullable
    public ParameterSource getParameterSource(final Parameter.Source source, final String name) {
        final String key = getSourceParameterKey(source, name);
        return parameterSources.containsKey(key) ? parameterSources.get(key).iterator().next() : null;
    }

    /**
     * Shortcut for parameters selection.
     * <p>
     * Note: when there are sub-resource method calls involved, the resulting return object would contain all sources!
     * But parameters could be distinguished easily by source object as it contains class and method.
     *
     * @param filter filter
     * @return selected sources
     */
    public List<ParameterSource> selectParameterSources(final Predicate<ParameterSource> filter) {
        return parameterSources.values().stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Makes sense to use only for multipart parameters which support multiple parameters declaration for a single
     * entry (input source and metadata). In all other case there should be only one source (the opposite is possible
     * only in case of incorrect declaration).
     *
     * @param source source type (path param, query param etc.)
     * @param name   parameter name
     * @return parameter source declaration
     */
    public Collection<ParameterSource> getParameterSources(final Parameter.Source source, final String name) {
        return parameterSources.get(getSourceParameterKey(source, name));
    }

    /**
     * Shortcut for path parameter source access.
     *
     * @param name parameter name
     * @return path parameter declaration source (method argument or bean field)
     */
    public ParameterSource getPathParamSource(final String name) {
        return getParameterSource(Parameter.Source.PATH, name);
    }

    /**
     * Shortcut for query parameter source access.
     *
     * @param name parameter name
     * @return query parameter declaration source (method argument or bean field)
     */
    public ParameterSource getQueryParamSource(final String name) {
        return getParameterSource(Parameter.Source.QUERY, name);
    }

    /**
     * Shortcut for header parameter source access.
     *
     * @param name parameter name
     * @return header parameter declaration source (method argument or bean field)
     */
    public ParameterSource getHeaderParamSource(final String name) {
        return getParameterSource(Parameter.Source.HEADER, name);
    }

    /**
     * Shortcut for matrix parameter source access.
     *
     * @param name parameter name
     * @return matrix parameter declaration source (method argument or bean field)
     */
    public ParameterSource getMatrixParamSource(final String name) {
        return getParameterSource(Parameter.Source.MATRIX, name);
    }

    /**
     * Shortcut for cookie parameter source access.
     *
     * @param name parameter name
     * @return cookie parameter declaration source (method argument or bean field)
     */
    public ParameterSource getCookieParamSource(final String name) {
        return getParameterSource(Parameter.Source.COOKIE, name);
    }

    /**
     * Returns form parameter source. When multiple sources available for parameter (often in multipart forms),
     * the first one would be returned.
     *
     * @param name form parameter name
     * @return parameter source
     */
    public ParameterSource getFormParamSource(final String name) {
        return getParameterSource(Parameter.Source.FORM, name);
    }

    /**
     * Returns all collected form parameters for a single name. Multiple sources may appear only for multipart
     * parameters: metadata and input stream could be declared in separate arguments.
     *
     * @param name parameter name
     * @return form parameter sources
     */
    public Collection<ParameterSource> getFormParamSources(final String name) {
        return getParameterSources(Parameter.Source.FORM, name);
    }

    /**
     * Shortcut for entity parameter source access.
     *
     * @return entity parameter declaration source (method argument or bean field)
     */
    public ParameterSource getEntityParamSource() {
        final List<ParameterSource> res = selectParameterSources(source -> source
                .getType().equals(Parameter.Source.ENTITY));
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * Register new parameter source.
     *
     * @param parameter parameter source
     */
    public void addParameterSource(final ParameterSource parameter) {
        final String key = getSourceParameterKey(parameter.getType(), parameter.getName());
        parameterSources.put(key, parameter);
    }

    private String getSourceParameterKey(final Parameter.Source source, final String name) {
        return source.name() + "_" + name;
    }
}

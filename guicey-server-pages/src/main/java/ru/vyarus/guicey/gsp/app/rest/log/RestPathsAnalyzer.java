package ru.vyarus.guicey.gsp.app.rest.log;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.guicey.gsp.views.template.Template;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Collects template resources for logging. Implementation is a copy of dropwizard's own logging from
 * {@link DropwizardResourceConfig} (which can't be used directly).
 *
 * @author Vyacheslav Rusakov
 * @since 06.12.2018
 */
public class RestPathsAnalyzer {

    private static final TypeResolver TYPE_RESOLVER = new TypeResolver();
    private final Set<ViewPath> paths = new HashSet<>();

    /**
     * Collects all registered template resource paths for console logging.
     *
     * @param config dropwizard resources configuration object
     * @return analyzer instance with all found template resources
     */
    public static RestPathsAnalyzer build(final DropwizardResourceConfig config) {
        final RestPathsAnalyzer analyzer = new RestPathsAnalyzer();
        for (Class<?> cls : config.getClasses()) {
            if (!cls.isAnnotationPresent(Template.class)) {
                continue;
            }
            final Resource resource = Resource.from(cls);
            // other template resources will be processed by other applications or not used at all
            if (resource != null) {
                populate("/", cls, false, resource, analyzer.paths);
            }
        }
        // manually added resources
        for (Resource resource : config.getResources()) {
            for (Resource childRes : resource.getChildResources()) {
                for (Class<?> childResHandlerClass : childRes.getHandlerClasses()) {
                    if (childResHandlerClass.isAnnotationPresent(Template.class)) {
                        populate(resource.getPath(), childResHandlerClass, false, childRes, analyzer.paths);
                    }
                }
            }
        }
        return analyzer;
    }

    /**
     * @param app application name
     * @return rest paths of required app
     */
    public Set<ViewPath> select(final String app) {
        final Set<ViewPath> res = new TreeSet<>();
        final String prefix = PathUtils.leadingSlash(PathUtils.trailingSlash(app));
        for (ViewPath path : paths) {
            if (path.getUrl().startsWith(prefix)) {
                res.add(path);
            }
        }
        return res;
    }

    private static void populate(final String rootPath,
                                 final Class<?> klass,
                                 final boolean isLocator,
                                 final Resource resource,
                                 final Set<ViewPath> handles) {
        String basePath = rootPath;
        if (!isLocator) {
            basePath = PathUtils.path(rootPath, resource.getPath());
        }

        for (ResourceMethod method : resource.getResourceMethods()) {
            // map direct resource methods
            handles.add(new ViewPath(method, resource, klass, basePath));
        }

        for (Resource childResource : resource.getChildResources()) {
            for (ResourceMethod method : childResource.getAllMethods()) {
                if (method.getType() == ResourceMethod.JaxrsType.RESOURCE_METHOD) {
                    final String path = PathUtils.path(basePath, childResource.getPath());
                    handles.add(new ViewPath(method, childResource, klass, path));
                } else if (method.getType() == ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR) {
                    final String path = PathUtils.path(basePath, childResource.getPath());
                    final ResolvedType responseType = TYPE_RESOLVER
                            .resolve(method.getInvocable().getResponseType());
                    final Class<?> erasedType = !responseType.getTypeBindings().isEmpty()
                            ? responseType.getTypeBindings().getBoundType(0).getErasedType()
                            : responseType.getErasedType();
                    final Resource erasedTypeResource = Resource.from(erasedType);
                    if (erasedTypeResource == null) {
                        handles.add(new ViewPath(method, childResource, erasedType, path));
                    } else {
                        populate(path, erasedType, true, erasedTypeResource, handles);
                    }
                }
            }
        }
    }

}

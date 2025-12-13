package ru.vyarus.dropwizard.guice.test.responsemodel.intercept;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.ext.Provider;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.test.responsemodel.model.ResponseModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Filter applied to all resources to intercept response object before its post-processing. For views, it means
 * intercept view instance before template rendering.
 * Intercepted objects could be accessed with  {@link ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker},
 * available for direct injection.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
@Provider
public class ResponseInterceptorFilter implements ContainerResponseFilter {

    @Inject
    private jakarta.inject.Provider<ResourceInfo> resourceInfo;
    private final boolean interceptErrors;

    private final List<ResponseModel> interceptedModels = new CopyOnWriteArrayList<>();

    /**
     * Create a filter instance.
     *
     * @param interceptErrors true to intercept error pages model
     */
    public ResponseInterceptorFilter(final boolean interceptErrors) {
        this.interceptErrors = interceptErrors;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) throws IOException {
        final Object response = responseContext.getEntity();
        if (response != null && (interceptErrors || responseContext.getStatus() < 400)) {
            final ResponseModel model = new ResponseModel();
            model.setResourcePath(PathUtils.leadingSlash(requestContext.getUriInfo().getPath()));
            model.setHttpMethod(requestContext.getRequest().getMethod());
            model.setStatusCode(responseContext.getStatus());

            final ResourceInfo info = resourceInfo.get();
            model.setResourceClass(info.getResourceClass());
            model.setResourceMethod(info.getResourceMethod());

            model.setModel(response);
            interceptedModels.add(model);
        }
    }

    /**
     * Note: the entire storage list is returned, so data could be cleared.
     *
     * @return all intercepted models
     */
    public List<ResponseModel> getInterceptedModels() {
        return interceptedModels;
    }
}

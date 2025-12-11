package ru.vyarus.guicey.gsp.views.test.ext.interceptor;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.ext.Provider;
import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;
import ru.vyarus.guicey.gsp.views.test.ext.ViewModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Filter applied to all GSP view resources annotated with {@link ru.vyarus.guicey.gsp.views.template.Template}.
 * Intercepts view response before actual HTML rendering to store raw model. Model would be available through
 * {@link ru.vyarus.guicey.gsp.views.test.ext.ViewModelTracker} object.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
@Provider
@Template
public class ViewModelFilter implements ContainerResponseFilter {

    @Inject
    private jakarta.inject.Provider<ResourceInfo> resourceInfo;
    private final boolean interceptErrors;

    private final List<ViewModel> interceptedModels = new CopyOnWriteArrayList<>();

    /**
     * Create a filter instance.
     *
     * @param interceptErrors true to intercept error pages model
     */
    public ViewModelFilter(final boolean interceptErrors) {
        this.interceptErrors = interceptErrors;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) throws IOException {
        final Object response = responseContext.getEntity();
        if (response != null && (interceptErrors || responseContext.getStatus() < 400)) {
            final ViewModel model = new ViewModel();
            // real resource path (but client called different url - rest was mapped)
            model.setResourcePath(requestContext.getUriInfo().getPath());
            model.setHttpMethod(requestContext.getRequest().getMethod());
            model.setStatusCode(responseContext.getStatus());

            final ResourceInfo info = resourceInfo.get();
            model.setResourceClass(info.getResourceClass());
            model.setResourceMethod(info.getResourceMethod());

            model.setModel(response);

            // original request uri
            model.setPath(TemplateContext.getInstance().getRequest().getRequestURI());
            interceptedModels.add(model);
        }
    }

    /**
     * Note: the entire storage list is returned, so data could be cleared.
     *
     * @return all intercepted models
     */
    public List<ViewModel> getInterceptedModels() {
        return interceptedModels;
    }
}

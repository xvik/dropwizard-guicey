package ru.vyarus.guicey.gsp.app.rest.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.gsp.views.template.ManualErrorHandling;
import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Request filter for {@link Template} annotated resources read configured template path (to be used in model).
 * Record matched resource class so relative templates could be checked relative to class even
 * when template path is specified directly into model.
 *
 * @author Vyacheslav Rusakov
 * @since 03.12.2018
 */
@Template
@Singleton
@Provider
public class TemplateAnnotationFilter implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(TemplateAnnotationFilter.class);

    @Inject
    private jakarta.inject.Provider<ResourceInfo> info;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final ResourceInfo resourceInfo = info.get();
        final Class<?> resourceClass = resourceInfo.getResourceClass();
        final Template template = resourceClass.getAnnotation(Template.class);
        if (template != null) {
            final TemplateContext context = TemplateContext.getInstance();
            // remember resource class to check relative templates
            context.setResourceClass(resourceClass);
            final String tpl = template.value();
            // could be empty when annotation used for marking resource only
            if (!tpl.isEmpty()) {
                context.setAnnotationTemplate(tpl);
                logger.debug("View template declared in annotation: {} ({})", tpl, resourceClass.getSimpleName());
            }
            final Method method = resourceInfo.getResourceMethod();
            context.setManualErrorHandling(resourceClass.isAnnotationPresent(ManualErrorHandling.class)
                    || (method != null && method.isAnnotationPresent(ManualErrorHandling.class)));
        }
    }
}

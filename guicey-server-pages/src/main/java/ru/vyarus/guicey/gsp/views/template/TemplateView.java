package ru.vyarus.guicey.gsp.views.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Charsets;
import io.dropwizard.views.common.View;

import javax.annotation.Nullable;
import java.nio.charset.Charset;

/**
 * View template rendering model. Must be used as base class for models instead of pure {@link View}.
 * <p>
 * Template name may be specified directly (within constructor) or automatically detected from {@link Template}
 * resource annotation. If template path starts with "/" it's considered absolute and searched directly
 * within classpath, otherwise template is considered relative to one of configured classpath locations.
 * Note that {@link Template} annotation defines templates relative to annotated class.
 * <p>
 * For error pages use {@link ErrorTemplateView} class.
 *
 * @author Vyacheslav Rusakov
 * @since 22.10.2018
 */
public class TemplateView extends View {

    private final TemplateContext context;


    /**
     * Template obtained from {@link Template} annotation on resource.
     */
    public TemplateView() {
        this(null);
    }

    /**
     * If template name is null, it will be obtained from {@link Template} annotation on resource.
     *
     * @param templatePath template path or null (to use annotation value)
     */
    public TemplateView(@Nullable final String templatePath) {
        this(templatePath, Charsets.UTF_8);
    }

    /**
     * If template name is null, it will be obtained from {@link Template} annotation on resource.
     *
     * @param templatePath template path or null (to use annotation value)
     * @param charset      charset or null
     */
    public TemplateView(@Nullable final String templatePath, @Nullable final Charset charset) {
        // template could be either absolute or relative
        super(TemplateContext.getInstance().lookupTemplatePath(templatePath), charset);
        this.context = TemplateContext.getInstance();
    }

    /**
     * Note that this object is the only way to get original request path because templates are always rendered
     * in rest endpoints after server redirect.
     *
     * @return additional info about current template.
     */
    @JsonIgnore
    public TemplateContext getContext() {
        return context;
    }

}

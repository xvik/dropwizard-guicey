package ru.vyarus.guicey.gsp.views.template;

import ru.vyarus.guicey.gsp.app.rest.support.TemplateAnnotationFilter;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declare template file in classpath, relative to current class or any registered root templates dir (in classpath).
 * <p>
 * It is required to use this annotation on template resources. Even if each method in resource use its own template,
 * resource class must be annotated with empty annotation.
 * <p>
 * Template path could also be absolute (in this case it must start with "/" - classpath root).
 * <p>
 * Annotation is also {@link NameBinding}, which allows easily apply filters (
 * {@link javax.ws.rs.container.ContainerRequestFilter} and {@link javax.ws.rs.container.ContainerResponseFilter})
 * only for template resources (see {@link TemplateAnnotationFilter} as example).
 *
 * @author Vyacheslav Rusakov
 * @since 03.12.2018
 */
@NameBinding
@Retention(RUNTIME)
@Target(TYPE)
public @interface Template {

    /**
     * @return template path, relative to annotated class or absolute path
     */
    String value() default "";
}

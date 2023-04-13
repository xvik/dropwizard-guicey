package ru.vyarus.dropwizard.guice.module.installer.feature.web;

import java.lang.annotation.*;

/**
 * Used together with {@link jakarta.servlet.annotation.WebServlet},
 * {@link jakarta.servlet.annotation.WebFilter} and {@link jakarta.servlet.annotation.WebListener} annotations
 * to specify target context.
 * <p>
 * By default, web extensions target main context. Adding {@code @AdminContext} will mean registration
 * in admin context. Using {@code @AdminContext(andMain = true)} will mean installation to both contexts.
 *
 * @author Vyacheslav Rusakov
 * @since 06.08.2016
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminContext {

    /**
     * Annotation presence switches publication to admin context. If both extensions must be installed in
     * both contexts use this option.
     *
     * @return true to also install extension to main context, false for only admin context.
     */
    boolean andMain() default false;
}

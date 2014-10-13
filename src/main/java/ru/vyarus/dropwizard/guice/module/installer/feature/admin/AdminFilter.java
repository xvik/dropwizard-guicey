package ru.vyarus.dropwizard.guice.module.installer.feature.admin;

import java.lang.annotation.*;

/**
 * Annotate filter to map it into admin context.
 * Either servlet names or uri patterns must be specified for mapping.
 *
 * @author Vyacheslav Rusakov
 * @since 13.10.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AdminFilter {

    /**
     * @return filter name
     */
    String name();

    /**
     * @return name of servlets to map filter on
     */
    String[] servlets() default { };

    /**
     * @return mapping patterns
     */
    String[] patterns() default { };
}

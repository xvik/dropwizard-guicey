package ru.vyarus.dropwizard.guice.module.installer.feature.admin;

import java.lang.annotation.*;

/**
 * Annotate servlet to map it into admin context.
 *
 * @author Vyacheslav Rusakov
 * @since 13.10.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AdminServlet {

    /**
     * @return servlet name
     */
    String name();

    /**
     * @return mapping patterns
     */
    String[] patterns();
}

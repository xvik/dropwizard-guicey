package ru.vyarus.guicey.admin.rest;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation to deny access to admin specific rest from user context.
 * Annotation may be used on resource class to hide all resource methods or directly
 * on methods (for hybrid cases).
 * <p>
 * When secured resource is accessed from user context, 403 error will be returned.
 * <p>
 * Requires {@link ru.vyarus.guicey.admin.AdminRestBundle} to be registered, otherwise will not
 * have any effect.
 *
 * @author Vyacheslav Rusakov
 * @since 04.08.2015
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AdminResource {
}

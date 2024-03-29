package ru.vyarus.guicey.validation.util;

import com.google.inject.matcher.Matcher;
import jakarta.ws.rs.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Matcher denies methods annotated with jax-rs annotations. This is required to avoid
 * duplicate validations (because dropwizard already applies validations on rest).
 * <p>
 * Normally, all rest resources are annotated with {@link jakarta.ws.rs.Path} so it is easy to filter all rest classes.
 * This matcher is required only for complex declaration cases.
 *
 * @author Vyacheslav Rusakov
 * @since 26.12.2019
 */
public class RestMethodMatcher implements Matcher<Method> {

    @Override
    public boolean matches(final Method method) {
        boolean res = false;
        for (Annotation ann : method.getDeclaredAnnotations()) {
            // found @GET, @POST, etc. annotated method
            if (ann.annotationType().getDeclaredAnnotation(HttpMethod.class) != null) {
                res = true;
                break;
            }
        }
        return res;
    }
}

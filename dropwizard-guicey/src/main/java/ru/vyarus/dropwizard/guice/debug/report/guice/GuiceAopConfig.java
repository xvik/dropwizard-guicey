package ru.vyarus.dropwizard.guice.debug.report.guice;

import com.google.inject.matcher.Matcher;
import org.aopalliance.intercept.MethodInterceptor;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.NonObjectMethodMatcher;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Guice AOP report configuration. Used to reduce report to only interesting parts.
 *
 * @author Vyacheslav Rusakov
 * @since 23.08.2019
 */
public class GuiceAopConfig {

    private boolean hideDeclarations;
    private Matcher<? super Class<?>> typeMatcher;
    // hide Object methods by default
    private Matcher<? super Method> methodMatcher = new NonObjectMethodMatcher();
    private final Set<Class<? extends MethodInterceptor>> showOnly = new HashSet<>();

    /**
     * Hide available interceptors block.
     *
     * @return config instance for chained calls
     */
    public GuiceAopConfig hideDeclarationsBlock() {
        hideDeclarations = true;
        return this;
    }

    /**
     * Filter bindings by type.
     *
     * @param matcher type matcher
     * @return config instance for chained calls
     */
    public GuiceAopConfig types(final Matcher<? super Class<?>> matcher) {
        typeMatcher = matcher;
        return this;
    }

    /**
     * Filter methods.
     *
     * @param matcher method matcher
     * @return config instance for chained calls
     */
    public GuiceAopConfig methods(final Matcher<? super Method> matcher) {
        methodMatcher = matcher;
        return this;
    }

    /**
     * Filter by interceptor (show only methods affected with this interceptor).
     *
     * @param interceptors interceptors to show
     * @return config instance for chained calls
     */
    @SafeVarargs
    public final GuiceAopConfig interceptors(final Class<? extends MethodInterceptor>... interceptors) {
        Collections.addAll(showOnly, interceptors);
        return this;
    }

    /**
     * @return true if declaration block must not be shown
     */
    public boolean isHideDeclarationsBlock() {
        return hideDeclarations;
    }

    /**
     * @return configured type matcher or null
     */
    public Matcher<? super Class<?>> getTypeMatcher() {
        return typeMatcher;
    }

    /**
     * @return configured method matcher or null
     */
    public Matcher<? super Method> getMethodMatcher() {
        return methodMatcher;
    }

    /**
     * @return list of interceptor classes to show or empty list
     */
    public Set<Class<? extends MethodInterceptor>> getShowOnly() {
        return showOnly;
    }
}

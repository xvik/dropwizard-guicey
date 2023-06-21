package ru.vyarus.guicey.jdbi3.module;

import com.google.inject.matcher.Matcher;

import java.lang.reflect.Method;

/**
 * Matcher to filter synthetic methods (to avoid warnings on aop proxies creation).
 *
 * @author Vyacheslav Rusakov
 * @since 17.09.2018
 */
public class NoSyntheticMatcher implements Matcher<Method> {

    private static final NoSyntheticMatcher NO_SYNTHETIC_MATCHER = new NoSyntheticMatcher();

    /**
     * @return method matcher for filtering synthetic methods
     */
    public static final NoSyntheticMatcher instance() {
        return NO_SYNTHETIC_MATCHER;
    }

    @Override
    public boolean matches(final Method method) {
        return !method.isSynthetic();
    }
}

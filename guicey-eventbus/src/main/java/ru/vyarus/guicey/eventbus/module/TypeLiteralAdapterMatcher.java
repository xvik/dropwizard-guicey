package ru.vyarus.guicey.eventbus.module;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;

/**
 * Wrapper for class matcher to be used for matching type literals.
 *
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
public class TypeLiteralAdapterMatcher implements Matcher<TypeLiteral<?>> {
    private final Matcher<? super Class<?>> classMatcher;

    public TypeLiteralAdapterMatcher(final Matcher<? super Class<?>> classMatcher) {
        this.classMatcher = classMatcher;
    }

    @Override
    public boolean matches(final TypeLiteral<?> typeLiteral) {
        return classMatcher.matches(typeLiteral.getRawType());
    }

    @Override
    public Matcher<TypeLiteral<?>> and(final Matcher<? super TypeLiteral<?>> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Matcher<TypeLiteral<?>> or(final Matcher<? super TypeLiteral<?>> other) {
        throw new UnsupportedOperationException();
    }
}

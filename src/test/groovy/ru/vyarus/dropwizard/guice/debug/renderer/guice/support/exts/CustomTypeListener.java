package ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * @author Vyacheslav Rusakov
 * @since 14.09.2019
 */
public class CustomTypeListener implements TypeListener {
    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
    }
}
package ru.vyarus.dropwizard.guice.debug.renderer.guice.support;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeConverter;

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class ConstantModule extends AbstractModule {

    @Override
    protected void configure() {
        convertToTypes(new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            public boolean matches(TypeLiteral<?> typeLiteral) {
                return typeLiteral.getType().equals(Sample.class);
            }
        }, new TypeConverter() {
            @Override
            public Object convert(String value, TypeLiteral<?> toType) {
                return new Sample();
            }
        });
        bindConstant().annotatedWith(Names.named("smth")).to(12);
        bindConstant().annotatedWith(Names.named("string")).to("12");
    }

    public static class Sample {}
}

package ru.vyarus.dropwizard.guice.support.provider.paramconv

import jakarta.ws.rs.ext.ParamConverter
import jakarta.ws.rs.ext.ParamConverterProvider
import jakarta.ws.rs.ext.Provider
import java.lang.annotation.Annotation
import java.lang.reflect.Type

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@Provider
class FooParamConverter implements ParamConverterProvider{

    @Override
    def <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (Foo.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) new FooConverter()
        }
        return null
    }

    private static class FooConverter implements ParamConverter<Foo> {
        @Override
        Foo fromString(String value) {
            return new Foo(value: value)
        }

        @Override
        String toString(Foo value) {
            return value.value
        }
    }
}

package ru.vyarus.dropwizard.guice.cases.hkscope.support

import javax.ws.rs.ext.ParamConverter
import javax.ws.rs.ext.ParamConverterProvider
import javax.ws.rs.ext.Provider
import java.lang.annotation.Annotation
import java.lang.reflect.Type

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
class GuiceParamConverterProvider implements ParamConverterProvider {

    @Override
    def <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        return null
    }
}

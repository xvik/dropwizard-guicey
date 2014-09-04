package ru.vyarus.dropwizard.guice.support.feature.abstr

import com.sun.jersey.spi.inject.InjectableProvider

import javax.ws.rs.core.Context
import java.lang.reflect.Type

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
abstract class AbstractJerseyInjectableProvider implements InjectableProvider<Context, Type>{
}

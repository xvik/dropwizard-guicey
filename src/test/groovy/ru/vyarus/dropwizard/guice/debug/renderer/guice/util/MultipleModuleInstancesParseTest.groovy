package ru.vyarus.dropwizard.guice.debug.renderer.guice.util

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.name.Names
import com.google.inject.spi.Elements
import ru.vyarus.dropwizard.guice.debug.report.guice.model.ModuleDeclaration
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelParser
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 09.09.2019
 */
class MultipleModuleInstancesParseTest extends Specification {

    def "Check multiple modules analysis"() {

        when: "multiple modules of the same type"
        def modules = [new Module(new Ext()), new Module(new Ext())]
        Injector injector = Guice.createInjector(modules)
        List<ModuleDeclaration> res = GuiceModelParser.parse(injector, Elements.getElements(modules))

        then: "bindings correctly analyzed"
        res.size() == 1
        with(res[0]) {
            declarations.size() == 2
        }
    }

    static class Module extends AbstractModule{
        Ext ext

        Module(Ext ext) {
            this.ext = ext
        }

        @Override
        protected void configure() {
            bind(Ext).annotatedWith(Names.named(ext.toString())).toInstance(ext)
        }
    }

    static class Ext { }
}

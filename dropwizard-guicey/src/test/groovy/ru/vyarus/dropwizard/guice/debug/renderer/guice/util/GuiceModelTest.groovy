package ru.vyarus.dropwizard.guice.debug.renderer.guice.util

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.PrivateModule
import com.google.inject.spi.ConstructorBinding
import com.google.inject.spi.Element
import com.google.inject.spi.Elements
import com.google.inject.spi.ProviderBinding
import com.google.inject.spi.ScopeBinding
import ru.vyarus.dropwizard.guice.debug.report.guice.model.BindingDeclaration
import ru.vyarus.dropwizard.guice.debug.report.guice.model.DeclarationType
import ru.vyarus.dropwizard.guice.debug.report.guice.model.ModuleDeclaration
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelParser
import spock.lang.Specification

import jakarta.inject.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 09.09.2019
 */
class GuiceModelTest extends Specification {

    def "Check declaration type detection"() {

        expect: "correctly recognized"
        DeclarationType.detect(ScopeBinding) == DeclarationType.Scope
        DeclarationType.detect(ConstructorBinding) == DeclarationType.Binding
        DeclarationType.detect(ProviderBinding) == null
    }

    def "Check module model"() {

        when: "two instances for same module"
        ModuleDeclaration mod1 = new ModuleDeclaration(type: Module)
        ModuleDeclaration mod2 = new ModuleDeclaration(type: Module)

        then: "objects equal"
        mod1.equals(mod2)
        mod1.hashCode() == mod2.hashCode()
        mod1.toString() == mod2.toString()
    }

    def "Check declaration model to string"() {

        when: "unbounded declaration"
        List<Element> res = Elements.getElements(new Module())
        BindingDeclaration dec = new BindingDeclaration(DeclarationType.LinkedKey, res[0])
        then: "simple to string"
        dec.toString() == "linkedkey -"

        when: "add key"
        dec.setKey(Key.get(Ext))
        then: "more info"
        dec.toString() == "linkedkey Ext"

        when: "add module"
        dec.setModule(Module.name)
        then: "full"
        dec.toString() == "linkedkey Ext (from module $Module.name)"

        when: "module declaration"
        ModuleDeclaration mod = new ModuleDeclaration(type: Module)
        then: "to string"
        mod.toString() == "Module"

    }

    def "Check declaration model"() {

        when: "two instances for same element"
        List<Element> res = Elements.getElements(new Module())
        BindingDeclaration dec1 = new BindingDeclaration(DeclarationType.LinkedKey, res[0])
        BindingDeclaration dec2 = new BindingDeclaration(DeclarationType.LinkedKey, res[0])

        then: "object equal"
        dec1.equals(dec2)
        dec1.hashCode() == dec2.hashCode()
        dec1.toString() == dec2.toString()
    }

    def 'Check parsed model correctness'() {

        when: "Parsing model"
        def module = new Module()
        Injector injector = Guice.createInjector(module)
        List<ModuleDeclaration> res = GuiceModelParser.parse(injector, Elements.getElements(module))

        then: "one root module"
        res.size() == 1
        with(res[0]) {
            type == Module
            parent == null
            children.size() == 2
            declarations.size() == 1
            markers.isEmpty()
            !privateModule
        }

        and: "one private"
        with(res[0].children[0]) {
            privateModule
            type == HiddenModule
            parent == Module.name
            declarations.size() ==  2
        }

        and: "one sub"
        with(res[0].children[1]) {
            !privateModule
            type == SubModule
            parent == Module.name
            declarations.size() ==  1
        }
    }

    static class Module extends AbstractModule {
        @Override
        protected void configure() {
            bind(Ext).to(ExtImpl)
            install(new HiddenModule())
            install(new SubModule())
        }
    }

    static interface Ext {}

    static class ExtImpl implements Ext {}

    static class HiddenModule extends PrivateModule {
        @Override
        protected void configure() {
            bind(HiddenExt)
            expose(HiddenExt)
        }
    }

    static class HiddenExt {}

    static class SubModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(SubExt).toProvider(new Provider<SubExt>() {
                @Override
                SubExt get() {
                    return new SubExt()
                }
            })
        }
    }

    static class SubExt {}
}

package ru.vyarus.dropwizard.guice.debug.renderer.guice.util

import com.google.inject.*
import com.google.inject.servlet.RequestScoped
import com.google.inject.servlet.ServletModule
import com.google.inject.servlet.ServletScopes
import com.google.inject.servlet.SessionScoped
import com.google.inject.spi.Element
import com.google.inject.spi.Elements
import ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor.GuiceScopingVisitor
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.module.support.scope.Prototype
import spock.lang.Specification

import javax.inject.Singleton
import java.lang.annotation.Annotation

/**
 * @author Vyacheslav Rusakov
 * @since 09.09.2019
 */
class ScopingVisitorTest extends Specification {

    GuiceScopingVisitor visitor = new GuiceScopingVisitor()

    def "Check scope detection"() {

        when: "parse injector"
        Injector injector = Guice.createInjector(new Module())
        then: "scoped correctly detected"
        scope(injector, Eager) == EagerSingleton
        scope(injector, Single) == Singleton
        scope(injector, Single2) == Singleton
        scope(injector, Proto) == Prototype
        scope(injector, NoScope) == Prototype
        scope(injector, NoScope2) == Prototype
        scope(injector, Req) == RequestScoped
        scope(injector, Sess) == SessionScoped

        scope(injector, SingAnn) == Singleton
        scope(injector, SingAnn2) == Singleton
        scope(injector, ProtAnn) == Prototype
        scope(injector, ReqAnn) == RequestScoped
        scope(injector, SessAnn) == SessionScoped
    }

    def "Check declaration scope detection"() {
        // this is never used, but just to see how it will work in this case
        when: "parse elements"
        List<Element> elements = Elements.getElements(new Module())
        then: "scoped correctly detected"
        scope(elements, Eager) == EagerSingleton
        scope(elements, Single) == Singleton
        scope(elements, Single2) == Singleton
        scope(elements, Proto) == Prototype
        scope(elements, NoScope) == Prototype
        scope(elements, NoScope2) == Prototype
        scope(elements, Req) == RequestScoped
        scope(elements, Sess) == SessionScoped

        // guice dont lookup annotations for elements
        scope(elements, SingAnn) == Prototype
        scope(elements, SingAnn2) == Prototype
        scope(elements, ProtAnn) == Prototype
        scope(elements, ReqAnn) == Prototype
        scope(elements, SessAnn) == Prototype
    }

    def "Check direct visitor cases"() {

        expect: "correct scope annotations"
        visitor.visitNoScoping() == null
        visitor.visitEagerSingleton() == EagerSingleton

        visitor.visitScope(Scopes.SINGLETON) == Singleton
        visitor.visitScope(Scopes.NO_SCOPE) == Prototype
        visitor.visitScope(ServletScopes.REQUEST) == RequestScoped
        visitor.visitScope(ServletScopes.SESSION) == SessionScoped

        visitor.visitScopeAnnotation(Singleton) == Singleton
        visitor.visitScopeAnnotation(com.google.inject.Singleton) == Singleton
        visitor.visitScopeAnnotation(Prototype) == Prototype
        visitor.visitScopeAnnotation(RequestScoped) == RequestScoped
        visitor.visitScopeAnnotation(SessionScoped) == SessionScoped
    }

    Class<? extends Annotation> scope(Injector injector, Class type) {
        visitor.performDetection(injector.getExistingBinding(Key.get(type)))
    }

    Class<? extends Annotation> scope(List<Element> elements, Class type) {
        visitor.performDetection(
                elements.find { it instanceof Binding && (it as Binding).getKey().getTypeLiteral().getRawType() == type } as Binding)
    }

    static class Module extends AbstractModule {

        @Override
        protected void configure() {
            install(new ServletModule())
            bindScope(Prototype, Scopes.NO_SCOPE)

            bind(Eager).asEagerSingleton()
            bind(Single).in(Scopes.SINGLETON)
            bind(Single2).in(Singleton)
            bind(Proto).in(Prototype)
            bind(NoScope).in(Scopes.NO_SCOPE)
            bind(NoScope2)
            bind(Req).in(ServletScopes.REQUEST)
            bind(Sess).in(ServletScopes.SESSION)

            bind(SingAnn)
            bind(SingAnn2)
            bind(ProtAnn)
            bind(ReqAnn)
            bind(SessAnn)
        }
    }

    static class Eager {}

    static class Single {}

    static class Single2 {}

    static class Proto {}

    static class NoScope {}

    static class NoScope2 {}

    static class NoScope3 {}

    static class Req {}

    static class Sess {}

    @Singleton
    static class SingAnn {}

    @com.google.inject.Singleton
    static class SingAnn2 {}

    @Prototype
    static class ProtAnn {}

    @RequestScoped
    static class ReqAnn {}

    @SessionScoped
    static class SessAnn {}
}

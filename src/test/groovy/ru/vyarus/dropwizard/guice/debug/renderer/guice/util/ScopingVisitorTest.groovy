package ru.vyarus.dropwizard.guice.debug.renderer.guice.util

import com.google.inject.*
import com.google.inject.servlet.RequestScoped
import com.google.inject.servlet.ServletModule
import com.google.inject.servlet.ServletScopes
import com.google.inject.servlet.SessionScoped
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

        when: "parse elements"
        Injector injector = Guice.createInjector(new Module())
        then: "scoped correctly detected"
        scope(injector, Eager) == EagerSingleton
        scope(injector, Single) == Singleton
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

    Class<? extends Annotation> scope(Injector injector, Class type) {
        injector.getExistingBinding(Key.get(type)).acceptScopingVisitor(visitor)
    }

    static class Module extends AbstractModule {

        @Override
        protected void configure() {
            install(new ServletModule())
            bindScope(Prototype, Scopes.NO_SCOPE)

            bind(Eager).asEagerSingleton()
            bind(Single).in(Scopes.SINGLETON)
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

    static class NoScope {}

    static class NoScope2 {}

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

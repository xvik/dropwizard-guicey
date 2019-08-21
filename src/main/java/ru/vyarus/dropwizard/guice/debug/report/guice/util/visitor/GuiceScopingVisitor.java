package ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor;

import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletScopes;
import com.google.inject.servlet.SessionScoped;
import com.google.inject.spi.DefaultBindingScopingVisitor;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.dropwizard.guice.module.support.scope.Prototype;

import java.lang.annotation.Annotation;

/**
 * Guice binding scope analyzer. Does not support custom scopes.
 */
public class GuiceScopingVisitor
        extends DefaultBindingScopingVisitor<Class<? extends Annotation>> {

    @Override
    public Class<? extends Annotation> visitEagerSingleton() {
        return EagerSingleton.class;
    }

    @Override
    public Class<? extends Annotation> visitScope(final Scope scope) {
        Class<? extends Annotation> res = null;
        if (scope == Scopes.SINGLETON) {
            res = javax.inject.Singleton.class;
        }
        if (scope == Scopes.NO_SCOPE) {
            res = Prototype.class;
        }
        if (scope == ServletScopes.REQUEST) {
            res = RequestScoped.class;
        }
        if (scope == ServletScopes.SESSION) {
            res = SessionScoped.class;
        }
        // not supporting custom scopes
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Annotation> visitScopeAnnotation(final Class scopeAnnotation) {
        return scopeAnnotation;
    }

    @Override
    public Class<? extends Annotation> visitNoScoping() {
        return Prototype.class;
    }
}

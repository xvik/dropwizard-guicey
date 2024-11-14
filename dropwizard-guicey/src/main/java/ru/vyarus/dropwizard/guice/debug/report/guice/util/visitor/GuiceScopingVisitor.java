package ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor;

import com.google.inject.Binding;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletScopes;
import com.google.inject.servlet.SessionScoped;
import com.google.inject.spi.DefaultBindingScopingVisitor;
import com.google.inject.spi.LinkedKeyBinding;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.support.scope.Prototype;

import java.lang.annotation.Annotation;

/**
 * Guice binding scope analyzer. Does not support custom scopes. Works correctly only on bindings from injector
 * (for module element only manually declared scopes are visible and not annotations).
 */
public class GuiceScopingVisitor
        extends DefaultBindingScopingVisitor<Class<? extends Annotation>> {

    /**
     * Method to call DIRECTLY on visitor instead of "normal" visitor appliance. Required for more accurate
     * scope resolution.
     *
     * @param binding binding to analyze
     * @return resolved scope (or proposed prototype scope when scope not detected)
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Annotation> performDetection(final Binding binding) {
        return finalizeScopeDetection((Class<? extends Annotation>) binding.acceptScopingVisitor(this), binding);
    }

    @Override
    public Class<? extends Annotation> visitEagerSingleton() {
        return EagerSingleton.class;
    }

    @Override
    public Class<? extends Annotation> visitScope(final Scope scope) {
        Class<? extends Annotation> res = null;
        if (Scopes.SINGLETON.equals(scope)) {
            res = javax.inject.Singleton.class;
        }
        if (Scopes.NO_SCOPE.equals(scope)) {
            res = Prototype.class;
        }
        if (ServletScopes.REQUEST.equals(scope)) {
            res = RequestScoped.class;
        }
        if (ServletScopes.SESSION.equals(scope)) {
            res = SessionScoped.class;
        }
        // not supporting custom scopes
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Annotation> visitScopeAnnotation(final Class scopeAnnotation) {
        // always return javax.inject annotation to simplify checks
        if (scopeAnnotation.equals(Singleton.class)) {
            return javax.inject.Singleton.class;
        }
        return scopeAnnotation;
    }

    @Override
    public Class<? extends Annotation> visitNoScoping() {
        // no scope annotation declared OR linked binding with scope annotation on TARGET class (no way to know it)

        return null;
    }

    /**
     * Method should be called manually! Scoping visitor would not detect scoping annotation on linked binding.
     * This method tries to fix this (to improve accuracy).
     *
     * @param scope   resolved scope or null
     * @param binding binding under analysis
     * @return scoping annotation (exactly resolved or assumed prototype)
     */
    private Class<? extends Annotation> finalizeScopeDetection(final Class<? extends Annotation> scope,
                                                               final Binding binding) {
        if (scope != null) {
            return scope;
        }
        Class<? extends Annotation> res = null;
        if (binding instanceof LinkedKeyBinding) {
            // NOTE: the link may be a part of long chain, but this is ignored - consider only length of 1
            // (which may obviously produce false scope value)
            final LinkedKeyBinding linkedBinding = (LinkedKeyBinding) binding;
            res = BindingUtils.findScopingAnnotation(linkedBinding.getLinkedKey().getTypeLiteral().getRawType(), true);
        }
        return res == null ? Prototype.class : visitScopeAnnotation(res);
    }
}

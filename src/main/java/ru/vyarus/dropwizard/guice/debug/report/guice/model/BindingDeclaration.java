package ru.vyarus.dropwizard.guice.debug.report.guice.model;


import com.google.common.base.Preconditions;
import com.google.inject.Key;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Guice binding declaration.
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2019
 */
public class BindingDeclaration {

    private DeclarationType type;
    // not Element because extensions (servlets, multibindings) use different declaration types
    private Object element;
    private Key key;
    private Key target;
    private String providedBy;
    // for scope bindings only scope set
    private Class<? extends Annotation> scope;
    private String source;
    private int sourceLine;
    // type listener, provision listener or aop interceptors declaration bindings (no other keys)
    // for other bindings contains strings with additional data (e.g. pattern for filters)
    private List<?> special;
    private final List<String> markers = new ArrayList<>();
    private String module;

    public BindingDeclaration(final DeclarationType type, final Object element) {
        Preconditions.checkState(type.getType().isAssignableFrom(element.getClass()),
                "%s requires %s, but %s binding provided", type.name(),
                type.getType().getSimpleName(), element.getClass().getSimpleName());
        this.type = type;
        this.element = element;
    }

    public DeclarationType getType() {
        return type;
    }

    public Object getElement() {
        return element;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(final Key key) {
        this.key = key;
    }

    public Key getTarget() {
        return target;
    }

    public void setTarget(final Key target) {
        this.target = target;
    }

    public String getProvidedBy() {
        return providedBy;
    }

    public void setProvidedBy(final String providedBy) {
        this.providedBy = providedBy;
    }

    public Class<? extends Annotation> getScope() {
        return scope;
    }

    public void setScope(final Class<? extends Annotation> scope) {
        this.scope = scope;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    public void setSourceLine(final int sourceLine) {
        this.sourceLine = sourceLine;
    }

    public List<?> getSpecial() {
        return special;
    }

    public void setSpecial(final List<?> special) {
        this.special = special;
    }

    public List<String> getMarkers() {
        return markers;
    }

    public String getModule() {
        return module;
    }

    public void setModule(final String module) {
        this.module = module;
    }

    @Override
    public String toString() {
        String res = type.name().toLowerCase() + " " + GuiceModelUtils.renderKey(key);
        if (module != null) {
            res += " (from module " + module + ")";
        }
        return res;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof BindingDeclaration && Objects.equals(element, ((BindingDeclaration) o).getElement());
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }
}

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

    private final DeclarationType type;
    // not Element because extensions (servlets, multibindings) use different declaration types
    private final Object element;
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

    /**
     * Create binding declaration.
     *
     * @param type    binding type
     * @param element binding element
     */
    public BindingDeclaration(final DeclarationType type, final Object element) {
        Preconditions.checkState(type.getType().isAssignableFrom(element.getClass()),
                "%s requires %s, but %s binding provided", type.name(),
                type.getType().getSimpleName(), element.getClass().getSimpleName());
        this.type = type;
        this.element = element;
    }

    /**
     * @return binding type
     */
    public DeclarationType getType() {
        return type;
    }

    /**
     * @return binding element
     */
    public Object getElement() {
        return element;
    }

    /**
     * @return binding key
     */
    public Key getKey() {
        return key;
    }

    /**
     * @param key binding key
     */
    public void setKey(final Key key) {
        this.key = key;
    }

    /**
     * @return target key for linked bindings or null
     */
    public Key getTarget() {
        return target;
    }

    /**
     * @param target target key
     */
    public void setTarget(final Key target) {
        this.target = target;
    }

    /**
     * @return provider key (render) or another provider identity, otherwise null
     */
    public String getProvidedBy() {
        return providedBy;
    }

    /**
     * @param providedBy rendered provider key or another provider identity
     */
    public void setProvidedBy(final String providedBy) {
        this.providedBy = providedBy;
    }

    /**
     * @return binding scope
     */
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    /**
     * @param scope binding scope
     */
    public void setScope(final Class<? extends Annotation> scope) {
        this.scope = scope;
    }

    /**
     * @return binding declaration source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source binding declaration source
     */
    public void setSource(final String source) {
        this.source = source;
    }

    /**
     * @return binding declaration source line
     */
    public int getSourceLine() {
        return sourceLine;
    }

    /**
     * @param sourceLine binding declaration source line
     */
    public void setSourceLine(final int sourceLine) {
        this.sourceLine = sourceLine;
    }

    /**
     * @return additional binding data (type listener, provision listener or aop interceptors declaration bindings.
     * etc.)
     */
    public List<?> getSpecial() {
        return special;
    }

    /**
     * @param special special binding data
     */
    public void setSpecial(final List<?> special) {
        this.special = special;
    }

    /**
     * @return binding markers (OVERRIDE, OVERRIDDEN, EXTENSION, REMOVED)
     */
    public List<String> getMarkers() {
        return markers;
    }

    /**
     * @return module name
     */
    public String getModule() {
        return module;
    }

    /**
     * @param module module name
     */
    public void setModule(final String module) {
        this.module = module;
    }

    @Override
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
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

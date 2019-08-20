package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model;


import com.google.inject.Key;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.util.GuiceModelUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Guice binding declaration.
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2019
 */
public class BindingDeclaration {

    private DeclarationType type;
    private Key key;
    private Key target;
    private String providedBy;
    private Class<? extends Annotation> scope;
    private String source;
    private int sourceLine;
    private List<?> special;
    private final List<String> markers = new ArrayList<>();

    public BindingDeclaration(final DeclarationType type) {
        this.type = type;
    }

    public DeclarationType getType() {
        return type;
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

    public void setProvidedBy(String providedBy) {
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

    @Override
    public String toString() {
        return type.name() + " " + GuiceModelUtils.renderKey(key);
    }
}

package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model;

import com.google.inject.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Guice module descriptor.
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2019
 */
public class ModuleDeclaration {

    private Class type;
    private String parent;
    private final List<ModuleDeclaration> children = new ArrayList<>();
    private final List<BindingDeclaration> declarations = new ArrayList<>();
    private final List<String> markers = new ArrayList<>();

    public Class getType() {
        return type;
    }

    public void setType(final Class type) {
        this.type = type;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public List<ModuleDeclaration> getChildren() {
        return children;
    }

    public List<BindingDeclaration> getDeclarations() {
        return declarations;
    }

    public List<String> getMarkers() {
        return markers;
    }

    public boolean isJITBindings() {
        return Module.class.equals(type);
    }

    @Override
    public String toString() {
        return type.getSimpleName();
    }
}

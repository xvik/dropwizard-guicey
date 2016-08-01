package ru.vyarus.dropwizard.guice.config.debug.util

import com.google.inject.Binder
import com.google.inject.Module
import ru.vyarus.dropwizard.guice.module.context.debug.util.RenderUtils
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 16.07.2016
 */
class RendererUtilsTest extends Specification {

    static Module ANONYMOUS = new Module() {
        @Override
        void configure(Binder binder) {
        }
    };

    def "Check installer render"() {

        expect:
        RenderUtils.renderInstaller(type, ["TEST", "SM"]) == render
        RenderUtils.renderDisabledInstaller(type).trim() == disabled

        where:
        type              | render                                                                 | disabled
        ResourceInstaller | "resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    *TEST, SM" | "-resource            (r.v.d.g.m.i.f.j.ResourceInstaller)"
    }

    def "Check class render"() {

        expect:
        RenderUtils.renderClass(type) == render

        where:
        type                 | render
        ResourceInstaller    | "r.v.d.g.m.i.f.j.ResourceInstaller"
        InnerClass           | "r.v.d.g.c.d.u.RendererUtilsTest\$InnerClass"
        ANONYMOUS.getClass() | "r.v.d.g.c.d.util.RendererUtilsTest\$1"
    }

    def "Check package render"() {

        expect:
        RenderUtils.renderPackage(type) == render

        where:
        type                 | render
        ResourceInstaller    | "r.v.d.g.m.i.f.jersey"
        InnerClass           | "r.v.d.g.c.d.u.RendererUtilsTest"
        ANONYMOUS.getClass() | "r.v.d.g.c.debug.util"
    }

    def "Check class line render"() {

        expect:
        RenderUtils.renderClassLine(type, ["TEST", "SM"]) == render

        where:
        type                 | render
        ResourceInstaller    | "ResourceInstaller            (r.v.d.g.m.i.f.jersey)     *TEST, SM"
        InnerClass           | "InnerClass                   (r.v.d.g.c.d.u.RendererUtilsTest) *TEST, SM"
        ANONYMOUS.getClass() | "RendererUtilsTest\$1          (r.v.d.g.c.debug.util)     *TEST, SM"
    }

    def "Check markers"() {

        expect:
        RenderUtils.markers(src) == res

        where:
        src            | res
        null           | ""
        ["TEST"]       | "*TEST"
        ["TEST", "MM"] | "*TEST, MM"
    }

    def "Check brackets"() {

        expect:
        RenderUtils.brackets("Sample") == "(Sample)"

    }

    class InnerClass {}
}
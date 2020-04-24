package ru.vyarus.dropwizard.guice.debug.util


import ru.vyarus.dropwizard.guice.debug.util.support.WithAnonymous
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 16.07.2016
 */
class RendererUtilsTest extends Specification {

    def "Check installer render"() {

        expect:
        RenderUtils.renderInstaller(type, ["TEST", "SM"]) == render
        RenderUtils.renderDisabledInstaller(type).trim() == disabled

        where:
        type              | render                                                                  | disabled
        ResourceInstaller | "resource             (r.v.d.g.m.i.f.j.ResourceInstaller)    *TEST, SM" | "-resource            (r.v.d.g.m.i.f.j.ResourceInstaller)"
    }

    def "Check class render"() {

        expect:
        RenderUtils.renderClass(type) == render

        where:
        type                               | render
        ResourceInstaller                  | "r.v.d.g.m.i.f.j.ResourceInstaller"
        InnerClass                         | "r.v.d.g.d.u.RendererUtilsTest\$InnerClass"
        WithAnonymous.ANONYMOUS.getClass() | "r.v.d.g.d.u.support.WithAnonymous\$1"
    }

    def "Check package render"() {

        expect:
        RenderUtils.renderPackage(type) == render

        where:
        type                               | render
        ResourceInstaller                  | "r.v.d.g.m.i.f.jersey"
        InnerClass                         | "r.v.d.g.d.u.RendererUtilsTest"
        WithAnonymous.ANONYMOUS.getClass() | "r.v.d.g.d.u.support"
    }

    def "Check class line render"() {

        expect:
        RenderUtils.renderClassLine(type, ["TEST", "SM"]) == render

        where:
        type                               | render
        ResourceInstaller                  | "ResourceInstaller            (r.v.d.g.m.i.f.jersey)     *TEST, SM"
        InnerClass                         | "InnerClass                   (r.v.d.g.d.u.RendererUtilsTest) *TEST, SM"
        WithAnonymous.ANONYMOUS.getClass() | "WithAnonymous\$1              (r.v.d.g.d.u.support)      *TEST, SM"
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
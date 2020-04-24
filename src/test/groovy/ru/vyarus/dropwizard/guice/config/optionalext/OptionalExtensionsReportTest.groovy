package ru.vyarus.dropwizard.guice.config.optionalext

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticRenderer
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeRenderer
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 11.12.2019
 */
@UseGuiceyApp(OptionalExtensionTest.App)
class OptionalExtensionsReportTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    DiagnosticRenderer diagnosticRenderer
    ContextTreeRenderer contextRenderer

    void setup() {
        contextRenderer = new ContextTreeRenderer(info)
        diagnosticRenderer = new DiagnosticRenderer(info)
    }

    def "Check optional extensions report"() {

        expect:
        render(new DiagnosticConfig()
                .printExtensions()
                .printDisabledItems()) == """

    EXTENSIONS =
        ExtAccepted                  (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL
        ExtAccepted2                 (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL
        HK2DebugFeature              (r.v.d.g.m.j.d.service)
        -ExtDisabled                 (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL
        -ExtDisabled2                (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL
"""

        render(new ContextTreeConfig()) == """

    APPLICATION
    ├── installer  ResourceInstaller            (r.v.d.g.m.i.f.jersey)
    ├── extension  ExtAccepted                  (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL
    ├── extension  -ExtDisabled                 (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL, DISABLED
    ├── module     GuiceBootstrapModule         (r.v.d.guice.module)
    │
    ├── Bundle                       (r.v.d.g.c.o.OptionalExtensionTest)
    │   ├── extension  ExtAccepted2                 (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL
    │   └── extension  -ExtDisabled2                (r.v.d.g.c.o.OptionalExtensionTest) *OPTIONAL, DISABLED
    │
    └── HOOKS
        │
        ├── HK2DebugBundle               (r.v.d.g.m.j.debug)
        │   ├── installer  JerseyFeatureInstaller       (r.v.d.g.m.i.f.jersey)
        │   ├── extension  HK2DebugFeature              (r.v.d.g.m.j.d.service)
        │   └── module     HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
        │
        └── GuiceRestrictedConfigBundle  (r.v.d.g.support.util)
            └── module     GRestrictModule              (r.v.d.g.s.u.GuiceRestrictedConfigBundle)
"""
    }

    String render(DiagnosticConfig config) {
        diagnosticRenderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    String render(ContextTreeConfig config) {
        contextRenderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

}

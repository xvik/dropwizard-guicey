package ru.vyarus.dropwizard.guice.cases.analysis

import com.google.inject.Injector
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 05.04.2021
 */
@TestGuiceyApp(AnnModuleApp)
class AnonymousModuleAnalysisTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector


    def "Check modules analyzed"() {

        expect: "All lambda modules registered"
        info.getModules().size() == 5

        and: "report correct"

        new GuiceBindingsRenderer(injector).renderReport(new GuiceConfig().hideGuiceBindings().hideGuiceyBindings())
                .replaceAll("\r", "").replaceAll(" +\n", "\n")
        // in jdk 8 inner lambda shown as null, above 8 as initialize
                .replace('$initialize$0(AnnModuleApp.java:25)', '$null$0(AnnModuleApp.java:25)') == """

    2 MODULES with 5 bindings
    │
    ├── Module1                      (r.v.d.g.c.a.AnnModuleApp)
    │   └── instance             [@Singleton]     @DefaultName Integer                            at ru.vyarus.dropwizard.guice.cases.analysis.AnnModuleApp\$Module1.lambda\$configure\$0(AnnModuleApp.java:44)
    │
    ├── HK2DebugModule               (r.v.d.g.m.j.d.HK2DebugBundle)
    │   ├── <provisionlistener>                   GuiceInstanceListener                           at ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle\$HK2DebugModule.configure(HK2DebugBundle.java:58)
    │   ├── untargetted          [@Singleton]     ContextDebugService                             at ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle\$HK2DebugModule.configure(HK2DebugBundle.java:60)
    │   └── untargetted          [@Singleton]     HK2InstanceListener                             at ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle\$HK2DebugModule.configure(HK2DebugBundle.java:61)
    │
    └── Module                       (com.google.inject)
        ├── instance             [@Singleton]     @DefaultName String                             at ru.vyarus.dropwizard.guice.cases.analysis.AnnModuleApp.lambda\$initialize\$1(AnnModuleApp.java:24)
        └── instance             [@Singleton]     @DefaultName Double                             at ru.vyarus.dropwizard.guice.cases.analysis.AnnModuleApp.lambda\$null\$0(AnnModuleApp.java:25)
"""
    }
}

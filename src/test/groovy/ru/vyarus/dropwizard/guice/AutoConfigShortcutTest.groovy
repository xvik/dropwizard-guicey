package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.support.auto2.AutoScanApp2
import ru.vyarus.dropwizard.guice.support.auto2.SampleResource
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 29.12.2022
 */
@TestGuiceyApp(AutoScanApp2)
class AutoConfigShortcutTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check auto scan configuration"() {

        when: "application started"

        then: "app package substituted"
        info.options.getValue(GuiceyOptions.ScanPackages) == [AutoScanApp2.package.name]

        then: "resource found"
        info.getExtensions(ResourceInstaller) == [SampleResource]

    }
}

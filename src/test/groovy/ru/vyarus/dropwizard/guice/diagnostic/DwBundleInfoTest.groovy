package ru.vyarus.dropwizard.guice.diagnostic

import io.dropwizard.ConfiguredBundle
import ru.vyarus.dropwizard.guice.diagnostic.support.DwBundleApp
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.BundleItemInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 11.07.2016
 */
@UseGuiceyApp(DwBundleApp)
class DwBundleInfoTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check dw bundle info"() {

        expect: "bundle info"
        BundleItemInfo bi = info.data.getInfo(DwBundleApp.DwBundle)
        bi.fromDwBundle
        !bi.fromLookup
        !bi.registeredDirectly
        bi.registeredBy == [ConfiguredBundle] as Set

    }
}
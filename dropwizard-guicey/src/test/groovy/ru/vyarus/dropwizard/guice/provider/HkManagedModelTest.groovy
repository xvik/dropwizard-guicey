package ru.vyarus.dropwizard.guice.provider

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo
import ru.vyarus.dropwizard.guice.support.provider.processor.Hk2ManagedModelApp
import ru.vyarus.dropwizard.guice.support.provider.processor.Hk2ManagedProcessor
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2022
 */
@TestDropwizardApp(Hk2ManagedModelApp)
class HkManagedModelTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check jersey managed processor"() {

        when: "lookup extension info"
        ExtensionItemInfo item = info.getInfo(Hk2ManagedProcessor)
        then: "jersey managed"
        item.isJerseyManaged()
    }
}

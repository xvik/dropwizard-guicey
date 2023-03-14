package ru.vyarus.dropwizard.guice.config

import io.dropwizard.core.Application
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.Disables
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.impl.ItemInfoImpl

import static ru.vyarus.dropwizard.guice.module.context.ConfigItem.*

/**
 * @author Vyacheslav Rusakov
 * @since 09.04.2018
 */
class DisablesPredicatesTest extends AbstractTest {

    def "Check predicates"() {

        expect:
        Disables.registeredBy(ConfigScope.Application).test(item(Extension, Sample))
        Disables.registeredBy(ConfigScope.Application.getKey()).test(item(Extension, Sample))
        !Disables.registeredBy(Serializable).test(item(Extension, Sample))
        !Disables.registeredBy(ItemId.from(Serializable)).test(item(Extension, Sample))

        Disables.itemType(Extension, Installer).test(item(Installer, Sample))
        !Disables.itemType(Extension, Installer).test(item(Bundle, Sample))

        Disables.extension().test(item(Extension, Sample))
        !Disables.extension().test(item(Installer, Sample))

        Disables.installer().test(item(Installer, Sample))
        !Disables.installer().test(item(Extension, Sample))

        Disables.module().test(item(ConfigItem.Module, Sample))
        !Disables.module().test(item(Extension, Sample))

        Disables.bundle().test(item(Bundle, Sample))
        !Disables.bundle().test(item(Extension, Sample))

        Disables.dropwizardBundle().test(item(DropwizardBundle, Sample))
        !Disables.dropwizardBundle().test(item(Bundle, Sample))

        Disables.type(Sample).test(item(Extension, Sample))
        !Disables.type(Sample).test(item(Extension, Sample2))

        Disables.inPackage('ru.vyarus').test(item(Extension, Sample))
        !Disables.inPackage('com.foo').test(item(Extension, Sample2))
    }

    def "Check composition"() {

        def predicate = Disables.registeredBy(Application)
                .and(Disables.installer())
                .and(Disables.type(Sample2).negate())

        expect:
        predicate.test(item(Installer, Sample))
        !predicate.test(item(Extension, Sample))
        !predicate.test(item(Installer, Sample2))
        !predicate.test(item(Installer, Sample, Sample))
    }

    ItemInfo item(ConfigItem type, Class cls, Class from = Application) {
        ItemInfoImpl info = new ItemInfoImpl(type, ItemId.from(cls))
        info.countRegistrationAttempt(ItemId.from(from))
        return info;
    }


    static class Sample {}

    static class Sample2 {}
}

package ru.vyarus.dropwizard.guice.diagnostic

import ru.vyarus.dropwizard.guice.diagnostic.support.AutoScanApp
import ru.vyarus.dropwizard.guice.diagnostic.support.features.EnvCommand
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigItem
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.context.info.CommandItemInfo
import ru.vyarus.dropwizard.guice.module.context.info.ItemId
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 27.07.2016
 */
@TestGuiceyApp(AutoScanApp)
class CommandInfoItemTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check command item info"() {

        expect: "command info"
        CommandItemInfo ci = info.getInfo(EnvCommand)
        !ci.registeredDirectly
        ci.registered
        ci.registrationAttempts == 1
        ci.itemType == ConfigItem.Command
        ci.type == EnvCommand
        ci.registeredBy == [ItemId.from(ClasspathScanner)] as Set
        ci.registrationScope == ItemId.from(ClasspathScanner)
        ci.registrationScopeType == ConfigScope.ClasspathScan
        ci.fromScan
        ci.environmentCommand
        ci.toString() == "$ConfigItem.Command $EnvCommand.simpleName" as String

    }
}
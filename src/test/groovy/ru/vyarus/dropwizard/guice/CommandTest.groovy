package ru.vyarus.dropwizard.guice

import ru.vyarus.dropwizard.guice.support.CustomModuleApplication
import ru.vyarus.dropwizard.guice.support.feature.NonInjactableCommand

/**
 * Command found with classpath scanning
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CommandTest extends AbstractTest {

    void cleanup() {
        GuiceBundle.getDeclaredField("injector").setAccessible(true)
        GuiceBundle.injector = null
    }

    def "Check command start"() {

        when: "run guice powered command"
        new CustomModuleApplication().run(['sample', 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml'] as String[])
        then: "command executed"
        GuiceBundle.injector
    }

    def "Check simple command not inject members"() {

        when: "run simple command, registered with classpath scan"
        new CustomModuleApplication().run(['nonguice', 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml'] as String[])
        then: "command was found and registered, but without injection because injector not constructed"
        NonInjactableCommand.instance.service == null

        when: "accessing injector"
        GuiceBundle.injector
        then: "it's not initialized"
        thrown(NullPointerException)
    }
}
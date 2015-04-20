package ru.vyarus.dropwizard.guice

import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.support.CustomModuleApplication
import ru.vyarus.dropwizard.guice.support.feature.NonInjactableCommand

/**
 * Command found with classpath scanning
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CommandTest extends AbstractTest {

    def "Check command start"() {

        when: "run guice powered command"
        def app = new CustomModuleApplication()
        app.run(['sample', 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml'] as String[])
        then: "command executed"
        InjectorLookup.getInjector(app).isPresent()
    }

    def "Check simple command not inject members"() {

        when: "run simple command, registered with classpath scan"
        def app = new CustomModuleApplication()
        app.run(['nonguice', 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml'] as String[])
        then: "command was found and registered, but without injection because injector not constructed"
        NonInjactableCommand.instance.service == null

        expect: "injector not available"
        !InjectorLookup.getInjector(app).isPresent()
    }
}
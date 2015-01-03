package ru.vyarus.dropwizard.guice.provider

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.CustomFeatureInjectableProvider
import ru.vyarus.dropwizard.guice.support.provider.InjectableProviderCheckApplication
import ru.vyarus.dropwizard.guice.support.provider.LocaleInjectableProvider
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
@UseDropwizardApp(InjectableProviderCheckApplication)
class InjectableProviderTest extends AbstractTest {

    void cleanupSpec() {
        LocaleInjectableProvider.resetCounters()
        CustomFeatureInjectableProvider.resetCounters()
    }

    def "Checks injectable provider usage"() {

        when: "calling resource which trigger providers"
        new URL("http://localhost:8080/prototype/").getText()
        new URL("http://localhost:8080/prototype/").getText()

        then: "provider instantiated once and called twice"
        LocaleInjectableProvider.callCounter == 2
        LocaleInjectableProvider.creationCounter == 1

        then: "provider instantiated once and called twice"
        CustomFeatureInjectableProvider.callCounter == 2
        CustomFeatureInjectableProvider.creationCounter == 1
    }
}
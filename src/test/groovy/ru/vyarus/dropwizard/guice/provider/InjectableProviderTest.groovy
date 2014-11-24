package ru.vyarus.dropwizard.guice.provider

import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.provider.CustomFeatureInjectableProvider
import ru.vyarus.dropwizard.guice.support.provider.InjectableProviderCheckApplication
import ru.vyarus.dropwizard.guice.support.provider.LocaleInjectableProvider

/**
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
class InjectableProviderTest extends AbstractTest {

    void setupSpec() {
        LocaleInjectableProvider.resetCounters()
        CustomFeatureInjectableProvider.resetCounters()
    }

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(InjectableProviderCheckApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

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
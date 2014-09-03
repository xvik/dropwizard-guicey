package ru.vyarus.dropwizard.guice

import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.support.TestApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration;

/**
 * Dummy test.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
class AppStartupTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(TestApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check application start"() {

        when: "application started"
        then: "successfully"
        true
    }
}

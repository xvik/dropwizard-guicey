package ru.vyarus.dropwizard.guice.order

import com.google.inject.Injector
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.order.Ext1
import ru.vyarus.dropwizard.guice.support.order.Ext2
import ru.vyarus.dropwizard.guice.support.order.Ext3
import ru.vyarus.dropwizard.guice.support.order.OrderedApplication

/**
 * @author Vyacheslav Rusakov 
 * @since 13.10.2014
 */
class OrderedTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(OrderedApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check extensions ordering"() {

        when: "application started"
        Injector injector = GuiceBundle.getInjector()
        FeaturesHolder holder = injector.getInstance(FeaturesHolder.class);

        then: "extensions ordered"
        holder.getFeatures(ManagedInstaller) == [Ext3, Ext1, Ext2]

    }
}
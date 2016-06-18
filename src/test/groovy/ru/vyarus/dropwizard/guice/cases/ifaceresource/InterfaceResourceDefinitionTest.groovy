package ru.vyarus.dropwizard.guice.cases.ifaceresource

import ru.vyarus.dropwizard.guice.cases.ifaceresource.support.InterfaceResourceApp
import ru.vyarus.dropwizard.guice.cases.ifaceresource.support.ResourceImpl
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 18.06.2016
 */
@UseDropwizardApp(InterfaceResourceApp)
class InterfaceResourceDefinitionTest extends Specification {

    @Inject
    FeaturesHolder holder

    def "Check resources recognition"() {

        expect: "only one resource recognized"
        holder.getFeatures(ResourceInstaller) == [ResourceImpl]
    }

    def "Check resource registered"() {

        expect: 'resource called'
        new URL("http://localhost:8080/res").getText() == 'called!'
    }
}
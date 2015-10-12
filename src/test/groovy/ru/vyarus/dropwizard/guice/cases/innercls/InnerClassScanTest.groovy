package ru.vyarus.dropwizard.guice.cases.innercls

import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2015
 */
@UseGuiceyApp(InnerClassScanApp)
class InnerClassScanTest extends Specification {

    @Inject
    FeaturesHolder holder;

    def "Check inner classes scan"() {

        expect:
        holder.getFeatures(JerseyProviderInstaller) as Set ==
                [AbstractExceptionMapper.FooExceptionMapper, AbstractExceptionMapper.BarExceptionMapper] as Set

    }
}
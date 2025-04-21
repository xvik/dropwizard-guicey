package ru.vyarus.dropwizard.guice.test.jupiter.setup.rest

import com.google.inject.Inject
import javax.ws.rs.WebApplicationException
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.HK2DebugFeature
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.test.rest.RestClient
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest
import ru.vyarus.dropwizard.guice.test.rest.support.ErrorResource
import ru.vyarus.dropwizard.guice.test.rest.support.ManagedBean
import ru.vyarus.dropwizard.guice.test.rest.support.Resource1
import ru.vyarus.dropwizard.guice.test.rest.support.Resource2
import ru.vyarus.dropwizard.guice.test.rest.support.RestExceptionMapper
import ru.vyarus.dropwizard.guice.test.rest.support.RestFilter1
import ru.vyarus.dropwizard.guice.test.rest.support.RestFilter2
import ru.vyarus.dropwizard.guice.test.rest.support.RestStubApp
import ru.vyarus.dropwizard.guice.test.rest.support.WebFilter

/**
 * @author Vyacheslav Rusakov
 * @since 26.03.2025
 */
@TestGuiceyApp(value = RestStubApp, debug = true)
class RestStubSpockTest extends AbstractTest {

    @StubRest(disableDropwizardExceptionMappers = true)
    RestClient rest

    @Inject
    GuiceyConfigurationInfo info

    @Inject
    RestFilter1 filter

    @Inject
    ManagedBean managed

    @Inject
    RestExceptionMapper exceptionMapper

    def "Check rest stub"() {

        expect:
        rest

        // extensions enabled
        [Resource1,
         Resource2,
         ErrorResource,
         RestFilter1,
         RestFilter2,
         ManagedBean,
         RestExceptionMapper,
         HK2DebugFeature] as Set ==  info.getExtensions() as Set

        // web extension auto disabled
        info.getExtensionsDisabled().contains(WebFilter)

        // managed called once
        1 == managed.beforeCnt
        0 == managed.afterCnt

        when:
        String res = rest.get("/1/foo", String)

        then:
        "foo" == res

        // rest filter used
        filter.called
    }

    def "Check rest error"() {

        when:
        rest.get("/error/foo", String)

        then:
        def ex = thrown(WebApplicationException)
        exceptionMapper.called
        "error" == ex.getResponse().readEntity(String)
    }
}

package ru.vyarus.dropwizard.guice.cases.dwaware

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 04.07.2015
 */
@UseGuiceyApp(DwAwareModuleApp)
class DwAwareModuleTest extends AbstractTest {

    def "Check dw aware module works"() {

        expect: "module autowired correctly"
        true
    }
}
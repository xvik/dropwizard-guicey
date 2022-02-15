package ru.vyarus.dropwizard.guice.web

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.web.ServletsApplication
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2014
 */
@TestDropwizardApp(ServletsApplication)
class WebModuleTest extends AbstractTest {

    def "Check web bindings"() {

        when: "calling filter"
        def res = new URL("http://localhost:8080/dummyFilter").getText()
        then: "filter active"
        res == 'Sample filter'

        when: "calling servlet"
        res = new URL("http://localhost:8080/dummyServlet").getText()
        then: "servlet active"
        res == 'Sample servlet'

        when: "calling extension servlet"
        res = new URL("http://localhost:8080/dummy").getText()
        then: "servlet active and filter around it active"
        res == 'dispatched addition'

        when: "calling extension filter"
        res = new URL("http://localhost:8080/sample").getText()
        then: "filter active"
        res == 'dispatched'

        when: "calling admin servlet"
        res = new URL("http://localhost:8081/adminServlet").getText()
        then: "servlet active"
        res == 'admin'

        when: "calling admin filter"
        res = new URL("http://localhost:8081/adminFilter").getText()
        then: "filter active"
        res == 'admin'
    }
}
package ru.vyarus.guicey.admin

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.admin.support.AdminRestApplication
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@TestDropwizardApp(value = AdminRestApplication,
        config = 'src/test/resources/ru/vyarus/guicey/admin/simpleConfig.yml')
class RequestScopeTest extends Specification {

    def "Check request scope exist when access from admin context"() {

        when: "rest with request scope"
        def res = new URL("http://localhost:8080/admin/rest/request/").getText()
        then: "ok"
        res == "hello"
    }
}
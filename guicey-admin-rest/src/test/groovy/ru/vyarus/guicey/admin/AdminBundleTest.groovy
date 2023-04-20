package ru.vyarus.guicey.admin

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.admin.support.AdminRestApplication

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@TestDropwizardApp(AdminRestApplication)
class AdminBundleTest extends AbstractTest {

    def "Check res access from user context"() {

        when: "opened rest"
        def res = new URL("http://localhost:8080/hybrid/hello").getText()
        then: "ok"
        res == "hello"

        when: "admin only rest"
        new URL("http://localhost:8080/hybrid/admin").getText()
        then: "not accessible"
        thrown(FileNotFoundException)

        when: "admin only rest (by class)"
        new URL("http://localhost:8080/admin/").getText()
        then: "not accessible"
        thrown(FileNotFoundException)
    }

    def "Check access from admin context"() {

        // when rest is registered to root, admin rest is accessible from  /api/
        when: "public rest"
        def res = new URL("http://localhost:8081/api/hybrid/hello").getText()
        then: "ok"
        res == "hello"

        when: "admin rest"
        res = new URL("http://localhost:8081/api/hybrid/admin").getText()
        then: "ok"
        res == "admin"

        when: "admin rest (by class annotation)"
        res = new URL("http://localhost:8081/api/admin/").getText()
        then: "ok"
        res == "hello"
    }
}
package ru.vyarus.dropwizard.guice.admin

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.admin.support.AdminRestApplication
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 08.08.2015
 */
@UseDropwizardApp(value = AdminRestApplication,
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/admin/simpleServerConfig.yml')
class SimpleServerTest extends AbstractTest {

        def "Check res access from user context"() {

                when: "opened rest"
                def res = new URL("http://localhost:8080/application/rest/hybrid/hello").getText()
                then: "ok"
                res == "hello"

                when: "admin only rest"
                new URL("http://localhost:8080/application/rest/hybrid/admin").getText()
                then: "not accessible"
                def ex = thrown(IOException)
                ex.getMessage().contains("403")

                when: "admin only rest (by class)"
                new URL("http://localhost:8080/application/rest/admin/").getText()
                then: "not accessible"
                ex = thrown(IOException)
                ex.getMessage().contains("403")
        }

        def "Check access from admin context"() {

                when: "public rest"
                def res = new URL("http://localhost:8080/admin/rest/hybrid/hello").getText()
                then: "ok"
                res == "hello"

                when: "admin rest"
                res = new URL("http://localhost:8080/admin/rest/hybrid/admin").getText()
                then: "ok"
                res == "admin"

                when: "admin rest (by class annotation)"
                res = new URL("http://localhost:8080/admin/rest/admin/").getText()
                then: "ok"
                res == "hello"
        }
}
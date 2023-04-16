package ru.vyarus.dropwizard.guice.examples

import org.glassfish.jersey.client.JerseyClientBuilder
import ru.vyarus.dropwizard.guice.examples.model.User
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.Entity
import jakarta.ws.rs.core.GenericType

/**
 * @author Vyacheslav Rusakov
 * @since 01.11.2018
 */
@TestDropwizardApp(value = Jdbi3Application, config = 'src/test/resources/test-config.yml')
class UserResourceTest extends AbstractTest {


    def "Check resource call"() {

        setup:
        Client client = JerseyClientBuilder.createClient()

        when: "create user"
        User res = client.target("http://localhost:8080/users").request()
                .buildPost(Entity.text("sample")).invoke().readEntity(User.class)
        then: "success"
        res.id == 1
        res.version == 1
        res.name == 'sample'

        when: "modifying user"
        res.name = "test"
        client.target("http://localhost:8080/users").request()
                .buildPut(Entity.json(res)).invoke()
        List<User> list = client.target("http://localhost:8080/users").request()
                .buildGet().invoke().readEntity(new GenericType<List<User>>() {})
        then: "modified"
        list.size() == 1
        with(list[0]) {
            name == "test"
            id == 1
            version == 2
        }
    }
}

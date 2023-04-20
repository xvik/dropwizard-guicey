package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.examples.model.User
import ru.vyarus.dropwizard.guice.examples.repository.UserRepository
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 09.12.2016
 */
@TestGuiceyApp(value = JdbiApplication, config = 'src/test/resources/test-config.yml')
class UserRepositoryTest extends AbstractTest {

    @Inject
    UserRepository repository

    def "Check repository"() {

        expect: "repository actions"
        repository.findAll().isEmpty()

        repository.save(new User(name: 'sample'))
        repository.findAll().size() == 1

        with(repository.findByName('sample')) {
            id == 1
            version == 1
            name == 'sample'
        }
    }

    def "Check hybrid method"() {

        expect: "user created"
        with(repository.createRandomUser()) {
            id > 0
            version > 0
            name.startsWith('test')
            repository.findByName(name) != null
        }

    }
}

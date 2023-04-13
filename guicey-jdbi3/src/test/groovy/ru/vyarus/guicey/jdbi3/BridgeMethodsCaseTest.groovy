package ru.vyarus.guicey.jdbi3

import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.jdbi3.support.SampleEagerApp
import ru.vyarus.guicey.jdbi3.support.repository.syntetic.NamedEntity
import ru.vyarus.guicey.jdbi3.support.repository.syntetic.RootRepo

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 23.11.2022
 */
@TestGuiceyApp(value = SampleEagerApp, config = 'src/test/resources/test-config.yml')
class BridgeMethodsCaseTest extends AbstractTest {

    @Inject
    RootRepo repo

    def "Check methods resolution"() {

        when: "init record"
        def entity = new NamedEntity(name: "test")
        long id = repo.save(entity)

        then: "read saved"
        def res = repo.get(id)
        res.name == "test"
    }
}

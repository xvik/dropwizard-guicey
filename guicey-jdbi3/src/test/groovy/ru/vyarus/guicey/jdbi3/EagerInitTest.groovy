package ru.vyarus.guicey.jdbi3

import com.google.inject.name.Named
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.jdbi3.installer.repository.sql.SqlObjectProvider
import ru.vyarus.guicey.jdbi3.support.SampleEagerApp
import ru.vyarus.guicey.jdbi3.support.repository.SampleRepository

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 23.06.2020
 */
@TestGuiceyApp(value = SampleEagerApp, config = 'src/test/resources/test-config.yml')
class EagerInitTest extends AbstractTest {

    @Inject @Named("jdbi3.proxies")
    Set<SqlObjectProvider> proxies

    @Inject
    SampleRepository repository

    def "Check eager proxies correctness"() {

        expect: "all proxies initialized"
        proxies.find { !it.initialized } == null

        and: "repository works"
        repository.all().empty
    }
}

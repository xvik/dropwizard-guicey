package ru.vyarus.guicey.jdbi

import com.google.inject.Inject
import ru.vyarus.guicey.jdbi.support.model.Sample
import ru.vyarus.guicey.jdbi.support.repository.SampleRepository

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
class MappingTest extends AbstractAppTest {

    @Inject
    SampleRepository repository

    def "Check repository and mapper works"() {

        when: "saving records"
        repository.save(new Sample(name: "test"))

        then: "read saved"
        def res = repository.all()
        res.size() == 1
        res[0].name == "test"
    }
}
package ru.vyarus.guicey.jdbi

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.jdbi.support.SampleApp
import ru.vyarus.guicey.jdbi.support.SampleConfiguration
import ru.vyarus.guicey.jdbi.support.ann.CustTx
import ru.vyarus.guicey.jdbi.support.repository.CustTxRepository
import ru.vyarus.guicey.jdbi.support.repository.SampleRepository
import ru.vyarus.guicey.jdbi.tx.InTransaction

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 06.12.2016
 */
@TestGuiceyApp(value = App, config = 'src/test/resources/test-config.yml')
class CustTxAnnMultiTest extends AbstractTest {

    @Inject
    CustTxRepository custrepo
    @Inject
    SampleRepository repo

    def "Check def ann not work"() {

        when: "call in scope of old ann"
        repo.all()
        then: "ok"
        true
    }

    def "Check new ann scope"() {

        when: "call in scope of new ann"
        custrepo.all()
        then: "ok"
        true

    }

    static class App extends Application<SampleConfiguration> {

        @Override
        void initialize(Bootstrap<SampleConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig(SampleApp.package.name)
                    .bundles(JdbiBundle.<SampleConfiguration> forDatabase { conf, env -> conf.database }
                            .withTxAnnotations(CustTx, InTransaction))
                    .build())
        }

        @Override
        void run(SampleConfiguration configuration, Environment environment) throws Exception {
        }
    }
}

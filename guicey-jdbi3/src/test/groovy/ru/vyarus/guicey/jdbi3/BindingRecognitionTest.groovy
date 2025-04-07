package ru.vyarus.guicey.jdbi3

import com.google.inject.AbstractModule
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.jdbi.v3.sqlobject.statement.SqlQuery
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository
import ru.vyarus.guicey.jdbi3.support.SampleConfiguration
import ru.vyarus.guicey.jdbi3.support.mapper.SampleMapper
import ru.vyarus.guicey.jdbi3.support.model.Sample
import ru.vyarus.guicey.jdbi3.tx.InTransaction
import spock.lang.IgnoreIf

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 15.01.2022
 */
@TestGuiceyApp(value = App, config = 'src/test/resources/test-config.yml')
// on appveyor test fails due to jvm bug (used 11.0.2)
@IgnoreIf({ env["APPVEYOR"] })
class BindingRecognitionTest extends AbstractTest {

    @Inject
    Repo repo

    def "Check correct repo definition detection"() {

        when: "trying to query repo"
        repo.all()
        then: "no error"
        true
    }

    static class App extends Application<SampleConfiguration> {
        @Override
        void initialize(Bootstrap<SampleConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new AbstractModule() {
                        @Override
                        protected void configure() {
                            // only right binding part is recognized as repo
                            bind(Repo).to(RepoImpl)
                        }
                    })
                    .bundles(JdbiBundle.<SampleConfiguration> forDatabase { conf, env -> conf.database })
                    .extensions(SampleMapper)
                    .build());
        }

        @Override
        void run(SampleConfiguration configuration, Environment environment) throws Exception {
        }
    }

    static interface Repo {
        @SqlQuery("select * from sample")
        List<Sample> all()
    }

    @JdbiRepository
    @InTransaction
    static interface RepoImpl extends Repo {}
}

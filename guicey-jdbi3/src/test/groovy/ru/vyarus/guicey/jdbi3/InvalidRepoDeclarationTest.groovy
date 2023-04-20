package ru.vyarus.guicey.jdbi3

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.TestSupport
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository
import ru.vyarus.guicey.jdbi3.support.SampleConfiguration
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 29.10.2019
 */
class InvalidRepoDeclarationTest extends Specification {

    def "Check incorrect repo definition detection"() {

        when: "staring app with incorrect repo declaration"
        TestSupport.runCoreApp(App, 'src/test/resources/test-config.yml')
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.getMessage() == "Incorrect repository BaseRepository declaration: base interface CrudRepository is also annotated with @JdbiRepository which may break AOP mappings. Only root repository class must be annotated."
    }

    static class App extends Application<SampleConfiguration> {
        @Override
        void initialize(Bootstrap<SampleConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(BaseRepository)
                    .bundles(JdbiBundle.<SampleConfiguration> forDatabase { conf, env -> conf.database })
                    .build());
        }

        @Override
        void run(SampleConfiguration configuration, Environment environment) throws Exception {
        }
    }

    @JdbiRepository
    static interface CrudRepository {
        int insert(String name);

        int update(String name);
    }

    @JdbiRepository
    static interface BaseRepository extends CrudRepository {
        @SqlUpdate("insert into table(name) values(:name)")
        int insert(String name);

        @SqlUpdate("update table set name = :name")
        int update(String name);
    }
}

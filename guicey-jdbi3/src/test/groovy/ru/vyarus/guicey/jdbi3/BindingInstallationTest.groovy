package ru.vyarus.guicey.jdbi3

import com.google.inject.AbstractModule
import com.google.inject.CreationException
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
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
class BindingInstallationTest extends Specification {
    def "Check incorrect repo definition detection"() {

        when: "staring app with incorrect repo declaration"
        TestSupport.runCoreApp(App, 'src/test/resources/test-config.yml')
        then: "error"
        def ex = thrown(CreationException)
        ex.getCause().getMessage()
                .replace('java.base/jdk.internal', 'sun') == "JDBI repository BaseRepository can't be installed from binding: sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)"
    }

    static class App extends Application<SampleConfiguration> {
        @Override
        void initialize(Bootstrap<SampleConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new AbstractModule() {
                        @Override
                        protected void configure() {
                            // suppose its some mocking attempt
                            bind(BaseRepository).toInstance(new BaseRepository() {
                                @Override
                                int insert(String name) {
                                    return 0
                                }

                                @Override
                                int update(String name) {
                                    return 0
                                }
                            })
                        }
                    })
                    .bundles(JdbiBundle.<SampleConfiguration> forDatabase { conf, env -> conf.database })
                    .build());
        }

        @Override
        void run(SampleConfiguration configuration, Environment environment) throws Exception {
        }
    }

    @JdbiRepository
    static interface BaseRepository {
        @SqlUpdate("insert into table(name) values(:name)")
        int insert(String name)

        @SqlUpdate("update table set name = :name")
        int update(String name)
    }
}

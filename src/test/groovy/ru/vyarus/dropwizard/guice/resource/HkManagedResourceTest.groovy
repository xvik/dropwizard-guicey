package ru.vyarus.dropwizard.guice.resource

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.core.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2018
 */
@TestDropwizardApp(App)
class HkManagedResourceTest extends AbstractTest {

    def "Check singleton hk resource"() {

        ExecutorService service = Executors.newFixedThreadPool(20)
        def res = Collections.synchronizedSet(new TreeSet())
        def expectedRes = [] as Set
        20.times { expectedRes.add("q=test$it") }

        when: "calling singleton resource concurrently"
        20.times { num ->
            service.submit({
                res.add(new URL("http://localhost:8080/?q=test$num").getText())
            })
        }
        service.shutdown()
        service.awaitTermination(2, TimeUnit.SECONDS)
        then: "ok"
        res == expectedRes

        expect: "singleton"
        Res.cnt == 1
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .useHK2ForJerseyExtensions()
                    .extensions(Res)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Path("/")
    static class Res {

        static int cnt = 0

        Res() {
            cnt++
        }

        @Context
        HttpServletRequest request

        @Path("/")
        @GET
        def smth() {
            return request.getQueryString()
        }
    }
}

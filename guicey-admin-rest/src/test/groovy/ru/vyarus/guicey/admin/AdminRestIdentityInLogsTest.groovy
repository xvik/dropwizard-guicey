package ru.vyarus.guicey.admin

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.junit.jupiter.api.extension.ExtendWith
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.admin.support.HybridResource
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension
import uk.org.webcompere.systemstubs.stream.SystemOut

/**
 * @author Vyacheslav Rusakov
 * @since 17.09.2024
 */
@TestDropwizardApp(App)
@ExtendWith(SystemStubsExtension)
class AdminRestIdentityInLogsTest extends AbstractTest {

    @SystemStub
    SystemOut out

    def "Check resource logs differ for admin context"() {

        when: "opened rest"
        def res = new URL("http://localhost:8080/hybrid/hello").getText()
        sleep(100)
        then: "ok"
        out.getText().replace("\r", "").contains("\"GET /hybrid/hello HTTP/1.1\"")

        when: "admin only rest"
        res = new URL("http://localhost:8081/api/hybrid/hello").getText()
        sleep(100)
        then: "admin context identified"
        out.getText().replace("\r", "").contains("\"GET /api/hybrid/hello HTTP/1.1\"")

    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new AdminRestBundle())
                    .extensions(HybridResource)
                    .build()
            )
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}

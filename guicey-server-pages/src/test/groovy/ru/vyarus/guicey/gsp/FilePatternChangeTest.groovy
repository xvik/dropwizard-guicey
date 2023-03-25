package ru.vyarus.guicey.gsp

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/conf.yml')
class FilePatternChangeTest extends AbstractTest {

    def "Check changed file detection regex"() {

        when: "accessing css resource"
        String res = get("/css/style.css")
        then: "ok"
        res.contains("sample page css")

        when: "accessing template page"
        res = getHtml("/template.ftl")
        then: "template rendered"
        res == "page: /template.ftl"

        when: "accessing html page"
        getHtml("/index.html")
        then: "path not found (detected that its not a template)"
        thrown(FileNotFoundException)
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                            // everything is a file, except direct .html files call
                                    .filePattern("(?:^|/)([^/]+\\.(?:(?!html)|(?:css)|(?:ftl)))(?:\\?.+)?\$")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}

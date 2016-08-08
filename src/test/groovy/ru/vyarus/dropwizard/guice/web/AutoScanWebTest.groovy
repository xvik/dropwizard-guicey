package ru.vyarus.dropwizard.guice.web

import com.google.inject.Inject
import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.WebInstallersBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.web.WebFilterInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.web.listener.WebListenerInstaller
import ru.vyarus.dropwizard.guice.module.installer.feature.web.WebServletInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.AdminGuiceFilter
import ru.vyarus.dropwizard.guice.support.feature.DummyService
import ru.vyarus.dropwizard.guice.support.util.BindModule
import ru.vyarus.dropwizard.guice.support.web.feature.*
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 07.08.2016
 */
@UseDropwizardApp(App)
class AutoScanWebTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector
    @Inject
    Environment environment

    def "Check auto scan with web module"() {

        expect: "servlet found"
        info.getExtensions(WebServletInstaller) as Set == [DummyServlet, AdminServlet] as Set
        injector.getBinding(DummyServlet)
        environment.getApplicationContext().getServletContext().getServletRegistrations().values().findAll {
            it.className.startsWith("ru.vyarus")
        }.size() == 1
        environment.getAdminContext().getServletContext().getServletRegistrations().values().findAll {
            it.className.startsWith("ru.vyarus")
        }.size() == 1

        and: "filter found"
        info.getExtensions(WebFilterInstaller) as Set == [DummyFilter, AdminFilter, FilterOnServlet] as Set
        injector.getBinding(DummyFilter)
        environment.getApplicationContext().getServletContext().getFilterRegistrations().values().findAll {
            it.className.startsWith("ru.vyarus")
        }.size() == 2
        environment.getAdminContext().getServletContext().getFilterRegistrations().values().findAll {
            it.className.startsWith("ru.vyarus") && it.className != AdminGuiceFilter.name
        }.size() == 1

        and: "web listener found"
        info.getExtensions(WebListenerInstaller) == [DummyWebListener]
        injector.getBinding(DummyWebListener)

    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(new WebInstallersBundle())
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.support.web.feature")
                    .modules(new BindModule(DummyService))
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}

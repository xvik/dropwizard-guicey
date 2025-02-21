package ru.vyarus.dropwizard.guice.config.disable

import com.google.inject.Binder
import com.google.inject.Inject
import com.google.inject.Module
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpFilter
import org.junit.jupiter.api.Test
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.Disables
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner
import ru.vyarus.dropwizard.guice.support.DefaultTestApp
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov
 * @since 21.02.2025
 */
@TestGuiceyApp(App)
class DisablesPredicateByInstallerTest {

    @Inject
    GuiceyConfigurationInfo info

    @Test
    void testDisableByInstaller() {
        expect:
        info.getExtensionsDisabled() == [DirectFilter, FilterFromScan, FilterFromBinding]
    }

    static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .enableAutoConfig()
                    // scan only inside class
                    .autoConfigFilter { DisablesPredicateByInstallerTest == it.getDeclaringClass() }
                    .option(GuiceyOptions.ScanProtectedClasses, true)
                    .extensions(DirectFilter)
            .modules(new Module() {
                @Override
                void configure(Binder binder) {
                    binder.bind(FilterFromBinding.class)
                }
            })
                    .disable(Disables.webExtension())
                    .build()
        }
    }

    @WebFilter
    @InvisibleForScanner
    static class DirectFilter extends HttpFilter {

    }

    @WebFilter
    @InvisibleForScanner
    static class FilterFromBinding extends HttpFilter {

    }

    @WebFilter
    static class FilterFromScan extends HttpFilter {

    }
}

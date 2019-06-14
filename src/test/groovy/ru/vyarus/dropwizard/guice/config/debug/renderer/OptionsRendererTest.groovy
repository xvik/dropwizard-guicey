package ru.vyarus.dropwizard.guice.config.debug.renderer

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsConfig
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsRenderer
import ru.vyarus.dropwizard.guice.module.context.option.Option
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
@UseGuiceyApp(App)
class OptionsRendererTest extends Specification {

    @Inject
    OptionsRenderer renderer

    def "Check all render"() {

        expect:
        render(new OptionsConfig()
                .showNotUsedMarker()
                .showNotDefinedOptions()) == """

    BundleX                   (r.v.d.g.c.d.r.OptionsRendererTest\$BundleXOptions)
        DebugFeature                   = false
        StrictMode                     = true                           *CUSTOM
        NullValue                      = null
        ListValue                      = [one, two]


    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [com.foo, com.bat]             *CUSTOM
        SearchCommands                 = false
        UseCoreInstallers              = true
        BindConfigurationInterfaces    = false
        BindConfigurationByPath        = true
        InjectorStage                  = PRODUCTION
        GuiceFilterRegistration        = [REQUEST]
        UseHkBridge                    = false


    Installers                (r.v.d.g.m.i.InstallersOptions)
        JerseyExtensionsManagedByGuice = true


    OtherOpts                 (r.v.d.g.c.d.r.OptionsRendererTest\$OtherOpts)
        Opt1                           = sample                         *CUSTOM, NOT_USED
""" as String;
    }


    def "Check not used marker disable"() {

        expect:
        render(new OptionsConfig()
                .showNotDefinedOptions()) == """

    BundleX                   (r.v.d.g.c.d.r.OptionsRendererTest\$BundleXOptions)
        DebugFeature                   = false
        StrictMode                     = true                           *CUSTOM
        NullValue                      = null
        ListValue                      = [one, two]


    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [com.foo, com.bat]             *CUSTOM
        SearchCommands                 = false
        UseCoreInstallers              = true
        BindConfigurationInterfaces    = false
        BindConfigurationByPath        = true
        InjectorStage                  = PRODUCTION
        GuiceFilterRegistration        = [REQUEST]
        UseHkBridge                    = false


    Installers                (r.v.d.g.m.i.InstallersOptions)
        JerseyExtensionsManagedByGuice = true


    OtherOpts                 (r.v.d.g.c.d.r.OptionsRendererTest\$OtherOpts)
        Opt1                           = sample                         *CUSTOM
""" as String;
    }


    def "Check custom marker disable"() {

        expect:
        render(new OptionsConfig()) == """

    BundleX                   (r.v.d.g.c.d.r.OptionsRendererTest\$BundleXOptions)
        StrictMode                     = true


    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [com.foo, com.bat]


    OtherOpts                 (r.v.d.g.c.d.r.OptionsRendererTest\$OtherOpts)
        Opt1                           = sample
""" as String;
    }

    def "Check option hide"() {

        expect:
        render(new OptionsConfig()
                .showNotDefinedOptions()
                .hideOptions(GuiceyOptions.GuiceFilterRegistration)) == """

    BundleX                   (r.v.d.g.c.d.r.OptionsRendererTest\$BundleXOptions)
        DebugFeature                   = false
        StrictMode                     = true                           *CUSTOM
        NullValue                      = null
        ListValue                      = [one, two]


    Guicey                    (r.v.dropwizard.guice.GuiceyOptions)
        ScanPackages                   = [com.foo, com.bat]             *CUSTOM
        SearchCommands                 = false
        UseCoreInstallers              = true
        BindConfigurationInterfaces    = false
        BindConfigurationByPath        = true
        InjectorStage                  = PRODUCTION
        UseHkBridge                    = false


    Installers                (r.v.d.g.m.i.InstallersOptions)
        JerseyExtensionsManagedByGuice = true


    OtherOpts                 (r.v.d.g.c.d.r.OptionsRendererTest\$OtherOpts)
        Opt1                           = sample                         *CUSTOM
""" as String;
    }

    def "Check group hide"() {

        expect:
        render(new OptionsConfig()
                .showNotDefinedOptions()
        .hideGroups(GuiceyOptions)) == """

    BundleX                   (r.v.d.g.c.d.r.OptionsRendererTest\$BundleXOptions)
        DebugFeature                   = false
        StrictMode                     = true                           *CUSTOM
        NullValue                      = null
        ListValue                      = [one, two]


    Installers                (r.v.d.g.m.i.InstallersOptions)
        JerseyExtensionsManagedByGuice = true


    OtherOpts                 (r.v.d.g.c.d.r.OptionsRendererTest\$OtherOpts)
        Opt1                           = sample                         *CUSTOM
""" as String;
    }

    String render(OptionsConfig config) {
        renderer.renderReport(config).replaceAll("\r", "").replaceAll(" +\n", "\n")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig("com.foo", "com.bat")
            // never used value
                    .option(OtherOpts.Opt1, "sample")
                    .option(BundleXOptions.StrictMode, true)
                    .modules(new DiagnosticBundle.DiagnosticModule())
                    .bundles(new GuiceyBundle() {
                @Override
                void initialize(GuiceyBootstrap bs) {
                    bs.option(BundleXOptions.DebugFeature)
                    bs.option(BundleXOptions.StrictMode)
                    bs.option(BundleXOptions.NullValue)
                    bs.option(BundleXOptions.ListValue)
                }
            })
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    static enum OtherOpts implements Option {

        Opt1(String, "foo"),
        Opt2(String, "bar");

        Class type
        Object value

        OtherOpts(Class type, Object value) {
            this.type = type
            this.value = value
        }

        @Override
        Class getType() {
            return type
        }

        @Override
        Object getDefaultValue() {
            return value
        }
    }

    static enum BundleXOptions implements Option {

        DebugFeature(Boolean, false),
        StrictMode(Boolean, true),
        NullValue(String, null),
        ListValue(List.class, Arrays.asList("one", "two"));

        Class type
        Object value

        BundleXOptions(Class type, Object value) {
            this.type = type
            this.value = value
        }

        @Override
        Class getType() {
            return type
        }

        @Override
        Object getDefaultValue() {
            return value
        }
    }
}
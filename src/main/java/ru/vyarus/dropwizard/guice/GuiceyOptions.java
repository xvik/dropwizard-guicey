package ru.vyarus.dropwizard.guice;

import com.google.inject.Stage;
import ru.vyarus.dropwizard.guice.module.context.option.Option;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Guicey core options. In most cases, direct option definition is not required because all options are covered
 * with shortcut method in {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}. Direct option definition
 * may be useful when option is dynamically resolved and so shortcut methods can't be used (will require builder
 * flow interruption and additional if statements).
 * <p>
 * Normally options are mostly useful for runtime configuration values access (e.g. check in some 3rd party
 * bundle what packages are configured for classpath scan).
 * <p>
 * Generally options are not limited to this enum and custom option enums may be used by 3rd party bundles.
 *
 * @author Vyacheslav Rusakov
 * @see Option for details
 * @see ru.vyarus.dropwizard.guice.module.context.option.Options for usage in guice services
 * @see ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo for reporting
 * @since 09.08.2016
 */
public enum GuiceyOptions implements Option {

    /**
     * Packages for classpath scan. Not empty value indicates auto scan mode enabled.
     * Empty by default.
     *
     * @see GuiceBundle.Builder#enableAutoConfig(String...)
     */
    ScanPackages(String[].class, new String[0]),

    /**
     * Enables commands search in classpath and dynamic installation. Requires auto scan mode.
     * Disabled by default.
     *
     * @see GuiceBundle.Builder#searchCommands()
     */
    SearchCommands(Boolean.class, false),

    /**
     * Automatic {@linkplain ru.vyarus.dropwizard.guice.module.installer.CoreInstallersBundle core installers}
     * installation.
     * Enabled by default.
     *
     * @see GuiceBundle.Builder#noDefaultInstallers()
     */
    UseCoreInstallers(Boolean.class, true),

    /**
     * Recognize {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle} from installed
     * dropwizard bundles.
     * Disabled by default.
     *
     * @see GuiceBundle.Builder#configureFromDropwizardBundles()
     */
    ConfigureFromDropwizardBundles(Boolean.class, false),

    /**
     * Bind all direct interfaces implemented by configuration objects to configuration instance in guice context.
     * Disabled by default.
     *
     * @see GuiceBundle.Builder#bindConfigurationInterfaces()
     */
    BindConfigurationInterfaces(Boolean.class, false),

    /**
     * Guice injector stage used for injector creation.
     * Production by default.
     *
     * @see GuiceBundle.Builder#build(Stage)
     */
    InjectorStage(Stage.class, Stage.PRODUCTION),

    /**
     * GuiceFilter registered for both contexts (application and admin) to provide guice
     * {@link com.google.inject.servlet.ServletModule} support and allow using request and session scopes.
     * By default, filter is registered only for direct requests.
     * Declare other types if required (but note that GuiceFilter does not support ASYNC!).
     * <p>
     * To disable guice filter installation use empty set: {@code EnumSet.noneOf(DispatcherType.class)}.
     * This will completely disable guice servlet modules support because without guice filter, guice web support
     * is useless (all filters and servlets registered in servlet module are dispatched by guice filter).
     * <p>
     * Note that even without guice servlet modules support HttpServletRequest and HttpServletResponse objects will be
     * still available for injection in resources (through hk bridging). Also, note that guice servlets initialization
     * took some time and application starts faster without it (~50ms). Use
     * {@link ru.vyarus.dropwizard.guice.module.installer.WebInstallersBundle} to register guice manged servlets
     * and filters.
     * IMPORTANT: after disabling guice filter, servlet and request scopes will no longer be available and
     * installation of guice ServletModule will be impossible (will fail on duplicate binding).
     * Also it will not be possible to use http request and response injections under filter and servlets
     * (it will work only with resources).
     */
    GuiceFilterRegistration(EnumSet.class, EnumSet.of(DispatcherType.REQUEST));

    private Class<?> type;
    private Object value;

    <T> GuiceyOptions(final Class<T> type, final T value) {
        this.type = type;
        this.value = value;
    }


    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Object getDefaultValue() {
        return value;
    }
}

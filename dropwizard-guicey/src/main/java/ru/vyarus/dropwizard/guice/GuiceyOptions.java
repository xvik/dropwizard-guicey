package ru.vyarus.dropwizard.guice;

import com.google.inject.Stage;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import ru.vyarus.dropwizard.guice.module.context.option.Option;

import jakarta.servlet.DispatcherType;
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
     * <p>
     * Special value {@link ru.vyarus.dropwizard.guice.module.GuiceyInitializer#APP_PKG} might be used to configure
     * application package (replaced into real package during initialization).
     *
     * @see GuiceBundle.Builder#enableAutoConfig(String...)
     */
    ScanPackages(String[].class, new String[0]),

    /**
     * Enables package-private and protected (nested) classes acception by scanner (as extension).
     * By default, only public extension classes are searched.
     * <p>
     * Option exists only for rare cases when extension classes are protected by historical reasons.
     * Otherwise, try to avoid using protected classes as extensions (no problems with that, just semantically not
     * correct)
     */
    ScanProtectedClasses(Boolean.class, false),

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
     * Introspect configuration object (using jackson serialization) and bind all internal values by path
     * ({@code @Inject @Config("path.to.value") Integer value}). Recognize unique sub configuration objects
     * for direct binding ({@code @Inject @Config SubConfig conf}). Enabled by default.
     * <p>
     * Note that path could be hidden using {@link com.fasterxml.jackson.annotation.JsonIgnore} on property getter.
     * <p>
     * Option exists only for edge cases when introspection fails and prevents application startup (should be
     * impossible) or due to project specific reasons (when internal bindings are not desirable). When
     * disabled, only configuration object itself would be bound (by all classes in hierarchy and interfaces).
     * Note that {@link ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree} will also not contain paths - option
     * disables entire introspection process.
     *
     * @see ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder
     * @see ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
     * @see ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule
     */
    BindConfigurationByPath(Boolean.class, true),

    /**
     * Track transitive dropwizard bundles registration. Affects only dropwizard bundles registered through
     * guicey api ({@link GuiceBundle.Builder#dropwizardBundles(ConfiguredBundle[])} (direct registration) and
     * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap#dropwizardBundles(ConfiguredBundle[])}
     * (registration within guicey bundle)). When enabled, registered dropwizard bundles would be registered in
     * dropwizard with decorated (to track execution) object and receive proxied bootstrap object instead
     * of raw bootstrap into {@link ConfiguredBundle#initialize(Bootstrap)}. This should not cause any problems
     * with normal bundles usage.
     * <p>
     * When disabled, guicey will be able to "see" only directly registered bundles (and so will be able to disable
     * and deduplicate only them).
     * <p>
     * NOTE: dropwizard bundles registered directly into bootstrap object (in application or in guicey bundle)
     * are not tracked in any case. It is assumed that guicey api would be used for bundles registration when
     * you want to track them.
     * <p>
     * {@link Bootstrap} object proxy creation results in ~200ms overhead, clearly visible on diagnostics report
     * (stats). But it's the only way to track transitive dropwizard bundles (not so big price).
     * Proxy object is not created if no dropwizard bundles registered through guicey api.
     */
    TrackDropwizardBundles(Boolean.class, true),

    /**
     * Search for extensions in supplied guice modules (registration from guice module). Overriding modules
     * are not taken into account. Also, removes disabled modules from inner module registrations (transitive
     * modules).
     * <p>
     * Normally extension class is registered directly (or found by classpath scan), recognized by installer, bound
     * to guice context (with a default binding) and installed in dropwizard. When option is enabled, the same is done
     * with guice bindings (declared in modules): binding classes analyzed by installers to detect extensions and
     * then extensions are installed. Overall it's the same, except no default bindings applied by framework.
     * <p>
     * It is ok if the same extension would be registered manually or detected by classpath scan and be declared
     * in guice module manually - guicey will gust avoid default binding.
     * <p>
     * Extensions, detected from guice modules may be disabled the same way as usual extensions (bindings
     * will be removed).
     * <p>
     * When modules analysis is enabled, guicey performs modules configuration (using SPI api) before actual injector
     * creation and, to avoid duplicate work by injector, parsed modules are repackaged (to preserve parsed
     * information). You can see on stats report that modules analysis time is cut off from injector creation time
     * (creation time grows if option disabled). Also repackaging is the only way to properly handle disables for
     * existing bindings.
     */
    AnalyzeGuiceModules(Boolean.class, true),

    /**
     * Support private modules analysis: in private module only exposed services could be visible.
     * As guicey works with extension classes only (for simplicity), for private modules it might be required to
     * add additional exposes (to expose extension binding directly). Also, for disabled extensions bindings would
     * be removed inside private modules too.
     * <p>
     * There are many different possible situations with private modules and quite possibly that not all of them
     * covered. Disable this option in case of problems to avoid disabling analysis entirely.
     */
    AnalyzePrivateGuiceModules(Boolean.class, true),

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
     * still available for injection in resources (through HK2 bridging). Also, note that guice servlets initialization
     * took some time and application starts faster without it (~50ms). Use
     * {@link ru.vyarus.dropwizard.guice.module.installer.WebInstallersBundle} to register guice manged servlets
     * and filters.
     * IMPORTANT: after disabling guice filter, servlet and request scopes will no longer be available and
     * installation of guice ServletModule will be impossible (will fail on duplicate binding).
     * Also it will not be possible to use http request and response injections under filter and servlets
     * (it will work only with resources).
     */
    GuiceFilterRegistration(EnumSet.class, EnumSet.of(DispatcherType.REQUEST)),

    /**
     * Enables guice bridge for HK2 to allow HK2 services to see guice beans. This is not often required and
     * so disabled by default. For example, it could be required if
     * {@link ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged} used to properly instantiate
     * service by HK2 when it also depends on guice services.
     * <p>
     * IMPORTANT: requires extra dependency on HK2 guice-bridge: 'org.glassfish.hk2:guice-bridge:2.6.1'
     * @deprecated in the next version HK2 support will be removed
     */
    @Deprecated
    UseHkBridge(Boolean.class, false);

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

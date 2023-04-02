package ru.vyarus.guicey.gsp;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.views.common.ViewBundle;
import io.dropwizard.views.common.ViewConfigurable;
import io.dropwizard.views.common.ViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;
import ru.vyarus.guicey.gsp.app.GlobalConfig;
import ru.vyarus.guicey.gsp.app.ServerPagesApp;
import ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder;
import ru.vyarus.guicey.gsp.app.asset.freemarker.FreemarkerTemplateLoader;
import ru.vyarus.guicey.gsp.app.ext.ServerPagesAppExtensionBundle;
import ru.vyarus.guicey.gsp.app.rest.log.RestPathsAnalyzer;
import ru.vyarus.guicey.gsp.app.rest.support.DirectTemplateExceptionMapper;
import ru.vyarus.guicey.gsp.app.rest.support.TemplateAnnotationFilter;
import ru.vyarus.guicey.gsp.app.rest.support.TemplateErrorResponseFilter;
import ru.vyarus.guicey.gsp.app.rest.support.TemplateExceptionListener;
import ru.vyarus.guicey.gsp.info.GspInfoModule;
import ru.vyarus.guicey.gsp.views.ConfiguredViewBundle;
import ru.vyarus.guicey.gsp.views.ViewRendererConfigurationModifier;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Bundle unifies dropwizard-views and dropwizard-assets bundles in order to bring server templating
 * simplicity like with jsp. The main goal is to make views rendering through rest endpoints hidden and
 * make template calls by their files to simplify static resources references (css ,js, images etc.). Also,
 * errors handling is unified (like in usual servlets, but individually for server pages application).
 * <p>
 * First of all global server pages support bundle must be installed ({@link #builder()}, preferably directly in the
 * application class). This will activates dropwizard-views support ({@link ViewBundle}). Do not register
 * {@link ViewBundle} manually!
 * <p>
 * Each server pages application is also registered
 * as separate bundle (using {@link #app(String, String, String)} or {@link #adminApp(String, String, String)}).
 * <p>
 * Views configuration could be mapped from yaml file in main bundle:
 * {@link ViewsBuilder#viewsConfiguration(ViewConfigurable)}. In order to fine tune configuration use
 * {@link AppBuilder#viewsConfigurationModifier(String, ViewRendererConfigurationModifier)} which could be used by
 * applications directly in order to apply required defaults. But pay attention that multiple apps could collide in
 * configuration (configure the same property)! Do manual properties merge instead of direct value set where possible
 * to maintain applications compatibility (e.g. you declare admin dashboard and main users app, which both use
 * freemarker and require default templates).
 * <p>
 * Renderers (pluggable template engines support) are loaded with service lookup mechanism (default for
 * dropwizard-views) but additional renderers could be registered with
 * {@link ViewsBuilder#addViewRenderers(ViewRenderer...)}. Most likely, server page apps will be bundled as 3rd party
 * bundles and so they can't be sure what template engines are installed in target application. Use
 * {@link AppBuilder#requireRenderers(String...)} to declare required template engines for each application and
 * fail fast if no required templates engine. Without required engines declaration template files will be served like
 * static files when direct template requested and rendering will fail for rest-mapped template.
 * <p>
 * Each application could be "extended" using {@link ServerPagesBundle#extendApp(String)} bundle. This way
 * extra classpath location is mapped into application root. Pages from extended context could reference resources from
 * the main context (most likely common root template will be used). Also, extended mapping could override
 * resources from the primary location (but note that in case of multiple extensions order is not granted).
 * Obvious case for extensions feature is dashboards, when extensions add extra pages to common dashboard
 * application, but all new pages still use common master template.
 * <p>
 * Application work scheme: assets servlet is registered on the configured path in order to serve static assets
 * (customized version of dropwizard {@link io.dropwizard.servlets.assets.AssetServlet} used which could
 * recognize both primary and extended locations). Special filter above servlet detects file calls (by extension,
 * but checks if requested file is template (and that's why list of supported templates is required)). If request
 * is not for file, it's redirected to rest endpoint in order to render view (note that direct template files will
 * also be rendered). Redirection scheme use application name, defined during bundle creation:
 * {rest prefix}/{app name}/{path from request}.
 * For example,
 * {@code .bundles(SpaPageBundle.app("ui", "/com/assets/path/", "ui/").build())}
 * Register application in main context, mapped to  "ui/" path, with static resources in "/com/assets/path/"
 * classpath path. Internal application name is "ui". When browser request any file directly, e.g.
 * "ui/styles.css" then file "/com/assets/path/styles.css" will be served. Any other path is redirected to rest
 * endpoint: e.g. "ui/dashboard/" is redirected to "{rest mapping}/ui/dashboard.
 * <pre>
 * <code>
 *  {@literal @}Template("dashboard.ftl")
 *  {@literal @}Path("ui/dahboard")
 *  {@literal @}Produces(MediaType.TEXT_HTML)
 *  public class DashboardPage {
 *   {@literal @}GET
 *   public DashboardView get() {
 *      return new DashboardView();
 *   }
 *  }
 * </code>
 * </pre>
 * Note that {@link ru.vyarus.guicey.gsp.views.template.Template} annotation on resource is required. Without it,
 * bundle will not be able to show path in console reporting. Also, configured template automatically applied
 * into view (so you don't have to specify template path in all methods (note that custom template path could
 * still be specified directly, when required). View class must extend
 * {@link ru.vyarus.guicey.gsp.views.template.TemplateView}. In all other aspects, it's pure dropwizard views.
 * {@link ru.vyarus.guicey.gsp.views.template.Template} annotation is jersey {@link javax.ws.rs.NameBinding} marker
 * so you can apply request/response filters only (!) for template resources (see {@link TemplateAnnotationFilter}
 * as example).
 * <p>
 * Note that all resources, started from application name prefix are considered to be used in application.
 * {@link ServerPagesBundle#extendApp(String)} mechanism is used only to declare additional static
 * resources (or direct templates). But in order to add new pages, handled by rest resources you dont need to do
 * anything - they just must start with correct prefix (you can see all application resources in console just after
 * startup).
 * <p>
 * In order to be able to render direct templates (without supporting rest endpoint) special rest
 * endpoint is registered which handles everything on application path (e.g. "ui/{file:.*}" for example application
 * above). Only POST and GET supported for direct templates.
 * <p>
 * Bundle unifies custom pages handling to be able to use default 404 or 500 pages (for both assets and resources).
 * Use builder {@link AppBuilder#errorPage(int, String)} method to map template (or pure html)
 * to response code (to be shown instead).
 * <p>
 * Bundle could also enable filter from {@link ru.vyarus.guicey.spa.SpaBundle} in order to support single
 * page applications routing (for cases when root page must be template and not just html, which makes direct usage of
 * {@link ru.vyarus.guicey.spa.SpaBundle} useless).
 * <p>
 * Information about configured application may be acquired through {@link ru.vyarus.guicey.gsp.info.GspInfoService}
 * guice bean. But it could be used only after complete gsp initialization (between dropwizard run and jersey start).
 *
 * @author Vyacheslav Rusakov
 * @see <a href="https://www.dropwizard.io/en/release-3.0.x/manual/views.html">dropwizard views</a>
 * @since 22.10.2018
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class ServerPagesBundle extends UniqueGuiceyBundle {

    /**
     * Default pattern for file request detection.
     *
     * @see AppBuilder#filePattern(String)
     */
    public static final String FILE_REQUEST_PATTERN = "(?:^|/)([^/]+\\.(?:[a-zA-Z\\d]+))(?:\\?.+)?$";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPagesBundle.class);

    private final GlobalConfig config;

    public ServerPagesBundle(final GlobalConfig config) {
        this.config = config;
    }

    /**
     * Creates global server pages support bundle which must be registered in the application. Bundle
     * installs standard dropwizard views bundle ({@link ViewBundle}). If views bundle is manually declared in
     * application, it must be removed (to avoid duplicates). View bundle owning is required for proper configuration
     * and to know all used template engines (renderers).
     * <p>
     * After global support is registered, server pages applications may be declared with
     * {@link #app(String, String, String)} and {@link #adminApp(String, String, String)}.
     * <p>
     * It is assumed that global bundles support is registered directly in the dropwizard application
     * (and not transitively in some bundle) and server page applications themselves could be registered
     * nearby (in dropwizard application) or in any bundle (for example, some dashboard bundle just registers
     * dashboard application, assuming that global server pages support would be activated).
     *
     * @return global views bundle builder
     */
    public static ViewsBuilder builder() {
        return new ServerPagesBundle.ViewsBuilder();
    }

    /**
     * Register application in main context.
     * Application names must be unique (when you register multiple server pages applications).
     * <p>
     * Application could be extended with {@link ServerPagesBundle#extendApp(String)} in another bundle.
     * <p>
     * NOTE global server pages support bundle must be installed with {@link #builder()} in dropwizard application.
     *
     * @param name       application name (used as servlet name)
     * @param assetsPath path to application resources (classpath); may be in form of package (dot-separated)
     * @param uriPath    mapping uri
     * @return builder instance for server pages application configuration
     * @see #builder()  for server pages applications global support
     */
    public static AppBuilder app(final String name,
                                 final String assetsPath,
                                 final String uriPath) {
        return app(name, assetsPath, uriPath, null);
    }

    /**
     * Same as {@link #app(String, String, String)} but with custom classloader to use for assets loading.
     * All additional registration through this builder ({@link AppBuilder#attachAssets(String)}) would also
     * be registered with provided class loader.
     * <p>
     * WARNING: custom class loader will be automatically supported for static resources, but template engine
     * may not be able to resolve template. For example, freemarker use class loader of resource class
     * serving view, so if resource class and view template are in the same class loader then it will work.
     * For sure direct template rendering (without custom resource class) will not work. Freemarker may be configured
     * to support all cases with custom template loader
     * (see {@link ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}) which must be configured
     * manually. Mustache is impossible to configure properly (with current mustache views renderer).
     *
     * @param name       application name (used as servlet name)
     * @param assetsPath path to application resources (classpath); may be in form of package (dot-separated)
     * @param uriPath    mapping uri
     * @param loader     class loader to use for assets loading
     * @return builder instance for server pages application configuration
     * @see #builder()  for server pages applications global support
     */
    public static AppBuilder app(final String name,
                                 final String assetsPath,
                                 final String uriPath,
                                 final ClassLoader loader) {
        LOGGER.debug("Registering server pages application {} on path {} with resources in {}",
                name, uriPath, assetsPath);
        return new AppBuilder(true, name, assetsPath, uriPath, loader);
    }

    /**
     * Register application in admin context.
     * Application names must be unique (when you register multiple server pages applications).
     * <p>
     * You can't register admin application on admin context root because there is already dropwizard
     * admin servlet {@link com.codahale.metrics.servlets.AdminServlet}.
     * <p>
     * Application could be extended with {@link ServerPagesBundle#extendApp(String)} in another bundle.
     * <p>
     * NOTE: global server pages support bundle must be installed with {@link #builder()} in dropwizard application.
     *
     * @param name       application name (used as servlet name)
     * @param assetsPath path to application resources (classpath)
     * @param uriPath    mapping uri
     * @return builder instance for server pages application configuration
     * @see #builder()  for server pages applications global support
     */
    public static AppBuilder adminApp(final String name,
                                      final String assetsPath,
                                      final String uriPath) {
        return adminApp(name, assetsPath, uriPath, null);
    }

    /**
     * Same as {@link #adminApp(String, String, String)} but with custom classloader to use for assets loading.
     * All additional registration through this builder ({@link AppBuilder#attachAssets(String)}) would also
     * be registered with provided class loader.
     * <p>
     * WARNING: custom class loader will be automatically supported for static resources, but template engine
     * may not be able to resolve template. For example, freemarker use class loader of resource class
     * serving view, so if resource class and view template are in the same class loader then it will work.
     * For sure direct template rendering (without custom resource class) will not work. Freemarker may be configured
     * to support all cases with custom template loader
     * (see {@link ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}) which must be configured
     * manually. Mustache is impossible to configure properly (with current mustache views renderer).
     *
     * @param name       application name (used as servlet name)
     * @param assetsPath path to application resources (classpath)
     * @param uriPath    mapping uri
     * @param loader     class loader to use for assets loading
     * @return builder instance for server pages application configuration
     * @see #builder()  for server pages applications global support
     */
    public static AppBuilder adminApp(final String name,
                                      final String assetsPath,
                                      final String uriPath,
                                      final ClassLoader loader) {
        LOGGER.debug("Registering admin server pages application {} on path {} with resources in {}",
                name, uriPath, assetsPath);
        return new AppBuilder(false, name, assetsPath, uriPath, loader);
    }

    /**
     * Extend registered application. The most common use case is adding or overriding static assets of target
     * application (e.g. for visual customization). Extension may be called before or after application
     * registration - it does not matter.
     * <p>
     * For example, if we register application like this
     * {@code ServerPagesBundle.app("ui", "/com/path/assets/", "/ui")} it will server static resources only from
     * "/com/path/assets/" package. Suppose we want to add another page (with direct template) into the app:
     * {@code ServerPagesBundle.extendApp("ui").attachAssets("/com/another/assets/")}. Now assets will be searched in
     * both packages and if we have "/com/another/assets/page.tpl" then calling url "/ui/page.tpl" will render template.
     * Resource in extended location could override original app resource: e.g. if we have
     * "/com/another/assets/style.css" (extended) and "/com/path/assets/style.css" (original app) then
     * "/ui/style.css" will return extended resource file.
     * <p>
     * For new views addition, you may simply register new rest resources with prefix, used by application and
     * it will detect it automatically (in example above app name is "ui").
     * <p>
     * If extended application is not registered no error will be thrown. This behaviour support optional application
     * extension support (extension will work if extended application registered and will not harm if not).
     * <p>
     * Unlimited number of extensions could be registered for the same application (all extensions will be applied).
     *
     * @param name extended application name
     * @return application extension bundle
     */
    public static ServerPagesAppExtensionBundle.AppExtensionBuilder extendApp(final String name) {
        return extendApp(name, null);
    }

    /**
     * Same as {@link #extendApp(String)} but with custom classloader to use for assets loading.
     * All additional registration through this builder ({@link AppBuilder#attachAssets(String)}) would also
     * be registered with provided class loader.
     * <p>
     * WARNING: custom class loader will be automatically supported for static resources, but template engine
     * may not be able to resolve template. For example, freemarker use class loader of resource class
     * serving view, so if resource class and view template are in the same class loader then it will work.
     * For sure direct template rendering (without custom resource class) will not work. Freemarker may be configured
     * to support all cases with custom template loader
     * (see {@link ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}) which must be configured
     * manually. Mustache is impossible to configure properly (with current mustache views renderer).
     *
     * @param name   extended application name
     * @param loader class loader to use for assets loading
     * @return application extension bundle
     */
    public static ServerPagesAppExtensionBundle.AppExtensionBuilder extendApp(final String name,
                                                                              final ClassLoader loader) {
        return new ServerPagesAppExtensionBundle.AppExtensionBuilder(name, loader);
    }

    /**
     * Method is available for custom template detection logic (similar that used inside server pages filter)
     * or to validate state in tests.
     *
     * @return list of used renderers (supported template engines)
     */
    public List<ViewRenderer> getRenderers() {
        return ImmutableList.copyOf(config.getRenderers());
    }

    /**
     * Method is available for custom views configuration state analysis logic (after startup) or to validate
     * state in tests.
     *
     * @return final views configuration object (unmodifiable)
     * @throws NullPointerException if views configuration is not yet created (views ot initialized)
     */
    public Map<String, Map<String, String>> getViewsConfig() {
        return ImmutableMap.copyOf(checkNotNull(config.getViewsConfig(),
                "Views configuration is not created yet"));
    }

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        loadRenderers();

        // register global config
        bootstrap
                .shareState(ServerPagesBundle.class, config)
                .modules(new GspInfoModule())
                .dropwizardBundles(new ConfiguredViewBundle(config))
                .extensions(
                        // @Template annotation support (even with multiple registrations should be created just once)
                        // note: applied only to annotated resources!
                        TemplateAnnotationFilter.class,
                        // template rest errors interception (global handlers)
                        TemplateErrorResponseFilter.class,
                        // intercept rest (template) rendering exceptions in rest
                        TemplateExceptionListener.class,
                        // Direct templates support
                        DirectTemplateExceptionMapper.class);
    }

    @Override
    public void run(final GuiceyEnvironment environment) {
        // init applications after guicey initialization
        environment.listen(new GuiceyLifecycleAdapter() {
            @Override
            protected void applicationRun(final ApplicationRunEvent event) {
                config.applyDelayedExtensions(environment);

                for (ServerPagesApp app : config.getApps()) {
                    // finalize app configuration
                    // assume noone will ever map gsp applications on intercepting paths!
                    // (e.g. /app/ and /app/app2)
                    app.install(environment.environment(), config);
                }
            }
        });
        // delayed initialization until jersey starts (required data not available before it)
        // (started only if real server starts)
        environment.listenServer(it -> {
            final Environment env = environment.environment();
            final String contextPath = env.getJerseyServletContainer()
                    .getServletConfig().getServletContext().getContextPath();
            // server.rootPath
            final String restMapping = PathUtils.trailingSlash(PathUtils.trimStars(env.jersey().getUrlPattern()));
            final RestPathsAnalyzer analyzer = RestPathsAnalyzer.build(env.jersey().getResourceConfig());
            for (ServerPagesApp app : config.getApps()) {
                app.jerseyStarted(contextPath, restMapping, analyzer);
            }
        });
    }

    private void loadRenderers() {
        // automatically add engines from classpath lookup
        final Iterable<ViewRenderer> renderers = ServiceLoader.load(ViewRenderer.class);
        renderers.forEach(config::addRenderers);
        Preconditions.checkState(!config.getRenderers().isEmpty(),
                "No template engines found (dropwizard views renderer)");

        final StringBuilder res = new StringBuilder("Available dropwizard-views renderers:")
                .append(Reporter.NEWLINE).append(Reporter.NEWLINE);
        for (ViewRenderer renderer : config.getRenderers()) {
            res.append(Reporter.TAB).append(String.format(
                    "%-15s (%s)", renderer.getConfigurationKey(), renderer.getClass().getName()))
                    .append(Reporter.NEWLINE);
        }
        LOGGER.info(res.toString());
    }

    /**
     * Global server pages support bundle builder.
     */
    public static class ViewsBuilder {

        private final GlobalConfig config = new GlobalConfig();

        /**
         * Additional view renderers (template engines support) to use for {@link ViewBundle} configuration.
         * Duplicate renderers are checked by renderer key (e.g. "freemarker" or "mustache") and removed.
         * <p>
         * NOTE: default renderers are always loaded with service loader mechanism so registered listeners could only
         * extend the list of registered renderers (for those renderers which does not provide descriptor
         * for service loading).
         *
         * @param renderers renderers to use for global dropwizard views configuration
         * @return builder instance for chained calls
         * @see ViewBundle#ViewBundle(Iterable)
         */
        public ViewsBuilder addViewRenderers(final ViewRenderer... renderers) {
            config.addRenderers(renderers);
            return this;
        }

        /**
         * Configures configuration provider for {@link ViewBundle} (usually mapping from yaml configuration).
         * <p>
         * Note that if you need to just modify configuration in one of server pages bundles, you can do this
         * with {@link #viewsConfigurationModifier(String, ViewRendererConfigurationModifier)} - special mechanism
         * to overcome global views limitation.
         *
         * @param configurable views configuration lookup.
         * @param <T>          configuration object type
         * @return builder instance for chained calls
         * @see ViewBundle#getViewConfiguration(Object)
         * @see #viewsConfigurationModifier(String, ViewRendererConfigurationModifier)
         * @see #printViewsConfiguration()
         */
        public <T extends Configuration> ViewsBuilder viewsConfiguration(
                final ViewConfigurable<T> configurable) {
            config.setConfigurable(configurable);
            return this;
        }

        /**
         * Dropwizard views configuration modification. In contrast to views configuration object provider
         * ({@link #viewsConfiguration(ViewConfigurable)}), this method is not global and so modifications
         * from all registered server page applications will be applied.
         * <p>
         * The main use case is configuration of the exact template engine. For example, in case of freemarker
         * this could be used to apply auto includes:
         * <pre>{@code  .viewsConfigurationModifier("freemarker", config -> config
         *                         // expose master template
         *                         .put("auto_include", "/com/my/app/ui/master.ftl"))}</pre>
         * <p>
         * Note that configuration object is still global (because dropwizard views support is global) and so
         * multiple server page applications could modify configuration. For example, if multiple applications will
         * declare auto includes (example above) then only one include will be actually used. Use
         * {@link ViewsBuilder#printViewsConfiguration()} to see the final view configuration.
         *
         * @param name     renderer name (e.g. freemarker, mustache, etc.)
         * @param modifier modification callback
         * @return builder instance for chained calls
         */
        public ViewsBuilder viewsConfigurationModifier(
                final String name,
                final ViewRendererConfigurationModifier modifier) {
            // note: no need to log about it because it's global config (logs will appear if application register
            // configurer)
            config.addConfigModifier(name, modifier);
            return this;
        }

        /**
         * Prints configuration object used for dropwizard views bundle ({@link ViewBundle}). Note that
         * initial views configuration object binding is configured with
         * {@link #viewsConfiguration(ViewConfigurable)} and it could be modified with
         * {@link #viewsConfigurationModifier(String, ViewRendererConfigurationModifier)}. Printing of the final
         * configuration (after all modification) could be useful for debugging.
         *
         * @return builder instance for chained calls
         */
        public ViewsBuilder printViewsConfiguration() {
            config.printConfiguration();
            return this;
        }

        /**
         * Configures custom freemarker {@link freemarker.cache.TemplateLoader} so freemarker could also see
         * templates declared in custom class loaders.
         *
         * @return builder instance for chained calls
         * @see #app(String, String, String, ClassLoader)
         * @see #adminApp(String, String, String, ClassLoader)
         * @see #extendApp(String, ClassLoader)
         */
        public ViewsBuilder enableFreemarkerCustomClassLoadersSupport() {
            return viewsConfigurationModifier("freemarker", config ->
                    config.put(freemarker.template.Configuration.TEMPLATE_LOADER_KEY,
                            FreemarkerTemplateLoader.class.getName()));
        }

        /**
         * @return configured bundle instance
         */
        public ServerPagesBundle build() {
            return new ServerPagesBundle(config);
        }
    }
}

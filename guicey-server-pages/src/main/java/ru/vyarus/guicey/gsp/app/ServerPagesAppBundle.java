package ru.vyarus.guicey.gsp.app;

import com.google.common.base.Preconditions;
import io.dropwizard.views.common.ViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.guicey.gsp.ServerPagesBundle;
import ru.vyarus.guicey.gsp.app.filter.redirect.ErrorRedirect;
import ru.vyarus.guicey.gsp.views.ViewRendererConfigurationModifier;
import ru.vyarus.guicey.gsp.views.template.ManualErrorHandling;
import ru.vyarus.guicey.spa.SpaBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vyarus.dropwizard.guice.module.installer.util.PathUtils.SLASH;

/**
 * Bundle for server pages application installation (initialized with either
 * {@link ServerPagesBundle#app(String, String, String)} or
 * {@link ServerPagesBundle#adminApp(String, String, String)}).
 * <p>
 * NOTE: global views support must be registered before this bundle!
 *
 * @author Vyacheslav Rusakov
 * @since 05.06.2019
 */
public class ServerPagesAppBundle implements GuiceyBundle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPagesAppBundle.class);
    private static final String COMMA = ", ";

    private final ServerPagesApp app;
    private GlobalConfig config;

    public ServerPagesAppBundle(final ServerPagesApp app) {
        this.app = app;
    }

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        this.config = bootstrap.sharedStateOrFail(ServerPagesBundle.class,
                "Either server pages support bundle was not installed (use %s.builder() to create bundle) "
                        + " or it was installed after '%s' application bundle",
                ServerPagesBundle.class.getSimpleName(), app.name);
        // register application globally
        config.register(app);
    }

    @Override
    public void run(final GuiceyEnvironment environment) {
        validateRequirements();
    }

    private void validateRequirements() {
        if (app.requiredRenderers == null) {
            return;
        }
        final List<String> available = new ArrayList<>();
        final List<String> required = new ArrayList<>(app.requiredRenderers);
        for (ViewRenderer renderer : config.getRenderers()) {
            final String key = renderer.getConfigurationKey();
            available.add(key);
            required.remove(key);
        }
        Preconditions.checkState(required.isEmpty(),
                "Required template engines are missed for server pages application '%s': %s "
                        + "(available engines: %s)",
                app.name, String.join(COMMA, required), String.join(COMMA, available));
    }

    /**
     * Server pages application bundle builder.
     */
    public static class AppBuilder {
        private final ServerPagesApp app;
        private final ClassLoader loader;

        public AppBuilder(final boolean mainContext,
                          final String name,
                          final String path,
                          final String uri,
                          final ClassLoader loader) {
            this.app = new ServerPagesApp();
            // if loader is null, default loader would be set automatically by AssetSources
            this.loader = loader;

            app.mainContext = mainContext;
            app.name = checkNotNull(name, "Name is required");
            // both side paths are required!
            app.uriPath = PathUtils.leadingSlash(PathUtils.trailingSlash(uri));

            app.mainAssetsPath = PathUtils.normalizeClasspathPath(path);
            checkArgument(!SLASH.equals(app.mainAssetsPath), "%s is the classpath root", app.mainAssetsPath);
            // register main path for assets lookup
            app.assetLocations.attach(app.mainAssetsPath, loader);
        }

        /**
         * Specify default mapping prefix for views rest resources. If not declared, application name will be
         * used as mapping prefix.
         * <p>
         * For example, if you specify "com.mycompany/app1" as prefix then all registered rest resources,
         * starting from this path will be mapped to gsp application (could be called relatively to application root).
         * <p>
         * Only one prefix may be declared: error will be thrown on duplicate declaration.
         *
         * @param prefix rest prefix to map as root views
         * @return builder instance for chained calls
         * @see #mapViews(String, String) for mapping other rest views on sub urls
         */
        public AppBuilder mapViews(final String prefix) {
            app.viewPrefixes.map(prefix);
            return this;
        }

        /**
         * Map view rest to sub url, using different prefix than default. This way it is possible to use view rest,
         * not started with default prefix. Direct template handler will also be applied to this prefix (to support
         * direct template rendering).
         * <p>
         * Only one prefix may be applied to one url (error will be thrown on duplicate registration)! But other
         * mappings are possible for larger sub url (partially overlapping).
         * <p>
         * Additional views could also be mapped through extension registration:
         * {@link ServerPagesBundle#extendApp(String)}.
         * <p>
         * Pay attention that additional asset locations may be required ({@link #attachAssets(String, String)},
         * because only templates relative to view class will be correctly resolved, but direct templates may fail
         * to resolve.
         *
         * @param subUrl sub url to map views to
         * @param prefix rest prefix to map as root views
         * @return builder instance for chained calls
         */
        public AppBuilder mapViews(final String subUrl, final String prefix) {
            app.viewPrefixes.map(subUrl, prefix);
            return this;
        }

        /**
         * Specifies required template types (view renderes) for application. This setting is optional and used only for
         * immediate application startup failing when no required renderer is configured in global server pages bundle
         * ({@link ServerPagesBundle#builder()}).
         * <p>
         * Without declaring required renderer, application will simply serve template files "as is" when no
         * appropriate renderer found (because template file will not be recognized as template).
         * <p>
         * Renderer name is a renderer configuration key, defined in {@link ViewRenderer#getConfigurationKey()}.
         *
         * @param names required renderer names
         * @return builder instance for chained calls
         */
        public AppBuilder requireRenderers(final String... names) {
            app.requiredRenderers = Arrays.asList(names);
            return this;
        }

        /**
         * Shortcut for {@link #spaRouting(String)} with default regexp.
         *
         * @return builder instance for chained calls
         */
        public AppBuilder spaRouting() {
            return spaRouting(null);
        }

        /**
         * Enable single page application html5 routing support.
         *
         * @param noRedirectRegex regex to match all cases when redirection not needed
         * @return builder instance for chained calls
         * @see SpaBundle for more info how it works
         * @see SpaBundle.Builder#preventRedirectRegex(String) for more info about regexp
         */
        public AppBuilder spaRouting(final String noRedirectRegex) {
            if (noRedirectRegex != null) {
                app.spaNoRedirectRegex = noRedirectRegex;
            }
            app.spaSupport = true;
            return this;
        }

        /**
         * Declares index page (served for "/" calls). Index page may also be a template. If index view is handled
         * with a rest then simply leave as "" (default): resource on path "{restPath}/{appMapping}/"
         * will be used as root page.
         * <p>
         * Pay attention that index is not set by default to "index.html" because most likely it would be some
         * template handled with rest resource (and so it would be too often necessary to override default).
         *
         * @param name index file name (by default "")
         * @return builder instance for chained calls
         */
        public AppBuilder indexPage(final String name) {
            app.indexFile = name;
            return this;
        }

        /**
         * Default error page (shown in case of exceptions and for all error return codes (&gt;=400)).
         *
         * @param path either path to static resource (inside registered classpath path) or resource url
         *             (without app name prefix)
         * @return builder instance for chained calls
         * @see #errorPage(int, String) for registereing error page on exact return code
         */
        public AppBuilder errorPage(final String path) {
            return errorPage(ErrorRedirect.DEFAULT_ERROR_PAGE, path);
        }

        /**
         * Show special page instead of response with specified status code.
         * Errors are intercepted both for assets and template rendering. For templates, jersey request listener
         * used to intercept actual exceptions (to be able to access actual exception inside error page).
         * Default dropwizard exception mapper will log error (as for usual rest).
         * <p>
         * Error pages should use {@link ru.vyarus.guicey.gsp.views.template.ErrorTemplateView} as (base) model
         * class in order to get access to context exception. It is not required, if error object itself not required
         * during rendering.
         * <p>
         * NOTE that error page is returned only if original request accept html response and otherwise no
         * error page will be shown. Intention here is to show human readable errors only for humans.
         * <p>
         * IMPORTANT: GSP errors mechanism override ExceptionMapper and dropwizard-view ErrorEntityWriter mechanisms
         * because exception is detected before them and request is redirected to error page. Both ExceptionMapper
         * and EntityWriter would be called, but their result will be ignored (still, ExceptionMapper is useful
         * for errors logging). This was done to avoid influence of global ExceptionMapper's to be sure custom
         * error page used. It is possible to ignore GSP error mechanism for exact rest methods by using
         * {@link ManualErrorHandling} annotation.
         *
         * @param code error code to map page onto
         * @param path either path to static resource (inside registered classpath path) or resource url
         *             (without app name prefix)
         * @return builder instance for chained calls
         * @see #errorPage(String) for global errors page
         */
        public AppBuilder errorPage(final int code, final String path) {
            checkArgument(code >= ErrorRedirect.CODE_400 || code == ErrorRedirect.DEFAULT_ERROR_PAGE,
                    "Only error codes (4xx, 5xx) allowed for mapping");
            app.errorPages.put(code, path);
            return this;
        }

        /**
         * Add additional assets location. Useful if you need to serve files from multiple folders.
         * From usage perspective, files from all registered resource paths are "copied" into one directory
         * and application could reference everything from "there".
         * <p>
         * It is the same as separate extension registration with {@link ServerPagesBundle#extendApp(String)}.
         * <p>
         * NOTE: extended paths are used in priority so some file exists on the same path, extended path will
         * "override" primary location.
         *
         * @param path assets classpath path (may be in form of package (dot-separated))
         * @return builder instance for chained calls
         * @see #attachAssets(String, String) to register assets on specific sub url
         */
        public AppBuilder attachAssets(final String path) {
            app.assetLocations.attach(path, loader);
            return this;
        }

        /**
         * Essentially the same as {@link #attachAssets(String)}, but attach classpath assets to application
         * sub url. As with root assets, multiple packages could be attached to url. Registration order is important:
         * in case if multiple packages contains the same file, file from the latest registered package will be used.
         * <p>
         * Example usage: suppose you need to serve fonts from some 3rd party jar, so you need to map this package
         * to sub url with {@code attachAssetsForUrl("/fonts/", "com.some.package.with.fonts")}. After that
         * all assets from registered package will be accessible by this url (e.g /fonts/myfont.ttf).
         *
         * @param subUrl sub url to serve assets from
         * @param path   assets classpath paths (may be in form of package (dot-separated))
         * @return builder instance for chained calls
         */
        public AppBuilder attachAssets(final String subUrl, final String path) {
            app.assetLocations.attach(subUrl, path, loader);
            return this;
        }

        /**
         * Shortcut for {@code #attachAssets("META-INF/resources/webjars/")}).
         * Useful if you want to use resources from webjars. All webjars package resources under the same path
         * (e.g. META-INF/resources/webjars/jquery/3.4.1/dist/jquery.min.js), so after enabling webjars support
         * you can reference any resource from webjar (in classpath) (e.g. as
         * {@code <script src="jquery/3.4.1/dist/jquery.min.js">}).
         *
         * @return builder instance for chained calls
         */
        public AppBuilder attachWebjars() {
            return attachAssets("META-INF/resources/webjars/");
        }

        /**
         * Differentiation of template call from static resource is based on fact: static resources
         * have extensions. So when "/something/some.ext" is requested and extension is not supported template
         * extension then it's direct asset. In case when you have static files without extension, you can
         * include them directly into detection regexp (using regex or (|) syntax).
         * <p>
         * Pattern must return detected file name as first matched group (so direct template could be detected).
         * Pattern is searched (find) inside path, not matched (so simple patterns will also work).
         *
         * @param regex regex for file request detection and file name extraction
         * @return builder instance for chained calls
         * @see ServerPagesBundle#FILE_REQUEST_PATTERN default pattern
         */
        public AppBuilder filePattern(final String regex) {
            app.fileRequestPattern = checkNotNull(regex, "Regex can't be null");
            return this;
        }

        /**
         * Dropwizard views configuration modification. Views configuration could be bound only in global server pages
         * support bundle
         * ({@link ServerPagesBundle.ViewsBuilder#viewsConfiguration(io.dropwizard.views.common.ViewConfigurable)}).
         * But it's often required to "tune" template engine specifically for application. This method allows global
         * views configuration modification for exact server pages application.
         * <p>
         * The main use case is configuration of the exact template engine. For example, in case of freemarker
         * this could be used to apply auto includes:
         * <pre>{@code  .viewsConfigurationModifier("freemarker", config -> config
         *                         // expose master template
         *                         .put("auto_include", "/com/my/app/ui/master.ftl"))}</pre>
         * <p>
         * Note that configuration object is still global (because dropwizard views support is global) and so
         * multiple server pages applications could modify configuration. For example, if multiple applications will
         * declare auto includes (example above) then only one include will be actually used. Use
         * {@link ServerPagesBundle.ViewsBuilder#printViewsConfiguration()} to see the final view configuration.
         *
         * @param name     renderer name (e.g. freemarker, mustache, etc.)
         * @param modifier modification callback
         * @return builder instance for chained calls
         */
        public AppBuilder viewsConfigurationModifier(
                final String name,
                final ViewRendererConfigurationModifier modifier) {
            // in case of multiple applications, it should be obvious from logs who changed config
            LOGGER.info("Server pages application '{}' modifies '{}' section of views configuration",
                    app.name, name);
            app.viewsConfigModifiers.put(name, modifier);
            return this;
        }

        /**
         * @return configured dropwizard bundle instance
         */
        public ServerPagesAppBundle build() {
            return new ServerPagesAppBundle(app);
        }
    }
}

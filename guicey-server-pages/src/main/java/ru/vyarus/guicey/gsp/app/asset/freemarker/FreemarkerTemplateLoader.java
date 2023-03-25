package ru.vyarus.guicey.gsp.app.asset.freemarker;

import freemarker.cache.URLTemplateLoader;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;

import java.net.URL;

/**
 * Special template loader for freemarker, required to support custom assets class loaders.
 * It is not required if custom class loaders not used!
 * <p>
 * The problem is that dropwizard views mechanism accepts only template path from classpath so if template
 * provided by a custom class loader, default mechanism would try to load it with application class loader.
 * This loader would take template name (already prepared to be absolute classpath path) and search it through
 * all registered class loaders (based on classpath prefixes).
 * <p>
 * Loader installed with main builder configuration
 * {@link ru.vyarus.guicey.gsp.ServerPagesBundle.ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}
 * shortcut (applied globally).
 *
 * @author Vyacheslav Rusakov
 * @since 12.04.2020
 */
public class FreemarkerTemplateLoader extends URLTemplateLoader {

    @Override
    protected URL getURL(final String name) {
        // ASSUMPTION: gsp prepared correct absolute url for the template and so this is already correct
        // template classpath location. Anyway, relative path would also be correctly resolved by fallback logic.
        return TemplateContext.getInstance().loadAsset(name);
    }
}

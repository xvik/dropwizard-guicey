package ru.vyarus.guicey.gsp.views.test.util;

import com.google.common.collect.ImmutableListMultimap;
import ru.vyarus.guicey.gsp.app.asset.AssetLookup;
import ru.vyarus.guicey.gsp.app.filter.redirect.TemplateRedirect;
import ru.vyarus.guicey.gsp.views.template.TemplateContext;

// Checkstyle disabled due to a false positive on JavadocType check like "Unknown tag 'Inject'."
// CHECKSTYLE:OFF
/**
 * Dummy implementation of template context for tests. Usage:
 * <pre>{@code
 * @TestGuiceyApp(MyApp.class)
 * public class MyTest {
 *
 *      @Inject
 *      MyViewRest rest;
 *
 *      @BeforeTest
 *      void setUp() {
 *          new TestTemplateContext().enable();
 *      }
 *
 *      @Test
 *      void test() {
 *          MyViewModel model = rest.callMethod()
 *      }
 * }
 * }</pre>
 * <p>
 * Note that a real http call is not performed (view resources produce test-html so it would not be possible
 * to receive model with a direct http call). Additionally, view template is not rendered (that's why we only need
 * to provide any real file instead of template to workaround template existence verification)!
 * <p>
 * Normally, gsp filter redirects into the rest context inside
 * {@link ru.vyarus.guicey.gsp.app.filter.redirect.TemplateRedirect} class. This class applies a dummy context,
 * which is enough to create a view instance (which checks template presence on creation).
 * <p>
 * This approach is very limited: here we only simulate context presence so resource method could create
 * model instance, but, if target method requires http-related objects, this will not work.
 * Better use {@link ru.vyarus.guicey.gsp.views.test.jupiter.InterceptViewModel} extension
 * (or {@link ru.vyarus.guicey.gsp.views.test.ext.ViewModelHook} for generic cases).
 *
 * @author Vyacheslav Rusakov
 * @since 04.12.2025
 */
// CHECKSTYLE:ON
public class TestTemplateContext extends TemplateContext {

    /**
     * Create test context with default config (config is not important).
     */
    public TestTemplateContext() {
        this("test", "/", "/test", "/view/");
    }

    /**
     * Create test context with custom configuration.
     *
     * @param appName                application name
     * @param rootUrl                root url
     * @param restSubContext         rest sub context
     * @param restPrefix             rest prefix
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TestTemplateContext(final String appName,
                               final String rootUrl,
                               final String restSubContext,
                               final String restPrefix) {
        super(appName, rootUrl, restSubContext, restPrefix, null,
                new AssetLookup("/", ImmutableListMultimap.of(), ImmutableListMultimap.of()),
                null, null, null);
        // dummy template: this file must actually exist so a model object could be created, but template itself
        // is not important as no actual rendering would be performed
        setAnnotationTemplate("/test/test.test");
    }

    /**
     * Register context. Must be called before view rest execution.
     */
    public void enable() {
        TemplateRedirect.setContext(this);
    }
}

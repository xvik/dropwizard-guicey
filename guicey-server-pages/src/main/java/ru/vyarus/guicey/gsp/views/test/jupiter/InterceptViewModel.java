package ru.vyarus.guicey.gsp.views.test.jupiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Checkstyle disabled due to a false positive on JavadocType check like "Unknown tag 'InterceptViewModel'."
// CHECKSTYLE:OFF
/**
 * Intercepts raw view model, used for HTML generation. Testing generated views HTML is not very easy, but pure model,
 * used for HTML rendering, could be tested easily.
 * <p>
 * Important: this wil not work with rest stubs ({@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest})
 * for gsp calls because it requires HTTP filter (which redirects to view resource).
 * <p>
 * Usage:
 * <pre>{@code
 * @TestDropwizardApp(MyApp.class)
 * public class MyTest {
 *      @InterceptViewModel
 *      ViewModelTracker modelTracker;
 *
 *      @WebClient(WebClientType.App)
 *      TestClient<?> client;
 *
 *      @Test
 *      void testModel() {
 *         // normal call for a view: real rendered html returned
 *         // Note that rest client is not used due to gsp usage (resource url differs)
 *         final String sample = client.get("sample", String.class);
 *         // model, used for render is intercepted
 *         final ViewModel trackedModel = modelTracker.getLastModel();
 *         final Model model = trackedModel.getModel();
 *      }
 *
 *      // model
 *      public static class Model extends TemplateView {
 *         private String name;
 *
 *         public Model(String name) {
 *             this.name = name;
 *         }
 *
 *         public String getName() {
 *             return name;
 *         }
 *      }
 *
 *      // resource
 *      @Path("/views/app")
 *      @Produces(MediaType.TEXT_HTML)
 *      @Template("directTemplate.ftl")
 *      public static class SampleRest {
 *
 *          @GET
 *          @Path("sample")
 *          public Model get() {
 *              return new Model("sample");
 *          }
 *       }
 * }
 * }</pre>
 * <p>
 * In this approach, the HTML page is actually rendered, so we could be sure that the model is correct for error-less
 * rendering. Note that model could be verified for view rendering errors too (model intercepted before actual
 * rendering).
 * <p>
 * Internally, extension uses resource response filter to intercept a raw model before actual HTML generation
 * (in the response writer). So, the test performs a completely normal view request and just intercepts the model.
 * <p>
 * Note that the tracker aggregates all view calls, so you can call multiple views and all models would be intercepted.
 * Works only for views annotated with {@link ru.vyarus.guicey.gsp.views.template.Template}, because otherwise
 * it would intercept all rest calls (which might bring some problems in testing).
 * <p>
 * By default, error page models are ignored (with code >= 400) to avoid confusion when testing error cases.
 * To intercept all models enable {@link #interceptErrors()} flag.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.gsp.views.test.ext.ViewModelHook for junit-less tests
 * @since 05.12.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// CHECKSTYLE:ON
public @interface InterceptViewModel {

    /**
     * When view rendering failed (response code >=400), in most cases, a special error view would be rendered.
     * Normally, this error model ({@link io.dropwizard.jersey.errors.ErrorMessage}) would also be intercepted,
     * but, for most cases, it would be not expected. To provide more predictable behavior, error response models
     * are ignored. When enabled, the latest model would be the error page model.
     *
     * @return true to intercept error models
     */
    boolean interceptErrors() default false;
}

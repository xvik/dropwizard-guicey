package ru.vyarus.dropwizard.guice.test.jupiter.ext.responsemodel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Checkstyle disabled due to a false positive on JavadocType check like "Unknown tag 'InterceptViewModel'."
// CHECKSTYLE:OFF
/**
 * Intercepts a raw response model (what resource method returned) from the real HTTP view call. It is useful when
 * a model object is serialized with a specialized writer, and so there is no way to get a raw model in response.
 * The best example is dropwizard-views where a model instance is used for HTML generation, but HTML validation is more
 * complex than checking the prepared model for correctness.
 * Also, it could be used for model serialization correctness check: you'll have a deserialized object from response
 * and intercepted original instance and compare them (useful in cases with complex serialization like time fields).
 * <p>
 * Interception appears inside the response filter before model processing (by custom writer). In case of views,
 * it means model is intercepted before HTML generation.
 * <p>
 * Important: interception works for any rest method, producing non-null entity.
 * <p>
 * Usage:
 * <pre>{@code
 * @TestDropwizardApp(MyApp.class)
 * public class MyTest {
 *      @InterceptModel
 *      ModelTracker modelTracker;
 *
 *      @WebClient(WebClientType.App)
 *      TestClient<?> client;
 *
 *      @Test
 *      void testModel() {
 *         // normal call for a view: real rendered html returned
 *         final String sample = client.get("sample", String.class);
 *         // model, used for render is intercepted
 *         final ResponseModel trackedModel = modelTracker.getLastModel();
 *         final Model model = trackedModel.getModel();
 *         // verifying model data (used for template rendering)
 *         Assertions.assertEquals("sample", model.getName());
 *      }
 *
 *      // model
 *      public static class Model extends TemplateView {
 *         private String name;
 *
 *         public Model(String name) {
 *             super("sample.ftl");
 *             this.name = name;
 *         }
 *
 *         public String getName() {
 *             return name;
 *         }
 *      }
 *
 *      // resource
 *      @Path("/")
 *      @Produces(MediaType.TEXT_HTML)
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
 * In the example above, the HTML page is actually rendered, so we could be sure that the model is correct for
 * error-less rendering. Note that model could be verified for view rendering errors too (model intercepted before
 * actual rendering).
 * <p>
 * Interception is performed with a custom {@link jakarta.ws.rs.container.ContainerResponseFilter}. If application
 * use other custom filters then interception will appear AFTER these filters (because filter-intercepter will be
 * registered after application filters).
 * <p>
 * Note that the tracker aggregates all rest calls, so you can call multiple rest methods and all models would be
 * intercepted.
 * <p>
 * By default, errors (status >= 400) are ignored to avoid confusion. For example, in case of view: view model
 * intercepted, but then template rendering fails - dropwizard will response with a custom error model, which is also
 * intercepted. By default, {@code tarcker.getLastModel()} would return model prepared by view.
 * Error model interception could be enabled and, in this case, the example above would return error model as
 * the last intercepted model: {@link #interceptErrors()}.
 * <p>
 * Note that {@link ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker} is a guice bean and could be obtained
 * from the injector (if required outside the test).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.responsemodel.ModelInterceptorHook for junit-less tests
 * @since 05.12.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// CHECKSTYLE:ON
public @interface InterceptModel {

    /**
     * When response model serialization failed (response code >=400), in most cases, a special error response view
     * would be rendered. Normally, this error model ({@link io.dropwizard.jersey.errors.ErrorMessage}) would also
     * be intercepted, but, for most cases, it would be not expected. To provide more predictable behavior, error
     * response models are ignored. When enabled, the latest model would be the error page model.
     *
     * @return true to intercept error models
     */
    boolean interceptErrors() default false;
}

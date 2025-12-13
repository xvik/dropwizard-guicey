package ru.vyarus.dropwizard.guice.test.responsemodel;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.GuiceyOptions;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.test.responsemodel.intercept.ResponseInterceptorModule;

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
 * Usage: <pre>{@code
 *   ModelInterceptorHook hook = new ModelInterceptorHook();
 *   // ... register hook
 *
 *   // after application startup (!) obtain tracker (could be obtained after HTTP call)
 *   ModelTracker tracker = hook.getTracker();
 *
 *   // perform resource call (e.g. view resource returning HTML)
 *   String res = client.get("/view/path", String.class);
 *
 *   // verify intercepted model
 *   // wrapper object is required to preserve context (to easily distinguish models)
 *   ResponseModel model = tracker.getLastModel();
 *   // actual view model (returned from resource)
 *   MyView actualViewModel = model.getModel();
 * }</pre>
 * <p>
 * By default, errors (status >= 400) are ignored to avoid confusion. For example, in case of view: view model
 * intercepted, but then template rendering fails - dropwizard will response with a custom error model, which is also
 * intercepted. By default, {@code tarcker.getLastModel()} would return model prepared by view.
 * Error model interception could be enabled and, in this case, the example above would return error model as
 * the last intercepted model.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
public class ModelInterceptorHook implements GuiceyConfigurationHook {

    private final boolean interceptErrors;
    private ModelTracker tracker;

    /**
     * Creates hook instance.
     *
     * @param interceptErrors true to intercept error pages model
     */
    public ModelInterceptorHook(final boolean interceptErrors) {
        this.interceptErrors = interceptErrors;
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        // filter intercepts view response before writer to extract raw model object
        builder.modules(new ResponseInterceptorModule(interceptErrors))
                // ResponseInterceptorFilter extension is bound in module and will be installed from bindings
                .onGuiceyStartup((config, env, injector) -> {
                    // when disabled extensions not searched in modules
                    Preconditions.checkState(injector.getInstance(Options.class).get(GuiceyOptions.AnalyzeGuiceModules),
                            "%s option is disabled, but it's required for extension to work properly",
                            GuiceyOptions.AnalyzeGuiceModules.name());
                    tracker = injector.getInstance(ModelTracker.class);
                });
    }

    /**
     * Important: could be called only after application startup! There is only one instance
     * of tracker per application, so it could be obtained at any moment (after startup).
     *
     * @return tracker, containing all intercepted models
     */
    public ModelTracker getTracker() {
        return Preconditions.checkNotNull(tracker, "Tracker not initialized yet");
    }
}

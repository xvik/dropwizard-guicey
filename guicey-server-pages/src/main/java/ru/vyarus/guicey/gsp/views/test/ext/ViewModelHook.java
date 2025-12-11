package ru.vyarus.guicey.gsp.views.test.ext;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.guicey.gsp.views.test.ext.interceptor.ViewModelInterceptorModule;

/**
 * Intercepts raw view model from the real HTTP view call. Used for view validation (because it is easier to
 * verify model correctness then generated HTML).
 * <p>
 * Works only for GSP calls (limited to resources annotated with {@link ru.vyarus.guicey.gsp.views.template.Template}).
 * <p>
 * Usage: <pre>{@code
 *   ViewModelHook hook = new ViewModelHook();
 *   // ... register hook
 *
 *   // after application startup (!) obtain tracker (could be obtained after HTTP call)
 *   ViewModelTracker tracker = hook.getTracker();
 *
 *   // perform HTTP view call
 *   String res = client.get("/view/path", String.class);
 *
 *   // verify intercepted model
 *   // model is not returned directly to let you distinguish different calls (context remains)
 *   ViewModel model = tracker.getLastModel();
 *   // actual view model (returned from resource)
 *   MyView actualViewModel = model.getModel();
 * }</pre>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.gsp.views.test.jupiter.InterceptViewModel for junit support
 * @since 05.12.2025
 */
public class ViewModelHook implements GuiceyConfigurationHook {

    private final boolean interceptErrors;
    private ViewModelTracker tracker;

    /**
     * Creates hook instance.
     *
     * @param interceptErrors true to intercept error pages model
     */
    public ViewModelHook(final boolean interceptErrors) {
        this.interceptErrors = interceptErrors;
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        // filter intercepts view response before writer to extract raw model object
        builder.modules(new ViewModelInterceptorModule(interceptErrors))
                // ViewModelFilter extension is bound in module and will be installed from bindings
                .onGuiceyStartup((config, env, injector) -> {
                    tracker = injector.getInstance(ViewModelTracker.class);
                });
    }

    /**
     * Important: could be called only after application startup! There is only one instance
     * of tracker per application, so it could be obtained at any moment (after startup).
     *
     * @return tracker, containing all intercepted models
     */
    public ViewModelTracker getTracker() {
        return Preconditions.checkNotNull(tracker, "Tracker not initialized yet");
    }
}

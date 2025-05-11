package ru.vyarus.dropwizard.guice.test.jupiter.env.listen;

import com.google.inject.Injector;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport;

/**
 * Event context wraps junit {@link org.junit.jupiter.api.extension.ExtensionContext} and provides access for
 * the main test objects (like injector, test support and http client). Custom object is required mainly for
 * lambda even listeners, which are unable to see default interface methods.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public class EventContext {

    private final ExtensionContext context;
    private final boolean debug;

    /**
     * Create event context.
     *
     * @param context extension context
     * @param debug   true if debug enabled in extension
     */
    public EventContext(final ExtensionContext context, final boolean debug) {
        this.context = context;
        this.debug = debug;
    }

    /**
     * @return junit context
     */
    public ExtensionContext getJunitContext() {
        return context;
    }

    /**
     * Useful to bind debug options on the extension debug (no need for additional keys).
     *
     * @return true if debug is enabled on guicey extension
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Normally, it is impossible that support would not be found (under called lifecycle methods).
     *
     * @return dropwizard support object (or guicey support)
     * @throws IllegalStateException if the support object not found (should be impossible)
     */
    public DropwizardTestSupport<?> getSupport() {
        return GuiceyExtensionsSupport.lookupSupport(context)
                .orElseThrow(() -> new IllegalStateException("Test support not found"));
    }

    /**
     * Normally, it is impossible that injector would not be found (under called lifecycle methods).
     *
     * @return injector instance
     * @throws IllegalStateException if the injector object not found (should be impossible)
     */
    public Injector getInjector() {
        return GuiceyExtensionsSupport.lookupInjector(context)
                .orElseThrow(() -> new IllegalStateException("Injector not found"));
    }

    /**
     * Shortcut to get bean directly from injector.
     *
     * @param type bean class
     * @param <T>  bean type
     * @return bean instance, never null (throw error if not found)
     */
    public <T> T getBean(final Class<T> type) {
        return getInjector().getInstance(type);
    }

    /**
     * Note that client is created even for pure guicey tests (in case if something external must be called).
     *
     * @return client instance
     * @throws IllegalStateException if the client object not found (should be impossible)
     */
    public ClientSupport getClient() {
        return GuiceyExtensionsSupport.lookupClient(context)
                .orElseThrow(() -> new IllegalStateException("Client not found"));
    }

    /**
     * @return true if complete application started, false for guice-only part
     */
    public boolean isWebStarted() {
        return !(getSupport() instanceof GuiceyTestSupport<?>);
    }
}

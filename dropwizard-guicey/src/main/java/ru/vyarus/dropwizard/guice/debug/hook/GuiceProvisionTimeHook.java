package ru.vyarus.dropwizard.guice.debug.hook;

import com.google.common.collect.ListMultimap;
import com.google.inject.Binding;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.debug.GuiceProvisionDiagnostic;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

import java.time.Duration;

/**
 * Hook enables guice provision time logs. It is assumed to be used to enable provision time logs for compiled
 * application with system property: {@code -Dguicey.hooks=provision-time}.
 * <p>
 * Also, hook could be used in tests to track created guice beans:
 * <pre><code>
 *   {@literal @}EnableHook
 *    static GuiceProvisionTimeHook hook = new GuiceProvisionTimeHook();
 *
 *   {@literal @}Test
 *    public void test() {
 *        hook.clearData()
 *        // anything requiring provision
 *        injector.getInstance(SomeService.class);
 *        // the report would contain only one bean creation
 *        System.out.println(hook.renderReport())
 *    }
 * </code></pre>
 *
 * @author Vyacheslav Rusakov
 * @since 25.03.2025
 */
public class GuiceProvisionTimeHook implements GuiceyConfigurationHook {

    public static final String ALIAS = "provision-time";
    private final GuiceProvisionDiagnostic diagnostic = new GuiceProvisionDiagnostic(true);

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        // this is the same as .printGuiceProvisionTime() call, but hook could be used in tests
        builder.bundles(diagnostic);
    }

    /**
     * Clear collected data.
     */
    public void clearData() {
        diagnostic.clear();
    }

    /**
     * Map format: binding - provisions time.
     *
     * @return recorded provision data
     */
    public ListMultimap<Binding<?>, Duration> getRecordedData() {
        return diagnostic.getRecordedData();
    }

    /**
     * @return render beans creation report from collected data
     */
    public String renderReport() {
        return diagnostic.renderReport();
    }
}

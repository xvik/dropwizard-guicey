package ru.vyarus.guicey.gsp.views.test.jupiter;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.guicey.gsp.views.test.ext.ViewModelHook;
import ru.vyarus.guicey.gsp.views.test.ext.ViewModelTracker;

import java.util.List;

/**
 * {@link ru.vyarus.guicey.gsp.views.test.jupiter.InterceptViewModel} test fields support implementation.
 * <p>
 * Clears recorded models after each test method.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
public class ViewModelTestSupport extends AnnotatedTestFieldSetup<InterceptViewModel, ViewModelTracker> {

    private static final String TEST_VIEWS_FIELDS = "TEST_VIEWS_FIELDS";
    private ViewModelHook hook;

    /**
     * Create view interceptor extension.
     */
    public ViewModelTestSupport() {
        super(InterceptViewModel.class, ViewModelTracker.class, TEST_VIEWS_FIELDS);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context,
                                 final AnnotatedField<InterceptViewModel, ViewModelTracker> field) {
        Preconditions.checkState(hook == null, "Only one @%s field should be declared in test",
                InterceptViewModel.class.getSimpleName());
        hook = new ViewModelHook(field.getAnnotation().interceptErrors());
    }

    @Override
    protected void registerHooks(final TestExtension extension) {
        extension.hooks(hook);
    }

    @Override
    protected <K> void initializeField(final AnnotatedField<InterceptViewModel, ViewModelTracker> field,
                                       final ViewModelTracker userValue) {
        // not used
    }

    @Override
    protected void beforeValueInjection(final EventContext context,
                                        final AnnotatedField<InterceptViewModel, ViewModelTracker> field) {
        // not used
    }

    @Override
    protected ViewModelTracker injectFieldValue(final EventContext context,
                                                final AnnotatedField<InterceptViewModel, ViewModelTracker> field) {
        return hook.getTracker();
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void report(final EventContext context,
                          final List<AnnotatedField<InterceptViewModel, ViewModelTracker>> annotatedFields) {
        final StringBuilder report = new StringBuilder("\nView model interception enabled (@")
                .append(InterceptViewModel.class.getSimpleName()).append(") on ").append(setupContextName)
                .append(":\n\n");
        fields.forEach(field -> report.append(
                String.format("\t%-40s",
                        field.getField().getDeclaringClass().getSimpleName() + "." + field.getField().getName())
        ));
        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<InterceptViewModel, ViewModelTracker> field,
                              final ViewModelTracker value) {
        // not used
    }

    @Override
    protected void afterTest(final EventContext context,
                             final AnnotatedField<InterceptViewModel, ViewModelTracker> field,
                             final ViewModelTracker value) {
        if (value != null) {
            // clear models after each test method
            value.clear();
        }
    }
}

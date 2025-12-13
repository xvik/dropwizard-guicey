package ru.vyarus.dropwizard.guice.test.jupiter.ext.responsemodel;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelInterceptorHook;

import java.util.List;

/**
 * {@link InterceptModel} test fields support implementation.
 * <p>
 * Clears recorded models after each test method.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
public class ModelFieldsSupport extends AnnotatedTestFieldSetup<InterceptModel, ModelTracker> {

    private static final String TEST_VIEWS_FIELDS = "TEST_VIEWS_FIELDS";
    private ModelInterceptorHook hook;

    /**
     * Create resource model interceptor extension.
     */
    public ModelFieldsSupport() {
        super(InterceptModel.class, ModelTracker.class, TEST_VIEWS_FIELDS);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context,
                                 final AnnotatedField<InterceptModel, ModelTracker> field) {
        Preconditions.checkState(hook == null, "Only one @%s field should be declared in test",
                InterceptModel.class.getSimpleName());
        hook = new ModelInterceptorHook(field.getAnnotation().interceptErrors());
    }

    @Override
    protected void registerHooks(final TestExtension extension) {
        extension.hooks(hook);
    }

    @Override
    protected <K> void initializeField(final AnnotatedField<InterceptModel, ModelTracker> field,
                                       final ModelTracker userValue) {
        // not used
    }

    @Override
    protected void beforeValueInjection(final EventContext context,
                                        final AnnotatedField<InterceptModel, ModelTracker> field) {
        // not used
    }

    @Override
    protected ModelTracker injectFieldValue(final EventContext context,
                                                final AnnotatedField<InterceptModel, ModelTracker> field) {
        return hook.getTracker();
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void report(final EventContext context,
                          final List<AnnotatedField<InterceptModel, ModelTracker>> annotatedFields) {
        final StringBuilder report = new StringBuilder("\nResource response model interception enabled (@")
                .append(InterceptModel.class.getSimpleName()).append(") on ").append(setupContextName)
                .append(":\n\n");
        fields.forEach(field -> report.append(
                String.format("\t%-40s",
                        field.getField().getDeclaringClass().getSimpleName() + "." + field.getField().getName())
        ));
        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<InterceptModel, ModelTracker> field,
                              final ModelTracker value) {
        // not used
    }

    @Override
    protected void afterTest(final EventContext context,
                             final AnnotatedField<InterceptModel, ModelTracker> field,
                             final ModelTracker value) {
        if (value != null) {
            // clear models after each test method
            value.clear();
        }
    }
}

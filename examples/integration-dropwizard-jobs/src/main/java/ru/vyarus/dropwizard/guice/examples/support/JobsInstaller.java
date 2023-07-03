package ru.vyarus.dropwizard.guice.examples.support;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.jobs.Job;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

/**
 * Installer performs utility tasks:
 * - searches for jobs and bind them to guice context (so {@link JobsManager} could install them
 * - print registered jobs to console
 *
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
public class JobsInstaller implements FeatureInstaller, BindingInstaller {

    private final Reporter reporter = new Reporter(JobsInstaller.class, "jobs =");

    @Override
    public boolean matches(Class<?> type) {
        return FeatureUtils.is(type, Job.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bind(Binder binder, Class<?> type, boolean lazy) {
        Preconditions.checkArgument(!lazy, "Job bean can't be lazy: %s", type.getName());
        registerJob(binder, (Class<? extends Job>) type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void manualBinding(Binder binder, Class<T> type, Binding<T> binding) {
        registerJob(binder, (Class<? extends Job>) type);
    }

    @Override
    public void report() {
        reporter.report();
    }

    private void registerJob(final Binder binder, final Class<? extends Job> type) {
        Multibinder.newSetBinder(binder, Job.class).addBinding().to(type);

        // here we can also look for class annotations and show more info in console
        // (omitted for simplicity)
        reporter.line("(%s)", type.getName());
    }
}

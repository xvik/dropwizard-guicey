package ru.vyarus.dropwizard.guice.examples.support;

import com.google.inject.Injector;
import io.dropwizard.jobs.Job;
import io.dropwizard.jobs.JobManager;
import org.quartz.spi.JobFactory;
import ru.vyarus.dropwizard.guice.examples.JobsAppConfiguration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Set;

/**
 * Bean will be recognized as Managed and installed automatically.
 * Note that native dropwizard-jobs-guice module is NOT used because it scans entire injector whereas all
 * jobs are revealed by the installer. Also, since dropwizard-jobs 5.1 guice module depends on guice 7 which can't be
 * used with dropwizard 3.
 *
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
@Singleton
public class JobsManager extends JobManager {

    private final GuiceJobFactory factory;

    @Inject
    public JobsManager(final Injector injector, final Set<Job> jobs, final JobsAppConfiguration config) {
        super(config, new ArrayList<>(jobs));
        this.factory = new GuiceJobFactory(injector);
    }

    @Override
    protected JobFactory getJobFactory() {
        return factory;
    }
}

package ru.vyarus.dropwizard.guice.examples.support;

import com.google.inject.Injector;
import io.dropwizard.jobs.GuiceJobManager;
import ru.vyarus.dropwizard.guice.examples.JobsAppConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Bean will be recognized as Managed and installed automatically.
 * Used as a replacement for {@link io.dropwizard.jobs.GuiceJobsBundle}.
 *
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
@Singleton
public class JobsManager extends GuiceJobManager {

    @Inject
    public JobsManager(Injector injector, JobsAppConfiguration configuration) {
        super(configuration, injector);
    }
}

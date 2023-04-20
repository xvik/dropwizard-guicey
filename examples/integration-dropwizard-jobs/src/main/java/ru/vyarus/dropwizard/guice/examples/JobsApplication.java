package ru.vyarus.dropwizard.guice.examples;

import com.codahale.metrics.SharedMetricRegistries;
import io.dropwizard.Application;
import io.dropwizard.jobs.Job;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
public class JobsApplication extends Application<JobsAppConfiguration> {

    public static void main(String[] args) throws Exception {
        new JobsApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<JobsAppConfiguration> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig()
                .build());

        // force dropwizard-jobs using main metrics registry for all jobs
        SharedMetricRegistries.add(Job.DROPWIZARD_JOBS_KEY, bootstrap.getMetricRegistry());
    }

    @Override
    public void run(JobsAppConfiguration configuration, Environment environment) throws Exception {

    }
}

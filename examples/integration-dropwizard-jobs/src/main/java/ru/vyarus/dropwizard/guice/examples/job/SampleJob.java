package ru.vyarus.dropwizard.guice.examples.job;

import io.dropwizard.jobs.Job;
import io.dropwizard.jobs.annotations.Every;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Singleton;

/**
 * Job will be registered automatically by {@link io.dropwizard.jobs.GuiceJobManager}
 * (which is activate in  {@link ru.vyarus.dropwizard.guice.examples.support.JobsManager}).
 *
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
@Singleton
@Every("1s")
public class SampleJob extends Job {

    public boolean iDidIt;

    @Override
    public void doJob(JobExecutionContext context) throws JobExecutionException {
        iDidIt = true;
    }
}

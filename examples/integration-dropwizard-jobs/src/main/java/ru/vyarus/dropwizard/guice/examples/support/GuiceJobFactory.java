package ru.vyarus.dropwizard.guice.examples.support;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 03.07.2023
 */
public class GuiceJobFactory implements JobFactory {

    private final Injector injector;

    @Inject
    public GuiceJobFactory(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
        return injector.getInstance(bundle.getJobDetail().getJobClass());
    }
}

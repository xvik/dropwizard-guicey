package ru.vyarus.dropwizard.guice.examples.hbn;

import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import ru.vyarus.dropwizard.guice.examples.HbnAppConfiguration;
import ru.vyarus.dropwizard.guice.examples.model.Sample;

/**
 * Configured dropwizard hibernate bundle.
 *
 * @author Vyacheslav Rusakov
 * @since 12.06.2016
 */
public class HbnBundle extends HibernateBundle<HbnAppConfiguration> {

    public HbnBundle() {
        super(Sample.class);
    }

    @Override
    public PooledDataSourceFactory getDataSourceFactory(HbnAppConfiguration configuration) {
        return configuration.getDataSourceFactory();
    }
}

package ru.vyarus.dropwizard.guice.examples.service;

import com.google.inject.Inject;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import ru.vyarus.dropwizard.guice.examples.model.Sample;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;

import java.util.List;

/**
 * Sample service, buld using dropwizard dao abstraction.
 * <p>
 * NOTE: @EagerSingleton is not required here, but used to force bean instance creation with guice context
 * in order to demonstrate session factory availability.
 *
 * @author Vyacheslav Rusakov
 * @since 12.06.2016
 */
@EagerSingleton
public class SampleService extends AbstractDAO<Sample> {

    @Inject
    public SampleService(final SessionFactory factory) {
        super(factory);
    }

    public long create(Sample sample) {
        return persist(sample).getId();
    }

    @SuppressWarnings("unchecked")
    public List<Sample> findAll() {
        return list(currentSession().createQuery("from Sample"));
    }
}

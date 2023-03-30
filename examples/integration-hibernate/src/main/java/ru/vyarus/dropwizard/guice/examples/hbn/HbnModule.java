package ru.vyarus.dropwizard.guice.examples.hbn;

import com.google.inject.AbstractModule;
import org.hibernate.SessionFactory;

/**
 * Guice module for {@link SessionFactory} binding.
 *
 * @author Vyacheslav Rusakov
 * @since 12.06.2016
 */
public class HbnModule extends AbstractModule {

    private final HbnBundle hbnBundle;

    public HbnModule(HbnBundle hbnBundle) {
        this.hbnBundle = hbnBundle;
    }

    @Override
    protected void configure() {
        // if hibernate bundle was registered before guice, then at this point it's run method
        // will be already called and so its safe to get session factory instance
        bind(SessionFactory.class).toInstance(hbnBundle.getSessionFactory());
    }
}

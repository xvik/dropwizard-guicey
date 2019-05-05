package ru.vyarus.dropwizard.guice.injector.jersey.scope;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * @author Vyacheslav Rusakov
 * @since 23.04.2019
 */
public class ThreadScopeModule extends AbstractModule {

    @Override
    protected void configure() {
        ThreadScope scope = new ThreadScope();
        bindScope(ThreadScoped.class, scope);

        bind(ThreadScope.class)
                .annotatedWith(Names.named("threadScope"))
                .toInstance(scope);
    }
}

package ru.vyarus.dropwizard.guice.support.feature;

import com.google.inject.Inject;
import io.dropwizard.core.Application;
import io.dropwizard.core.cli.EnvironmentCommand;
import io.dropwizard.core.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;

/**
 * @author Vyacheslav Rusakov
 * @since 03.09.2014
 */
public class DummyCommand extends EnvironmentCommand<TestConfiguration> {

    // NOTE: have to convert into java because groovy 4 was failing to compile properly (generated constructor)
    public DummyCommand(Application<TestConfiguration> app) {
        super(app, "sample", "sample command");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, TestConfiguration configuration) throws Exception {
        DefaultGroovyMethods.println(this, "I'm alive! " + getService().hey());
    }

    public DummyService getService() {
        return service;
    }

    public void setService(DummyService service) {
        this.service = service;
    }

    @Inject
    private DummyService service;
}

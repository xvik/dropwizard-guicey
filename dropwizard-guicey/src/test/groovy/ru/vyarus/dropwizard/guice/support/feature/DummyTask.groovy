package ru.vyarus.dropwizard.guice.support.feature
/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
class DummyTask extends io.dropwizard.servlets.tasks.Task {

    DummyTask() {
        super("mytask")
    }

    @Override
    void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {

    }
}

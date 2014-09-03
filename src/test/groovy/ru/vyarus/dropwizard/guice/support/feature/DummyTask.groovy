package ru.vyarus.dropwizard.guice.support.feature

import com.google.common.collect.ImmutableMultimap

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2014
 */
class DummyTask extends io.dropwizard.servlets.tasks.Task{

    DummyTask() {
        super("mytask")
    }

    @Override
    void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {

    }
}

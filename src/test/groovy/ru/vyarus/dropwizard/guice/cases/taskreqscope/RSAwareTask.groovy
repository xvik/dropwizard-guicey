package ru.vyarus.dropwizard.guice.cases.taskreqscope

import com.google.common.collect.ImmutableMultimap
import io.dropwizard.servlets.tasks.Task

import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
class RSAwareTask extends Task{

    @Inject
    Provider<HttpServletRequest> requestProvider;

    RSAwareTask() {
        super("rsaware")
    }

    @Override
    void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        // fail if request is not available
        requestProvider.get();
        output.append("success")
    }
}

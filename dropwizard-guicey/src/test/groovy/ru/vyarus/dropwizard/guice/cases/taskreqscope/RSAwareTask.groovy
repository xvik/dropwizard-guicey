package ru.vyarus.dropwizard.guice.cases.taskreqscope


import io.dropwizard.servlets.tasks.Task

import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.servlet.http.HttpServletRequest

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
class RSAwareTask extends Task {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    RSAwareTask() {
        super("rsaware")
    }

    @Override
    void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        // fail if request is not available
        requestProvider.get();
        output.append("success")
    }
}

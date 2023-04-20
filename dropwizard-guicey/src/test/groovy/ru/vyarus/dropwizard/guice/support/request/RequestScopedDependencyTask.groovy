package ru.vyarus.dropwizard.guice.support.request

import com.google.common.base.Preconditions
import com.google.inject.Inject
import com.google.inject.Provider
import io.dropwizard.servlets.tasks.Task

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Just to show that this WILL NOT work, because guice filter registered only for jersey context.
 *
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
class RequestScopedDependencyTask extends Task {

    @Inject
    Provider<RequestScopedBean> requestScopedBeanProvider
    @Inject
    Provider<HttpServletRequest> requestProvider
    @Inject
    Provider<HttpServletResponse> responseProvider

    RequestScopedDependencyTask() {
        super("rs")
    }

    @Override
    void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        Preconditions.checkNotNull(requestProvider.get())
        Preconditions.checkNotNull(responseProvider.get())
        Preconditions.checkState(requestScopedBeanProvider.get().foo().equals('foo'));
    }
}

package ru.vyarus.guicey.admin.log;

import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.internal.HttpChannelState;
import org.eclipse.jetty.util.Callback;
import ru.vyarus.guicey.admin.rest.AdminRestServlet;

/**
 * This is almost a copy of {@link io.dropwizard.request.logging.LogbackAccessRequestLogAwareHandler} registered
 * in the main context. This handler is required to prepare a correct request instance for logger
 * (see {@code channelRequest.setLoggedRequest} line). Without handler,
 * {@link io.dropwizard.request.logging.LogbackAccessRequestLog} will fail with error.
 * <p>
 * In admin context, request log is not registered, but admin servlet calls main context and so invokes
 * request logger. To avoid exceptions, we need to prepare logger request inside admin context, but only for
 * rest emulation servlet.
 * <p>
 * Pay attention: admin rest requests are logged the same way as usual rest requests!
 *
 * @author Vyacheslav Rusakov
 * @since 17.09.2024
 */
public class LogbackAccessRequestLogAwareCustomHandler extends Handler.Wrapper {

    private final boolean identifyAdminContext;

    public LogbackAccessRequestLogAwareCustomHandler(final boolean identifyAdminContext) {
        this.identifyAdminContext = identifyAdminContext;
    }

    @Override
    public boolean handle(final Request request,
                          final Response response,
                          final Callback callback) throws Exception {
        final boolean handled = super.handle(request, response, callback);
        // apply ONLY for rest simulation (for other cases simply not required, because requests not logged)
        if (handled && request.getAttribute(AdminRestServlet.ADMIN_PROPERTY) != null) {
            ServletContextRequest servletContextRequest = Request.as(request, ServletContextRequest.class);
            if (identifyAdminContext) {
                // indicate admin context call in log
                servletContextRequest = (ServletContextRequest) servletContextRequest
                        .wrap(request, HttpURI.build(request.getHttpURI())
                        .uri(request.getHttpURI() + " (ADMIN REST)"));
            }
            if (servletContextRequest != null) {
                final Request unwrapped = Request.unWrap(request);
                if (!(unwrapped instanceof HttpChannelState.ChannelRequest channelRequest)) {
                    throw new IllegalStateException(
                            "Expecting unwrapped request to be an instance of HttpChannelState.ChannelRequest");
                }
                channelRequest.setLoggedRequest(servletContextRequest);
            }
        }
        return handled;
    }
}

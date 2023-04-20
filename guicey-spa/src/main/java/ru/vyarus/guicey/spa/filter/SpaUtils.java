package ru.vyarus.guicey.spa.filter;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Core SPA routes detection logic.
 *
 * @author Vyacheslav Rusakov
 * @since 16.01.2019
 */
public final class SpaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaUtils.class);

    private SpaUtils() {
    }

    /**
     * Note that root path is not the index page, but root mapping path, which will implicitly lead to index page.
     *
     * @param currentPath current path
     * @param rootPath    application root path
     * @return true if provided path is application root path, false otherwise
     */
    public static boolean isRootPage(final String currentPath, final String rootPath) {
        final String path = PathUtils.trailingSlash(currentPath);
        return path.equals(rootPath);
    }

    /**
     * Checks if provided request expects html response (by accept header). Did not consider wildcard type
     * ({@literal *}/{@literal *})) as html request, because browser request resources (like fonts) with such type.
     * Only direct text/html type is recognized (assuming human request).
     *
     * @param req request instance
     * @return true if request expect html, false otherwise
     */
    public static boolean isHtmlRequest(final HttpServletRequest req) {
        final String accept = req.getHeader(HttpHeaders.ACCEPT);
        if (Strings.emptyToNull(accept) != null) {
            // accept header could contain multiple mime types
            for (String type : accept.split(",")) {
                try {
                    // only exact accept, no wildcard
                    if (MediaType.valueOf(type).equals(MediaType.TEXT_HTML_TYPE)) {
                        return true;
                    }
                } catch (Exception ex) {
                    // ignore errors for better behaviour
                    LOGGER.debug("Failed to parse media type '" + type + "':", ex.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Checks if request could be actually a client side route. SPA route should be a html request
     * (by accepted type) and not match to provided pattern (describing non-routing urls).
     *
     * @param req        request instance
     * @param noRedirect no-redirect pattern
     * @return true if request could be SPA route, false if not
     */
    public static boolean isSpaRoute(final HttpServletRequest req, final Pattern noRedirect) {
        return isHtmlRequest(req) && !noRedirect.matcher(req.getRequestURI()).find();
    }

    /**
     * Applies response header to prevent caching (because SPA page should not be cached).
     *
     * @param resp response instance
     */
    public static void noCache(final HttpServletResponse resp) {
        resp.setHeader(HttpHeaders.CACHE_CONTROL, "must-revalidate,no-cache,no-store");
    }

    /**
     * Perform server redirect into root page. Means that current request considered as SPA route and server
     * should return index page as response (under the same url) so client could handle url as internal navigation.
     * <p>
     * No cache header is applied to response to prevent index page caching (by this route).
     *
     * @param req    request instance
     * @param res    response instance
     * @param target spa root path
     * @throws IOException      on error
     * @throws ServletException on error
     */
    public static void doRedirect(final HttpServletRequest req,
                                  final HttpServletResponse res,
                                  final String target) throws IOException, ServletException {
        // remove previous error (404)
        res.reset();
        // redirect to root
        noCache(res);
        req.getRequestDispatcher(target).forward(req, res);
    }
}

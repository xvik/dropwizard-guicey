package ru.vyarus.dropwizard.guice.module.installer.feature.web.util;

import com.google.common.base.Strings;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext;

import jakarta.servlet.Filter;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

/**
 * Web installers utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 08.08.2016
 */
public final class WebUtils {

    private WebUtils() {
    }

    /**
     * When filter name not set in annotation, name generates as: . (dot) at the beginning to indicate
     * generated name, followed by lower-cased class name. If class ends with "filter" then it will be cut off.
     * For example, for class "MyCoolFilter" generated name will be ".mycool".
     *
     * @param filter filter annotation
     * @param type   filter type
     * @return filter name or generated name if name not provided
     */
    public static String getFilterName(final WebFilter filter, final Class<? extends Filter> type) {
        final String name = Strings.emptyToNull(filter.filterName());
        return name != null ? name : generateName(type, "filter");
    }

    /**
     * @param servlet servlet annotation
     * @param type    servlet type
     * @return servlet name or generated name if name not provided
     */
    public static String getServletName(final WebServlet servlet, final Class<? extends HttpServlet> type) {
        final String name = Strings.emptyToNull(servlet.name());
        return name != null ? name : generateName(type, "servlet");
    }

    /**
     * @param context context annotation
     * @return true if main context installation required, false otherwise
     */
    public static boolean isForMain(final AdminContext context) {
        return context == null || context.andMain();
    }

    /**
     * @param context context annotation
     * @return true if admin context installation required, false otherwise
     */
    public static boolean isForAdmin(final AdminContext context) {
        return context != null;
    }

    /**
     * Returns "" for main context (assuming its default) and "A" for admin context. "MA" returned if both contexts
     * installation configuration.
     *
     * @param context context annotation
     * @return target contexts marker string
     */
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public static String getContextMarkers(final AdminContext context) {
        String res = "";
        if (isForAdmin(context)) {
            if (isForMain(context)) {
                res += "M";
            }
            res += "A";
        }
        return res;
    }

    /**
     * @param annotation filter registration annotation
     * @return "async" string if filter support async and empty string otherwise
     */
    public static String getAsyncMarker(final WebFilter annotation) {
        return getAsyncMarker(annotation.asyncSupported());
    }

    /**
     * @param annotation servlet registration annotation
     * @return "async" string if servlet support async and empty string otherwise
     */
    public static String getAsyncMarker(final WebServlet annotation) {
        return getAsyncMarker(annotation.asyncSupported());
    }

    private static String getAsyncMarker(final boolean async) {
        return async ? "async" : "";
    }

    private static String generateName(final Class<?> type, final String keyword) {
        String probe = '.' + type.getSimpleName().toLowerCase();
        final int targetLength = probe.length() - keyword.length();
        // leave prefix if remaining part is too short (and if target name equal prefix)
        if (probe.endsWith(keyword) && targetLength > 2) {
            probe = probe.substring(0, targetLength);
        }
        return probe;
    }
}

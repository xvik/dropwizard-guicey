package ru.vyarus.dropwizard.guice.debug.util;

import com.google.common.base.Joiner;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Helper utilities for diagnostic info rendering. Uses copy of logback's
 * {@link ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator}
 * to shrink full class name to predictable size.
 *
 * @author Vyacheslav Rusakov
 * @since 14.07.2016
 */
public final class RenderUtils {
    private static final ClassNameAbbreviator PACKAGE_FORMATTER = new ClassNameAbbreviator(20);
    private static final ClassNameAbbreviator CLASS_FORMATTER = new ClassNameAbbreviator(36);

    private RenderUtils() {
    }

    /**
     * Render installer line. Assuming installer class always ends with Installer and by removing this postfix
     * and converting rest to lower case we can get human readable installer name. Result may be worse for
     * multi-word cases, but anyway it's pretty readable.
     * <p>
     * Format: human-readable-installer-name (installer-class) *markers.
     *
     * @param type    installer class
     * @param markers markers
     * @return rendered installer line
     */
    public static String renderInstaller(final Class<FeatureInstaller> type, final List<String> markers) {
        return String.format("%-20s %-38s %s",
                FeatureUtils.getInstallerExtName(type), brackets(renderClass(type)), markers(markers));
    }

    /**
     * Renders disabled installer line. The same as installer line, but with '-' before installer name and
     * without markers.
     *
     * @param type disabled installer class
     * @return rendered disabled installer line
     */
    public static String renderDisabledInstaller(final Class<FeatureInstaller> type) {
        return String.format("-%-19s %-38s",
                FeatureUtils.getInstallerExtName(type), brackets(renderClass(type)));
    }

    /**
     * Renders class as: class-simple-name (class-package) *markers.
     * For anonymous class simple name will be Class$1.
     *
     * @param type    class
     * @param markers markers
     * @return rendered class line
     */
    public static String renderClassLine(final Class<?> type, final List<String> markers) {
        return renderClassLine(type, 0, markers);
    }

    /**
     * Shortcut for {@link #renderClassLine(Class, List)} to render without markers.
     *
     * @param type class
     * @return rendered clas line
     */
    public static String renderClassLine(final Class<?> type) {
        return renderClassLine(type, null);
    }

    /**
     * Renders scope as: class-simple-name#pos (class-package) *markers.
     * For position 1 position is not rendered.
     *
     * @param type    instance class
     * @param pos     instance position number (registration order) in scope
     * @param markers markers
     * @return rendered scope line
     */
    public static String renderClassLine(final Class<?> type, final int pos, final List<String> markers) {
        return String.format("%-28s %-26s %s", getClassName(type) + renderPositionPostfix(pos),
                brackets(renderPackage(type)), markers(markers));
    }

    /**
     * Render disabled class as: -class-simple-name (class-package).
     *
     * @param type class
     * @return rendered disabled class line
     */
    public static String renderDisabledClassLine(final Class<?> type) {
        return renderDisabledClassLine(type, 0, null);
    }

    /**
     * Render disabled class as: -class-simple-name#pos (class-package) *markers.
     *
     * @param type    class
     * @param pos     instance position
     * @param markers markers (may be null)
     * @return rendered disabled class line
     */
    public static String renderDisabledClassLine(final Class<?> type, final int pos, final List<String> markers) {
        return String.format("-%-27s %-26s %s", getClassName(type) + renderPositionPostfix(pos),
                brackets(renderPackage(type)), markers(markers));
    }

    /**
     * @param type class to render
     * @return class rendered in abbreviated manner (to fit it into 36 chars)
     * @see ClassNameAbbreviator
     */
    public static String renderClass(final Class<?> type) {
        return CLASS_FORMATTER.abbreviate(type.getName());
    }

    /**
     * If provided type is inner class then declaring class will be rendered instead of package.
     *
     * @param type class to render package
     * @return class package rendered in abbreviated manner (to fit nto 20 chars)
     * @see ClassNameAbbreviator
     */
    public static String renderPackage(final Class<?> type) {
        // For some proxies package and declaring class may be null, so use full class names
        // May appear with anonymous hooks declaration in tests
        return PACKAGE_FORMATTER.abbreviate(type.isMemberClass() && !type.isAnonymousClass()
                ? (type.getDeclaringClass() != null ? type.getDeclaringClass().getName() : type.getName())
                : (type.getPackage() != null ? type.getPackage().getName() : type.getName()));
    }

    /**
     * @param string string to apply brackets
     * @return provided string inside brackets
     */
    public static String brackets(final String string) {
        return "(" + string + ")";
    }

    /**
     * Renders markers as: *marker1,marker2..
     *
     * @param markers markers to render (may be null)
     * @return rendered markers or empty string if no markers provided
     */
    public static String markers(final List<String> markers) {
        String signs = "";
        if (markers != null && !markers.isEmpty()) {
            signs = "*" + Joiner.on(", ").join(markers);
        }
        return signs;
    }

    /**
     * Should be used instead of {@link Class#getSimpleName()}  because for anonymous classes it leads to empty string.
     *
     * @param type type to get class name from
     * @return class name
     */
    public static String getClassName(final Class<?> type) {
        String name = type.getSimpleName();
        if (name.isEmpty()) {
            // for anonymous classes name will be empty instead of e.g. SomeType$1
            name = type.getName().substring(type.getName().lastIndexOf('.') + 1);
        }
        return name;
    }

    /**
     * The same as {@link #getClassName(Class)} but for inner classes would preserve upper classes.
     * For example, would print {@code SomeClass$Inner} instead of just {@code Inner} for inner class.
     *
     * @param type  type to get class name from
     * @return full class name (including root classes for inner class declarations)
     */
    public static String getFullClassName(final Class<?> type) {
        return type.getName().substring(type.getName().lastIndexOf('.') + 1);
    }

    /**
     * Render annotation. Supports only "value" annotation method - other possible methods simply ignored.
     *
     * @param annotation annotation to render
     * @return rendered annotation string
     */
    @SuppressFBWarnings("DE_MIGHT_IGNORE")
    public static String renderAnnotation(final Annotation annotation) {
        final StringBuilder res = new StringBuilder("@").append(annotation.annotationType().getSimpleName());
        // NOTE custom config annotations might contain custom values - it can't be known for sure
        try {
            final Method valueMethod = FeatureUtils.findMethod(annotation.annotationType(), "value");
            final Object value = FeatureUtils.invokeMethod(valueMethod, annotation);
            if (value != null) {
                res.append("(\"").append(value).append("\")");
            }
        } catch (Exception ignored) {
            // no value field in annotation
        }
        return res.toString();
    }

    private static String renderPositionPostfix(final int pos) {
        return pos > 1 ? "#" + pos : "";
    }
}

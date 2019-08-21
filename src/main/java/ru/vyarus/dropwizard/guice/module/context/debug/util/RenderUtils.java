package ru.vyarus.dropwizard.guice.module.context.debug.util;

import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import com.google.common.base.Joiner;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import java.util.List;

/**
 * Helper utilities for diagnostic info rendering. Use logback's {@link TargetLengthBasedClassNameAbbreviator}
 * to shrink full class name to predictable size.
 *
 * @author Vyacheslav Rusakov
 * @since 14.07.2016
 */
public final class RenderUtils {
    private static final TargetLengthBasedClassNameAbbreviator PACKAGE_FORMATTER =
            new TargetLengthBasedClassNameAbbreviator(20);
    private static final TargetLengthBasedClassNameAbbreviator CLASS_FORMATTER =
            new TargetLengthBasedClassNameAbbreviator(36);

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
     * @see TargetLengthBasedClassNameAbbreviator
     */
    public static String renderClass(final Class<?> type) {
        return CLASS_FORMATTER.abbreviate(type.getName());
    }

    /**
     * If provided type is inner class then declaring class will be rendered instead of package.
     *
     * @param type class to render package
     * @return class package rendered in abbreviated manner (to fit nto 20 chars)
     * @see TargetLengthBasedClassNameAbbreviator
     */
    public static String renderPackage(final Class<?> type) {
        return PACKAGE_FORMATTER.abbreviate(type.isMemberClass() && !type.isAnonymousClass()
                ? type.getDeclaringClass().getName() : type.getPackage().getName());
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

    private static String renderPositionPostfix(int pos) {
        return pos > 1 ? "#" + pos : "";
    }
}

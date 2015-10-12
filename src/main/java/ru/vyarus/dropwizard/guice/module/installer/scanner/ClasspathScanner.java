package ru.vyarus.dropwizard.guice.module.installer.scanner;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import ru.vyarus.dropwizard.guice.module.installer.scanner.util.OReflectionHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Classpath scanner, reduced to provided packages.
 * Ignores classes annotated with {@code InvisibleForScanner}.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
public class ClasspathScanner {
    private final Set<String> packages;

    public ClasspathScanner(final Set<String> packages) {
        this.packages = validate(packages);
    }

    /**
     * Scan configured classpath packages.
     *
     * @param visitor visitor to investigate found classes
     */
    public void scan(final ClassVisitor visitor) {
        for (String pkg : packages) {
            List<Class<?>> found;
            try {
                found = OReflectionHelper.getClassesFor(pkg, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to scan classpath", e);
            }
            for (Class<?> cls : found) {
                if (!cls.isAnnotationPresent(InvisibleForScanner.class)) {
                    visitor.visit(cls);
                }
            }
        }
    }

    /**
     * @param packages specified packages
     * @return original set if validation pass
     * @throws IllegalStateException if packages intersect
     */
    private Set<String> validate(final Set<String> packages) {
        final List<String> pkg = Lists.newArrayList(packages);
        Collections.sort(pkg, new Comparator<String>() {
            @Override
            public int compare(final String o1, final String o2) {
                return Integer.compare(o1.length(), o2.length());
            }
        });
        for (int i = 0; i < pkg.size(); i++) {
            final String path = pkg.get(i);
            for (int j = i + 1; j < pkg.size(); j++) {
                final String path2 = pkg.get(j);
                Preconditions.checkState(!path2.startsWith(path + "."),
                        "Autoscan path '%s' is already covered by '%s' and may lead "
                                + "to duplicate instances in runtime",
                        path2, path);
            }
        }
        return packages;
    }
}

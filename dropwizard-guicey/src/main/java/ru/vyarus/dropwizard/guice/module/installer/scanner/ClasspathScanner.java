package ru.vyarus.dropwizard.guice.module.installer.scanner;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.stat.StatTimer;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker;
import ru.vyarus.dropwizard.guice.module.installer.scanner.util.OReflectionHelper;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.ScanClassesCount;
import static ru.vyarus.dropwizard.guice.module.context.stat.Stat.ScanTime;

/**
 * Classpath scanner, reduced to provided packages.
 * Ignores classes annotated with {@link InvisibleForScanner}.
 * <p>
 * Actual scan is performed only on first {@link #scan(ClassVisitor)} call. Later scans used cached classes.
 * {@link #cleanup()} must be used to clear cache.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2014
 */
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
public class ClasspathScanner {
    private static final int SCAN_THRESHOLD = 1000;

    private final Logger logger = LoggerFactory.getLogger(ClasspathScanner.class);

    private final StatsTracker tracker;

    private final Set<String> packages;
    private final boolean acceptProtectedClasses;
    private List<Class> scanned;

    /**
     * Create a scanner.
     *
     * @param packages packages to scan
     */
    public ClasspathScanner(final Set<String> packages) {
        // for backwards compatibility allow using without tracker
        this(packages, false, null);
    }

    /**
     * Create a scanner.
     *
     * @param packages               packages to scan
     * @param acceptProtectedClasses look protected classes
     * @param tracker                tracker instance
     */
    public ClasspathScanner(final Set<String> packages,
                            final boolean acceptProtectedClasses,
                            final StatsTracker tracker) {
        this.packages = validate(packages);
        this.acceptProtectedClasses = acceptProtectedClasses;
        this.tracker = tracker;
        // perform scan before to fill cache and get accurate traversing stats
        performScan();
    }

    /**
     * Scan configured classpath packages.
     *
     * @param visitor visitor to investigate found classes
     */
    public void scan(final ClassVisitor visitor) {
        if (scanned == null) {
            performScan();
        }
        for (Class<?> cls : scanned) {
            visitor.visit(cls);
        }
    }

    /**
     * Should be called to flush scanner cache.
     */
    public void cleanup() {
        scanned = null;
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

    @SuppressWarnings("PMD.PrematureDeclaration")
    private void performScan() {
        final StatTimer timer = tracker == null ? null : tracker.timer(ScanTime);
        int count = 0;
        scanned = Lists.newArrayList();
        for (String pkg : packages) {
            final List<Class<?>> found;
            try {
                found = OReflectionHelper.getClassesFor(
                        pkg, Thread.currentThread().getContextClassLoader(), acceptProtectedClasses);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to scan classpath", e);
            }
            count += found.size();
            for (Class<?> cls : found) {
                // only static inner classes are allowed because guice will not be able to instantiate inner class
                final boolean isInner = cls.getEnclosingClass() != null && !Modifier.isStatic(cls.getModifiers());
                if (!isInner && !cls.isAnnotationPresent(InvisibleForScanner.class)) {
                    scanned.add(cls);
                }
            }
        }
        if (count > SCAN_THRESHOLD) {
            logger.warn("{} classes were loaded while scanning '{}' packages. Reduce packages to scan "
                    + "to increase efficiency.", count, Joiner.on(',').join(packages));
        }
        if (timer != null) {
            timer.stop();
            tracker.count(ScanClassesCount, count);
        }
    }
}

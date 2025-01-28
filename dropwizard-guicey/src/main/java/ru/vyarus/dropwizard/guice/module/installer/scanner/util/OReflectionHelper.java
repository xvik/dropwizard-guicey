package ru.vyarus.dropwizard.guice.module.installer.scanner.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Reflection utility taken from orientdb (http://orientechnologies.com).
 * Helper class to browse .class files.
 *
 * @author Antony Stubbs
 */
@SuppressWarnings({"PMD", "all"})
public final class OReflectionHelper {
    private static final String CLASS_EXTENSION = ".class";

    private OReflectionHelper() {
    }

    // preserved for backwards compatibility
    public static List<Class<?>> getClassesFor(final String iPackageName,
                                               final ClassLoader iClassLoader) throws ClassNotFoundException {
        return getClassesFor(iPackageName, iClassLoader, false);
    }

    @SuppressFBWarnings("DCN_NULLPOINTER_EXCEPTION")
    public static List<Class<?>> getClassesFor(final String iPackageName,
                                               final ClassLoader iClassLoader,
                                               final boolean acceptProtectedClasses) throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname.
        // There may be more than one if a package is split over multiple jars/paths
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        final ArrayList<File> directories = new ArrayList<File>();
        try {
            // Ask for all resources for the path
            final String packageUrl = iPackageName.replace('.', '/');
            Enumeration<URL> resources = iClassLoader.getResources(packageUrl);
            if (!resources.hasMoreElements()) {
                resources = iClassLoader.getResources(packageUrl + CLASS_EXTENSION);
                if (resources.hasMoreElements()) {
                    throw new IllegalArgumentException(iPackageName + " does not appear to be a valid package but a class");
                }
            } else {
                while (resources.hasMoreElements()) {
                    final URL res = resources.nextElement();
                    if ("jar".equalsIgnoreCase(res.getProtocol())) {
                        final JarURLConnection conn = (JarURLConnection) res.openConnection();
                        final JarFile jar = conn.getJarFile();
                        for (JarEntry e : Collections.list(jar.entries())) {

                            if (e.getName().startsWith(iPackageName.replace('.', '/')) && e.getName().endsWith(CLASS_EXTENSION)) {
                                final String className = e.getName().replace("/", ".").substring(0, e.getName().length() - 6);
                                final Class<?> cls = Class.forName(className, true, iClassLoader);
                                if (isAcceptibleClass(cls, acceptProtectedClasses)) {
                                    classes.add(cls);
                                }
                            }
                        }
                    } else {
                        directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
                    }
                }
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(iPackageName + " does not appear to be " + "a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(iPackageName + " does not appear to be " + "a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying " + "to get all resources for " + iPackageName);
        }

        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                final File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            classes.addAll(findClasses(file, iPackageName, iClassLoader, acceptProtectedClasses));
                        } else {
                            String className;
                            if (file.getName().endsWith(CLASS_EXTENSION)) {
                                className = file.getName().substring(0, file.getName().length() - CLASS_EXTENSION.length());
                                final Class<?> cls = Class.forName(iPackageName + '.' + className, true, iClassLoader);
                                if (isAcceptibleClass(cls, acceptProtectedClasses)) {
                                    classes.add(cls);
                                }
                            }
                        }
                    }
                }
            } else {
                throw new ClassNotFoundException(iPackageName + " (" + directory.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param iDirectory   The base directory
     * @param iPackageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(final File iDirectory, String iPackageName,
                                              ClassLoader iClassLoader,
                                              boolean acceptProtected) throws ClassNotFoundException {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!iDirectory.exists()) {
            return classes;
        }

        iPackageName += "." + iDirectory.getName();

        String className;
        final File[] files = iDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().contains(".")) {
                        continue;
                    }
                    classes.addAll(findClasses(file, iPackageName, iClassLoader, acceptProtected));
                } else if (file.getName().endsWith(CLASS_EXTENSION)) {
                    className = file.getName().substring(0, file.getName().length() - CLASS_EXTENSION.length());
                    final Class<?> cls = Class.forName(iPackageName + '.' + className, true, iClassLoader);
                    if (isAcceptibleClass(cls, acceptProtected)) {
                        classes.add(cls);
                    }
                }
            }
        }
        return classes;
    }

    private static boolean isAcceptibleClass(final Class type, final boolean acceptProtected) {
        // only public non-anonymous classes allowed
        return Modifier.isPublic(type.getModifiers()) || (acceptProtected &&
                // package private or protected
                (type.getModifiers() == 0 || Modifier.isProtected(type.getModifiers())));
    }
}

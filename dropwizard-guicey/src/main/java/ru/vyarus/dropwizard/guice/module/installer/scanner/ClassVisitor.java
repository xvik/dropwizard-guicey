package ru.vyarus.dropwizard.guice.module.installer.scanner;

/**
 * Visitor for classpath scanner.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@FunctionalInterface
public interface ClassVisitor {

    /**
     * Called for every class found by classpath scanner (but avoiding classes annotated with
     * {@code @InvisibleForScanner}).
     *
     * @param type type to investigate
     */
    void visit(Class<?> type);
}

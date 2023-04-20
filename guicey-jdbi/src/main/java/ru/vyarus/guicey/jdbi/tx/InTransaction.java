package ru.vyarus.guicey.jdbi.tx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for unit of work and transaction declaration. Code executed under the scope of annotation will
 * share the same transaction (and handle).
 * <p>
 * Use on class to mark all methods as transactional.
 * <p>
 * Support nesting: nested annotated elements will participate in outer transaction (and so exceptions will rollback
 * entire transaction).
 *
 * @author Vyacheslav Rusakov
 * @since 4.12.2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface InTransaction {
}

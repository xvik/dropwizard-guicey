package ru.vyarus.guicey.jdbi.installer.repository;

import java.lang.annotation.*;

/**
 * Annotation for marking JDBI dao classes (abstract classes or interfaces).
 * Such classes will be recognized and installed by {@link RepositoryInstaller}.
 * Annotated daos may be used as any other bean - no need to combine daos manually (like you have to do
 * in normal JDBI), all daos automatically participate in current unit of work and so share the same transaction.
 * <p>
 * Annotated classes may inject guice beans usign FIELD injection: constructor injection is impossible because
 * jdbi creates proxy instance and it's not aware of guice.
 * <p>
 * Annotated classes participate in guice aop! For example, transaction annotation may be used on abstract (!) dao
 * classes to declare dao-wide unit of work.
 *
 * @author Vyacheslav Rusakov
 * @since 4.12.2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface JdbiRepository {
}

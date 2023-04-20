package ru.vyarus.dropwizard.guice.examples.repository.mapper.bind;

import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import ru.vyarus.dropwizard.guice.examples.model.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It's not installed by any guicey installer because DBI recognize annotations directly from usage.
 *
 * @author Vyacheslav Rusakov
 * @since 09.12.2016
 */
@BindingAnnotation(UserBind.UserBinder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface UserBind {

    class UserBinder implements BinderFactory<UserBind> {
        @Override
        public Binder build(UserBind annotation) {
            return (Binder<UserBind, User>) (q, bind, arg) -> {
                q.bind("id", arg.getId())
                        .bind("version", arg.getVersion())
                        .bind("name", arg.getName());
            };
        }
    }
}

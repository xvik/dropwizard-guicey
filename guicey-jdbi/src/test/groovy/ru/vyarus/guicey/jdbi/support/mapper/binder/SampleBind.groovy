package ru.vyarus.guicey.jdbi.support.mapper.binder

import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.Binder
import org.skife.jdbi.v2.sqlobject.BinderFactory
import org.skife.jdbi.v2.sqlobject.BindingAnnotation
import ru.vyarus.guicey.jdbi.support.model.Sample

import java.lang.annotation.*

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
@BindingAnnotation(SampleBind.SampleBinder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface SampleBind {

    static class SampleBinder implements BinderFactory<SampleBind> {

        @Override
        Binder build(SampleBind annotation) {
            return new Binder<SampleBind, Sample>() {
                @Override
                void bind(SQLStatement q, SampleBind bind, Sample arg) {
                    q.bind("id", arg.getId())
                            .bind("name", arg.getName())
                }
            }
        }
    }
}
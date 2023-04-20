package ru.vyarus.guicey.jdbi3.support.repository.syntetic

import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizerFactory
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizingAnnotation
import org.jdbi.v3.sqlobject.customizer.SqlStatementParameterCustomizer

import java.lang.annotation.*
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Type

/**
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
@SqlStatementCustomizingAnnotation(NamedBinder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface NamedBind {

    static class NamedBinder implements SqlStatementCustomizerFactory {


        @Override
        public SqlStatementParameterCustomizer createForParameter(Annotation annotation,
                                                                  Class<?> sqlObjectType,
                                                                  Method method,
                                                                  Parameter param,
                                                                  int index,
                                                                  Type type) {
            { stmt, sample ->
                ((SqlStatement) stmt)
                        .bind("id", ((NamedEntity) sample).id)
                        .bind("name", ((NamedEntity) sample).name)
            }
        }
    }
}
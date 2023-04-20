package ru.vyarus.dropwizard.guice.support.provider.annotated

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER, ElementType.FIELD])
@Documented
@interface Auth {

}
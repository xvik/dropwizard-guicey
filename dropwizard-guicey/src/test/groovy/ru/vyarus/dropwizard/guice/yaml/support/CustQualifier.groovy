package ru.vyarus.dropwizard.guice.yaml.support

import com.google.inject.BindingAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Vyacheslav Rusakov
 * @since 23.11.2023
 */

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD])
@BindingAnnotation
@interface CustQualifier {
}
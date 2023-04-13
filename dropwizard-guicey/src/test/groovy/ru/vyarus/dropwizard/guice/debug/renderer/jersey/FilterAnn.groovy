package ru.vyarus.dropwizard.guice.debug.renderer.jersey

import jakarta.ws.rs.NameBinding
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Vyacheslav Rusakov
 * @since 28.10.2019
 */
@NameBinding
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface FilterAnn {

}
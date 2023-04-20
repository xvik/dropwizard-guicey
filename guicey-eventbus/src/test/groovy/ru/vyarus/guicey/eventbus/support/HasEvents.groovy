package ru.vyarus.guicey.eventbus.support

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface HasEvents {

}
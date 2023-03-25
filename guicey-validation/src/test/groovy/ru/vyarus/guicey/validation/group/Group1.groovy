package ru.vyarus.guicey.validation.group

import ru.vyarus.guice.validator.group.annotation.ValidationGroups

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.ElementType.TYPE

/**
 * @author Vyacheslav Rusakov
 * @since 07.09.2021
 */
@Target([TYPE, METHOD])
@Retention(RetentionPolicy.RUNTIME)
@ValidationGroups(Group1)
@interface Group1 {

}

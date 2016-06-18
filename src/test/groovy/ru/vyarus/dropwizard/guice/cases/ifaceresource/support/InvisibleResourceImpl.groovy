package ru.vyarus.dropwizard.guice.cases.ifaceresource.support

/**
 * This resource could not be recognized because @Path annotation is not on 1st level interface.
 *
 * @author Vyacheslav Rusakov
 * @since 18.06.2016
 */
class InvisibleResourceImpl implements SecondLevelResource {

    @Override
    String latest() {
        return null
    }
}

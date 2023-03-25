package ru.vyarus.dropwizard.guice.cases.ifaceresource.support
/**
 * @author Vyacheslav Rusakov
 * @since 18.06.2016
 */
class ResourceImpl implements ResourceContract {

    @Override
    String latest() {
        return "called!"
    }
}

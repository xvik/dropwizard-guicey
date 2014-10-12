package ru.vyarus.dropwizard.guice.module.installer.order;

/**
 * Marker interface for installer trigger extensions sorting.
 * It is important, for example, for managed objects.
 * <p>Will not work for {@link ru.vyarus.dropwizard.guice.module.installer.install.BindingInstaller}</p>
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
public interface Ordered {
}

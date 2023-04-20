package ru.vyarus.dropwizard.guice.debug.renderer.guice.support.exts;

import com.google.inject.spi.ProvisionListener;

/**
 * @author Vyacheslav Rusakov
 * @since 14.09.2019
 */
public class CustomProvisionListener implements ProvisionListener {
    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
    }
}

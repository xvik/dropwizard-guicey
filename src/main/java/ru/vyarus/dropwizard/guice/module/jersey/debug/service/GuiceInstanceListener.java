package ru.vyarus.dropwizard.guice.module.jersey.debug.service;

import com.google.common.collect.Lists;
import com.google.inject.spi.ProvisionListener;

import javax.inject.Inject;
import java.util.List;

/**
 * Listens guice for created services and checks correctness for services registered by
 * {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} based installers.
 * <p/>
 * Some services will be registered before debug service gets injected into listener instance,
 * so before this listener simply collects all created objects to check after.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
public class GuiceInstanceListener implements ProvisionListener {

    private ContextDebugService contextDebugService;
    private final List<Class<?>> created = Lists.newArrayList();

    @Override
    public <T> void onProvision(final ProvisionInvocation<T> provision) {
        final Class type = provision.getBinding().getKey().getTypeLiteral().getRawType();
        if (contextDebugService == null) {
            created.add(type);
        } else {
            checkType(type);
        }
    }

    @Inject
    public void setContextDebugService(final ContextDebugService contextDebugService) {
        this.contextDebugService = contextDebugService;
        checkCollected();
    }

    private void checkCollected() {
        for (Class<?> type : created) {
            checkType(type);
        }
        created.clear();
    }

    private void checkType(final Class<?> type) {
        if (contextDebugService.getManagedTypes().contains(type)) {
            contextDebugService.guiceManage(type);
        }
    }
}

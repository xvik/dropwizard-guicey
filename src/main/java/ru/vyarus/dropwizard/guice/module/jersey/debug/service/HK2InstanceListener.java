package ru.vyarus.dropwizard.guice.module.jersey.debug.service;

import com.google.common.collect.Lists;
import org.glassfish.hk2.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Listens HK2 for created services and checks correctness for services registered by
 * {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller} based installers.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
@Singleton
public class HK2InstanceListener implements InstanceLifecycleListener {

    private final Logger logger = LoggerFactory.getLogger(HK2InstanceListener.class);

    private final ContextDebugService contextDebugService;

    @Inject
    public HK2InstanceListener(final ContextDebugService contextDebugService) {
        this.contextDebugService = contextDebugService;
    }

    @Override
    public Filter getFilter() {
        final List<String> managedTypes = Lists.transform(contextDebugService.getManagedTypes(), Class::getName);
        return d -> d.getDescriptorType() == DescriptorType.CLASS
                && managedTypes.contains(d.getImplementation());
    }

    @Override
    public void lifecycleEvent(final InstanceLifecycleEvent event) {
        final Class<?> implClass = event.getActiveDescriptor().getImplementationClass();
        // checking only object creation
        if (event.getEventType() == InstanceLifecycleEventType.PRE_PRODUCTION) {
            logger.info("HK2 creates service: {}", implClass.getName());
            contextDebugService.hkManage(implClass);
        }
    }
}

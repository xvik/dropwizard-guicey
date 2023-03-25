package ru.vyarus.dropwizard.guice.module.jersey.debug.service;

import com.google.common.collect.Lists;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.JerseyExtensionsManagedByGuice;

/**
 * Debug service checks and collect information on jersey (and HK2) related types instantiation.
 * Actual bean instantiation detection by guice or HK2 is performed by specific listeners.
 * Service only checks correctness and tracks instantiated objects.
 * <p>
 * Only objects installed by {@link JerseyInstaller} installers are tracked.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
@Singleton
public class ContextDebugService {

    private final Provider<ExtensionsHolder> holder;
    private final Options options;
    private final List<Class<?>> hkManaged = Lists.newArrayList();
    private final List<Class<?>> guiceManaged = Lists.newArrayList();

    private final Lock lock = new ReentrantLock();
    private List<Class<?>> managedTypes;

    @Inject
    public ContextDebugService(final Provider<ExtensionsHolder> holder, final Options options) {
        this.holder = holder;
        this.options = options;
    }

    /**
     * @return list of all types that must be tracked
     */
    public List<Class<?>> getManagedTypes() {
        if (managedTypes == null) {
            lock.lock();
            try {
                if (managedTypes == null) {
                    managedTypes = buildManagedTypes();
                }
            } finally {
                lock.unlock();
            }
        }
        return managedTypes;
    }

    /**
     * Called by specific HK2 lifecycle listener to check if bean is properly instantiated by HK2.
     *
     * @param type instantiated bean type
     */
    public void hkManage(final Class<?> type) {
        if (!JerseyBinding.isJerseyManaged(type, options.get(JerseyExtensionsManagedByGuice))) {
            throw new WrongContextException("HK2 creates service %s which must be managed by guice.",
                    type.getName());
        }
        hkManaged.add(type);
    }

    /**
     * Called by specific guice provision listener to check if bean is properly instantiated by guice.
     *
     * @param type instantiated bean type
     */
    public void guiceManage(final Class<?> type) {
        if (JerseyBinding.isJerseyManaged(type, options.get(JerseyExtensionsManagedByGuice))) {
            throw new WrongContextException("Guice creates service %s which must be managed by HK2.",
                    type.getName());
        }
        guiceManaged.add(type);
    }

    /**
     * @return classes of all tracked beans instantiated by HK2 (so far)
     */
    public List<Class<?>> getHkManaged() {
        return Lists.newArrayList(hkManaged);
    }

    /**
     * @return classes of all tracked beans instantiated by guice (so far)
     */
    public List<Class<?>> getGuiceManaged() {
        return Lists.newArrayList(guiceManaged);
    }

    private List<Class<?>> buildManagedTypes() {
        final List<Class<?>> managedTypes = Lists.newArrayList();
        for (Class<? extends FeatureInstaller> installer : holder.get().getInstallerTypes()) {
            if (!(JerseyInstaller.class.isAssignableFrom(installer))) {
                continue;
            }
            final List<Class<?>> features = holder.get().getExtensions(installer);
            if (features != null) {
                managedTypes.addAll(features);
            }
        }
        return managedTypes;
    }
}

package ru.vyarus.dropwizard.guice.module.context.info;

import com.google.inject.Module;
import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;

/**
 * Guice module configuration information. Note that only root modules directly registered
 * in the application or bundle are tracked. Guice modules, registered inside modules
 * (during guice context start) are not visible.
 *
 * @author Vyacheslav Rusakov
 * @since 03.04.2018
 */
public interface ModuleItemInfo extends InstanceItemInfo<Module>, DisableSupport {

    /**
     * @return true if overriding module (module overrides bindings of other modules), false if normal module
     */
    boolean isOverriding();
}

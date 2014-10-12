package ru.vyarus.dropwizard.guice.support.installer

import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.scanner.InvisibleForScanner

/**
 * Checks that annotation work for installers.
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@InvisibleForScanner
class InvisibleInstaller implements FeatureInstaller<Object> {
    @Override
    boolean matches(Class<?> type) {
        return false
    }

    @Override
    void report() {
    }
}

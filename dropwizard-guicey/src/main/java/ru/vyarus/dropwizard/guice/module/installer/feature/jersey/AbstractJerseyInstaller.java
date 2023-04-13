package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;

import com.google.inject.Binder;
import com.google.inject.ScopeAnnotation;
import com.google.inject.binder.AnnotatedBindingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding;
import ru.vyarus.dropwizard.guice.module.installer.option.InstallerOptionsSupport;

import jakarta.inject.Scope;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.ForceSingletonForJerseyExtensions;
import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.JerseyExtensionsManagedByGuice;
import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.isJerseyManaged;

/**
 * Base class for jersey installers ({@link JerseyInstaller}). Provides common utilities.
 *
 * @param <T> extensions type
 * @author Vyacheslav Rusakov
 * @since 28.04.2018
 */
public abstract class AbstractJerseyInstaller<T> extends InstallerOptionsSupport implements
        FeatureInstaller,
        JerseyInstaller<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Checks if lazy flag could be counted (only when extension is managed by guice). Prints warning in case
     * of incorrect lazy marker usage.
     *
     * @param type extension type
     * @param lazy lazy marker (annotation presence)
     * @return lazy marker if guice managed type and false when hk managed.
     */
    protected boolean isLazy(final Class<?> type, final boolean lazy) {
        if (isJerseyExtension(type) && lazy) {
            logger.warn("@{} is ignored, because @{} set: {}",
                    LazyBinding.class.getSimpleName(), JerseyManaged.class.getSimpleName(), type.getName());
            return false;
        }
        return lazy;
    }

    /**
     * @param type extension type
     * @return true if extension should be managed by hk, false to manage by guice
     */
    protected boolean isJerseyExtension(final Class<?> type) {
        return isJerseyManaged(type, option(JerseyExtensionsManagedByGuice));
    }

    /**
     * Bind to guice context. Singleton scope will be forced if it's not disabled (
     * {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#ForceSingletonForJerseyExtensions}) and
     * if no explicit scope is declared with annotation on bean.
     *
     * @param binder guice binder
     * @param type   extension type
     */
    protected void bindInGuice(final Binder binder, final Class<?> type) {
        final AnnotatedBindingBuilder<?> binding = binder.bind(type);
        if (isForceSingleton(type, false)) {
            // force singleton only if no explicit scope annotation present
            binding.in(Singleton.class);
        }
    }

    /**
     * Singleton binding should not be forced if bean has explicit scope declaration.
     *
     * @param type      bean type
     * @param hkManaged true if bean is going to be managed by hk, false for guice management
     * @return true to force singleton bindings for hk extensions (resources, filters etc), false otherwise
     */
    protected boolean isForceSingleton(final Class<?> type, final boolean hkManaged) {
        return ((Boolean) option(ForceSingletonForJerseyExtensions)) && !hasScopeAnnotation(type, hkManaged);
    }

    /**
     * Checks scope annotation presence directly on bean. Base classes are not checked as scope is not
     * inheritable.
     *
     * @param type      bean type
     * @param hkManaged true if bean is going to be managed by hk, false for guice management
     * @return true if scope annotation found, false otherwise
     */
    private boolean hasScopeAnnotation(final Class<?> type, final boolean hkManaged) {
        boolean found = false;
        for (Annotation ann : type.getAnnotations()) {
            final Class<? extends Annotation> annType = ann.annotationType();
            if (annType.isAnnotationPresent(Scope.class)) {
                found = true;
                break;
            }
            // guice has special marker annotation
            if (!hkManaged && annType.isAnnotationPresent(ScopeAnnotation.class)) {
                found = true;
                break;
            }
        }
        return found;
    }
}

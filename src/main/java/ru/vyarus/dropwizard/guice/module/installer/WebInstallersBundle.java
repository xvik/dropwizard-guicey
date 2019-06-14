package ru.vyarus.dropwizard.guice.module.installer;

import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.WebFilterInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.WebServletInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.listener.WebListenerInstaller;

/**
 * Bundle adds servlet installers for filters, servlets and listeners installation.
 * Standard java.servlet.annotation annotations ({@link javax.servlet.annotation.WebFilter},
 * {@link javax.servlet.annotation.WebServlet}, {@link javax.servlet.annotation.WebListener}) are used.
 * Note that these annotations are not recognized by jetty automatically, because dropwizard doesn't include
 * jetty-annotations modules.
 * <p>
 * By default, everything is installed for main context. Special annotation
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext} must be used to install into admin
 * or both contexts.
 * <p>
 * Also bundle assigns application ({@link Environment#getApplicationContext()}) and
 * admin ({@link Environment#getAdminContext()}) contexts display names
 * ({@link MutableServletContextHandler#setDisplayName(String)}). Does not override already assigned name.
 * <p>
 * If only annotations used for filters and servlets definitions consider disabling support for guice
 * {@link com.google.inject.servlet.ServletModule} using
 * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#GuiceFilterRegistration}.
 *
 * @author Vyacheslav Rusakov
 * @since 06.08.2016
 */
public class WebInstallersBundle implements GuiceyBundle {

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap.installers(
                WebFilterInstaller.class,
                WebServletInstaller.class,
                WebListenerInstaller.class
        );
    }

    @Override
    public void run(GuiceyEnvironment environment) {
        nameContext(environment.environment().getApplicationContext(), "Application context");
        nameContext(environment.environment().getAdminContext(), "Admin context");
    }

    private void nameContext(final MutableServletContextHandler context, final String name) {
        if (context.getDisplayName() == null) {
            context.setDisplayName(name);
        }
    }
}

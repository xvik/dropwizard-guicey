package ru.vyarus.dropwizard.guice.module.installer.feature.web.listener;

import com.google.common.collect.ImmutableList;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext;
import ru.vyarus.dropwizard.guice.module.installer.feature.web.util.WebUtils;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.option.InstallerOptionsSupport;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.DenySessionListenersWithoutSession;

/**
 * Search for servlet and session listeners annotated with {@link WebListener} (servlet api annotation).
 * Such listeners will not be installed by jetty because dropwizard didn't depend on jetty-annotations.
 * <p>
 * As stated in {@link WebListener} annotation javadoc both servlet (4) and session events (3) supported. But
 * dropwizard by default is stateless, so http listeners will be installed only if session handler defined.
 * Single extension could implement multiple listener interfaces (even all of them).
 * <p>
 * IMPORTANT: For session listeners registered for context without sessions support enabled only warning will
 * be showed in logs (and listeners will not be installed). Error is not thrown to let writing more universal
 * bundles with listener extensions (session related extensions will simply not work).
 * Use {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#DenySessionListenersWithoutSession}
 * to throw exception instead of warning.
 * <p>
 * By default, everything is installed for main context. Special annotation
 * {@link ru.vyarus.dropwizard.guice.module.installer.feature.web.AdminContext} must be used to install into admin
 * or both contexts.
 * <p>
 * Reporting format: <pre>[context markers: M - main, A - admin] (class)</pre>.
 * If listener registered only in main context, then context marker (M) is not shown.
 *
 * @author Vyacheslav Rusakov
 * @since 06.08.2016
 */
@Order(110)
public class WebListenerInstaller extends InstallerOptionsSupport
        implements FeatureInstaller, InstanceInstaller<EventListener>, Ordered {

    private static final List<Class<? extends EventListener>> CONTEXT_LISTENERS = ImmutableList.of(
            ServletContextListener.class,
            ServletContextAttributeListener.class,
            ServletRequestListener.class,
            ServletRequestAttributeListener.class
    );

    private static final List<Class<? extends EventListener>> SESSION_LISTENERS = ImmutableList.of(
            HttpSessionListener.class,
            HttpSessionAttributeListener.class,
            HttpSessionIdListener.class
    );

    private static final List<Class<? extends EventListener>> SUPPORTED = ImmutableList
            .<Class<? extends EventListener>>builder()
            .addAll(CONTEXT_LISTENERS)
            .addAll(SESSION_LISTENERS)
            .build();

    private final ListenerReporter reporter = new ListenerReporter();

    private SessionListenersSupport support;

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, EventListener.class)
                && FeatureUtils.hasAnnotation(type, WebListener.class)
                && hasMatch(type, SUPPORTED);
    }

    @Override
    public void install(final Environment environment, final EventListener instance) {
        final Class<? extends EventListener> extType = FeatureUtils.getInstanceClass(instance);
        final boolean isContextListener = hasMatch(extType, CONTEXT_LISTENERS);
        final boolean isSessionListener = hasMatch(extType, SESSION_LISTENERS);
        final AdminContext context = FeatureUtils.getAnnotation(extType, AdminContext.class);
        reporter.listener(extType, WebUtils.getContextMarkers(context));
        // lazy init delayed listeners processing
        if (isSessionListener && support == null) {
            support = new SessionListenersSupport(option(DenySessionListenersWithoutSession));
            environment.lifecycle().manage(support);
        }
        if (WebUtils.isForMain(context)) {
            configure(environment.getApplicationContext(), instance, isContextListener, isSessionListener);
        }
        if (WebUtils.isForAdmin(context)) {
            configure(environment.getAdminContext(), instance, isContextListener, isSessionListener);
        }
    }

    @Override
    public void report() {
        reporter.report();
    }

    private void configure(final MutableServletContextHandler environment, final EventListener listener,
                           final boolean context, final boolean session) {
        if (session) {
            support.add(environment, listener);
        }
        if (context) {
            environment.addEventListener(listener);
        }
    }

    private boolean hasMatch(final Class<?> type, final List<Class<? extends EventListener>> types) {
        boolean res = false;
        for (Class<? extends EventListener> listenerType : types) {
            if (listenerType.isAssignableFrom(type)) {
                res = true;
                break;
            }
        }
        return res;
    }

    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("implements " + EventListener.class.getSimpleName()
                + " + @" + WebListener.class.getSimpleName());
    }
}

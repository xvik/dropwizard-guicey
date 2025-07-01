package ru.vyarus.guicey.gsp.info;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.views.common.ViewRenderer;
import ru.vyarus.guicey.gsp.app.ServerPagesGlobalState;
import ru.vyarus.guicey.gsp.app.ServerPagesApp;
import ru.vyarus.guicey.gsp.info.model.GspApp;

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Guicey service providing information about registered server pages applications. Useful for reporting or diagnostic.
 *
 * @author Vyacheslav Rusakov
 * @since 03.12.2019
 */
@Singleton
public class GspInfoService {

    private final ServerPagesGlobalState config;

    /**
     * Create a gsp info service.
     *
     * @param config configuration
     */
    public GspInfoService(final ServerPagesGlobalState config) {
        this.config = config;
    }

    /**
     * @return names of registered dropwizard-views renderers
     */
    public List<String> getViewRendererNames() {
        checkLock();
        return config.getRenderers().stream().map(ViewRenderer::getConfigurationKey).collect(Collectors.toList());
    }

    /**
     * @return list or registered dropwizard-views registered renderers
     */
    public List<ViewRenderer> getViewRenderers() {
        checkLock();
        return ImmutableList.copyOf(config.getRenderers());
    }

    /**
     * @return views configuration (including all customizations)
     */
    public Map<String, Map<String, String>> getViewsConfig() {
        checkLock();
        return ImmutableMap.copyOf(config.getViewsConfig());
    }

    /**
     * @return registered gsp applications info
     */
    public List<GspApp> getApplications() {
        checkLock();
        final List<GspApp> res = new ArrayList<>();
        for (ServerPagesApp app : config.getApps()) {
            res.add(app.getInfo(config));
        }
        return res;
    }

    /**
     * @param name application name
     * @return application info or null if no application with provided name registered
     */
    public GspApp getApplication(final String name) {
        checkLock();
        for (ServerPagesApp app : config.getApps()) {
            if (name.equals(app.getName())) {
                return app.getInfo(config);
            }
        }
        return null;
    }

    private void checkLock() {
        // configuration is locked in view bundle (dw), which is called after guice bundle,
        // so info will become available after guice context start
        Preconditions.checkArgument(config.isLocked(), "GSP bundle is not yet initialized");
    }
}

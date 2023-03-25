package ru.vyarus.guicey.gsp.views;

import io.dropwizard.core.Configuration;
import io.dropwizard.views.common.ViewBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.gsp.app.GlobalConfig;

import java.util.HashMap;
import java.util.Map;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Views bundle with custom configuration handling.
 *
 * @author Vyacheslav Rusakov
 * @since 11.01.2019
 */
public class ConfiguredViewBundle extends ViewBundle<Configuration> {
    private final Logger logger = LoggerFactory.getLogger(ConfiguredViewBundle.class);
    private final GlobalConfig globalConfig;

    public ConfiguredViewBundle(final GlobalConfig globalConfig) {
        super(globalConfig.getRenderers());
        this.globalConfig = globalConfig;
    }

    @Override
    public Map<String, Map<String, String>> getViewConfiguration(final Configuration configuration) {
        globalConfig.lock();
        Map<String, Map<String, String>> config;
        if (globalConfig.getConfigurable() == null) {
            config = new HashMap<>();
        } else {
            config = globalConfig.getConfigurable()
                    .getViewConfiguration(configuration);
            if (config == null) {
                config = new HashMap<>();
            }
        }
        // only one bundle could configure global configuration provider, but all bundles
        // could modify resulted configuration
        for (String key : globalConfig.getConfigModifiers().keySet()) {
            if (!config.containsKey(key)) {
                config.put(key, new HashMap<>());
            }
            final Map<String, String> cfg = config.get(key);
            for (ViewRendererConfigurationModifier modifier
                    : globalConfig.getConfigModifiers().get(key)) {
                modifier.modify(cfg);
            }
        }
        if (globalConfig.isPrintConfiguration()) {
            logger.info("Views configuration: {}{}", NEWLINE, renderConfig(config));
        }
        return globalConfig.viewsConfig(config);
    }

    /**
     * Render views configuration.
     *
     * @param config views configuration
     * @return rendered configuration object for logs
     */
    private static String renderConfig(final Map<String, Map<String, String>> config) {
        final StringBuilder res = new StringBuilder(NEWLINE);
        if (config.isEmpty()) {
            res.append(TAB).append("empty configuration").append(NEWLINE);
        }
        for (Map.Entry<String, Map<String, String>> entry : config.entrySet()) {
            res.append(TAB).append(entry.getKey()).append(NEWLINE);
            if (entry.getValue().isEmpty()) {
                res.append(NEWLINE);
            }
            for (Map.Entry<String, String> ventry : entry.getValue().entrySet()) {
                res.append(TAB).append(TAB)
                        .append(ventry.getKey()).append(" = ").append(ventry.getValue()).append(NEWLINE);
            }
        }
        return res.toString();
    }
}

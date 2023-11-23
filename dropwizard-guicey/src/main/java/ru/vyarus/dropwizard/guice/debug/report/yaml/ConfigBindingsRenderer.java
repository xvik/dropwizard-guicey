package ru.vyarus.dropwizard.guice.debug.report.yaml;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Key;
import io.dropwizard.core.Configuration;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.debug.util.TreeNode;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigPath;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

import java.lang.annotation.Annotation;
import java.util.Collection;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Renders available configuration bindings.
 * <p>
 * By default all not null bindings are rendered. Optional renders:
 * <ul>
 * <li>With configuration tree</li>
 * <li>Configuration tree only</li>
 * <li>Show null values</li>
 * <li>Hide dropwizard Configuration properties</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 13.06.2018
 */
public class ConfigBindingsRenderer implements ReportRenderer<BindingsConfig> {

    private static final String EQUAL = " = ";
    private static final String CONFIG = "@Config";
    private static final String SPACE = " ";

    private final ConfigurationTree tree;

    public ConfigBindingsRenderer(final ConfigurationTree tree) {
        this.tree = tree;
    }

    @Override
    public String renderReport(final BindingsConfig config) {
        final StringBuilder res = new StringBuilder();
        if (config.isShowConfigurationTree()) {
            renderConfigurationTree(config, res);
        }
        if (config.isShowBindings()) {
            renderRootTypes(config, res);
            renderUniqueSubConfigs(config, res);
            renderQualified(res);
            renderPaths(config, res);
        }
        return res.toString();
    }

    private void renderConfigurationTree(final BindingsConfig config, final StringBuilder res) {
        final TreeNode node = new TreeNode(tree.getRootTypes().get(0).getSimpleName() + " (visible paths)");
        tree.findAllRootPaths().forEach(it -> renderSubtree(node, it, config));
        if (node.hasChildren()) {
            res.append(NEWLINE).append(NEWLINE);
            // do not render empty tree
            node.render(res);
        }
    }

    private void renderSubtree(final TreeNode root, final ConfigPath path, final BindingsConfig config) {
        if (isHidden(path, config)) {
            return;
        }
        final StringBuilder name = new StringBuilder()
                .append(path.getLastPathLevel()).append(": ").append(path.toStringType());
        if (!path.isCustomType()) {
            name.append(EQUAL).append(path.toStringValue());
        }
        final TreeNode node = root.child(name.toString());
        path.getChildren().forEach(it -> renderSubtree(node, it, config));
    }

    private void renderRootTypes(final BindingsConfig config, final StringBuilder res) {
        if (tree.getRootTypes().size() == 1 && config.isShowCustomConfigOnly()) {
            // only Configuration binding exists
            return;
        }
        res.append(NEWLINE).append(NEWLINE).append(TAB)
                .append("Configuration object bindings:").append(NEWLINE);
        for (Class type : tree.getRootTypes()) {
            if (config.isShowCustomConfigOnly() && type.equals(Configuration.class)) {
                continue;
            }
            res.append(TAB).append(TAB)
                    .append(CONFIG).append(SPACE).append(type.getSimpleName())
                    .append(NEWLINE);
        }
    }

    private void renderUniqueSubConfigs(final BindingsConfig config, final StringBuilder res) {
        boolean header = false;
        for (ConfigPath item : tree.getUniqueTypePaths()) {
            if (isHidden(item, config)) {
                continue;
            }
            if (!header) {
                // delayed render for no displayed items case
                res.append(NEWLINE).append(NEWLINE).append(TAB)
                        .append("Unique sub configuration objects bindings:").append(NEWLINE);
                header = true;
            }
            res.append(NEWLINE).append(TAB).append(TAB)
                    .append(item.getRootDeclarationClass().getSimpleName()).append('.').append(item.getPath())
                    .append(NEWLINE).append(TAB).append(TAB).append(TAB).append(CONFIG).append(SPACE);
            renderPath(item, res);
            res.append(NEWLINE);
        }
    }

    private void renderQualified(final StringBuilder res) {
        final Multimap<Key<?>, ConfigPath> bindings = LinkedHashMultimap.create();
        for (ConfigPath item : tree.getPaths()) {
            if (item.getQualifier() != null) {
                final Key<?> key = Key.get(item.getDeclaredTypeWithGenerics(), item.getQualifier());
                bindings.put(key, item);
            }
        }

        boolean header = false;
        for (Key<?> key : bindings.keySet()) {
            final Collection<ConfigPath> values = bindings.get(key);
            final ConfigPath first = values.iterator().next();
            final Annotation qualifier = first.getQualifier();
            if (qualifier == null) {
                continue;
            }
            if (!header) {
                // delayed render for no displayed items case
                res.append(NEWLINE).append(NEWLINE).append(TAB)
                        .append("Qualified bindings:").append(NEWLINE);
                header = true;
            }

            res.append(TAB).append(TAB).append(RenderUtils.renderAnnotation(qualifier)).append(' ');

            if (values.size() > 1) {
                res.append("Set<").append(first.toStringDeclaredType()).append("> = (aggregated values)\n");
                for (ConfigPath path : values) {
                    res.append(TAB).append(TAB).append(TAB);
                    renderPath(path, res);
                    res.append(" (").append(path.getPath()).append(')').append(NEWLINE);
                }
            } else {
                renderPath(first, res);
                res.append(" (").append(first.getPath()).append(')').append(NEWLINE);
            }
        }
    }

    private void renderPaths(final BindingsConfig config, final StringBuilder res) {
        boolean header = false;
        Class rootConfig = null;
        for (ConfigPath item : tree.getPaths()) {
            if (isHidden(item, config)) {
                continue;
            }
            if (!header) {
                // delayed render for no displayed items case
                res.append(NEWLINE).append(NEWLINE).append(TAB)
                        .append("Configuration paths bindings:").append(NEWLINE);
                header = true;
            }
            if (rootConfig != item.getRootDeclarationClass()) {
                // root declaring configuration class (sub section)
                rootConfig = item.getRootDeclarationClass();
                res.append(NEWLINE).append(TAB).append(TAB)
                        .append(rootConfig.getSimpleName()).append(':').append(NEWLINE);
            }

            res.append(TAB).append(TAB).append(TAB)
                    .append(CONFIG).append("(\"").append(item.getPath()).append("\") ");
            renderPath(item, res);
            res.append(NEWLINE);
        }
    }

    private void renderPath(final ConfigPath path, final StringBuilder res) {
        res.append(path.toStringDeclaredType()).append(path.isObjectDeclaration() ? '*' : "");
        if (path.getDeclaredType() != path.getValueType()) {
            res.append(" (with actual type ").append(path.toStringType()).append(')');
        }
        res.append(EQUAL).append(path.toStringValue());
    }

    private boolean isHidden(final ConfigPath path, final BindingsConfig config) {
        return (!config.isShowNullValues() && path.getValue() == null)
                || (config.isShowCustomConfigOnly() && path.getRootDeclarationClass().equals(Configuration.class));
    }
}

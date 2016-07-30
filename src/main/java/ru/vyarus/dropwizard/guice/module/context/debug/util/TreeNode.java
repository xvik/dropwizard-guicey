package ru.vyarus.dropwizard.guice.module.context.debug.util;

import com.google.common.collect.Lists;

import java.util.List;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Tree structure used for pretty console tree render. Tree rendered with one tab border. Subtrees separated by
 * empty line.
 *
 * @author Vyacheslav Rusakov
 * @since 18.07.2016
 */
public class TreeNode {

    private static final String THROUGH = "\u2502   ";
    private static final String LEAF = "\u251c\u2500\u2500 ";
    private static final String LAST_LEAF = "\u2514\u2500\u2500 ";

    private final String name;
    private final List<TreeNode> children = Lists.newArrayList();

    /**
     * Creates new node.
     *
     * @param name node name
     * @param args string format arguments
     */
    public TreeNode(final String name, final Object... args) {
        this.name = String.format(name, args);
    }

    /**
     * @param name node name
     * @param args string format arguments
     * @return child node instance, already attached to current node
     */
    public TreeNode child(final String name, final Object... args) {
        final TreeNode node = new TreeNode(name, args);
        children.add(node);
        return node;
    }

    /**
     * Add child node. Useful for situations when node could be appear empty (due to builder specifics) and
     * empty nodes must be avoided. In other cases {@link #child(String, Object...)} is simpler to use.
     *
     * @param node node to add
     */
    public void child(final TreeNode node) {
        children.add(node);
    }

    /**
     * @return true when node is subtree, false otherwise
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Renders tree to provided builder.
     *
     * @param res target builder
     */
    public void render(final StringBuilder res) {
        render(res, "", true, false);
    }

    private void render(final StringBuilder res, final String prefix,
                        final boolean isTail, final boolean gapBefore) {
        if (prefix.isEmpty()) {
            // root node
            res.append(TAB).append(name).append(NEWLINE);
        } else {
            if (gapBefore) {
                // gap before or after subtree
                res.append(prefix).append(THROUGH).append(NEWLINE);
            }
            // child node
            res.append(prefix).append(isTail ? LAST_LEAF : LEAF).append(name).append(NEWLINE);
        }
        for (int i = 0; i < children.size(); i++) {
            final boolean last = i == children.size() - 1;
            final TreeNode child = children.get(i);
            final boolean afterSubtree = i > 0 && !child.hasChildren() && children.get(i - 1).hasChildren();
            // empty line before subtree or before next child after subtree
            final boolean gap = child.hasChildren() || afterSubtree;
            child.render(res, prefix + (isTail ? TAB : THROUGH), last, gap);
        }
    }
}

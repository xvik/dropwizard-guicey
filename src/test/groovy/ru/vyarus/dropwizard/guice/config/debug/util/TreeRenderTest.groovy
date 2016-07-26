package ru.vyarus.dropwizard.guice.config.debug.util

import ru.vyarus.dropwizard.guice.module.context.debug.util.TreeNode
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 26.07.2016
 */
class TreeRenderTest extends Specification {

    def "Check model"() {

        when: "create root node"
        TreeNode node = new TreeNode("sample")
        then: "no children"
        !node.hasChildren()

        when: "add child"
        node.child(new TreeNode("sup"))
        then: "has children"
        node.hasChildren()
        node.children.size() == 1

        when: "add child simple"
        node.child("sup2")
        then: "has children"
        node.hasChildren()
        node.children.size() == 2
    }

    def "Check single node render"() {

        when:
        TreeNode node = new TreeNode("sample")
        then:
        render(node) == """
    sample
"""
    }

    def "Check small tree"() {

        when:
        TreeNode node = new TreeNode("sample")
        node.child("sub1")
        node.child("sub2")
        node.child("sub3")
        then:
        render(node) == """
    sample
    ├── sub1
    ├── sub2
    └── sub3
"""
    }

    def "Check sub tree"() {

        when:
        TreeNode node = new TreeNode("sample")
        TreeNode child = node.child("sub1")
        child.child("sub2")
        child.child("sub3")
        then:
        render(node) == """
    sample
    │
    └── sub1
        ├── sub2
        └── sub3
"""
    }

    def "Check composite sub tree"() {

        when:
        TreeNode node = new TreeNode("sample")
        node.child("sub0")
        TreeNode child = node.child("sub1")
        child.child("sub2")
        child.child("sub3")
        then:
        render(node) == """
    sample
    ├── sub0
    │
    └── sub1
        ├── sub2
        └── sub3
"""
    }

    def "Check deep sub tree"() {

        when:
        TreeNode node = new TreeNode("sample")
        node.child("sub0")
        TreeNode child = node.child("sub1")
        child.child("sub2")
        TreeNode child2 = child.child("sub3")
        child2.child("sub4")
        child2.child("sub5")
        then:
        render(node) == """
    sample
    ├── sub0
    │
    └── sub1
        ├── sub2
        │
        └── sub3
            ├── sub4
            └── sub5
"""
    }

    def "Check sub tree in the middle"() {

        when:
        TreeNode node = new TreeNode("sample")
        TreeNode child = node.child("sub1")
        child.child("sub2")
        child.child("sub3")
        node.child("sub4")
        node.child("sub5")
        then:
        render(node) == """
    sample
    │
    ├── sub1
    │   ├── sub2
    │   └── sub3
    │
    ├── sub4
    └── sub5
"""
    }

    private static String render(TreeNode node) {
        StringBuilder res = new StringBuilder(Reporter.NEWLINE)
        node.render(res)
        res.toString().replaceAll("\r", "").replaceAll(" +\n", "\n")
    }
}
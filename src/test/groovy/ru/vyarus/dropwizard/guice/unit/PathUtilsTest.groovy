package ru.vyarus.dropwizard.guice.unit

import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 29.11.2019
 */
class PathUtilsTest extends Specification {

    def "Check path combine"() {

        expect:
        PathUtils.path(base, path) == res

        where:
        base         | path          | res
        '/'          | 'sample.txt'  | '/sample.txt'
        ''           | 'sample.txt'  | 'sample.txt'
        '/foo/'      | '/sample.txt' | '/foo/sample.txt'
        '  /foo/  '  | '/sample.txt' | '/foo/sample.txt'
        '/foo\\bar/' | '/sample.txt' | '/foo/bar/sample.txt'
        'http://foo' | 'bar' | 'http://foo/bar'
    }

    def "Check path"() {

        expect:
        PathUtils.path(arr) == res

        where:
        arr                                    | res
        ['/', "/one/", '/'] as String[]        | '/one/'
        ['', "/one/", '/'] as String[]         | '/one/'
        ['', "one/", '/'] as String[]          | 'one/'
        [null, "one", null] as String[]        | 'one'
        [null, "one", null, "two"] as String[] | 'one/two'
    }

    def "Check path cleanup"() {

        expect:
        PathUtils.normalize(path) == res

        where:
        path           | res
        '/some/'       | '/some/'
        '//some//foo'  | '/some/foo'
        'some / foo'   | 'some/foo'
        'some / / foo' | 'some/foo'
        'some\\foo'    | 'some/foo'
    }

    def "Check prefix slash"() {

        expect:
        PathUtils.leadingSlash(path) == res

        where:
        path    | res
        ''      | '/'
        '/'     | '/'
        'foo'   | '/foo'
        '/foo/' | '/foo/'
    }

    def "Check end slash"() {

        expect:
        PathUtils.trailingSlash(path) == res

        where:
        path    | res
        ''      | ''
        '/'     | '/'
        'foo'   | 'foo/'
        '/foo/' | '/foo/'
    }

    def "Check trim stars"() {

        expect:
        PathUtils.trimStars(path) == res

        where:
        path     | res
        ''       | ''
        '/'      | '/'
        '/*'     | '/'
        '*/'     | '/'
        '/foo/*' | '/foo/'
        '*/*/*'  | '/*/'
    }

    def "Check trim slashes"() {

        expect:
        PathUtils.trimSlashes(path) == res

        where:
        path       | res
        ''         | ''
        '/'        | ''
        '/foo/'    | 'foo'
        '/foo/bar' | 'foo/bar'
        'foo/bar/' | 'foo/bar'
    }

    def "Check trim leading slash"() {

        expect:
        PathUtils.trimLeadingSlash(path) == res

        where:
        path       | res
        ''         | ''
        '/'        | ''
        '/foo/'    | 'foo/'
        '/foo/bar' | 'foo/bar'
        'foo/bar/' | 'foo/bar/'
    }

    def "Check trim trailing slash"() {

        expect:
        PathUtils.trimTrailingSlash(path) == res

        where:
        path       | res
        ''         | ''
        '/'        | ''
        '/foo/'    | '/foo'
        '/foo/bar' | '/foo/bar'
        'foo/bar/' | 'foo/bar'
    }

    def "Check class path"() {
        expect:
        PathUtils.packagePath(cls) == res

        where:
        cls       | res
        Integer   | 'java/lang/'
        PathUtils | 'ru/vyarus/dropwizard/guice/module/installer/util/'
    }

    def "Check relative path normalization"() {
        expect:
        PathUtils.normalizeRelativePath(path) == res

        where:
        path         | res
        '/'          | ''
        ''           | ''
        '/foo/'      | 'foo/'
        '  /foo/  '  | 'foo/'
        '/foo\\bar/' | 'foo/bar/'
        '/foo/bar'   | 'foo/bar/'
    }

    def "Check classpath path normalization"() {
        expect:
        PathUtils.normalizeClasspathPath(path) == res

        where:
        path          | res
        '/'           | ''
        ''            | ''
        '/foo/'       | 'foo/'
        '  /foo/  '   | 'foo/'
        '/foo\\bar/'  | 'foo/bar/'
        '/foo/bar'    | 'foo/bar/'
        'foo.bar'     | 'foo/bar/'
        'foo . bar'   | 'foo/bar/'
        '  foo.bar  ' | 'foo/bar/'
    }

    def "Check relative path"() {

        expect:
        PathUtils.normalizeRelativePath(path) == res

        where:
        path        | res
        '/'         | ''
        ''          | ''
        '/foo'      | 'foo/'
        '  /foo/  ' | 'foo/'
        '/foo\\bar' | 'foo/bar/'
        '/foo/bar'  | 'foo/bar/'
    }

    def "Check relative url"() {

        expect:
        PathUtils.relativize(path) == res

        where:
        path        | res
        '/'         | ''
        ''          | ''
        '/foo'      | 'foo'
        '  /foo/  ' | 'foo/'
        '/foo\\bar' | 'foo/bar'
        '/foo/bar'  | 'foo/bar'
    }
}

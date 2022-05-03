package ru.vyarus.dropwizard.guice.yaml

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.jersey.filter.AllowedMethodsFilter
import io.dropwizard.jetty.GzipHandlerFactory
import io.dropwizard.jetty.ServerPushFilterFactory
import io.dropwizard.logging.DefaultLoggingFactory
import io.dropwizard.logging.LoggingFactory
import io.dropwizard.metrics.MetricsFactory
import io.dropwizard.request.logging.RequestLogFactory
import io.dropwizard.server.DefaultServerFactory
import io.dropwizard.server.ServerFactory
import io.dropwizard.servlets.tasks.TaskConfiguration
import io.dropwizard.setup.AdminFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.setup.HealthCheckConfiguration
import io.dropwizard.util.Duration
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.yaml.ConfigPath
import ru.vyarus.dropwizard.guice.module.yaml.ConfigTreeBuilder
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.dropwizard.guice.yaml.support.*
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 05.05.2018
 */
@TestGuiceyApp(App)
class ConfigInspectorTest extends Specification {

    Object NOT_SET = new Object()

    @Inject
    Bootstrap bootstrap

    def "Check configuration introspection"() {

        when: "check default config"
        def res = ConfigTreeBuilder.build(bootstrap, create(Configuration))
        then:
        printConfig(res) == """[Configuration] admin (AdminFactory) = AdminFactory[healthChecks=HealthCheckConfiguration[servletEnabled= true, minThreads=1, maxThreads=4, workQueueSize=1], tasks=TaskConfiguration[printStackTraceOnError=false]]
[Configuration] admin.healthChecks (HealthCheckConfiguration) = HealthCheckConfiguration[servletEnabled= true, minThreads=1, maxThreads=4, workQueueSize=1]
[Configuration] admin.healthChecks.maxThreads (Integer) = 4
[Configuration] admin.healthChecks.minThreads (Integer) = 1
[Configuration] admin.healthChecks.servletEnabled (Boolean) = true
[Configuration] admin.healthChecks.workQueueSize (Integer) = 1
[Configuration] admin.tasks (TaskConfiguration) = TaskConfiguration[printStackTraceOnError=false]
[Configuration] admin.tasks.printStackTraceOnError (Boolean) = false
[Configuration] health (Optional<HealthFactory>) = Optional.empty
[Configuration] logging (LoggingFactory as DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1111111]}
[Configuration] logging.appenders (List<AppenderFactory<ILoggingEvent>> as ArrayList<AppenderFactory<ILoggingEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
[Configuration] logging.level (String) = "INFO"
[Configuration] logging.loggers (Map<String, JsonNode> as HashMap<String, JsonNode>) = {}
[Configuration] metrics (MetricsFactory) = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false}
[Configuration] metrics.frequency (Duration) = 1 minute
[Configuration] metrics.reportOnStop (Boolean) = false
[Configuration] metrics.reporters (List<ReporterFactory> as ArrayList<ReporterFactory>) = []
[Configuration] server (ServerFactory as DefaultServerFactory) = DefaultServerFactory{applicationConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminMaxThreads=64, adminMinThreads=1, applicationContextPath='/', adminContextPath='/'}
[Configuration] server.adminConnectors (List<ConnectorFactory> as ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
[Configuration] server.adminContextPath (String) = "/"
[Configuration] server.adminMaxThreads (Integer) = 64
[Configuration] server.adminMinThreads (Integer) = 1
[Configuration] server.allowedMethods (Set<String> as HashSet<String>) = [HEAD, DELETE, POST, GET, OPTIONS, PUT, PATCH]
[Configuration] server.applicationConnectors (List<ConnectorFactory> as ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
[Configuration] server.applicationContextPath (String) = "/"
[Configuration] server.detailedJsonProcessingExceptionMapper (Boolean) = false
[Configuration] server.dumpAfterStart (Boolean) = false
[Configuration] server.dumpBeforeStop (Boolean) = false
[Configuration] server.enableThreadNameFilter (Boolean) = true
[Configuration] server.gid (Integer) = null
[Configuration] server.group (String) = null
[Configuration] server.gzip (GzipHandlerFactory) = io.dropwizard.jetty.GzipHandlerFactory@1111111
[Configuration] server.gzip.bufferSize (DataSize) = 8 kibibytes
[Configuration] server.gzip.compressedMimeTypes (Set<String>) = null
[Configuration] server.gzip.deflateCompressionLevel (Integer) = -1
[Configuration] server.gzip.enabled (Boolean) = true
[Configuration] server.gzip.excludedMimeTypes (Set<String>) = null
[Configuration] server.gzip.excludedPaths (Set<String>) = null
[Configuration] server.gzip.excludedUserAgentPatterns (Set<String> as HashSet<String>) = []
[Configuration] server.gzip.gzipCompatibleInflation (Boolean) = true
[Configuration] server.gzip.includedMethods (Set<String>) = null
[Configuration] server.gzip.includedPaths (Set<String>) = null
[Configuration] server.gzip.minimumEntitySize (DataSize) = 256 bytes
[Configuration] server.gzip.syncFlush (Boolean) = false
[Configuration] server.idleThreadTimeout (Duration) = 1 minute
[Configuration] server.maxQueuedRequests (Integer) = 1024
[Configuration] server.maxThreads (Integer) = 1024
[Configuration] server.minThreads (Integer) = 8
[Configuration] server.nofileHardLimit (Integer) = null
[Configuration] server.nofileSoftLimit (Integer) = null
[Configuration] server.registerDefaultExceptionMappers (Boolean) = true
[Configuration] server.requestLog (RequestLogFactory<Object> as LogbackAccessRequestLogFactory) = io.dropwizard.request.logging.LogbackAccessRequestLogFactory@1111111
[Configuration] server.requestLog.appenders (List<AppenderFactory<IAccessEvent>> as ArrayList<AppenderFactory<IAccessEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
[Configuration] server.rootPath (Optional<String>) = Optional.empty
[Configuration] server.serverPush (ServerPushFilterFactory) = io.dropwizard.jetty.ServerPushFilterFactory@1111111
[Configuration] server.serverPush.associatePeriod (Duration) = 4 seconds
[Configuration] server.serverPush.enabled (Boolean) = false
[Configuration] server.serverPush.maxAssociations (Integer) = 16
[Configuration] server.serverPush.refererHosts (List<String>) = null
[Configuration] server.serverPush.refererPorts (List<Integer>) = null
[Configuration] server.shutdownGracePeriod (Duration) = 30 seconds
[Configuration] server.startsAsRoot (Boolean) = null
[Configuration] server.uid (Integer) = null
[Configuration] server.umask (String) = null
[Configuration] server.user (String) = null"""
        res.rootTypes == [Configuration]
        res.uniqueTypePaths.size() == 9
        res.paths.size() == 65
        check(res, "server", DefaultServerFactory)
        check(res, "server.maxThreads", Integer, 1024)
        check(res, "server.idleThreadTimeout", Duration, Duration.minutes(1))
        check(res, "server.allowedMethods", HashSet, AllowedMethodsFilter.DEFAULT_ALLOWED_METHODS, [String] as Class[])
        check(res, "logging", DefaultLoggingFactory)
        check(res, "metrics", MetricsFactory)

        when: "check simple config type"
        res = ConfigTreeBuilder.build(bootstrap, create(SimpleConfig))
        then:
        printConfig(res) == """[SimpleConfig] bar (Boolean) = null
[SimpleConfig] foo (String) = null
[SimpleConfig] prim (Integer) = 0"""
        res.rootTypes == [SimpleConfig, Configuration]
        res.uniqueTypePaths.size() == 9
        res.paths.size() == 68
        check(res, "foo", String)
        check(res, "bar", Boolean)
        check(res, "prim", Integer)

        when: "object field type"
        res = ConfigTreeBuilder.build(bootstrap, create(ObjectPropertyConfig))
        def elt = res.findByPath("sub")
        then: "Object remain as declared type"
        printConfig(res) == "[ObjectPropertyConfig] sub (Object) = null"
        res.rootTypes == [ObjectPropertyConfig, Configuration]
        res.uniqueTypePaths.size() == 9
        res.paths.size() == 66
        check(res, "sub", Object)
        elt.isObjectDeclaration()
        elt.declaredType == Object
        elt.valueType == Object
        elt.declaredTypeGenericClasses == []

        when: "object wield with non null value"
        def conf = create(ObjectPropertyConfig)
        conf.sub = new ArrayList()
        res = ConfigTreeBuilder.build(bootstrap, conf)
        elt = res.findByPath("sub")
        then: "declared type taken from value"
        printConfig(res) == "[ObjectPropertyConfig] sub (List<Object>* as ArrayList<Object>) = []"
        check(res, "sub", ArrayList)
        elt.declaredType == List
        elt.valueType == ArrayList
        elt.declaredTypeGenericClasses == [Object]
        elt.isObjectDeclaration()

        when: "check complex config type"
        res = ConfigTreeBuilder.build(bootstrap, create(ComplexConfig))
        then:
        printConfig(res) == """[ComplexConfig] one (ComplexConfig.Parametrized<Integer>) = null
[ComplexConfig] one.list (List<Integer>) = null
[ComplexConfig] sub (SubConfig) = null
[ComplexConfig] sub.sub (String) = null
[ComplexConfig] sub.two (ComplexConfig.Parametrized<String>) = null
[ComplexConfig] sub.two.list (List<String>) = null"""
        res.rootTypes == [ComplexConfig, Iface, Configuration]
        res.uniqueTypePaths.size() == 10
        res.uniqueTypePaths.find { it.valueType == ComplexConfig.SubConfig } != null
        res.uniqueTypePaths.find { it.valueType == ComplexConfig.Parametrized } == null
        res.paths.size() == 71
        check(res, "sub", ComplexConfig.SubConfig)
        check(res, "sub.sub", String)
        check(res, "sub.two", ComplexConfig.Parametrized, null, String)
        check(res, "sub.two.list", List, null, String)
        check(res, "one", ComplexConfig.Parametrized, null, Integer)
        check(res, "one.list", List, null, Integer)
    }

    def "Check complex generic case"() {

        when: "check field with contradicting generic"
        def configuration = create(ComplexGenericCase)
        def value = new ComplexGenericCase.SubImpl<String>()
        configuration.sub = value
        def res = ConfigTreeBuilder.build(bootstrap, configuration)
        then:
        printConfig(res) == """[ComplexGenericCase] sub (ComplexGenericCase.Sub<String> as ComplexGenericCase.SubImpl<String>) = ru.vyarus.dropwizard.guice.yaml.support.ComplexGenericCase\$SubImpl@1111111
[ComplexGenericCase] sub.smth (String) = "sample"
[ComplexGenericCase] sub.val (String) = null"""
        check(res, "sub", ComplexGenericCase.SubImpl, value, String)
        check(res, "sub.smth", String, "sample")
        check(res, "sub.val", String, null)
    }

    def "Check not unique sub config"() {

        when: "config with duplicate sub config usages"
        def res = ConfigTreeBuilder.build(bootstrap, create(NotUniqueSubConfig))
        then: "sub config not unique"
        printConfig(res) == """[NotUniqueSubConfig] sub1 (NotUniqueSubConfig.SubConfig<String>) = null
[NotUniqueSubConfig] sub1.sub (String) = null
[NotUniqueSubConfig] sub2 (NotUniqueSubConfig.SubConfig<String>) = null
[NotUniqueSubConfig] sub2.sub (String) = null
[NotUniqueSubConfig] sub3 (NotUniqueSubConfig.SubConfig<Integer>) = null
[NotUniqueSubConfig] sub3.sub (String) = null"""
        !res.uniqueTypePaths.contains(NotUniqueSubConfig.SubConfig)

        and: "sub config generic recognized"
        res.findByPath('sub1').declaredTypeGenericClasses == [String]
        res.findByPath('sub2').declaredTypeGenericClasses == [String]
        res.findByPath('sub3').declaredTypeGenericClasses == [Integer]
    }

    def "Check declaration type lowering"() {

        when: "properties declared as list implementation"
        def res = ConfigTreeBuilder.build(bootstrap, create(TooBroadDeclarationConfig))
        then: "property types lowered"
        printConfig(res) == """[TooBroadDeclarationConfig] bar (List<Integer> as TooBroadDeclarationConfig.ExtraList<String, Integer>) = null
[TooBroadDeclarationConfig] foo (List<String> as ArrayList<String>) = null"""

        and: "sub config generic recognized"
        res.findByPath('foo').toStringDeclaredType() == "List<String>"
        res.findByPath('foo').toStringType() == "ArrayList<String>"
        res.findByPath('bar').toStringDeclaredType() == "List<Integer>"
        res.findByPath('bar').toStringType() == "TooBroadDeclarationConfig.ExtraList<String, Integer>"
    }

    def "Check configuration lookup methods"() {

        when:
        def res = ConfigTreeBuilder.build(bootstrap, create(NotUniqueSubConfig))
        then:
        res.getUniqueTypePaths().collect { it.getDeclaredType() } as Set ==
                [ServerPushFilterFactory, LoggingFactory, ServerFactory, GzipHandlerFactory, MetricsFactory, RequestLogFactory,
                 TaskConfiguration, AdminFactory, HealthCheckConfiguration] as Set
        res.findByPath("sub1").getDeclaredType() == NotUniqueSubConfig.SubConfig
        res.findByPath("sub1.sub").getDeclaredType() == String
        res.findAllByType(NotUniqueSubConfig.SubConfig).collect { it.path } == ["sub1", "sub2", "sub3"]
        res.findAllFrom(NotUniqueSubConfig).collect { it.path } == ["sub1", "sub1.sub", "sub2", "sub2.sub", "sub3", "sub3.sub"]
        res.findAllRootPaths().collect { it.path } == ["sub1", "sub2", "sub3", "admin", "health", "logging", "metrics", "server"]
        res.findAllRootPathsFrom(NotUniqueSubConfig).collect { it.path } == ["sub1", "sub2", "sub3"]
        res.findAllRootPathsFrom(Configuration).collect { it.path } == ["admin", "health", "logging", "metrics", "server"]
        res.valueByPath("not.exists") == null
        res.valueByPath("sub1") == null
        res.valueByType(NotUniqueSubConfig.SubConfig) == null
        res.valuesByType(NotUniqueSubConfig.SubConfig).isEmpty()
        res.valueByUniqueDeclaredType(NotUniqueSubConfig.SubConfig) == null

        when: "complex config with null unique"
        def config = create(ComplexConfig)
        config.one = new ComplexConfig.Parametrized()
        res = ConfigTreeBuilder.build(bootstrap, config)
        then:
        res.getUniqueTypePaths().collect { it.getDeclaredType() } as Set ==
                [ComplexConfig.SubConfig, ServerPushFilterFactory, LoggingFactory, ServerFactory, GzipHandlerFactory, MetricsFactory, RequestLogFactory,
                 TaskConfiguration, AdminFactory, HealthCheckConfiguration] as Set
        res.findByPath("sub").getDeclaredType() == ComplexConfig.SubConfig
        res.findByPath("sub.sub").getDeclaredType() == String
        res.findAllByType(ComplexConfig.SubConfig).collect { it.path } == ["sub"]
        res.findAllByType(ComplexConfig.Parametrized).collect { it.path } == ["one", "sub.two"]
        res.findAllFrom(ComplexConfig).collect { it.path } == ["one", "one.list", "sub", "sub.sub", "sub.two", "sub.two.list"]
        res.findAllRootPaths().collect { it.path } == ["one", "sub", "admin", "health", "logging", "metrics", "server"]
        res.findAllRootPathsFrom(ComplexConfig).collect { it.path } == ["one", "sub"]
        res.findAllRootPathsFrom(Configuration).collect { it.path } == ["admin", "health", "logging", "metrics", "server"]
        res.valueByPath("not.exists") == null
        res.valueByPath("sub") == null
        res.valueByType(ComplexConfig.SubConfig) == null
        res.valuesByType(ComplexConfig.SubConfig).isEmpty()
        res.valueByUniqueDeclaredType(ComplexConfig.SubConfig) == null
        res.valuesByType(ComplexConfig.Parametrized).size() == 1
        res.valueByUniqueDeclaredType(ComplexConfig.Parametrized) == null

        when: "complex config with non null unique"
        config = create(ComplexConfig)
        config.sub = new ComplexConfig.SubConfig()
        config.sub.two = new ComplexConfig.Parametrized()
        res = ConfigTreeBuilder.build(bootstrap, config)
        then:
        res.valueByPath("sub") != null
        res.valueByPath("sub") instanceof ComplexConfig.SubConfig
        res.valuesByType(ComplexConfig.SubConfig).size() == 1
        res.valueByUniqueDeclaredType(ComplexConfig.SubConfig) != null
        res.valueByUniqueDeclaredType(ComplexConfig.SubConfig) instanceof ComplexConfig.SubConfig
        res.valueByUniqueDeclaredType(ComplexConfig.SubConfig).two != null
        res.valuesByType(ComplexConfig.Parametrized).size() == 1
        res.valueByUniqueDeclaredType(ComplexConfig.Parametrized) == null
    }

    def "Check value accessors"() {

        when: "config with not unique custom type"
        def config = create(NotUniqueSubConfig)
        config.sub1 = new NotUniqueSubConfig.SubConfig(sub: "val")
        config.sub2 = new NotUniqueSubConfig.SubConfig()
        def res = ConfigTreeBuilder.build(bootstrap, config)
        then:
        res.valueByPath("not.exists") == null
        res.valueByPath("sub1") != null
        res.valueByPath("sub1.sub") == "val"
        res.valueByType(NotUniqueSubConfig.SubConfig) != null
        res.valuesByType(NotUniqueSubConfig.SubConfig).size() == 2
        res.valueByUniqueDeclaredType(NotUniqueSubConfig.SubConfig) == null

        when: "config with unique custom type"
        config = create(ComplexGenericCase)
        config.sub = new ComplexGenericCase.SubImpl()
        res = ConfigTreeBuilder.build(bootstrap, config)
        then:
        res.valueByPath("not.exists") == null
        res.valueByPath("sub") != null
        res.valueByPath("sub.smth") == "sample"
        res.valueByPath("sub.val") == null
        res.valueByType(ComplexGenericCase.Sub) != null
        res.valuesByType(ComplexGenericCase.Sub).size() == 1
        res.valueByUniqueDeclaredType(ComplexGenericCase.Sub) != null
        res.valueByUniqueDeclaredType(ComplexGenericCase.Sub) instanceof ComplexGenericCase.Sub
    }

    def "Check item methods"() {

        when: "1st level path"
        def res = ConfigTreeBuilder.build(bootstrap, create(NotUniqueSubConfig))
        def path = res.findByPath("sub1")
        then:
        path.root == null
        path.children.collect { it.path } == ["sub1.sub"]
        path.declarationClass == NotUniqueSubConfig
        path.declaredType == NotUniqueSubConfig.SubConfig
        path.valueType == NotUniqueSubConfig.SubConfig
        path.declaredTypeGenerics == [String]
        path.declaredTypeGenericClasses == [String]
        path.valueTypeGenerics == [String]
        path.valueTypeGenericClasses == [String]
        path.path == "sub1"
        path.value == null
        path.customType
        !path.objectDeclaration
        path.rootDeclarationClass == NotUniqueSubConfig
        path.toString() == "[NotUniqueSubConfig] sub1 (NotUniqueSubConfig.SubConfig<String>) = null"
        path.equals(path)

        when: "2nd level path"
        path = res.findByPath("sub1.sub")
        then:
        path.root.path == "sub1"
        path.children.isEmpty()
        path.declarationClass == NotUniqueSubConfig.SubConfig
        path.declaredType == String
        path.valueType == String
        path.declaredTypeGenerics == []
        path.declaredTypeGenericClasses == []
        path.valueTypeGenerics == []
        path.valueTypeGenericClasses == []
        path.path == "sub1.sub"
        path.value == null
        !path.customType
        !path.objectDeclaration
        path.rootDeclarationClass == NotUniqueSubConfig
        path.toString() == "[NotUniqueSubConfig] sub1.sub (String) = null"
        !path.equals(res.findByPath("sub1"))
        path.hashCode() != res.findByPath("sub1").hashCode()

        when: "object property with value"
        def config = create(ObjectPropertyConfig)
        config.sub = new ArrayList()
        res = ConfigTreeBuilder.build(bootstrap, config)
        path = res.findByPath("sub")
        then:
        path.root == null
        path.children.isEmpty()
        path.declarationClass == ObjectPropertyConfig
        path.declaredType == List
        path.valueType == ArrayList
        path.declaredTypeGenerics == [Object]
        path.declaredTypeGenericClasses == [Object]
        path.valueTypeGenerics == [Object]
        path.valueTypeGenericClasses == [Object]
        path.path == "sub"
        path.value != null
        !path.customType
        path.objectDeclaration
        path.rootDeclarationClass == ObjectPropertyConfig
        path.toString() == "[ObjectPropertyConfig] sub (List<Object>* as ArrayList<Object>) = []"

    }


    def "Check failed getter access"() {

        when: "introspecting config with crashing getter"
        def res = ConfigTreeBuilder.build(bootstrap, new FailedGetterConfig())
        then:
        res.findByPath('sample') == null
    }

    def "Check ignored property"() {

        when: "config with ignored getter"
        def res = ConfigTreeBuilder.build(bootstrap, new IgnorePathConfig())
        then:
        res.findByPath('foo') != null
        res.findByPath('prop') == null
    }

    def "Check disabled introspection"() {

        when: "check default config"
        def res = ConfigTreeBuilder.build(bootstrap, create(Configuration), false)
        then:
        res.rootTypes.size() > 0
        res.paths.isEmpty()
        res.uniqueTypePaths.isEmpty()
    }

    def "Check recursive property"() {

        when: "config with recursive property"
        def res = ConfigTreeBuilder.build(bootstrap, new RecursiveConfig())
        then:
        res.findByPath('customProperty') != null
        res.findByPath('customProperty.value') != null
        res.findByPath('customProperty.customProperty') != null
        res.findByPath('customProperty.customProperty.customProperty') == null

        when: "config with non null object on recursion path"
        res = ConfigTreeBuilder.build(bootstrap, new RecursiveConfig(
                customProperty: new RecursiveConfig.CustomProperty(customProperty: new RecursiveConfig.CustomProperty())))
        then:
        res.findByPath('customProperty') != null
        res.findByPath('customProperty.value') != null
        res.findByPath('customProperty.customProperty') != null
        res.findByPath('customProperty.customProperty.value') != null
        res.findByPath('customProperty.customProperty.customProperty') != null
        res.findByPath('customProperty.customProperty.customProperty.value') == null

        when: "recursion is indirect"
        res = ConfigTreeBuilder.build(bootstrap, new RecursiveIndirectlyConfig())
        then:
        res.findByPath('next') != null
        res.findByPath('next.value') != null
        res.findByPath('next.next') != null
        res.findByPath('next.next.next') != null
        res.findByPath('next.next.next.next') == null
    }

    private <T extends Configuration> T create(Class<T> type) {
        bootstrap.configurationFactoryFactory
                .create(type, bootstrap.validatorFactory.validator, bootstrap.objectMapper, "dw").build()
    }

    private boolean check(ConfigurationTree config, String path, Class type, Object value = NOT_SET, Class[] generics = null) {
        ConfigPath item = config.findByPath(path)
        assert item != null
        assert item.valueType == type
        if (value != NOT_SET) {
            assert item.value == value
        }
        if (generics != null) {
            assert item.getDeclaredTypeGenerics().toArray(new Class[0]) == generics
        }
        true
    }

    private String printConfig(ConfigurationTree config) {
        boolean skipDwConf = config.getRootTypes().size() > 1
        def res = removePointers(
                config.paths.sort { one, two -> one.path.compareTo(two.path) }.findAll {
                    !skipDwConf || it.rootDeclarationClass != Configuration
                }.join('\n'))
        println res
        res
    }

    private String removePointers(String str) {
        str.replaceAll('@[^] \n]+', '@1111111')
    }


    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}

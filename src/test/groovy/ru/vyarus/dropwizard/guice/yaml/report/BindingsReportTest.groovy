package ru.vyarus.dropwizard.guice.yaml.report

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.module.yaml.report.BindingsConfig
import ru.vyarus.dropwizard.guice.module.yaml.report.ConfigBindingsRenderer
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import ru.vyarus.dropwizard.guice.yaml.support.ComplexGenericCase
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 13.06.2018
 */
@UseGuiceyApp(App)
class BindingsReportTest extends Specification {

    @Inject
    ConfigurationTree tree

    def "Check default render report"() {
        expect:

        // NOTE @Config("server.requestLog") RequestLogFactory<Object> should be
        // @Config("server.requestLog") RequestLogFactory<RequestLog>, but its not because
        // type is lowered on declaration: private RequestLogFactory<?> requestLog; (AbstractServerFactory)
        // SO case is: type information INTENTIONALLY lowered
        render(new BindingsConfig()) == """

    Configuration object bindings:
        @Config ComplexGenericCase
        @Config Configuration


    Unique sub configuration objects bindings:

        Configuration.logging
            @Config LoggingFactory (with actual type DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1111111]}

        Configuration.metrics
            @Config MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false}

        Configuration.server
            @Config ServerFactory (with actual type DefaultServerFactory) = DefaultServerFactory{applicationConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminMaxThreads=64, adminMinThreads=1, applicationContextPath='/', adminContextPath='/'}

        Configuration.server.gzip
            @Config GzipHandlerFactory = io.dropwizard.jetty.GzipHandlerFactory@1111111

        Configuration.server.requestLog
            @Config RequestLogFactory<Object> (with actual type LogbackAccessRequestLogFactory) = io.dropwizard.request.logging.LogbackAccessRequestLogFactory@1111111

        Configuration.server.serverPush
            @Config ServerPushFilterFactory = io.dropwizard.jetty.ServerPushFilterFactory@1111111


    Configuration paths bindings:

        Configuration:
            @Config("logging") LoggingFactory (with actual type DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1111111]}
            @Config("logging.appenders") List<AppenderFactory<ILoggingEvent>> (with actual type ArrayList<AppenderFactory<ILoggingEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
            @Config("logging.level") String = "INFO"
            @Config("logging.loggers") Map<String, JsonNode> (with actual type HashMap<String, JsonNode>) = {}
            @Config("metrics") MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false}
            @Config("metrics.frequency") Duration = 1 minute
            @Config("metrics.reportOnStop") Boolean = false
            @Config("metrics.reporters") List<ReporterFactory> (with actual type ArrayList<ReporterFactory>) = []
            @Config("server") ServerFactory (with actual type DefaultServerFactory) = DefaultServerFactory{applicationConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminMaxThreads=64, adminMinThreads=1, applicationContextPath='/', adminContextPath='/'}
            @Config("server.adminConnectors") List<ConnectorFactory> (with actual type ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
            @Config("server.adminContextPath") String = "/"
            @Config("server.adminMaxThreads") Integer = 64
            @Config("server.adminMinThreads") Integer = 1
            @Config("server.allowedMethods") Set<String> (with actual type HashSet<String>) = [HEAD, DELETE, POST, GET, OPTIONS, PUT, PATCH]
            @Config("server.applicationConnectors") List<ConnectorFactory> (with actual type ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
            @Config("server.applicationContextPath") String = "/"
            @Config("server.detailedJsonProcessingExceptionMapper") Boolean = false
            @Config("server.dumpAfterStart") Boolean = false
            @Config("server.dumpBeforeStop") Boolean = false
            @Config("server.enableThreadNameFilter") Boolean = true
            @Config("server.gzip") GzipHandlerFactory = io.dropwizard.jetty.GzipHandlerFactory@1111111
            @Config("server.gzip.bufferSize") DataSize = 8 kibibytes
            @Config("server.gzip.deflateCompressionLevel") Integer = -1
            @Config("server.gzip.enabled") Boolean = true
            @Config("server.gzip.excludedUserAgentPatterns") Set<String> (with actual type HashSet<String>) = []
            @Config("server.gzip.gzipCompatibleInflation") Boolean = true
            @Config("server.gzip.minimumEntitySize") DataSize = 256 bytes
            @Config("server.gzip.syncFlush") Boolean = false
            @Config("server.idleThreadTimeout") Duration = 1 minute
            @Config("server.maxQueuedRequests") Integer = 1024
            @Config("server.maxThreads") Integer = 1024
            @Config("server.minThreads") Integer = 8
            @Config("server.registerDefaultExceptionMappers") Boolean = true
            @Config("server.requestLog") RequestLogFactory<Object> (with actual type LogbackAccessRequestLogFactory) = io.dropwizard.request.logging.LogbackAccessRequestLogFactory@1111111
            @Config("server.requestLog.appenders") List<AppenderFactory<IAccessEvent>> (with actual type ArrayList<AppenderFactory<IAccessEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
            @Config("server.rootPath") Optional<String> = Optional.empty
            @Config("server.serverPush") ServerPushFilterFactory = io.dropwizard.jetty.ServerPushFilterFactory@1111111
            @Config("server.serverPush.associatePeriod") Duration = 4 seconds
            @Config("server.serverPush.enabled") Boolean = false
            @Config("server.serverPush.maxAssociations") Integer = 16
            @Config("server.shutdownGracePeriod") Duration = 30 seconds
"""
    }

    def "Check full render report"() {
        expect:

        // NOTE @Config("server.requestLog") RequestLogFactory<Object> should be
        // @Config("server.requestLog") RequestLogFactory<RequestLog>, but its not because
        // type is lowered on declaration: private RequestLogFactory<?> requestLog; (AbstractServerFactory)
        // SO case is: type information INTENTIONALLY lowered
        render(new BindingsConfig()
                .showConfigurationTree()
                .showNullValues()) == """

    ComplexGenericCase (visible paths)
    │
    ├── sub: ComplexGenericCase.Sub<String>
    │   └── smth: String = null
    │
    ├── logging: DefaultLoggingFactory
    │   ├── appenders: ArrayList<AppenderFactory<ILoggingEvent>> = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
    │   ├── level: String = "INFO"
    │   └── loggers: HashMap<String, JsonNode> = {}
    │
    ├── metrics: MetricsFactory
    │   ├── frequency: Duration = 1 minute
    │   ├── reportOnStop: Boolean = false
    │   └── reporters: ArrayList<ReporterFactory> = []
    │
    └── server: DefaultServerFactory
        ├── adminConnectors: ArrayList<ConnectorFactory> = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
        ├── adminContextPath: String = "/"
        ├── adminMaxThreads: Integer = 64
        ├── adminMinThreads: Integer = 1
        ├── allowedMethods: HashSet<String> = [HEAD, DELETE, POST, GET, OPTIONS, PUT, PATCH]
        ├── applicationConnectors: ArrayList<ConnectorFactory> = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
        ├── applicationContextPath: String = "/"
        ├── detailedJsonProcessingExceptionMapper: Boolean = false
        ├── dumpAfterStart: Boolean = false
        ├── dumpBeforeStop: Boolean = false
        ├── enableThreadNameFilter: Boolean = true
        ├── gid: Integer = null
        ├── group: String = null
        ├── idleThreadTimeout: Duration = 1 minute
        ├── maxQueuedRequests: Integer = 1024
        ├── maxThreads: Integer = 1024
        ├── minThreads: Integer = 8
        ├── nofileHardLimit: Integer = null
        ├── nofileSoftLimit: Integer = null
        ├── registerDefaultExceptionMappers: Boolean = true
        ├── rootPath: Optional<String> = Optional.empty
        ├── shutdownGracePeriod: Duration = 30 seconds
        ├── startsAsRoot: Boolean = null
        ├── uid: Integer = null
        ├── umask: String = null
        ├── user: String = null
        │
        ├── gzip: GzipHandlerFactory
        │   ├── bufferSize: DataSize = 8 kibibytes
        │   ├── compressedMimeTypes: Set<String> = null
        │   ├── deflateCompressionLevel: Integer = -1
        │   ├── enabled: Boolean = true
        │   ├── excludedMimeTypes: Set<String> = null
        │   ├── excludedPaths: Set<String> = null
        │   ├── excludedUserAgentPatterns: HashSet<String> = []
        │   ├── gzipCompatibleInflation: Boolean = true
        │   ├── includedMethods: Set<String> = null
        │   ├── includedPaths: Set<String> = null
        │   ├── minimumEntitySize: DataSize = 256 bytes
        │   └── syncFlush: Boolean = false
        │
        ├── requestLog: LogbackAccessRequestLogFactory
        │   └── appenders: ArrayList<AppenderFactory<IAccessEvent>> = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
        │
        └── serverPush: ServerPushFilterFactory
            ├── associatePeriod: Duration = 4 seconds
            ├── enabled: Boolean = false
            ├── maxAssociations: Integer = 16
            ├── refererHosts: List<String> = null
            └── refererPorts: List<Integer> = null


    Configuration object bindings:
        @Config ComplexGenericCase
        @Config Configuration


    Unique sub configuration objects bindings:

        ComplexGenericCase.sub
            @Config ComplexGenericCase.Sub<String> = null

        Configuration.logging
            @Config LoggingFactory (with actual type DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1111111]}

        Configuration.metrics
            @Config MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false}

        Configuration.server
            @Config ServerFactory (with actual type DefaultServerFactory) = DefaultServerFactory{applicationConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminMaxThreads=64, adminMinThreads=1, applicationContextPath='/', adminContextPath='/'}

        Configuration.server.gzip
            @Config GzipHandlerFactory = io.dropwizard.jetty.GzipHandlerFactory@1111111

        Configuration.server.requestLog
            @Config RequestLogFactory<Object> (with actual type LogbackAccessRequestLogFactory) = io.dropwizard.request.logging.LogbackAccessRequestLogFactory@1111111

        Configuration.server.serverPush
            @Config ServerPushFilterFactory = io.dropwizard.jetty.ServerPushFilterFactory@1111111


    Configuration paths bindings:

        ComplexGenericCase:
            @Config("sub") ComplexGenericCase.Sub<String> = null
            @Config("sub.smth") String = null

        Configuration:
            @Config("logging") LoggingFactory (with actual type DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1111111]}
            @Config("logging.appenders") List<AppenderFactory<ILoggingEvent>> (with actual type ArrayList<AppenderFactory<ILoggingEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
            @Config("logging.level") String = "INFO"
            @Config("logging.loggers") Map<String, JsonNode> (with actual type HashMap<String, JsonNode>) = {}
            @Config("metrics") MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[], reportOnStop=false}
            @Config("metrics.frequency") Duration = 1 minute
            @Config("metrics.reportOnStop") Boolean = false
            @Config("metrics.reporters") List<ReporterFactory> (with actual type ArrayList<ReporterFactory>) = []
            @Config("server") ServerFactory (with actual type DefaultServerFactory) = DefaultServerFactory{applicationConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminConnectors=[io.dropwizard.jetty.HttpConnectorFactory@1111111], adminMaxThreads=64, adminMinThreads=1, applicationContextPath='/', adminContextPath='/'}
            @Config("server.adminConnectors") List<ConnectorFactory> (with actual type ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
            @Config("server.adminContextPath") String = "/"
            @Config("server.adminMaxThreads") Integer = 64
            @Config("server.adminMinThreads") Integer = 1
            @Config("server.allowedMethods") Set<String> (with actual type HashSet<String>) = [HEAD, DELETE, POST, GET, OPTIONS, PUT, PATCH]
            @Config("server.applicationConnectors") List<ConnectorFactory> (with actual type ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
            @Config("server.applicationContextPath") String = "/"
            @Config("server.detailedJsonProcessingExceptionMapper") Boolean = false
            @Config("server.dumpAfterStart") Boolean = false
            @Config("server.dumpBeforeStop") Boolean = false
            @Config("server.enableThreadNameFilter") Boolean = true
            @Config("server.gid") Integer = null
            @Config("server.group") String = null
            @Config("server.gzip") GzipHandlerFactory = io.dropwizard.jetty.GzipHandlerFactory@1111111
            @Config("server.gzip.bufferSize") DataSize = 8 kibibytes
            @Config("server.gzip.compressedMimeTypes") Set<String> = null
            @Config("server.gzip.deflateCompressionLevel") Integer = -1
            @Config("server.gzip.enabled") Boolean = true
            @Config("server.gzip.excludedMimeTypes") Set<String> = null
            @Config("server.gzip.excludedPaths") Set<String> = null
            @Config("server.gzip.excludedUserAgentPatterns") Set<String> (with actual type HashSet<String>) = []
            @Config("server.gzip.gzipCompatibleInflation") Boolean = true
            @Config("server.gzip.includedMethods") Set<String> = null
            @Config("server.gzip.includedPaths") Set<String> = null
            @Config("server.gzip.minimumEntitySize") DataSize = 256 bytes
            @Config("server.gzip.syncFlush") Boolean = false
            @Config("server.idleThreadTimeout") Duration = 1 minute
            @Config("server.maxQueuedRequests") Integer = 1024
            @Config("server.maxThreads") Integer = 1024
            @Config("server.minThreads") Integer = 8
            @Config("server.nofileHardLimit") Integer = null
            @Config("server.nofileSoftLimit") Integer = null
            @Config("server.registerDefaultExceptionMappers") Boolean = true
            @Config("server.requestLog") RequestLogFactory<Object> (with actual type LogbackAccessRequestLogFactory) = io.dropwizard.request.logging.LogbackAccessRequestLogFactory@1111111
            @Config("server.requestLog.appenders") List<AppenderFactory<IAccessEvent>> (with actual type ArrayList<AppenderFactory<IAccessEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
            @Config("server.rootPath") Optional<String> = Optional.empty
            @Config("server.serverPush") ServerPushFilterFactory = io.dropwizard.jetty.ServerPushFilterFactory@1111111
            @Config("server.serverPush.associatePeriod") Duration = 4 seconds
            @Config("server.serverPush.enabled") Boolean = false
            @Config("server.serverPush.maxAssociations") Integer = 16
            @Config("server.serverPush.refererHosts") List<String> = null
            @Config("server.serverPush.refererPorts") List<Integer> = null
            @Config("server.shutdownGracePeriod") Duration = 30 seconds
            @Config("server.startsAsRoot") Boolean = null
            @Config("server.uid") Integer = null
            @Config("server.umask") String = null
            @Config("server.user") String = null
"""
    }

    def "Check custom config render report"() {
        expect:
        render(new BindingsConfig()
                .showCustomConfigOnly()
                .showNullValues()
                .showConfigurationTree()) == """

    ComplexGenericCase (visible paths)
    │
    └── sub: ComplexGenericCase.Sub<String>
        └── smth: String = null


    Configuration object bindings:
        @Config ComplexGenericCase


    Unique sub configuration objects bindings:

        ComplexGenericCase.sub
            @Config ComplexGenericCase.Sub<String> = null


    Configuration paths bindings:

        ComplexGenericCase:
            @Config("sub") ComplexGenericCase.Sub<String> = null
            @Config("sub.smth") String = null
"""
    }


    def "Check empty report"() {
        expect:
        render(new BindingsConfig()
                .showCustomConfigOnly()
                .showConfigurationTree()) == """

    Configuration object bindings:
        @Config ComplexGenericCase
"""
    }


    def "Check tree only report"() {
        expect:
        render(new BindingsConfig()
                .showConfigurationTreeOnly()) == """

    ComplexGenericCase (visible paths)
    │
    ├── logging: DefaultLoggingFactory
    │   ├── appenders: ArrayList<AppenderFactory<ILoggingEvent>> = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
    │   ├── level: String = "INFO"
    │   └── loggers: HashMap<String, JsonNode> = {}
    │
    ├── metrics: MetricsFactory
    │   ├── frequency: Duration = 1 minute
    │   ├── reportOnStop: Boolean = false
    │   └── reporters: ArrayList<ReporterFactory> = []
    │
    └── server: DefaultServerFactory
        ├── adminConnectors: ArrayList<ConnectorFactory> = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
        ├── adminContextPath: String = "/"
        ├── adminMaxThreads: Integer = 64
        ├── adminMinThreads: Integer = 1
        ├── allowedMethods: HashSet<String> = [HEAD, DELETE, POST, GET, OPTIONS, PUT, PATCH]
        ├── applicationConnectors: ArrayList<ConnectorFactory> = [io.dropwizard.jetty.HttpConnectorFactory@1111111]
        ├── applicationContextPath: String = "/"
        ├── detailedJsonProcessingExceptionMapper: Boolean = false
        ├── dumpAfterStart: Boolean = false
        ├── dumpBeforeStop: Boolean = false
        ├── enableThreadNameFilter: Boolean = true
        ├── idleThreadTimeout: Duration = 1 minute
        ├── maxQueuedRequests: Integer = 1024
        ├── maxThreads: Integer = 1024
        ├── minThreads: Integer = 8
        ├── registerDefaultExceptionMappers: Boolean = true
        ├── rootPath: Optional<String> = Optional.empty
        ├── shutdownGracePeriod: Duration = 30 seconds
        │
        ├── gzip: GzipHandlerFactory
        │   ├── bufferSize: DataSize = 8 kibibytes
        │   ├── deflateCompressionLevel: Integer = -1
        │   ├── enabled: Boolean = true
        │   ├── excludedUserAgentPatterns: HashSet<String> = []
        │   ├── gzipCompatibleInflation: Boolean = true
        │   ├── minimumEntitySize: DataSize = 256 bytes
        │   └── syncFlush: Boolean = false
        │
        ├── requestLog: LogbackAccessRequestLogFactory
        │   └── appenders: ArrayList<AppenderFactory<IAccessEvent>> = [io.dropwizard.logging.ConsoleAppenderFactory@1111111]
        │
        └── serverPush: ServerPushFilterFactory
            ├── associatePeriod: Duration = 4 seconds
            ├── enabled: Boolean = false
            └── maxAssociations: Integer = 16
"""
    }


    String render(BindingsConfig config) {
        new ConfigBindingsRenderer(tree).renderReport(config)
                .replaceAll("\r", "")
                .replaceAll(" +\n", "\n")
                .replaceAll('@[^]C \n]+', '@1111111')
    }

    static class App extends Application<ComplexGenericCase> {
        @Override
        void initialize(Bootstrap<ComplexGenericCase> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(ComplexGenericCase configuration, Environment environment) throws Exception {
        }
    }
}
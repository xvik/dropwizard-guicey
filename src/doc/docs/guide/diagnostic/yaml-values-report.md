# Yaml values report

Prints all available [direct yaml values](../yaml-values.md):

```java
GuiceBundle.builder()
    ...
    .printConfigurationBindings()
     // or printCustomConfigurationBindings() to avoid dropwizard bindings 
    .build()
```

Will print:

```
INFO  [2018-06-18 05:55:03,532] ru.vyarus.dropwizard.guice.module.yaml.report.DebugConfigBindings: Available configuration bindings = 

    ComplexGenericCase (visible paths)
    │   
    ├── sub: ComplexGenericCase.Sub<String>
    │   └── smth: String = null
    │   
    ├── logging: DefaultLoggingFactory
    │   ├── level: String = "INFO"
    │   ├── loggers: RegularImmutableMap<String, JsonNode> = {}
    │   └── appenders: SingletonImmutableList<AppenderFactory<ILoggingEvent>> = [io.dropwizard.logging.ConsoleAppenderFactory@1b7332a7]
    │   
    ├── metrics: MetricsFactory
    │   ├── frequency: Duration = 1 minute
    │   └── reporters: RegularImmutableList<ReporterFactory> = []
    │   
    └── server: DefaultServerFactory
        ├── maxThreads: Integer = 1024
        ├── minThreads: Integer = 8
        ├── maxQueuedRequests: Integer = 1024
        ├── idleThreadTimeout: Duration = 1 minute
        ├── nofileSoftLimit: Integer = null
        ├── nofileHardLimit: Integer = null
        ├── gid: Integer = null
        ├── uid: Integer = null
        ├── user: String = null
        ├── group: String = null
        ├── umask: String = null
        ├── startsAsRoot: Boolean = null
        ├── registerDefaultExceptionMappers: Boolean = true
        ├── detailedJsonProcessingExceptionMapper: Boolean = false
        ├── shutdownGracePeriod: Duration = 30 seconds
        ├── allowedMethods: HashSet<String> = [HEAD, DELETE, POST, GET, OPTIONS, PUT, PATCH]
        ├── enableThreadNameFilter: Boolean = true
        ├── applicationConnectors: ArrayList<ConnectorFactory> = [io.dropwizard.jetty.HttpConnectorFactory@33063f5b]
        ├── adminConnectors: ArrayList<ConnectorFactory> = [io.dropwizard.jetty.HttpConnectorFactory@227a47]
        ├── adminMaxThreads: Integer = 64
        ├── adminMinThreads: Integer = 1
        ├── applicationContextPath: String = "/"
        ├── adminContextPath: String = "/"
        │   
        ├── serverPush: ServerPushFilterFactory
        │   ├── enabled: Boolean = false
        │   ├── associatePeriod: Duration = 4 seconds
        │   ├── maxAssociations: Integer = 16
        │   ├── refererHosts: List<String> = null
        │   └── refererPorts: List<Integer> = null
        │   
        ├── rootPath: Optional<String> = Optional.empty
        │   
        ├── requestLog: LogbackAccessRequestLogFactory
        │   └── appenders: SingletonImmutableList<AppenderFactory<IAccessEvent>> = [io.dropwizard.logging.ConsoleAppenderFactory@58a2b4c]
        │   
        └── gzip: GzipHandlerFactory
            ├── enabled: Boolean = true
            ├── minimumEntitySize: Size = 256 bytes
            ├── bufferSize: Size = 8 kilobytes
            ├── excludedUserAgentPatterns: HashSet<String> = []
            ├── compressedMimeTypes: Set<String> = null
            ├── includedMethods: Set<String> = null
            ├── deflateCompressionLevel: Integer = -1
            ├── gzipCompatibleInflation: Boolean = true
            └── syncFlush: Boolean = false


    Configuration object bindings:
        @Config ComplexGenericCase
        @Config Configuration


    Unique sub configuration objects bindings:

        ComplexGenericCase.sub
            @Config ComplexGenericCase.Sub<String> = null

        Configuration.logging
            @Config LoggingFactory (with actual type DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1b7332a7]}

        Configuration.metrics
            @Config MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[]}

        Configuration.server
            @Config ServerFactory (with actual type DefaultServerFactory) = DefaultServerFactory{applicationConnectors=[io.dropwizard.jetty.HttpConnectorFactory@33063f5b], adminConnectors=[io.dropwizard.jetty.HttpConnectorFactory@227a47], adminMaxThreads=64, adminMinThreads=1, applicationContextPath=/, adminContextPath=/}

        Configuration.server.gzip
            @Config GzipHandlerFactory = io.dropwizard.jetty.GzipHandlerFactory@15405bd6

        Configuration.server.requestLog
            @Config RequestLogFactory<RequestLog> (with actual type LogbackAccessRequestLogFactory) = io.dropwizard.request.logging.LogbackAccessRequestLogFactory@6de30571

        Configuration.server.serverPush
            @Config ServerPushFilterFactory = io.dropwizard.jetty.ServerPushFilterFactory@3240b2a4


    Configuration paths bindings:

        ComplexGenericCase:
            @Config("sub") ComplexGenericCase.Sub<String> = null
            @Config("sub.smth") String = null

        Configuration:
            @Config("logging") LoggingFactory (with actual type DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1b7332a7]}
            @Config("logging.appenders") List<AppenderFactory<ILoggingEvent>> (with actual type SingletonImmutableList<AppenderFactory<ILoggingEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@1b7332a7]
            @Config("logging.level") String = "INFO"
            @Config("logging.loggers") Map<String, JsonNode> (with actual type RegularImmutableMap<String, JsonNode>) = {}
            @Config("metrics") MetricsFactory = MetricsFactory{frequency=1 minute, reporters=[]}
            @Config("metrics.frequency") Duration = 1 minute
            @Config("metrics.reporters") List<ReporterFactory> (with actual type RegularImmutableList<ReporterFactory>) = []
            @Config("server") ServerFactory (with actual type DefaultServerFactory) = DefaultServerFactory{applicationConnectors=[io.dropwizard.jetty.HttpConnectorFactory@33063f5b], adminConnectors=[io.dropwizard.jetty.HttpConnectorFactory@227a47], adminMaxThreads=64, adminMinThreads=1, applicationContextPath=/, adminContextPath=/}
            @Config("server.adminConnectors") List<ConnectorFactory> (with actual type ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@227a47]
            @Config("server.adminContextPath") String = "/"
            @Config("server.adminMaxThreads") Integer = 64
            @Config("server.adminMinThreads") Integer = 1
            @Config("server.allowedMethods") Set<String> (with actual type HashSet<String>) = [HEAD, DELETE, POST, GET, OPTIONS, PUT, PATCH]
            @Config("server.applicationConnectors") List<ConnectorFactory> (with actual type ArrayList<ConnectorFactory>) = [io.dropwizard.jetty.HttpConnectorFactory@33063f5b]
            @Config("server.applicationContextPath") String = "/"
            @Config("server.detailedJsonProcessingExceptionMapper") Boolean = false
            @Config("server.enableThreadNameFilter") Boolean = true
            @Config("server.gid") Integer = null
            @Config("server.group") String = null
            @Config("server.gzip") GzipHandlerFactory = io.dropwizard.jetty.GzipHandlerFactory@15405bd6
            @Config("server.gzip.bufferSize") Size = 8 kilobytes
            @Config("server.gzip.compressedMimeTypes") Set<String> = null
            @Config("server.gzip.deflateCompressionLevel") Integer = -1
            @Config("server.gzip.enabled") Boolean = true
            @Config("server.gzip.excludedUserAgentPatterns") Set<String> (with actual type HashSet<String>) = []
            @Config("server.gzip.gzipCompatibleInflation") Boolean = true
            @Config("server.gzip.includedMethods") Set<String> = null
            @Config("server.gzip.minimumEntitySize") Size = 256 bytes
            @Config("server.gzip.syncFlush") Boolean = false
            @Config("server.idleThreadTimeout") Duration = 1 minute
            @Config("server.maxQueuedRequests") Integer = 1024
            @Config("server.maxThreads") Integer = 1024
            @Config("server.minThreads") Integer = 8
            @Config("server.nofileHardLimit") Integer = null
            @Config("server.nofileSoftLimit") Integer = null
            @Config("server.registerDefaultExceptionMappers") Boolean = true
            @Config("server.requestLog") RequestLogFactory<RequestLog> (with actual type LogbackAccessRequestLogFactory) = io.dropwizard.request.logging.LogbackAccessRequestLogFactory@6de30571
            @Config("server.requestLog.appenders") List<AppenderFactory<IAccessEvent>> (with actual type SingletonImmutableList<AppenderFactory<IAccessEvent>>) = [io.dropwizard.logging.ConsoleAppenderFactory@58a2b4c]
            @Config("server.rootPath") Optional<String> = Optional.empty
            @Config("server.serverPush") ServerPushFilterFactory = io.dropwizard.jetty.ServerPushFilterFactory@3240b2a4
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
```     

Here you can see:

* All visible configuration values (as tree).
* Types and interfaces for configuration object
* Unique sub configuration objects
* Values bound to guice context

## Guice

Report is mostly intended to be used to see available guice bindings and that's why 
`@Config` annotation is shown almsot everywhere. For example, 

```
@Config("server.serverPush.enabled") Boolean = false
```

means that the following injection will work:

```java
@Inject @Config("server.serverPush.enabled") Boolean pushEnabled;
```

And it will be set to "false". 

!!! tip
    Report is printed before guice injector creation to let you review all available bindings
    when injector creation fails due to incorrect configuration value binding. 

The same for unique objects:

```
Configuration.logging
            @Config LoggingFactory (with actual type DefaultLoggingFactory) = DefaultLoggingFactory{level=INFO, loggers={}, appenders=[io.dropwizard.logging.ConsoleAppenderFactory@1b7332a7]}
```

Means available injection:

```java
@Inject @Config LoggingFactory logginFactory;
```

!!! note
    Declaration type is always used (`LoggingFactory`) even when runtype type is known
    (`(with actual type DefaultLoggingFactory)`). This is important to keep 
    bindings consistent (otherwise they would always change, depending on actual config value).

## Direct values access

But report is also handful for direct values access (through `ConfigurationTree` [object](../yaml-values.md#introspected-configuration)).

For example, accessing value by path:

```java
tree.valueByPath("logging.level") == INFO
```

Or unique object access:

```java
tree.valueByUniqueDeclaredType(MetricsFactory.class) == <factory instance>
```                        

## Report customization

Report is implemented as guicey [event listener](../events.md) and provide additional customization 
options, so if default configuration (from shortcut methods above) does not fit your needs
you can register listener directly with required configuration.
 
For example, custom bindings report is configured like this:
 
```java
listen(new YamlBindingsDiagnostic(
            new BindingsConfig()
                    .showConfigurationTree()
                    .showNullValues()
                    .showCustomConfigOnly()));
``` 

Report rendering logic may also be used directly as report provide separate renderer object
implementing `ReportRenderer`. Renderer not bound to guice context and assume direct instantiation. 

For examples of direct renderer usage see [events](../events.md) implementation:

* `RunPhaseEvent.renderConfigurationBindings()` 
          

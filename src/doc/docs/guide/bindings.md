# Bindings

!!! note
    Guicey is compatible with some [guice restrictive options](injector.md#restrictive-options) (like disableCircularProxies)    

## Registered bindings

Guicey binds:

* Dropwizard objects:
    * `io.dropwizard.setup.Bootstrap` 
    * `io.dropwizard.Configuration` 
    * `io.dropwizard.setup.Environment`
* Detailed configuration bindings (by root classes, interfaces, yaml path or unique sub type)
* All installed extensions (extensions are explicitly bound to guice, except some specific installers which do manual bindings).
* Jersey objects (including request scoped) 
* Guicey special objects


### Configuration

`Configuration` bound to guice as: 

* `io.dropwizard.Configuration`
* Your configuration class (`#!java MyConfiguration extends Configuration`)
* All classes between them

For example, if

```java
MyConfiguration extends MyAbstractConfiguration extends Configuration
```

Then `MyAbstractConfiguration` will be also bound and following injection will work:

```java 
@Inject MyAbstractConfiguration conf
```

!!! note
    Configuration object bindings could be declared with or without qualifier `@Config`.
    So `@Inject @Config MyAbstractConfiguration conf` will also work. It is suggested to always
    use qualifier (to get in common with direct value bindings), but it's up to you.

If root configuration classes implement interfaces then configuration could be bound by interface.
This may be used to support common `Has<Something>` configuration interfaces convention used to recognize your extension configuration in configuration object.

```java
public interface HasFeatureX {
    FeatureXConfig getFetureXConfig();
}
    
public class MyConfiguration extends Configuration implements HasFeatureXConfig {...}

public class MyBean {
    @Inject @Config HasFeatureX conf;
    ...
}
```

!!! important
    It is required to use qualifier `@Config` for interface binding (to avoid accidental binding clashes).
    There is a **deprecated** bundle option `.bindConfigurationInterfaces()`, which enables direct
    interfaces bindings without qualifier (feature remains for compatibility, prefer qualified interfaces bindings).

Interface binding will ignore interfaces in `java.*` or `groovy.*` packages (to avoid unnecessary bindings).

!!! tip
    Consider using direct sub configuration object binding (see below) instead of marker interface
    if object uniqueness is guaranteed in user configuration.

#### Configuration tree

Guicey introspects configuration instance using jackson serialization api. That means only values
visible for jackson serialization will be presented.

!!! note
    Jackson will see all properties which either have getter and setter or annotated with `@JsonProperty`. For example,
    ```java
    public class MyConfig extends Configuration {
            
        private String one // visible (getter and setter)
        @JsonProperty
        private String two // visible (annotation)
        private String three // invisible (no getter)

        public void setOne(String one) { ... }
        public String getOne() { ... }

        public void setTwo(String two) { ... }

        public void setThree(String three) { ... }
    }
    ``` 

Tree of visible configuration paths could be accessed with bound 
`ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree` object.
It could be used for config analysis, reporting or something else.

For simple cases, direct configuration value bindings could be used (see below).

!!! tip
    To prevent binding of configuration property use `@JsonIgnore` on property *getter*
    ```java
    private String prop
    
    // dropwizard will set value from yaml
    public void setProp(Stirng prop) { ... }

    // property will not be available as path binding
    @JsonIgnore    
    public String getProp() { ... }
    ```

#### Unique sub configuration

It is quite common to group configuration properties into sub objects like:

```java
public class MyConfig extends Configuration {
    @JsonProperty AuthConfig auth;
    @JsonProperty DbConfig db;
}
```

Guicey detects such unique objects and provides direct bindings for them:

```java
@Inject @Config AuthConfig auth;
@Inject @Config DbConfig db;
```

This is very useful for re-usable modules, which are not aware of your configuration 
object structure, but require only one sub configuration object:

```java
public class MyConfig extends Configuration {
    @JsonProperty FeatureXConfig featureX;
}
```

Somewhere in module service:

```java
public class FeatureXService {
    @Inject @Config FeatureXConfig featureX; 
}
```

!!! important
    Sub configuration object uniqueness is checked as direct match, so you may have
    ```java
    @JsonProperty SubConfig sub
    @JsonProperty SubConfigExt subExt
    ```
    where `class SubConfigExt extends SubConfig`, but still both objects would be considered unique.
    Whereas
    ```java
    @JsonProperty SubConfig sub1
    @JsonProperty SubConfig sub2
    ```
    will not.
    
!!! tip
    Guicey bundles and guice modules also could use sub configuration objects directly:
    ```java
    GuiceyBootstrap#configuration(SubConfig.class)
    DropwizardAwareModule#configuration(SubConfig.class)
    ```    

#### Configuration by path

All visible configuration paths values are directly bindable:

```java
public class MyConfig extends Configuration {
    SubConf sub;
}

public class SubConf {
    String smth;
    List<String> values;
}
```

```java
@Inject @Config("sub") SubConf sub;
@Inject @Config("sub.smth") String smth;
@Inject @Config("sub.values") List<String> values;
```

!!! note
    Path bindings are available even for null values. For example, if sub configuration object
    is null, all it's sub paths will still be available (by class declarations). 
    The only exception is conditional mappin like dropwizard "server" when available paths
    could change, depending on configuration (what configuration class will be used)

!!! note 
    Generified types are bound only with generics (with all available type information).
    If you will have `SubConf<T> sub` in config, then it will be bound with correct generic `SubConfig<String>`
    (suppose generic T is declared as String).
    
Value type, declared in configuration class is used for binding, but there are two exceptions.

If declared type is declared as collection (Set, List, Map) implementation then binding will use
base collection interface:

```java
ArrayList<String> value

@Inject @Config("value") List<String> vlaue;
```

If, for some (unforgivable) reason, property is declared as Object in configuration,
then binding type will depend on value presence:

* `@Config("path") Object val` - when value is null
* `@Config("path") ValueType val` - actual value type, when value is not null       

It is assumed that in such case value would be always present (some sort of property-selected binding, like dropwizard "server").

!!! tip
    You can declare you own additional bindings using `ConfigurationTree` (accessible from guice module), 
    which contains all paths information (including declaration and actual types with generics).

#### Configuration bindings report

You can print all available configuration bindings with values:

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

#### Introspected configuration

`ConfigurationTree` object provides access for introspected configuration tree:

* `getRootTypes()` - all classes in configuration hierarchy (including interfaces)
* `getPaths()` - all paths (including all steps ("sub", "sub.value"))
* `getUniqueTypePaths()` - paths of unique sub configuration types

Each path item (`ConfigPath`) contains:

* Root path reference ("sub.value" reference "sub")
* Child sub-paths ("sub" reference "sub.value")
* Declaration class (type used in configuration class)
* Value type (type of actual value; when value null - declaration type (but they still could be different for collections))
* Current path name
* Current path value
* Generics for declaration and value types (may be incomplete for value type)
* Custom type marker: contains sub paths or just looks like sub configuration
* Declaration type (class where property was declared - configuration object containing property)

You can traverse up or down from any path (tree structure).

`ConfigurationTree` provides basic search methods (more as usage example):

* `findByPath(String)` - search path by case-insensitive match
* `findAllByType(Class)` - find all paths with assignable declared value
* `findAllFrom(Class<? extends Configuration>)` - find all paths, started in specified configuration class
* `findAllRootPaths()` - get all root paths (1st level paths) 
* `findAllRootPathsFrom(Class<? extends Configuration>)` - all 1st level paths of configuration class
* `valueByPath(String)` - return path value or null if value null or path not exists
* `valuesByType(Class)` - all not null values with assignable type
* `valueByType(Class)` - first not null value with assignable type
* `valueByUniqueDeclaredType(Class)` - value of unique sub conifguration or null if value is null or config is not unique

Paths are sorted by configuration class (to put custom properties upper) and by path name
(for predictable paths order).

### Environment binding

Dropwizard `io.dropwizard.setup.Environment` is bound to guice context.

It is mostly useful to perform additional configurations in guice bean for features not covered with installers. 
For example:

```java
public class MyBean {
    
    @Inject
    public MyBean(Environment environment) {
        environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener {
            public void serverStarted(Server server) {
                callSomeMethod();
            }
        })
    }
}
```

It's not the best example, but it illustrates usage (and such things usually helps to quick-test something). 

See also [authentication configuration example](../examples/authentication.md).

### Jersey specific bindings

Jersey bindings are not immediately available, because HK2 context starts after guice, 
so use `Provider` to inject these bindings.

These bindings available after HK2 context start:

* `javax.ws.rs.core.Application`
* `javax.ws.rs.ext.Providers`
* `org.glassfish.hk2.api.ServiceLocator`
* `org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider`

Request-scoped bindings:

* `javax.ws.rs.core.UriInfo`
* `javax.ws.rs.container.ResourceInfo`
* `javax.ws.rs.core.HttpHeaders`
* `javax.ws.rs.core.SecurityContext`
* `javax.ws.rs.core.Request`
* `org.glassfish.jersey.server.ContainerRequest`
* `org.glassfish.jersey.server.internal.process.AsyncContext`

!!! tip
    Read about jersey bindings implementation in [lifecycle section](lifecycle.md#access-guice-beans-from-jersey).

### Request and response

By default, `GuiceFilter` is enabled on both contexts (admin and main). So you can inject
request and response objects and use under filter, servlet or resources calls (guice filter wraps all web interactions).

If you disable guice filter with [.noGuiceFilter()](configuration.md#servletmodule) then
guicey will bridge objects from HK2 context:

* `javax.servlet.http.HttpServletRequest`
* `javax.servlet.http.HttpServletResponse`
 
!!! attention ""
    This means you can still inject them, but request and response will
    only be available under resource calls (the only part managed by jersey).

Example usage:

```java
@Inject Provider<HttpServletRequest> requestProvider;
```

!!! note
    Pay attention, that in guice-managed resources `@Context` field bindings 
    [must be replaced with providers](../installers/resource.md#@context-usage).

### Options

`ru.vyarus.dropwizard.guice.module.context.option.Options` binding provides access to [guicey options](options.md):
```java
@Inject Options options;
``` 

Example usage:

```java
Preconditions.checkState(options.get(GuiceyOptions.UseHkBridge), 
                                 "HK2 guice bridge is required!")
```

### Guicey configuration

`ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo` binding provides access
to [guicey configuration details](diagnostic.md):

```java
@Inject GuiceyConfigurationInfo info
```

## Binding scope

As you know, guice assume prototype scope by default (when no scope annotation declared).

Guice scope annotations:

* `@Singleton` - single instance per context
* `@RequestScoped` - object per request (if guice filter support is [not disabled](web.md#disable-servletmodule-support)
* `@Prototype` - prototype scope annotation (useful for jersey services to override force singleton scope)

Jersey extensions ([resources](../installers/resource.md), [providers](../installers/jersey-ext.md)) **force
singleton scope** for extensions without explicit binding annotation (but this could be disabled with 
[an option](../installers/resource.md)).
Use explicit scope annotations where singletons are not required.

### Request scope transition

!!! note ""
    This is guice feature, it is just mentioned here.

Guice could access request scoped bindings only in current thread. If you need to access 
request scoped binding in different thread, you need to transfer request scope into that thread:

```java
@Singleton 
public class RequestBean {

    // provider will not work in other thread because of request scope
    @Inject
    Provider<UriInfo> uri;
    
    public void doSomethingInRequestScope() {
            // wrap logic that require request scope 
            Callable<String> action = ServletScopes.transferRequest(() -> {
                // access request scoped binding in different thread 
                return uri.get().getQueryParameters().getFirst("q");
            });            
            CompletableFuture.runAsync(() -> {
                try {
                    // execute action in different thread
                    action.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
}            
```

### Extensions initialisation

All guicey extensions are bound to guice context (`.bind(ExtType)`, to be compatible with `.requireExplicitBindings()` strict option) 
that means that all singleton scoped extensions will be initialized in time of injector creation
(because by default, `Stage.PRODCUTION` used).

!!! tip
    You can change guice injector stage to `Stage.DEVELOPMENT` in the [main bundle](configuration.md#stage)
    to avoid singletons initialization during context creation (only `.asEagerSingleton()`)
    bindings will be initialized). But don't do it without a *really good reason*
    (for example, legacy codebase required development stage).        

Also, you can avoid extension binding by annotating it with `@LazyBinding`: this 
way extension will not be bound to guice context and so will initialize only
on first usage (obtaining from context).

### HK2 scope

By default, all jersey extensions (resources, providers) are managed by guice. But you can switch it to be managed by HK2 by default

```java
GuiceBundle.builder()
    ...
   .useHK2ForJerseyExtensions()
   .build() 
```

!!! warning
    You will not be able to use guice AOP on HK2 managed beans. 
    
Depending on default mode selected, you can use annotations to change extension DI:

* `@HK2Managed` - extension managed by HK2 (useful in default mode when all beans created by guice)
* `@GuiceManaged` - extension managed by guice (default, useful only when `.useHK2ForJerseyExtensions()` enabled)

### Bindings override

Guicey provides direct support for [overriding bindings](configuration.md#override-guice-bindings)
and even [override overridden bindings in tests](test.md#overriding-overridden-beans).
       
# Bindings

Guicey always installs `GuiceBootstrapModule` which registers the following bindings:

* Dropwizard objects:
    * `io.dropwizard.setup.Bootstrap` 
    * `io.dropwizard.Configuration` 
    * `io.dropwizard.setup.Environment`
* Detailed [configuration bindings](#configuration) (by root classes, interfaces, yaml path or unique subtype)
* [Jersey objects](#jersey-specific-bindings) (including [request scoped](#request-and-response)) 
* Guicey [special objects](#guicey-configuration)
* All installed [extensions](#extension-bindings) 

!!! tip
    All registered bindings could be seen with [guice report](../diagnostic/guice-report.md).

## Configuration

`Configuration` bound to guice as: 

* `io.dropwizard.Configuration`
* Your configuration class (`#!java MyConfiguration extends Configuration`)
* All classes between them

For example, if

```java
MyConfiguration extends MyAbstractConfiguration extends Configuration
```

Then `MyAbstractConfiguration` will be also bound and the following injection will work:

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

Interface binding will ignore interfaces in `java.*` or `groovy.*` packages (to avoid unnecessary bindings).

!!! tip
    Consider using direct sub configuration [object binding](#unique-objects) instead of marker interface
    if object uniqueness is guaranteed in user configuration.

## Yaml config introspection

Guicey performs `Configuration` [object introspection](#yaml-config-introspection) to provide access for 
configured yaml values by path or through sub-objects.

!!! warning
    Additional configuration objects are not bound when yaml introspection [is disabled](../yaml-values.md#disable-configuration-introspection)

Raw introspection result could be injected directly (for reporting or analysis):

```java
@Inject ConfigurationTree tree;
```

!!! tip
    All available configuration bindings could be seen with `.printConfigurationBindings()` report
    (use `.printAllConfigurationBindings()` to see also core dropwizard configuration values).
    
    This report is executed before injector creation so use it in cases when injector fail to inject configration value
    to validate binsinds correctness.   

### Unique objects

Unique sub-object (sub configuration classes appeared once) could be injected directly.

For example, the following config contains two (custom) unique sub objects:
```java
public class MyConfig extends Configuration {
    @JsonProperty AuthConfig auth;
    @JsonProperty DbConfig db;
}
```

Which could be injected directly:

```java
@Inject @Config AuthConfig auth;
@Inject @Config DbConfig db;
```  

!!! tip
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

### Value by path

All visible configuration paths values can be directly bound:

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
    The only exception is conditional mapping like dropwizard `server` when available paths
    could change, depending on configuration (what configuration type will be used)

!!! note 
    Generified types are bound only with generics (with all available type information).
    If you have `SubConf<T> sub` in config, then it will be bound with correct generic `SubConfig<String>`
    (suppose generic T is declared as String).

Value type, **declared in configuration class** is used for binding, but there are two exceptions.

First, if declared type is declared as collection (`Set`, `List`, `Map`) implementation then binding will use
base collection interface:

```java
ArrayList<String> value

@Inject @Config("value") List<String> vlaue;
```  

Second, if, for some (unforgivable) reason, property is declared as `Object` in configuration,
then binding type will depend on value presence:

* `@Config("path") Object val` - when value is null
* `@Config("path") ValueType val` - actual value type, when value is not null       

It is assumed that in such case value would be always present (some sort of property-selected binding, like dropwizard `server`).

!!! tip
    If provided value bindings are not enough for your case, you can declare your own value bindings:
    
    Get introspcetion result object (`ConfigurationTree`) in guice module and bind values
    the way you need it:
    ```java
    public class MyConfigBindingsModule extends DropwizardAwareModule {
        @Override
        public void configure() {
            bind(String.class).annotatedWith(Names.named("db.url"), configuration("db.url));
        }
    }
    ``` 

## Environment binding

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

See also [authentication configuration example](../../examples/authentication.md).

## Jersey specific bindings

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
* `org.glassfish.jersey.server.AsyncContext`

!!! tip
    Read about jersey bindings implementation in [lifecycle section](../lifecycle.md#access-guice-beans-from-jersey).

## Request and response

By default, `GuiceFilter` is enabled on both contexts (admin and main). So you can inject
request and response objects and use under filter, servlet or resources calls (guice filter wraps all web interactions).

If you disable guice filter with [.noGuiceFilter()](servletmodule.md) then
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
    [must be replaced with providers](../../installers/resource.md#@context-usage).

## Options

`ru.vyarus.dropwizard.guice.module.context.option.Options` binding provides access to [guicey options](../options.md):
```java
@Inject Options options;
``` 

Example usage:

```java
Preconditions.checkState(options.get(GuiceyOptions.UseHkBridge), 
                                 "HK2 guice bridge is required!")
```

## Guicey configuration

`ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo` binding provides access
to [guicey configuration details](../diagnostic/configuration-report.md):

```java
@Inject GuiceyConfigurationInfo info
```        

## Extension bindings

!!! summary
    Normally, you don't need to know about extension bindings because they are mostly useless 
    for application, but this section is important in case of problems. 

In order to support guice `binder().requireExplicitBindings()` option guicey binds
all extensions with untargeted binding: `binder().bind(YourExtension.class)`.

But there are three exceptions:

* Installers with custom binding logic (like a [plugins installer](../../installers/plugin.md))
* If extension was detected from binding (obviously binding already exists)
* If extension is annotated with`@LazyBinding`

As injector is created in `Stage.PRODUCTION`, all singleton extensions will be instantiated
in time of injector startup (injector stage could be changed in [main bundle](../configuration.md#main-bundle)).

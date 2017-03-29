# Bindings

All extensions, installed by guicey installers are explicitly bound to guice (except cases when 
it's not possible, like [eager extension](../installers/eager.md)).

Also, guicey binds some dropwizard and jersey objects. 

## Configuration

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

When `.bindConfigurationInterfaces()` enabled, all direct interfaces implemented by configuration class (or any subclass) are bound.
This may be used to support common `Has<Something>` configuration interfaces convention used to recognize your extension configuration in configuration object.

For example:

```java
    GuiceBundle.builder()
        .bundConfigurationInterfactes()
        ...

    public interface HasFeatureX {
        FeatureXConfig getFetureXConfig();
    }
        
    public class MyConfiguration extends Configuration implements HasFeatureXConfig {...}
    
    public class MyBean {
        @Inject HasFeatureX configuration;
        ...
    }
```

Interface binding will ignore interfaces in `java.*` or `groovy.*` packages (to avoid unnecessary bindings).

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

See also [authentication configuration example](../examples/authentication.md).

## Jersey specific bindings

Jersey bindings are not immediately available, because HK context starts after guice, 
so use `Provider` to inject these bindings.

These bindings available after HK context start:

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

## Request and response

By default, `GuiceFilter` is enabled on both contexts (admin and main). So you can inject
request and response objects and use under filter, servlet or resources calls (guice filter wraps all web interactions).

If you disable guice filter with [.noGuiceFilter()](configuration.md#servletmodule) then
guicey will bridge objects from HK context:

* `javax.servlet.http.HttpServletRequest`
* `javax.servlet.http.HttpServletResponse`
 
!!! attention ""
    This means you can still inject them, but request and response will
    only be available under resource calls (the only part managed by jersey).

Example usage:

```java
@Inject Provider<HttpServletRequest> requestProvider;
```

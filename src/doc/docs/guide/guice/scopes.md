# Scopes

!!! summary "Reminder"
    By default, all guice beans are created in [prototype scope](https://github.com/google/guice/wiki/Scopes).
    Guicey only force singleton scope for jersey extensions ([resources](../../installers/resource.md) and [extensions](../../installers/jersey-ext.md)))
    
Available scopes:

* `@Singleton` - single instance per context
* `@RequestScoped` - object per request (if guice filter support is [not disabled](../web.md#disable-servletmodule-support))
* `@Prototype` - prototype scope annotation (useful to override forced singleton scope for jersey services)

!!! note ""
    Session scope (`@SessionScoped`) is usually useless, because sessions are not enabled by default in dropwizard (but it is possible to enable them).

!!! tip
    Scopes of registered beans could be checked in [guice report](../diagnostic/guice-report.md)

## Prototype

Normally, prototype scope (new instance on each new injection) is the default - no need to explicitly specify it.

The only possible usage is overriding default forced singleton scope for jersey extensions.
For example, resource declared like this:

```java                  
@Path("/my")
public class MyResource {}
```        

Will be singleton. Prototype scope must be explicitly declared (if required):

```java                  
@Path("/my")
@Prototype
public class MyResource {}
```              

!!! note
    `@Prototype` scope annotation support is registered by guicey

## Singleton

Both `com.google.inject.Singleton` and `javax.inject.Singleton` annotations could be used.

!!! tip
    Prefer declaring `@Singleton` scope on all beans, except cases when different scope is required.     

## Request

By default, `GuiceFilter` is registered for both application and admin contexts. 
And so request (and session) scopes will be be available in both contexts.

```java
@RequestScoped
public class MyRequestScopedBean { ... }
```

In order to access request scoped beans in other beans you'll need to use provider:

```java
Provider<MyRequestScopedBean> myBeanProvider;
```

Some [jersey objects](bindings.md#jersey-specific-bindings) are already bound in request scope  

Context request and response objects are also available through request scope:

```java
Provider<HttpServletRequest> requestProvider
Provider<HttpServletResponse> responseProvider
```         

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
            // jersey object must be resolved inside hk request scope (to store it in guice request scope)
            // so guice could see its instance later in another thread
            uri.get();

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

!!! warning
    Pay attention that for jersey scope objects provider must be called first **in the current thread**!
    Guice would be able to propagate to another thread only objects already present in guice request scope.
    Without it, `provider.get()`, called under different thread would be delegated to jersey,
    which is not aware of this thread and so fail to provide its request scope object.
    
    Such additional call is not required for pure guice-managed request scope objects.  

### Request scope simulation

Sometimes, request scoped beans may need to be used somewhere without request (for example,
inside scheduled job). Of course, this is not correct situation, and the best way is to re-design
services, but not always possible.

As a workaround, request scope could be simulated:

```java
@Inject
Provider<RScopedService> service;
...
final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
try (final RequestScoper.CloseableScope ignored = scope.open()) {
  // work with request-scoped bean
  service.get().doSomething();
}
```  

## Eager singleton

By default, guicey create injector in `PRODUCTION` stage, so all registered singletons
will be instantiated immediately.

But if you rely on guice JIT (instantiation by injection) it may defer bean creation 
(until it will be requested first time).

To always start beans (even in `DEVELOPMENT` stage) guice provide eager singleton option:
`bind(MyService.class).asEagerSingleton()`.

For cases when you don't want to manually declare bean, but require it to start with guice context
you can either implement [Managed](../../installers/managed.md) or mark bean 
as [@EagerSingleton](../../installers/eager.md) (the latter will simply bind annotated bean as
guice eager singleton instead of you).
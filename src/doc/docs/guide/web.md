# Web features

##  Guice ServletModule support

By default, `GuiceFilter` is registered for both application and admin contexts. And so request and session scopes will be 
be available in both contexts. Also it makes injection of request and response objects available with provider (in any bean).

To register servlets and filters for main context use `ServletModule`, e.g.

```java
public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter("/*").through(MyFilter.class)
        serve("/myservlet").with(MyServlet.class)
    }
}
```

### Request scoped beans

You can use request scoped beans in both main and admin contexts. 

```java
@RequestScoped
public class MyRequestScopedBean { ... }
```

To obtain bean reference use provider:

```java
Provider<MyRequestScopedBean> myBeanProvider;
```

You can inject request and response objects in any bean:

```java
Provider<HttpServletRequest> requestProvider
Provider<HttpServletResponse> responseProvider
```

### Limitations

By default, `GuiceFilter` is registered with `REQUEST` dispatcher type. If you need to use other types use option:

```java
    .option(GuiceyOptions.GuiceFilterRegistration, EnumSet.of(REQUEST, FORWARD))
```

!!! warning
    Note that async servlets and filters can't be used with guice servlet module (and so it is impossible to register `GuiceFilter` for `ASYNC` type). 
    Use [web installers](#web-installers) for such cases. 

!!! warning
    `GuiceFilter` dispatch all requests for filters and servlets registered by `ServletModule` internally and there may be problems combining servlets from `ServletModule`
    and filters in main scope.

### Disable ServletModule support

If you don't use servlet modules (for example, because web installers cover all needs) you can disable guice servlet modules support:
```java
GuiceBundle.builder()
    .noGuiceFilter()
```

It will:

* Avoid registration of `GuiceFilter` in both contexts
* Remove request and session guice scopes support (because no ServletModule registered)
* Prevent installation of any `ServletModule` (error will be thrown indicating duplicate binding)
* `HttpServletRequest` and `HttpServletResponse` still may be injected in resources with `Provider` 
(but it will not be possible to use such injections in servlets, filters or any other place)

Disabling saves about ~50ms of startup time. 

## Web installers

Servlet api 3.0 provides `@WebServlet`, `@WebFilter` and `@WebListener` annotations, but they are not recognized in dropwizard
(because dropwizard does not depend on jersey-annotations module). Web installers recognize this annotations and register guice-managed filters, servlets and listeners 
instances.

Web installers are disabled by default. To eable:

```java
GuiceBundle.builder()
    .useWebInstallers()
```

It will register [WebInstallersBundle](https://github.com/xvik/dropwizard-guicey/tree/master/src/main/java/ru/vyarus/dropwizard/guice/module/installer/WebInstallersBundle.java).

!!! note ""
    Web installers are not enabled by default, because dropwizard is primarily rest oriented framework and you may not use custom servlets and filters at all
    (so no need to spent time trying to recognize them). Moreover, using standard servlet api annotations may confuse users and so 
    it must be user decision to enable such support. Other developers should be guided bu option name and its javadoc (again to avoid confusion, no matter that
    it will work exactly as expected)

### Differences with GuiceServlet module

There is a difference between using web installers and registering servlets and filters with [guice servlet module](#guice-servletmodule-support).
Guice servlet module handles registered servlets and filters internally in `GuiceFilter` (which is installed by guicey in both app and admin contexts).
As a side effect, there are some compatibility issues between guice servlets and native filters (rare and usually not blocking, but still).
Web installers use guice only for filter or servlet instance creation and register this instance directly in dropwizard environment (using annotation metadata).  

In many cases, annotations are more convenient way to declare servlet or filter registration comparing to servlet module. 

!!! tip 
    Using annotations you can register async [servlets](../installers/servlet.md#async) and [filters](../installers/filter.md#async) (with annotations `asyncSupported=true` option).
    In contrast, it is impossible to register async with guice servlet module.

### Admin context

By default, web installers (servlet, filter, listener) target application context. If you want to install into admin context then use `@AdminContext` annotation.

For example: 

```java
@AdminContext
@WebServlet("/mapped")
public class MyServlet extneds HttpServlet { ... }
```

Will install servlet in admin context only.

If you want to install in both contexts use andMain attribute:

```java
@AdminContext(andMain = true)
@WebServlet("/mapped")
public class MyServlet extneds HttpServlet { ... }
```

In example above, servlet registered in both contexts.

 
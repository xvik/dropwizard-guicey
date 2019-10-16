# ServletModule

By default, [GuiceFilter](https://github.com/google/guice/wiki/Servlets) is registered for both application and admin contexts:

* [ServletModule](https://github.com/google/guice/wiki/ServletModule) can be used for filters and servlets declaration 
* [Request (and session) scope](scopes.md#request) is available in both contexts

Example of servlet and filter registration through guice module:

```java
public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter("/*").through(MyFilter.class);
        serve("/myservlet").with(MyServlet.class);
    }
}    

GuiceBundle.builder()
    .modules(new WebModule())
    .build()
```       


!!! warning
    Rest context is mapped to root by default. To change it use configuration file:
    
    ```yaml
    server:
        rootPath: '/rest/*'
    ```
    
!!! tip
    It may be more handy to use [web extensions](../web.md) instead of direct registrations.
    For example, it is the only way to bind servlets in admin context.    

## Limitations

By default, `GuiceFilter` is registered with `REQUEST` dispatcher type. If you need to use other types use option:

```java
    .option(GuiceyOptions.GuiceFilterRegistration, EnumSet.of(REQUEST, FORWARD))
```

!!! warning
    Note that async servlets and filters can't be used with guice servlet module (and so it is impossible to register `GuiceFilter` for `ASYNC` type). 
    Use [web installers](../web.md) for such cases. 

!!! warning
    `GuiceFilter` dispatch all requests for filters and servlets registered by `ServletModule` internally and there may be problems combining servlets from `ServletModule`
    and filters in main scope.

## Disable ServletModule support

!!! danger 
    Option is deprecated because request scope will become mandatory for the next guicey version
    (due to HK2 remove).    

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

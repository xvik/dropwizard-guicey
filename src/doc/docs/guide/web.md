# Web features

# Servlets, filters

Servlets and filters could be registered either with guice [ServletModule](guice/servletmodule.md)
or using [extensions](extensions.md#web).

Installation through extensions has more abilities comparing to `ServletModule`:

* Installation into [admin context](../installers/servlet.md#admin-context)
* [Async support](../installers/servlet.md#admin-context)
* Filter may be applied to exact servlet(s) (`#!java @WebFilter(servletNames = "servletName")`)
* Request, servlet context or session [listeners installation](../installers/listener.md)

`ServletModule` allows mappings [by regexp](https://github.com/google/guice/wiki/ServletRegexKeyMapping):

```java
serveRegex("(.)*ajax(.)*").with(MyAjaxServlet.class)
```

!!! warning
    It is important to note that `GuiceFilter` dispatch all requests for filters and servlets 
    registered by `ServletModule` internally and there may be problems combining servlets from `ServletModule` and 
    filters in main scope.
    
Alternatively, you can always register servlet or filter manually with dropwizard api:

```java
public class App extends Application {
    public void initialize(Bootstrap bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder().build());
    }
    
    public void run(Configuration configuration, Environment environment) {
        final MyFilter filter = InjectorLookup.getInjector(this).get()
                                        .getInstance(MyFilterBean.class);
        environment.servlets().addFilter("manualFilter", filter)
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }
}
```    

!!! tip
    If you don't want to use web installers or have problems with it (e.g. because they use `javax.servlet` annotations)
    you can disable all of them at once by disabling bundle:
    
    ```java
    GuiceBundle.builder()
        .disableBindles(WebInstallersBundle.class)
        ...
    ```

## Resources

Dropwizard provides [AssetsBundle](https://www.dropwizard.io/en/stable/manual/core.html#serving-assets) 
for serving static files from classpath.

But, if you develop SPA application with HTML5 routes, server will not handle these routes
properly. Use guicey [SPA bundle](../extras/spa.md) which adds proper SPA routing support above dropwizard `AssetBundle` 

## Templates 

Dropwizard provides [ViewBundle](https://www.dropwizard.io/en/stable/manual/views.html)
for handling templates (freemarker and mustache out of the box, more engines could be plugged).

But it is not quite handful to use it together with static resources (`AssetsBundle`). 
If you would like to have JSP-like behaviour (when templates and resources live at the same
location and so could easily reference each other) - then use guicey [GSP bundle](../extras/gsp.md) 
(which is actually just a "glue" for dropwizard `ViewBundle` and `AssetsBundle`).   
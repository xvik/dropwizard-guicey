# Web features

## Servlets, filters

Servlets and filters could be registered either with guice [ServletModule](guice/servletmodule.md)
or using [extensions](extensions.md#web).

### Guice servlet module

Example:

```java
public class WebModule extends ServletModule {

    @Override
    protected void configureServlets() {
        filter("/*").through(MyFilter.class);
        serve("/myservlet").with(MyServlet.class);
    }
}  
```

!!! success "Pros"
    Only `ServletModule` allows mappings [by regexp](https://github.com/google/guice/wiki/ServletRegexKeyMapping):
    
    ```java
    serveRegex("(.)*ajax(.)*").with(MyAjaxServlet.class)
    ```

!!! warning
    It is important to note that `GuiceFilter` dispatch all requests for filters and servlets 
    registered by `ServletModule` internally and so you may have problems combining servlets from 
    `ServletModule` with filters in main scope.
    
    It is never a blocking issues, but often "not obvious to understand" situations.

### Web extensions

Extensions declared with standard `javax.servlet` annotations.

Servlet registration: 

```java
@WebServlet("/mapped")
public class MyServlet extends HttpServlet { ... }
```       

Extension [recognized](../installers/servlet.md) by `@WebServlet` annotation.

Could be registered on admin context:

```java
@WebServlet("/mapped")
@AdminContext
public class MyServlet extends HttpServlet { ... }
```   

Or even on both contexts at the same time: `#!java @AdminContext(andAdmin=true)`. 

Filter:

```java
@WebFilter("/some/*")
public class MyFilter implements Filter { ... }
```

Extension [recognized](../installers/filter.md) by `@WebFilter` annotation. 

Web listeners (servlet, request, session):

```java
@WebListener
public class MyListener implements ServletContextListener {...}
```

Extension [recognized](../installers/listener.md) by `@WebListener` annotation.


!!! success "Pros"
    Installation through extensions has more abilities comparing to `ServletModule`:
    
    * Installation into [admin context](../installers/servlet.md#admin-context)
    * [Async support](../installers/servlet.md#admin-context)
    * Filter may be applied to exact servlet(s) (`#!java @WebFilter(servletNames = "servletName")`)
    * Request, servlet context or session [listeners installation](../installers/listener.md)

If you don't want to use web installers or have problems with it (e.g. because they use `javax.servlet` annotations)
you can disable all of them at once by disabling bundle:

```java
GuiceBundle.builder()
    .disableBindles(WebInstallersBundle.class)
    ...
```

### Manual registration
    
Alternatively, you can always register servlet or filter manually with dropwizard api:

```java
public class App extends Application {
    public void initialize(Bootstrap bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder().build());
    }
    
    public void run(Configuration configuration, Environment environment) {
        final MyFilter filter = InjectorLookup.getInstance(this, MyFilterBean.class).get();
        environment.servlets().addFilter("manualFilter", filter)
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }
}
```    

## Resources

Dropwizard provides [AssetsBundle](https://www.dropwizard.io/en/stable/manual/core.html#serving-assets) 
for serving static files from classpath:

```java                       
bootstrap.addBundle(new AssetsBundle("/assets/app/", "/", "index.html"));
```                           

`http://localhost:8080/foo.css` --> `src/main/resources/assets/app/foo.css`   
`http://localhost:8080/` --> `src/main/resources/assets/app/index.html`

### HTML5 routing

But, if you develop SPA application with HTML5 routes, server will not handle these routes
properly. Use guicey [SPA bundle](../extras/spa.md) which adds proper SPA routing support above dropwizard `AssetBundle` 

```java
GuiceBundle.builder()
    .bundles(SpaBundle.app("spaApp", "/assets/app/", "/").build());
```          

`http://localhost:8080/` --> `src/main/resources/assets/app/index.html`   
`http://localhost:8080/route/path` --> `src/main/resources/assets/app/index.html`

## Templates 

Dropwizard provides [ViewBundle](https://www.dropwizard.io/en/stable/manual/views.html)
for handling templates (freemarker and mustache out of the box, more engines could be plugged).

```java
bootstrap.addBundle(new ViewBundle());
```    

Which allows you to serve rendered templates [from rest endpoints](https://www.dropwizard.io/en/stable/manual/views.html).


### Templates + resources

But it is not quite handful to use it together with static resources ([AssetsBundle](#resources))
because static resources will have different urls (as they are not served from rest).  

If you would like to have JSP-like behaviour (when templates and resources live at the same
location and so could easily reference each other) - then use guicey [GSP bundle](../extras/gsp.md) 
(which is actually just a "glue" for dropwizard `ViewBundle` and `AssetsBundle`).

```java
com/exmaple/app/
    person.ftl
    foo.ftl
    style.css
```     

```html  
<#-- Sample template without model (/foo.ftl) -->
<html>
    <body>        
        <h1>Hello, it's a template: ${12+22}!</h1>
    </body>
</html>
```      

```html  
<#-- Template with model, rendered by rest endpoint (/person/) -->
<#-- @ftlvariable name="" type="com.example.views.PersonView" -->
<html>   
    <head>  
        <link href="/style.css" rel="stylesheet">
    </head>
    <body>
        <!-- calls getPerson().getName() and sanitizes it -->
        <h1>Hello, ${person.name?html}!</h1>
    </body>
</html>
```

```java
public class PersonView extends TemplateView {
    private final Person person;

    public PersonView(Person person) {    
        super('person.ftl');
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
```      

```java       
// Path starts with application name  
@Path("/com.example.app/person/")  
@Produces(MediaType.TEXT_HTML)    
// Important marker
@Template
public class PersonPage {      
    
    @Inject
    private PersonDAO dao;

    @GET  
    @Path("/")
    public PersonView getMaster() {
        return new PersonView(dao.find(1));
    }    

    @GET  
    @Path("/{id}")
    public PersonView getPerson(@PathParam("id") String id) {
        return new PersonView(dao.find(id));
    }   
}
```       

```java
GuiceBundle.builder()                                     
    .bundles(
             // global views support
             ServerPagesBundle.builder().build(),
             // application registration
             ServerPagesBundle.app("com.example.app", "/com/example/app/", "/")   
                                 // rest path as index page
                                 .indexPage("person/")
                                 .build());
```                 

Static resource call:

`http://localhost:8080/style.css` --> `src/main/resources/com/example/app/style.css`

Direct template call:

`http://localhost:8080/foo.ftl` --> `src/main/resources/com/example/app/foo.ftl`

Rest-driven template call:

`http://localhost:8080/person/12` --> `/rest/com.example.app/person/12`

Index page:

`http://localhost:8080/` --> `/rest/com.example.app/person/`

!!! note "Summary"
    Declaration differences from pure dropwizard views:
    
    * Model extends `TemplateView`
    * Rest endpoints always annotated with `@Template`
    * Rest endpoints paths starts with registered application name (`#!java ServerPagesBundle.app("com.example.app"`)
    to be able to differentiate rest for different UI applications     
    
!!! warning
    Standard errors handling in views ([templates](https://www.dropwizard.io/en/stable/manual/views.html#man-views-template-errors),
    [custome pages](https://www.dropwizard.io/en/stable/manual/views.html#custom-error-pages)) is replaced by 
    [custom mechanism](../extras/gsp.md#error-pages), required to implement per-ui-app errors support.
    
 
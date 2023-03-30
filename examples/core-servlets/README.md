### Servlets and filters registration example

There are multiple way to register [servlets and filters](http://xvik.github.io/dropwizard-guicey/5.0.0/guide/web/).

#### Extensions

The simplest is to use web installers, relying on web annotations:

```java
@WebFilter("/sample/*")
@Singleton
public class SampleFilter extends HttpFilter {

    @Inject
    SampleService service;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        ...
    }
}
```  


```java
@WebServlet("/sample")
@Singleton
public class SampleServlet extends HttpServlet {

    @Inject
    private SampleService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ...
    }
}
```

```java
@WebListener
@Singleton
public class SampleRequestListener implements ServletRequestListener {

    @Inject
    private SampleService service;

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ...
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ...
    }
}
```  

For admin context registration only additional annotation must be added:

```java
@Singleton
@WebServlet("/admin")
@AdminContext
public class AdminServlet extends HttpServlet {
    ...
}
```

Extensions registration:

```java
bootstrap.addBundle(GuiceBundle.builder()
        // classpath scan or direct bindings (not in servlet module) could be used for registration instead
        .extensions(
                SampleServlet.class,
                SampleFilter.class,
                AdminServlet.class,
                SampleRequestListener.class)
```       

```
http://localhost:8080/sample --- filter + servlet
http://localhost:8081/admin  --- admin context servlet
```  

#### Guice servlet module

Guice filter is registered in both main and admin contexts and so all servlets and filters
registered in servlet module will apply to both contexts:

```java
.modules(new ServletModule() {
            @Override
            protected void configureServlets() {
                // note that if filter will be mapped as /gsample/* it will not apply to servlet call
                serve("/gsample").with(GuiceServlet.class);
                filter("/gsample").through(GuiceFilter.class);
            }
        })
```    

```
http://localhost:8080/gsample --- filter + servlet
http://localhost:8081/gsample  --- admin context servlet
```    

Note that servlets and filters registered through `ServletModule` are not recognized 
as guicey extension and so it would not be possible to disable exact servlet or filter 
(with `.disableExtensions(...)`). 

#### Reporting

All mapped servlets and filters could be easily seen on web report:

```java
GuiceBundle.builder()
    ...
    .printWebMappings()
```            

```
    MAIN /
    ├── filter     /sample/*                    SampleFilter                 (r.v.d.g.examples.web)                                  [REQUEST]       .sample
    │   
    ├── filter     /*                   async   GuiceFilter                  (c.g.inject.servlet)                                    [REQUEST]       Guice Filter
    │   ├── guicefilter     /gsample                     GuiceFilter                    r.v.d.g.e.ServletsDemoApplication$1
    │   └── guiceservlet    /gsample                     GuiceServlet                   r.v.d.g.e.ServletsDemoApplication$1
    │   
    ├── servlet    /sample                      SampleServlet                (r.v.d.g.examples.web)                                                  .sample


    ADMIN /
    │   
    ├── filter     /*                   async   AdminGuiceFilter             (r.v.d.g.m.i.internal)                                  [REQUEST]       Guice Filter
    │   ├── guicefilter     /gsample                     GuiceFilter                    r.v.d.g.e.ServletsDemoApplication$1
    │   └── guiceservlet    /gsample                     GuiceServlet                   r.v.d.g.e.ServletsDemoApplication$1
    │   
    ├── servlet    /admin                       AdminServlet                 (r.v.d.g.examples.web)                                                  .admin

```
### Hibernate integration sample

[dropwiard-hibernate](http://www.dropwizard.io/1.0.0/docs/manual/hibernate.html) is configured exactly as
 it's described in docs, but extracted to [separate class](src/main/java/ru/vyarus/dropwizard/guice/examples/hbn/HbnBundle.java) for simplicity:
 
 ```java
 public class HbnBundle extends HibernateBundle<HbnAppConfiguration> {
 
     public HbnBundle() {
         super(Sample.class);
     }
 
     @Override
     public PooledDataSourceFactory getDataSourceFactory(HbnAppConfiguration configuration) {
         return configuration.getDataSourceFactory();
     }
 }
 ```
 
 [Guice module](src/main/java/ru/vyarus/dropwizard/guice/examples/hbn/HbnModule.java) 
 used to provide SessionFactory instance into guice context:
 
 ```java
 public class HbnModule extends AbstractModule {
 
     private final HbnBundle hbnBundle;
 
     public HbnModule(HbnBundle hbnBundle) {
         this.hbnBundle = hbnBundle;
     }
 
     @Override
     protected void configure() {
         bind(SessionFactory.class).toInstance(hbnBundle.getSessionFactory());
     }
 }
 ```
 
 And in [application](src/main/java/ru/vyarus/dropwizard/guice/examples/HbnApplication.java) init:
 
 ```java
 @Override
     public void initialize(Bootstrap<HbnAppConfiguration> bootstrap) {
         final HbnBundle hibernate = new HbnBundle();
         // register hbn bundle before guice to make sure factory initialized before guice context start
         bootstrap.addBundle(hibernate);
         bootstrap.addBundle(GuiceBundle.builder()
                 .enableAutoConfig("ru.vyarus.dropwizard.guice.examples")
                 .modules(new HbnModule(hibernate))
                 .build());
     }
 ```

Overall it's a complete example with [one entity](src/main/java/ru/vyarus/dropwizard/guice/examples/model/Sample.java)
and [simple resource](src/main/java/ru/vyarus/dropwizard/guice/examples/rest/SampleResource.java).

[Test](src/test/groovy/ru/vyarus/dropwizard/guice/examples/HbnResourceTest.groovy) starts application
with in-memory h2 db ([see config](src/test/resources/config.yml)).

#### Session in servlet or filter

If you need to access hibernate in servlet or filter you will need to manage session manually.
For example:

```java                
@WebFilter("/*")
@Singleton
public class MyFilter implments Filter {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public void doFilter(ServletRequest servletRequest, 
                         ServletResponse servletResponse,
                         FilterChain filterChain) 
                throws IOException, ServletException {

        Session session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
        try {
           // session opened, hibernate could be used
        } finally {
          ManagedSessionContext.unbind(sessionFactory); 
          session.close();
        }
    } 
}
``` 
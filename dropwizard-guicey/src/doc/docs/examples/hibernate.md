# Hibernate integration

Example of [dropwizard-hibernate](https://www.dropwizard.io/en/release-4.0.x/manual/hibernate.html) bundle usage with guicey.

!!! abstract ""
    Example [source code](https://github.com/xvik/dropwizard-guicey/examples/tree/master/integration-hibernate)

## Configuration

Additional dependencies required:

```groovy
    implementation 'io.dropwizard:dropwizard-hibernate:2.0.2'
    implementation 'com.h2database:h2:1.4.199'
```  

!!! note
    Both versions are managed by [BOM](../extras/bom.md)

For simplicity, an embedded H2 database is used.

Overall configuration is exactly the same as described in [dropwizard docs](https://www.dropwizard.io/en/release-4.0.x/manual/hibernate.html), 
but extracted to separate class for simplicity:
 
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

!!! note
    All model classes are configured inside the constructor: `#!java super(Sample.class);`

Configuration class:
 
```java
public class HbnAppConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
}
``` 

Configuration file for in-memory database and automatic schema creation:

```yaml
database:
  driverClass: org.h2.Driver
  user: sa
  password:
  url: jdbc:h2:mem:sample

  properties:
    charSet: UTF-8
    hibernate.dialect: org.hibernate.dialect.H2Dialect
    hibernate.hbm2ddl.auto: create
```
 
## Guice integration 
 
Guice module used to provide SessionFactory instance into guice context:

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

Application:

```java
@Override
public void initialize(Bootstrap<HbnAppConfiguration> bootstrap) {
    final HbnBundle hibernate = new HbnBundle();
    // register hbn bundle before guice to make sure factory initialized before guice context start
    bootstrap.addBundle(hibernate);
    bootstrap.addBundle(GuiceBundle.builder()
            .enableAutoConfig("com.myapp.package")
            .modules(new HbnModule(hibernate))
            .build());
}
```

## Usage

It is simpler to use dropwizard `AbstractDAO` for hibernate logic:

```java
public class SampleService extends AbstractDAO<Sample> {

    @Inject
    public SampleService(SessionFactory factory) {
        super(factory);
    }

    public void create(Sample sample) {
        return persist(sample);
    }

    public List<Sample> findAll() {
        return list(currentSession().createQuery("from Sample"));
    }
}
```

!!! attention ""
    You will need to use dropwizard `@UnitOfWork` annotation to declare transaction scope.
    
For example:
    
```java
@Path("/sample")
@Produces("application/json")
public class SampleResource {

    @Inject
    private SampleService service;

    @GET
    @Path("/")
    @Timed
    @UnitOfWork
    public Response doStaff() {
        final Sample sample = new Sample("sample");
        service.create(sample);
        final List<Sample> res = service.findAll();
        // using response to render entities inside unit of work and avoid lazy load exceptions
        return Response.ok(res).build();
    }
}
```    

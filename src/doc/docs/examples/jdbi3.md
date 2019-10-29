# JDBI3 integration

Example of [guicey-jdbi3](../extras/jdbi3.md) extension usage.

!!! abstract ""
    Example [source code](https://github.com/xvik/dropwizard-guicey-examples/tree/master/jdbi3)


The [JDBI3 extension](../extras/jdbi3.md) allows:

* using jdbi proxies as guice beans
* using injection inside proxies
* using AOP on proxies
* using annotations for transaction definition
* automatic repository and mapper installation

## Configuration

Additional dependencies required:

```groovy
compile 'ru.vyarus.guicey:guicey-jdbi3:5.0.0-0-rc.2'
compile 'com.h2database:h2:1.4.199'
```

!!! note
    Both versions are managed by [BOM](../extras/bom.md)

[dropwizard-jdbi3](http://www.dropwizard.io/1.3.5/docs/manual/jdbi3.html) is used to configure 
and create dbi instance:

```java
public class Jdbi3AppConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDatabase() {
        return database;
    }
}
```

For simplicity, an embedded H2 database is used:

```yaml
database:
  driverClass: org.h2.Driver
  user: sa
  password:
  url: jdbc:h2:~/sample
  properties:
    charSet: UTF-8
  maxWaitForConnection: 1s
  validationQuery: "SELECT 1"
  validationQueryTimeout: 3s
  minSize: 8
  maxSize: 32
  checkConnectionWhileIdle: false
  evictionInterval: 10s
  minIdleTime: 1 minute
```

!!! warning
    Database scheme must be created manually. You can use 
    [dropwizard-flyway](https://github.com/dropwizard/dropwizard-flyway) module to prepare database. 
    See [example app source](https://github.com/xvik/dropwizard-guicey-examples/tree/master/jdbi3) for details. 
 

JDBI instance created exactly as described in [dropwizard docs](http://www.dropwizard.io/1.3.5/docs/manual/jdbi3.html) 
using provided db configuration:

```java
GuiceBundle.builder()
    .bundles(JdbiBundle.<JdbiAppConfiguration>forDatabase((conf, env) -> conf.getDatabase()))
    .withPlugins(new H2DatabasePlugin()))
```

!!! note 
    You can use [pre-build jdbi instance](../extras/jdbi3.md#usage) instead.

## Repository definition

!!! warning
    All jdbi repositories must be annotated with `@JdbiRepository` to let the [repository installer](../extras/jdbi3.md#repository)
    recognize and properly install them.

```java
@JdbiRepository
@InTransaction
public interface UserRepository extends Crud<User> {
    
    @Inject
    RandomNameGenerator getGenerator();

    // sample of hybrid method in repository, using injected service
    default User createRandomUser() {
        final User user = new User();
        user.setName(getGenerator().generateName());
        save(user);
        return user;
    }

    @Override
    @SqlUpdate("insert into users (name, version) values (:name, :version)")
    @GetGeneratedKeys
    long insert(@UserBind User entry);

    @SqlUpdate("update users set version=:version, name=:name where id=:id and version=:version - 1")
    @Override
    int update(@UserBind User entry);

    @SqlQuery("select * from users")
    List<User> findAll();

    @SqlQuery("select * from users where name = :name")
    User findByName(@Bind("name") String name);
}
```

Where `Crud` base interface tries to unify repositories and provide hibernate-like optimistic locking behaviour 
(on each entity save version field is assigned/incremented and checked during update to prevent data loss):

```java
public interface Crud<T extends IdEntity> {

    @InTransaction
    default T save(final T entry) {
        // hibernate-like optimistic locking mechanism: provided entity must have the same version as in database
        if (entry.getId() == 0) {
            entry.setVersion(1);
            entry.setId(insert(entry));
        } else {
            final int ver = entry.getVersion();
            entry.setVersion(ver + 1);
            if (update(entry) == 0) {
                throw new ConcurrentModificationException(String.format(
                        "Concurrent modification for object %s %s version %s",
                        entry.getClass().getName(), entry.getId(), ver));
            }
        }
        return entry;
    }

    long insert(T entry);

    int update(T entry);
}
```

!!! note ""
    You don't necessarily need to use `Crud` - it's an advanced usage example.
    
The repository is annotated with `@InTransaction` to allow direct usage; repository method calls are the smallest transaction scope. 
The transaction scope can be enlarged by using annotations on calling guice beans or 
[declaring transactions manually](../extras/jdbi3.md#manual-transaction-definition).
In order to better understand how transactions work, read the [unit of work docs section](../extras/jdbi3.md#unit-of-work).

!!! note
    `@InTransaction` is handled with guice AOP, so you can use any other guice aop related features.

!!! attention 
    Constructor injection is impossible in repositories, but you can use getter injections:
    ```java
     @Inject
     RandomNameGenerator getGenerator();
    ```

## Row mapper

Row mapper is used to map query result set to entity: 

```java
public class UserMapper implements RowMapper<User> {

    @Override
    public User map(ResultSet r, StatementContext ctx) throws SQLException {
        User user = new User();
        user.setId(r.getLong("id"));
        user.setVersion(r.getInt("version"));
        user.setName(r.getString("name"));
        return user;
    }
}
```

Mappers are installed with the [mapper installer](../extras/jdbi3.md#row-mapper).
If auto scan is enabled then all mappers will be detected automatically and registered in the jdbi instance.
Mappers are instantiated as normal guice beans without restrictions which means you can use injection and aop 
(it's only not shown in example mapper).

!!! note
    The mapper installer mostly automates (and unifies) registration. If your mapper does not need to be guice bean
    and you don't want to use auto configuration then you can register it manually in jdbi instance, making it available for injection.

Also, see complementing binding annotation, used to bind object to query parameters:

```java
@SqlStatementCustomizingAnnotation(UserBind.UserBinder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface UserBind {

    class UserBinder implements SqlStatementCustomizerFactory {

        @Override
        public SqlStatementParameterCustomizer createForParameter(
                Annotation annotation,
                Class<?> sqlObjectType,
                Method method,
                Parameter param,
                int index,
                Type paramType) {
            
            return (stmt, obj) -> {
                User arg = (User) obj;
                ((SqlStatement) stmt)
                        .bind("id", arg.getId())
                        .bind("version", arg.getVersion())
                        .bind("name", arg.getName());
            };
        }
    }
}
```

See `@UserBind` usage above in repository definition.

There is no custom installer for annotation because it's detected automatically by JDBI.  

## Usage

Repositories are used as normal guice beans:

```java
@Path("/users")
@Produces("application/json")
public class UserResource {

    @Inject
    private final UserRepository repository;

    @POST
    @Path("/")
    public User create(String name) {
        User user = new User();
        user.setName(name);
        return repository.save(user);
    }
    
    @GET
    @Path("/")
    public List<User> findAll() {
        return repository.findAll();
    }
}
```

`UserMapper` and `UserBind` are used implicitly to convert the POJO into a db record and back.

You can use `@InTransaction` on repository method to enlarge transaction scope, but, in contrast
to hibernate you dont't have to always declare it to avoid lazy initialization exception 
(because jdbi produces simple pojos).

!!! note
    `@InTrasaction` is named to avoid confusion with the commonly used `@Transactional` annotation.
    You [can bind any annotation class](../extras/jdbi3.md#intransaction) if you like to use a different name (the annotation is just a marker)

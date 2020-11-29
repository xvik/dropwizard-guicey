# JDBI integration

Example of [guicey-jdbi](../extras/jdbi.md) extension usage.

!!! abstract ""
    Example [source code](https://github.com/xvik/dropwizard-guicey-examples/tree/master/ext-jdbi)


The [JDBI extension](../extras/jdbi.md) allows:

* using jdbi proxies as guice beans
* using injection inside proxies
* using AOP on proxies
* using annotations for transaction definition
* automatic repository and mapper installation

## Configuration

Additional dependencies required:

```groovy
implementation 'ru.vyarus.guicey:guicey-jdbi:5.2.0-1'
implementation 'com.h2database:h2:1.4.199'
```

!!! note
    guicey-jdbi version could be managed with [BOM](../extras/bom.md)

[dropwizard-jdbi](https://www.dropwizard.io/en/release-1.3.x/manual/jdbi.html) is used to configure 
and create dbi instance:

```java
public class JdbiAppConfiguration extends Configuration {

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
    See [example app source](https://github.com/xvik/dropwizard-guicey-examples/tree/master/ext-jdbi) for details. 
 

DBI instance created exactly as described in [dropwizard docs](https://www.dropwizard.io/en/release-1.3.x/manual/jdbi.html) 
using provided db configuration:

```java
GuiceBundle.builder()
    .bundles(JdbiBundle.<JdbiAppConfiguration>forDatabase((conf, env) -> conf.getDatabase()))
```

!!! note 
    You can use [pre-build dbi instance](../extras/jdbi.md#usage) instead.

## Repository definition

!!! warning
    All jdbi repositories must be annotated with `@JdbiRepository` to let the [repository installer](../extras/jdbi.md#repository)
    recognize and properly install them.

```java
@JdbiRepository
@InTransaction
public abstract class UserRepository extends Crud<User> {

    // have to use field injection because class is still used by dbi (which is no aware of guice) for proxy creation
    @Inject
    private RandomNameGenerator generator;

    // sample of hybrid method in repository, using injected service
    public User createRandomUser() {
        final User user = new User();
        user.setName(generator.generateName());
        save(user);
        return user;
    }

    @Override
    @SqlUpdate("insert into users (name, version) values (:name, :version)")
    @GetGeneratedKeys
    public abstract long insert(@UserBind User entry);

    @SqlUpdate("update users set version=:version, name=:name where id=:id and version=:version - 1")
    @Override
    public abstract int update(@UserBind User entry);

    @SqlQuery("select * from users")
    public abstract List<User> findAll();

    @SqlQuery("select * from users where name = :name")
    public abstract User findByName(@Bind("name") String name);
}
```

Where `Crud` base class tries to unify repositories and provide hibernate-like optimistic locking behaviour 
(on each entity save version field is assigned/incremented and checked during update to prevent data loss):

```java
public abstract class Crud<T extends IdEntity> {

    @InTransaction
    public T save(final T entry) {
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

    public abstract long insert(T entry);

    public abstract int update(T entry);
}
```

!!! note ""
    You don't necessarily need to use `Crud` - it's an advanced usage example.
    
The repository is annotated with `@InTransaction` to allow direct usage; repository method calls are the smallest transaction scope. 
The transaction scope can be enlarged by using annotations on calling guice beans or 
[declaring transactions manually](../extras/jdbi.md#manual-transaction-definition).
In order to better understand how transactions work, read the [unit of work docs section](../extras/jdbi.md#unit-of-work).

!!! note
    `@InTransaction` is handled with guice AOP, so you can use any other guice aop related features.

!!! attention 
    Constructor injection is impossible in repositories, but you can use field injections:
    ```java
     @Inject
     private RandomNameGenerator generator;
    ```

## Result set mapper

Result set mapper is used to map query result set to entity: 

```java
public class UserMapper implements ResultSetMapper<User> {

    @Override
    public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        User user = new User();
        user.setId(r.getLong("id"));
        user.setVersion(r.getInt("version"));
        user.setName(r.getString("name"));
        return user;
    }
}
```

Mappers are installed with the [mapper installer](../extras/jdbi.md#result-set-mapper).
If auto scan is enabled then all mappers will be detected automatically and registered in the dbi instance.
Mappers are instantiated as normal guice beans without restrictions which means you can use injection and aop 
(it's only not shown in example mapper).

!!! note
    The mapper installer mostly automates (and unifies) registration. If your mapper does not need to be guice bean
    and you dont want to use auto configuration then you can register it manually in dbi instance, making it available for injection.

Also, see complementing binding annotation, used to bind object to query parameters:

```java
@BindingAnnotation(UserBind.UserBinder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface UserBind {

    class UserBinder implements BinderFactory<UserBind> {
        @Override
        public Binder build(UserBind annotation) {
            return (Binder<UserBind, User>) (q, bind, arg) -> {
                q.bind("id", arg.getId())
                        .bind("version", arg.getVersion())
                        .bind("name", arg.getName());
            };
        }
    }
}
```

See `@UserBind` usage above in repository definition.

There is no custom installer for annotation because it's detected automatically by DBI.  

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
    You [can bind any annotation class](../extras/jdbi.md#intransaction) if you like to use a different name (the annotation is just a marker)

### JDBI integration sample

Use [JDBI guicey extension](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi) to:
* use jdbi proxies as guice beans
* be able to use injection inside proxies
* be able to use AOP on proxies
* use annotations for transaction definition
* automatic repositories and mapper installation

[Dropwizard jdbi integration](http://www.dropwizard.io/1.0.5/docs/manual/jdbi.html) is used to configure 
and create dbi instance. See [configuration](src/main/java/ru/vyarus/dropwizard/guice/examples/JdbiAppConfiguration.java).

For simplicity, embedded H2 database used.
Database scheme must be created before launching application. 
[Dropwizard-flyway](https://github.com/dropwizard/dropwizard-flyway) used to prepare database (it's actually used only for [manual run](#manual-run) - 
tests use flyway directly). See [db scheme](src/main/resources/db/migration/V1__setup.sql). 
 

DBI instance created exactly as described in [dropwizard docs](http://www.dropwizard.io/1.0.5/docs/manual/jdbi.html) 
using provided db configuration:

```java
.bundles(JdbiBundle.<JdbiAppConfiguration>forDatabase((conf, env) -> conf.getDatabase()))
```

(You can provide pre-build dbi instance instead).

`JdbiBundle` will activate additional installers.

#### Repository

[Repositories installer](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi#repository): all jdbi proxies must be annotated with `@JdbiRepository` so installer could recognize them.
See [UserRepository](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/UserRepository.java) 

Repository is annotated with `@InTransaction` to allow using repositories directly: repository method call is the smallest transaction scope. 
Transaction scope could be enlarged by using annotation on calling guice beans or 
[declaring transaction manually](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi#manual-transaction-definition).
In order to better understand how transactions work read [unit of work docs section](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi#unit-of-work).

Note that `InTransaction` is handled with guice AOP, so you can use any other guice aop related features.

Constructor injection is impossible in repositories, but you can use field injections:

```java
 @Inject
 private RandomNameGenerator generator;
```


As an extra demonstration, base repository class ([Crud](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/Crud.java)) 
implements hibernate-like optimistic lock concept: on each entity save versino field is assigned/incremented and 
checked during update to prevent data loss. 

#### Result set mapper

[Result set mapper installer](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi#result-set-mapper): detects all implementations of `ResultSetMapper`.

Result set mapper is used to map query result set to entity: [UserMapper](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/mapper/UserMapper.java).
It's automatically registered in dbi instance. Mapper are instantiated as normal guice beans without restrictions: so you can use injection and aop 
(it's only not shown in example mapper).

Also, see complementing [UserBind](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/mapper/bind/UserBind.java) 
annotation, used to bind object to query parameters:

```java
@SqlUpdate("update users set version=:version, name=:name where id=:id and version=:version - 1")
public abstract int update(@UserBind User entry);
```

There is no custom installer for annotation because it's detected automatically by DBI.  

#### Tests

You can see in tests example of in-memory H2 db creation re-using application migration scripts. Flyway used in tests directly (bundle nto used).
Special bundle [FlywayInitBundle](src/test/groovy/ru/vyarus/dropwizard/guice/examples/util/FlywayInitBundle.groovy) is installed using
guicey [bundles lookup mechanism](https://github.com/xvik/dropwizard-guicey#bundle-lookup) (enabled by defualt):

```groovy
PropertyBundleLookup.enableBundles(FlywayInitBundle)
```

### Manual run

If you want to start application manually, use prepared [example-config.yml](example-config.yml). It's configured to use persistent databse, located at `~/sample`.

First create schema:
```
JdbiApplication db migrate
```

Then run app normally

```
JdbiApplication server example-config.yml
```

Note: it's psuedo-commands just to show application start parameters (assuming you will run main class form IDE).
 
If you need to re-create databse use:
```
JdbiApplication db clean
JdbiApplication db migrate
```

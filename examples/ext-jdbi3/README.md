### JDBI3 integration sample

Use [JDBI3 guicey extension](https://github.com/xvik/dropwizard-guicey/tree/master/guicey-jdbi3) to:
* use jdbi proxies as guice beans
* be able to use injection inside proxies
* be able to use AOP on proxies
* use annotations for transaction definition
* automatic repositories and mapper installation

[Dropwizard jdbi3 integration](https://www.dropwizard.io/en/release-4.0.x/manual/jdbi3.html) is used to configure 
and create jdbi instance. See [configuration](src/main/java/ru/vyarus/dropwizard/guice/examples/Jdbi3AppConfiguration.java).

For simplicity, embedded H2 database used.
Database scheme must be created before launching application. 
[Dropwizard-flyway](https://github.com/dropwizard/dropwizard-flyway) used to prepare database (it's actually used only for [manual run](#manual-run) - 
tests use flyway directly). See [db scheme](src/main/resources/db/migration/V1__setup.sql). 
 

JDBI instance created exactly as described in [dropwizard docs](https://www.dropwizard.io/en/release-4.0.x/manual/jdbi3.html) 
using provided db configuration:

```java
.bundles(JdbiBundle.<JdbiAppConfiguration>forDatabase((conf, env) -> conf.getDatabase()))
```

(You can provide pre-build dbi instance instead).

`JdbiBundle` will activate additional installers.

Note custom jdbi plugin installation for H2:

```java
.withPlugins(new H2DatabasePlugin()))
```

#### Repository

[Repositories installer](https://github.com/xvik/dropwizard-guicey/tree/master/guicey-jdbi3#repository): all jdbi proxies must be annotated with `@JdbiRepository` so installer could recognize them.
See [UserRepository](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/UserRepository.java) 

Repository is annotated with `@InTransaction` to allow using repositories directly: repository method call is the smallest transaction scope. 
Transaction scope could be enlarged by using annotation on calling guice beans or 
[declaring transaction manually](https://github.com/xvik/dropwizard-guicey/tree/master/guicey-jdbi3#manual-transaction-definition).
In order to better understand how transactions work read [unit of work docs section](https://github.com/xvik/dropwizard-guicey/tree/master/guicey-jdbi3#unit-of-work).

Note that `InTransaction` is handled with guice AOP, so you can use any other guice aop related features.

Repositories are restricted to interfaces, but use can declare custom logic in default methods. 
In order to use guice beans inside default methods, "injection getters" must be used:

```java
 @Inject
 RandomNameGenerator getGgenerator();
```

As an extra demonstration, base repository class ([Crud](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/Crud.java)) 
implements hibernate-like optimistic lock concept: on each entity save version field is assigned/incremented and 
checked during update to prevent data loss. 

#### Row mapper

[Row mapper installer](https://github.com/xvik/dropwizard-guicey/tree/master/guicey-jdbi3#row-mapper): detects all implementations of `RowMapper`.

Row mapper is used to map query result set to entity: [UserMapper](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/mapper/UserMapper.java).
It's automatically registered in jdbi instance. Mapper are instantiated as normal guice beans without restrictions: so you can use injection and aop 
(it's only not shown in example mapper).

Also, see complementing [UserBind](src/main/java/ru/vyarus/dropwizard/guice/examples/repository/mapper/bind/UserBind.java) 
annotation, used to bind object to query parameters:

```java
@SqlUpdate("update users set version=:version, name=:name where id=:id and version=:version - 1")
int update(@UserBind User entry);
```

There is no custom installer for annotation because it's detected automatically by JDBI.  

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
Jdbi3Application db migrate
```

Then run app normally

```
Jdbi3Application server example-config.yml
```

Note: it's psuedo-commands just to show application start parameters (assuming you will run main class form IDE).
 
If you need to re-create databse use:
```
Jdbi3Application db clean
Jdbi3Application db migrate
```

# JDBI integration

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi) module

!!! warning ""
    **DEPRECATED**: because jdbi2 dropwizard module is deprecated and moved [outside of core modules](https://github.com/dropwizard/dropwizard-jdbi).
    Migrate [to jdbi3](#migration-to-jdbi3) 

Integrates [JDBI2](http://jdbi.org/) with guice. Based on [dropwizard-jdbi](https://www.dropwizard.io/en/release-1.3.x/manual/jdbi.html) integration.
 
Features:

* DBI instance available for injection
* Introduce unit of work concept, which is managed by annotations and guice aop (very like spring's @Transactional)
* Repositories (JDBI proxies for interfaces and abstract classes):
    - installed automatically (when classpath scan enabled)
    - are normal guice beans, supporting aop and participating in global (thread bound) transaction.
    - no need to compose repositories anymore (e.g. with @CreateSqlObject) to gain single transaction.
* Automatic installation for custom `ResultSetMapper` 

Added installers:

* [RepositoryInstaller](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi/src/main/java/ru/vyarus/guicey/jdbi/installer/repository/RepositoryInstaller.java) - sql proxies
* [MapperInstaller](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi/src/main/java/ru/vyarus/guicey/jdbi/installer/MapperInstaller.java) - result set mappers  
 
## Setup

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-jdbi.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-jdbi)

Avoid version in dependency declaration below if you use [extensions BOM](bom.md). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-jdbi</artifactId>
  <version>{{ gradle.ext }}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-jdbi:{{ gradle.ext }}'
```

See the most recent version in the badge above.

## Usage

Register bundle:

```java
GuiceBundle.builder()        
        .bundles(JdbiBundle.<ConfType>forDatabase((conf, env) -> conf.getDatabase()))
        ...
```

Here default DBI instance will be created from database configuration (much like it's described in 
[dropwizard documentation](https://www.dropwizard.io/en/release-1.3.x/manual/jdbi.html)).

Or build DBI instance yourself:

```java
JdbiBundle.forDbi((conf, env) -> locateDbi())
```

### Unit of work

Unit of work concept states for: every database related operation must be performed inside unit of work.

In DBI such approach was implicit: you were always tied to initial handle. This lead to cumbersome usage of
sql object proxies: if you create it on-demand it would always create new handle; if you want to combine
multiple objects in one transaction, you have to always create them manually for each transaction.

Integration removes these restrictions: dao (repository) objects are normal guice beans and transaction
scope is controlled by `@InTransaction` annotation (note that such name was intentional to avoid confusion with
DBI own's Transaction annotation and more common Transactional annotations).

At the beginning of unit of work, DBI handle is created and bound to thread (thread local).
All repositories are simply using this bound handle and so share transaction inside unit of work.

#### @InTransaction

Annotation on method or class declares transactional scope. For example:

```java
@Inject MyDAO dao

@InTransaction
public Result doSomething() {
   dao.select();
   ...
}
```

Transaction opened before doSomething() method and closed after it. 
Dao call is also performed inside transaction.
If exception appears during execution, it's propagated and transaction rolled back.

Nested annotations are allowed (they simply ignored).

Note that unit of work is not the same as transaction scope (transaction scope could be less or equal to unit of work). 
But, for simplicity, you may think of it as the same things, if you always use `@InTransaction` annotation. 

If required, you may use your own annotation for transaction definition:

```java
JdbiBundle.forDatabase((conf, env) -> conf.getDatabase())
    .withTxAnnotations(MyCustomTransactional.class);
```

Note that this will override default annotation support. If you want to support multiple annotations then specify
all of them:

```java
JdbiBundle.forDatabase((conf, env) -> conf.getDatabase())
    .withTxAnnotations(InTransaction.class, MyCustomTransactional.class);
```

#### Context Handle

Inside unit of work you may reference current handle by using:

```java
@Inject Provider<Handle>
```

#### Manual transaction definition

You may define transaction (with unit of work) without annotation using:

```java
@Inject TransactionTenpate template;
...
template.inTrabsansaction((handle) -> doSomething())
```

Note that inside such manual scope you may also call any repository bean, as it's absolutely the same definition as 
with annotation.

### Repository

Declare repository (interface or abstract class) as usual, using DBI annotations. 
It only must be annotated with `@JdbiRepository` so installer
could recognize it and register in guice context.

!!! warning ""
    Singleton scope will be forced for repositories.

```java
@JdbiRepository
@InTransaction
public interface MyRepository {     
    
    @SqlQuery("select name from something where id = :id")
    String findNameById(@Bind("id") int id);
}
```

Note the use of `@InTransaction`: it was used to be able to call repository methods without extra annotations
(the lowest transaction scope it's repository itself). It will make beans "feel the same" as usual DBI on demand
sql object proxies.

`@InTransaction` annotation is handled using guice aop. You can use any other guice aop related features.

You can also use injection inside repositories, but only field injection:
 
```java
public abstract class MyRepo {
    @Inject SomeBean bean;
}
``` 

Constructor injection is impossible, because DBI sql proxies are still used internally and DBI will not be able
to construct proxy for class with constructor injection.

*Don't use DBI @Transaction and @CreateSqlObject annotations anymore*: probably they will even work, but they are not
needed now and may confuse.

All installed repositories are reported into console:

```
INFO  [2016-12-05 19:42:27,374] ru.vyarus.guicey.jdbi.installer.repository.RepositoryInstaller: repositories = 

    (ru.vyarus.guicey.jdbi.support.repository.SampleRepository)
```

### Result set mapper

If you have custom implementations of `ResultSetMapper`, it may be registered automatically. 
You will be able to use injections there because mappers become ususal guice beans (singletons).
When classpath scan is enabled, such classes will be searched and installed automatically.

```java
public class CustomMapper implements ResutlSetMapper<Custom> {
    @Override
    public Cusom map(int row, ResultSet rs, StatementContext ctx) {
        // mapping here
        return custom;
    }
}
```

And now Custom type could be used for queries:

```java
@JdbiRepository
@InTransaction
public interface CustomRepository {     
    
    @SqlQuery("select * from custom where id = :id")
    Custom findNameById(@Bind("id") int id);
}
```

All installed mappers are reported to console:

```
INFO  [2016-12-05 20:02:25,399] ru.vyarus.guicey.jdbi.installer.MapperInstaller: jdbi mappers = 

    Sample               (ru.vyarus.guicey.jdbi.support.mapper.SampleMapper)
```

## Manual unit of work definition

If, for some reason, you don't need transaction at some place, you can declare raw unit of work and use 
assigned handle directly:

```java
@Inject UnitManager manager;

manager.beginUnit();
try {
    Handle handle = manager.get();
    // logic executed in unit of work but without transaction
} finally {
    manager.endUnit();
}
```

Repositories could also be called inside such manual unit (as unit of work is correctly started).

## Migration to jdbi3

* Use [guicey-jdbi3](jdbi3.md)

* Module package changed from `ru.vyarus.guicey.jdbi` to `ru.vyarus.guicey.jdbi3`.

* `Jdbi` object was previously bind as `DBI` interface. Now it's bound as `Jdbi` (as interface was removed in jdbi3).

* New methods in JdbiBundle:
    - withPlugins - install custom plugins
    - withConfig - to simplify manual configuration

* In jdbi3 `ResultSetMapper` was changed to `RowMapper` (and ColumnMapper). Installer supports RowMapper automatic installation.

* If you were using binding annotations then:
    - `@BindingAnnotation` -> `@SqlStatementCustomizingAnnotation`
    - `BindingFactory` ->  `SqlStatementCustomizerFactory`

* Sql object proxies must be interfaces now (jdbi3 restriction). But as java 8 interfaces support default methods,
its not a big problem
    - instead of field injection (to access other proxies), now getter annotated with @Inject must be used.
        
See [jdbi3 migration gude](http://jdbi.org/#_upgrading_from_v2_to_v3) for other (pure jdbi related) differences
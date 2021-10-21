# JDBI3 integration

!!! summary ""
    [Extensions project](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi3) module

Integrates [JDBI3](http://jdbi.org/) with guice. Based on [dropwizard-jdbi3](https://www.dropwizard.io/en/release-2.0.x/manual/jdbi3.html) integration.
 
Features:

* JDBI instance available for injection
* Introduce unit of work concept, which is managed by annotations and guice aop (very like spring's @Transactional)
* Repositories (JDBI proxies for interfaces):
    - installed automatically (when classpath scan enabled)
    - are normal guice beans, supporting aop and participating in global (thread bound) transaction.
    - no need to compose repositories anymore (e.g. with @CreateSqlObject) to gain single transaction.
    - can reference guice beans (with annotated getters)
* Automatic installation for custom `RowMapper` 

Added installers:

* [RepositoryInstaller](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi3/src/main/java/ru/vyarus/guicey/jdbi3/installer/repository/RepositoryInstaller.java) - sql proxies
* [MapperInstaller](https://github.com/xvik/dropwizard-guicey-ext/tree/master/guicey-jdbi3/src/main/java/ru/vyarus/guicey/jdbi3/installer/MapperInstaller.java) - row mappers  
 
## Setup

!!! important 
    Since dropwizard 2.0.22 dropwizard-jdbi3 [requires Java 11 by default](https://github.com/dropwizard/dropwizard/releases/tag/v2.0.22),
    use `guicey-jdbi3-jdk8` instead (meta package fixing classpath) for java 8 compatibility.

[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus.guicey/guicey-jdbi3.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus.guicey/guicey-jdbi3)

Avoid version in dependency declaration below if you use [extensions BOM](../guicey-bom). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus.guicey</groupId>
  <artifactId>guicey-jdbi3</artifactId>
  <version>{{ gradle.ext }}</version>
</dependency>
```

Gradle:

```groovy
implementation 'ru.vyarus.guicey:guicey-jdbi3:{{ gradle.ext }}'
```

See the most recent version in the badge above.

!!! note ""
    [Migration from jdbi2](jdbi.md#migration-to-jdbi3)

## Usage

Register bundle:

```java
GuiceBundle.builder()        
        .bundles(JdbiBundle.<ConfType>forDatabase((conf, env) -> conf.getDatabase()))
        ...
```

Here default JDBI instance will be created from database configuration (much like it's described in 
[dropwizard documentation](https://www.dropwizard.io/en/release-2.0.x/manual/jdbi3.html)).

Or build JDBI instance yourself:

```java
JdbiBundle.forDbi((conf, env) -> locateDbi())
```

Jdbi3 introduce plugins concept. Dropwizard will automatically register `SqlObjectPlugin`, `GuavaPlugin`, `JodaTimePlugin`.
If you need to install custom plugin:

```java
JdbiBundle.forDbi((conf, env) -> locateDbi())
    .withPlugins(new H2DatabasePlugin())
```

Also, If custom registration must be performed on jdbi instance:

```java
JdbiBundle.forDbi((conf, env) -> locateDbi())
    .withConfig((jdbi) -> { jdbi.callSomething() })
```

Such configuration block will be called just after jdbi instance creation (but before injector creation).

### Unit of work

Unit of work concept states for: every database related operation must be performed inside unit of work.

In JDBI such approach was implicit: you were always tied to initial handle. This lead to cumbersome usage of
sql object proxies: if you create it on-demand it would always create new handle; if you want to combine
multiple objects in one transaction, you have to always create them manually for each transaction.

Integration removes these restrictions: dao (repository) objects are normal guice beans and transaction
scope is controlled by `@InTransaction` annotation (note that such name was intentional to avoid confusion with
JDBI own's Transaction annotation and more common Transactional annotations).

At the beginning of unit of work, JDBI handle is created and bound to thread (thread local).
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

##### Transaction configuration

Transaction isolation level and readonly flag could be defined with annotation:

```java
@InTransaction(TransactionIsolationLevel.READ_UNCOMMITTED)

@InTransaction(readOnly = true)
```

In case of nested transactions error will be thrown if:

* Current transaction level is different then nested one
* Current transaction is read only and nexted one is not  (note that some drivers, like h2, ignore readOnly flag completely)

For example:

```java
@InTransaction
public void action() {
    nestedAction();
}

@InTransaction(TransactionIsolationLevel.READ_UNCOMMITTED)
public void nestedAction() {
...    
}
``` 

When `action()` method called new transaction is created with default level
(usually READ_COMMITTED). When `nestedAction()` is called exception will be thrown
because it's transaction level requirement (READ_UNCOMMITTED) contradict with current transaction.

##### Custom transactional annotation

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

If you need to support transaction configuration (level and read only settings) with your annotation then:
 
1. Add required properties into annotation itself (see `@InTransaction` as example).
2. Create implementation of `TxConfigFactory` (see `InTransactionTxConfigFactory` as example)
3. Register factory inside your annotation with `@TxConfigSupport(MyCustomAnnotationTxConfigFactory.class)` 

Your factory will be instantiated as guice bean so annotate it as Singleton, if possible
to avoid redundant instances creation.

Configuration is resolved just once for each method, so yur factory will be called just once 
for each annotated (with your custom annotation) method. 

#### Context Handle

Inside unit of work you may reference current handle by using:

```java
@Inject Provider<Handle>
```

#### Manual transaction definition

You may define transaction (with unit of work) without annotation using:

```java
@Inject TransactionTempate template;
...
template.inTrasansaction((handle) -> doSomething())
```

Note that inside such manual scope you may also call any repository bean, as it's absolutely the same definition as 
with annotation.

You can also specify transaction config (if required):

```java
@Inject TransactionTempate template;
...
template.inTrasansaction(
        new TxConfig().level(TransactionIsolationLevel.READ_UNCOMMITTED), 
        (handle) -> doSomething())
```


### Repository

Declare repository (interface or abstract class) as usual, using DBI annotations. 
It only must be annotated with `@JdbiRepository` so installer
could recognize it and register in guice context.

!!! note
    singleton scope will be forced for repositories.

```java
@JdbiRepository
@InTransaction
public interface MyRepository {     
    
    @SqlQuery("select name from something where id = :id")
    String findNameById(@Bind("id") int id);
}
```

Note the use of `@InTransaction`: it was used to be able to call repository methods without extra annotations
(the lowest transaction scope it's repository itself). It will make beans "feel the same" as usual JDBI on demand
sql object proxies.

`@InTransaction` annotation is handled using guice aop. You can use any other guice aop related features.

!!! warning 
    *Don't use JDBI `@Transaction` and `@CreateSqlObject` annotations anymore*: probably they will even work, but they are not
    needed now and may confuse.

All installed repositories are reported into console:

```
INFO  [2016-12-05 19:42:27,374] ru.vyarus.guicey.jdbi3.installer.repository.RepositoryInstaller: repositories = 

    (ru.vyarus.guicey.jdbi3.support.repository.SampleRepository)
```

### Laziness

By default, JDBI proxies for declared repositories created only on first repository method call.
Lazy behaviour is important to take into account all registered JDBI extensions. Laziness also
slightly speeds up application startup. 

If required, you can enable eager initialization during bundle construction:   

```java
JdbiBundle.forDatabase((conf, env) -> conf.getDatabase())
    .withEagerInitialization()
```

In the eager mode all proxies would be constructed after application initialization (before web part initialization).

### Guice beans access

You can access guice beans by annotating getter with `@Inject` (javax or guice):

```java
@JdbiRepository
@InTransaction
public interface MyRepository {     

    @Inject
    MyOtherRepository getOtherRepo();
    
    @SqlQuery("select name from something where id = :id")
    String findNameById(@Bind("id") int id);
    
    default String doSomething(int id) {
        String name = findNameById(id);
        return getOtherRepo().doSOmethingWithName(name);
    }
}
```

Here call to `getOtherRepo()` will return `MyOtherRepository` guice bean, which is actually
another proxy.  

### Row mapper

If you have custom implementations of `RowMapper`, it may be registered automatically. 
You will be able to use injections there because mappers become ususal guice beans (singletons).
When classpath scan is enabled, such classes will be searched and installed automatically.

```java
public class CustomMapper implements RowMapper<Custom> {
    @Override
    Custom map(ResultSet rs, StatementContext ctx) throws SQLException {
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
INFO  [2016-12-05 20:02:25,399] ru.vyarus.guicey.jdbi3.installer.MapperInstaller: jdbi mappers = 

    Sample               (ru.vyarus.guicey.jdbi3.support.mapper.SampleMapper)
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
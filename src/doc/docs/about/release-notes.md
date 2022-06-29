# 5.6.0 Release Notes

!!! summary ""
    [5.5.0 release notes](http://xvik.github.io/dropwizard-guicey/5.5.0/about/release-notes/)

* Dropwizard 2.1 compatibility
* Junit 5 extensions enhancements

## Dropwizard 2.1 compatibility

Release upgrades guicey to [dropwizard 2.1](https://github.com/dropwizard/dropwizard/releases/tag/v2.1.0)

[Dropwizard upgrade notes](https://www.dropwizard.io/en/latest/manual/upgrade-notes/upgrade-notes-2_1_x.html#upgrade-notes-for-dropwizard-2-1-x)

### Java 8 issue

!!! warning
    On java 8 you'll see the following warning on application startup:

    ```
    WARN  [2022-06-06 16:39:24,946] com.fasterxml.jackson.module.blackbird.BlackbirdModule: Unable to find Java 9+ MethodHandles.privateLookupIn.  Blackbird is not performing optimally!
    ! java.lang.NoSuchMethodError: java.lang.invoke.MethodHandles.privateLookupIn(Ljava/lang/Class;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/MethodHandles$Lookup;
    ! at java.lang.invoke.MethodHandleNatives.resolve(Native Method)
    ! at java.lang.invoke.MemberName$Factory.resolve(MemberName.java:975)
    ! at java.lang.invoke.MemberName$Factory.resolveOrFail(MemberName.java:1000)
    ! ... 86 common frames omitted
    ! Causing: java.lang.NoSuchMethodException: no such method: java.lang.invoke.MethodHandles.privateLookupIn(Class,Lookup)Lookup/invokeStatic
    ! at java.lang.invoke.MemberName.makeAccessException(MemberName.java:871)
    ! at java.lang.invoke.MemberName$Factory.resolveOrFail(MemberName.java:1003)
    ! at java.lang.invoke.MethodHandles$Lookup.resolveOrFail(MethodHandles.java:1386)
    ! at java.lang.invoke.MethodHandles$Lookup.findStatic(MethodHandles.java:780)
    ! at com.fasterxml.jackson.module.blackbird.util.ReflectionHack$Java9Up.init(ReflectionHack.java:39)
    ! at com.fasterxml.jackson.module.blackbird.util.ReflectionHack$Java9Up.<clinit>(ReflectionHack.java:34)
    ...
    ```

It looks like an exception, but this is **just a warning**! Application work will not be affected.

This happens because dropwizard replaced afterburner with blackbird. 
Change appeared in [io.dropwizard.jackson.Jackson](https://github.com/dropwizard/dropwizard/blob/release/2.1.x/dropwizard-jackson/src/main/java/io/dropwizard/jackson/Jackson.java#L58):

before afterburner was registered ONLY for java 8:

```java
mapper.registerModule(new JodaModule());
if (JavaVersion.isJava8()) {
    mapper.registerModule(new AfterburnerModule());
}
mapper.registerModule(new FuzzyEnumModule());
```

now blackbird is always registered:

```java
mapper.registerModule(new JodaModule());
mapper.registerModule(new BlackbirdModule());
mapper.registerModule(new FuzzyEnumModule());
```

!!! note
    Both modules are not required: they just enhance jackson performance in some (high-pressure) scenarios.
    Afterburner supposed to be used for java 8 (and works well!). Blackbird is a rewrite of afterburner for
    java 11 (and above) compatibility, because afterburner has problems on recent java versions.
    Blackbird has java 8 **compatibility**, but it was never meant to be used for java 8!

The obvious solution would be to disable blackbird for java 8: there was [a discussion proposing this](https://github.com/dropwizard/dropwizard/discussions/5319), 
but dropwizard maintainers did not wish to do it (I still hope they would change their mind).

You can't hide this message [with logger configuration](https://github.com/dropwizard/dropwizard/discussions/5268#discussioncomment-2723607)
because it happens before logger initialization. 

But you can configure logger manually:

```java
@Override
public void initialize(Bootstrap<Configuration> bootstrap) {
        ((LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory())
                            .getLogger(BlackbirdModule.class).setLevel(Level.ERROR);
}
```

This way you'll hide the warning and continue using blackbird.

The other way is to manually replace `ObjectMapper`:

```java
@Override
public void initialize(Bootstrap<Configuration> bootstrap) {
    ObjectMapper mapper = new ObjectMapper()
    mapper.registerModule(new GuavaModule());
    mapper.registerModule(new GuavaExtrasModule());
    mapper.registerModule(new CaffeineModule());
    mapper.registerModule(new JodaModule());

    // optional but preferred
    mapper.registerModule(new AfterburnerModule());

    mapper.registerModule(new FuzzyEnumModule());
    mapper.registerModule(new ParameterNamesModule());
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new JavaTimeModule());
    mapper.setPropertyNamingStrategy(new AnnotationSensitivePropertyNamingStrategy());
    mapper.setSubtypeResolver(new DiscoverableSubtypeResolver());
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    
    // override default mapper
    bootstrap.setObjectMapper(mapper);
```

As you can see this is basically a copy of dropwizard [Jackson class](https://github.com/dropwizard/dropwizard/blob/release/2.1.x/dropwizard-jackson/src/main/java/io/dropwizard/jackson/Jackson.java#L58) 
logic with one change.

Using this method you can bring back the afterburner, but you'll need an additional dependency:

```groovy
implementation 'com.fasterxml.jackson.module:jackson-module-afterburner:2.13.0'
```

## Junit 5 extensions enhancements

### Test support objects

#### Environment setup

In order to simplify environment setup in tests, new interface added: 

```java
public interface TestEnvironmentSetup {
    Object setup(TestExtension extension);
}
```

For example, it might be used to setup test database:

```java
public class TestDbSetup implements TestEnvironmentSetup {

    @Override
    public Object setup(TestExtension extension) {
        // pseudo code
        Db db = DbFactory.startTestDb();
        // register required configuration
        extension
                .configOverride("database.url", ()-> db.getUrl())
                .configOverride("database.user", ()-> db.getUser())
                .configOverride("database.password", ()-> db.getPassword);
        // assuming object implements Closable
        return db;
    }
}
```

!!! tip "Motivation"
    Previously, additional junit extensions were required for such kind of setup,
    but there was a problem with configuration (because guicey generates system property
    key for each test and so it is not possible to configure application directly with
    system property.
    Also, it was problematic to move such initialization into base class because
    it could be done only with static fields.
    New interface should greatly simplify maintaining test environments.

Only configuration overrides and guicey hooks are allowed for registration.

!!! note
    To avoid confusion with guicey hooks: setup object required to prepare test environment before test (and apply
    required configurations) whereas hooks is a general mechanism for application customization (not only in tests).
    Setup objects are executed before application startup (before `DropwizardTestSupport` object creation) and hooks
    are executed by started application.

It is often required not only to start/create something before test, but also
properly stop/destroy it after. To do it simply return any `Closable` (or `AutoClosable`)
and it would be called just after application shutdown.

If no managed object required - you may return whatever else (even null), nothing would happen.
This was done to simplify lambda declarations.

##### Registration

Registration is the same as with hooks:

* `setup` attribute in extension annotations
* `setup()` methods in extension builders (registered in fields)
* Test fields, annotated with `@EnableSetup`

Simple lambdas might be used for registration, for example:

```java
@EnableSetup
static TestEnvironmentSetup db = ext -> {
             Db db = new Db();
             ext.configOverride("db.url", ()->db.getUrl())
             return db;
        };
```

Field-based declaration might be useful when such initializations must be declared
in base test class (and affect all tests).

#### Test init log

All registered setup objects and hooks (registered in test) now logged on test startup:

```
18:39:03.390 [main] INFO  r.v.d.g.t.j.e.c.TestExtensionsTracker - Guicey test extensions:

	Setup objects = 
		HookObjectsLogTest$Test2$$Lambda$349/1644231115 (r.v.d.g.t.j.hook)               	@EnableSetup field Test2.setup

	Test hooks = 
		HookObjectsLogTest$Base$$Lambda$341/1127224355 (r.v.d.g.t.j.hook)                	@EnableHook field Base.base1
		Ext1                         (r.v.d.g.t.j.h.HookObjectsLogTest)                  	@RegisterExtension class
		HookObjectsLogTest$Test2$$Lambda$345/484589713 (r.v.d.g.t.j.hook)                	@RegisterExtension instance
		Ext3                         (r.v.d.g.t.j.h.HookObjectsLogTest)                  	HookObjectsLogTest$Test2$$Lambda$349/1644231115 class
		HookObjectsLogTest$Test2$$Lambda$369/1911152052 (r.v.d.g.t.j.hook)               	HookObjectsLogTest$Test2$$Lambda$349/1644231115 instance
		HookObjectsLogTest$Test2$$Lambda$350/537066525 (r.v.d.g.t.j.hook)                	@EnableHook field Test2.ext1
```

Setup objects and hooks are shown in execution order. Setup objects go first because they
might also register hooks. Registration source hints are shown on the right. 
There should be enough information to clearly understand test initialization sequence.

#### @EnableHook field type

Before, it was impossible to use exact hook class type in field declaration:

```java
@EnableHook
static GuiceyConfigurationHook hook = new MyHook();
```

But now any class could be used:

```java
@EnableHook
static MyHook hook = new MyHook();
```

### Extensions registration changes

#### Start application for each test method

It is now possible to start application before each test method:

```java
@RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class).create()

// injection would be re-newed for each test method
@Inject Bean bean;

@Test
public void test1() {
    Assertions.assertEquals(0, bean.value);
    bean.value = 10    
}

@Test
public void test2() {
    Assertions.assertEquals(0, bean.value);
    bean.value = 10
}
```

Note that field is **not static**. In this case extension would be activated for each method.

Also, `@EnableHook` and `@EnableSetup` fields might also be not static (but static fields would also work) in this case:

```java
@RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class).create()

@EnableSetup
MySetup setup = new MySetup()
```

#### Configure application from 3rd party junit extension

!!! note
    Generally, setup objects usage should be simpler then writing additional junit 
    extensions for environment setup, but if you already have an extension,
    the following should simplify configuration.

3rd party junit extension should only store required values using junit storage and 
they could be applied now with a new method `configOverrideByExtension`:

```java
public class ConfigExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // do something and then store value
        context.getStore(ExtensionContext.Namespace.GLOBAL).put("ext1", 10);
    }
}

@ExtendWith(ConfigExtension.class)
public class SampleTest {
    
    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
            .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1")
            .create();
}
```

Here junit extension stores value and guicey extension will retrieve and apply value 
from store. Configuration path and storage key are the same here, but they could be different:


```java
.configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "storage.key", "config.path")
```

Apply configuration path 'config.path' value from junit storage under key `storage.key`. 


#### Small builder improvements

`.hooks(Class)` method now accepts multiple hooks at once:

```java
.hooks(Hook1.class, Hook2.class); 
```

`.configOverrides(String...)` method now could be called multiple times:

```java
.configOverrides("foo:1", "bar:2")
.configOverrides("over:3")
```


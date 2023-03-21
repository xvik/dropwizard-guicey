# JUnit 5

!!! note ""
    [Migration from JUnit 4](junit4.md#migrating-to-junit-5)

Junit 5 [user guide](https://junit.org/junit5/docs/current/user-guide/)

## Setup

You will need the following dependencies (assuming BOM used for versions management):

```groovy
testImplementation 'io.dropwizard:dropwizard-testing'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
testRuntimeOnly 'org.junit.jupiter:junit-jupiter'
```

!!! tip
    If you already have junit4 or spock tests, you can activate [vintage engine](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4) 
    so all tests could work  **together** with junit 5: 
    ```groovy    
    testRuntimeOnly 'org.junit.vintage:junit-vintage-engine'
    ```

!!! note
    In gradle you need to explicitly [activate junit 5 support](https://docs.gradle.org/current/userguide/java_testing.html#using_junit5) with
    ```groovy
    test {
        useJUnitPlatform()
        ...
    }                    
    ```
    
!!! warning
    Junit 5 annotations are **different** from junit4, so if you have both junit 5 and junit 4
    make sure correct classes (annotations) used for junit 5 tests:
    ```java
    import org.junit.jupiter.api.Assertions;
    import org.junit.jupiter.api.Test;
    ```    

## Dropwizard extensions compatibility

Guicey extensions can be used with dropwizard extensions. But this may be required only in edge cases
when multiple applications startup is required.

!!! info
    There is a difference in extensions implementation. 
    
    Dropwizard extensions work as:
    junit extension `@ExtendWith(DropwizardExtensionsSupport.class)` looks for fields 
    implementing `DropwizardExtension` (like `DropwizardAppExtension`) and start/stop them according to test lifecycle.
    
    Guicey extensions completely implemented as junit extensions (and only hook fields are manually searched). 
    Also, guciey extension rely on junit parameters injection. Both options has pros and cons.
    

## Extensions

Provided extensions:

* `@TestGuiceyApp` - for lightweight tests (without starting web part, only guice context)
* `@TestDropwizardApp` - for complete integration tests

Both extensions allow using injections directly in test fields.

Extensions are compatible with [parallel execution](#parallel-execution) (no side effects).

[Alternative declaration](#alternative-declaration) might be used for [deferred configuration](#deferred-configuration)
or [starting application for each test method](#start-application-by-test-method). 

Pre-configured [http client](#client) might be used for calling testing application endpoints.

!!! note
    You can use junit 5 extensions with [Spock 2](spock2.md)

Test environment might be prepared with [setup objects](#test-environment-setup)
and application might be re-configured with [hooks](#application-test-modification)
    
## Testing core logic

`@TestGuiceyApp` runs all guice logic without starting jetty (so resources, servlets and filters will not be available).
`Managed` objects will still be handled correctly.

```java
@TestGuiceyApp(MyApplication.class)
public class AutoScanModeTest {

    @Inject 
    MyService service;
    
    @Test
    public void testMyService() {        
        Assertions.assertEquals("hello", service.getSmth());     
    }
```

Also, injections work as method parameters:

```java
@TestGuiceyApp(MyApplication.class)
public class AutoScanModeTest {
    
    public void testMyService(MyService service) {        
        Assertions.assertEquals("hello", service.getSmth());     
    }
```

Application started before all tests in annotated class and stopped after them.

## Testing web logic

`@TestDropwizardApp` is useful for complete integration testing (when web part is required):

```groovy
@TestDropwizardApp(MyApplication.class)
class WebModuleTest {

    @Inject 
    MyService service

    @Test
    public void checkWebBindings(ClientSupport client) {

        Assertions.assertEquals("Sample filter and service called", 
            client.targetMain("servlet").request().buildGet().invoke().readEntity(String.class));
        
        Assertions.assertTrur(service.isCalled());
```

### Random ports

In order to start application on random port you can use configuration shortcut:

```groovy
@TestDropwizardApp(value = MyApplication.class, randomPorts = true)
```

!!! note
    Random ports will be applied even if configuration with exact ports provided:
    ```groovy
    @TestDropwizardApp(value = MyApplication, 
                      config = 'path/to/my/config.yml', 
                      randomPorts = true)
    ```
    Also, random ports support both server types (default and simple)
    
Real ports could be resolved with [ClientSupport](#client) object.

### Rest mapping

Normally, rest mapping configured with `server.rootMapping=/something/*` configuration, but
if you don't use custom configuration class, but still want to re-map rest, shortcut could be used:

```groovy
@TestDropwizardApp(value = MyApplication.class, restMapping="something")
```

In contrast to config declaration, attribute value may not start with '/' and end with '/*' -
it would be appended automatically. 

This option is only intended to simplify cases when custom configuration file is not yet used in tests
(usually early PoC phase). It allows you to map servlet into application root in test (because rest is no
more resides in root). When used with existing configuration file, this parameter will override file definition.

## Guice injections

Any guice bean may be injected directly into test field:

```groovy
@Inject
SomeBean bean
```

This may be even bean not declared in guice modules (JIT injection will occur).

To better understand injection scopes look the following test:

```groovy
@TestGuiceyApp(AutoScanApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InjectionScopeTest {

    // new instance injected on each test
    @Inject
    TestBean bean;

    // the same context used for all tests (in class), so the same bean instance inserted before each test
    @Inject
    TestSingletonBean singletonBean;

    @Test
    @Order(1)
    public void testInjection() {
        bean.value = 5;
        singletonBean.value = 15;

        Assertions.assertEquals(5, bean.value);
        Assertions.assertEquals(15, singletonBean.value);

    }

    @Test
    @Order(2)
    public void testSharedState() {

        Assertions.assertEquals(0, bean.value);
        Assertions.assertEquals(15, singletonBean.value);
    }

    // bean is in prototype scope
    public static class TestBean {
        int value;
    }

    @Singleton
    public static class TestSingletonBean {
        int value;
    }
}
```


!!! note
    Guice AOP will not work on test methods (because test instances not created by guice).

## Parameter injection

Any **declared** guice bean may be injected as method parameter:

```java
@Test
public void testSomthing(DummyBean bean) 
```

(where `DummyBean` is manually declared in some module or JIT-instantiated during injector creation).

For not declared beans injection (JIT) special annotation must be used:

```java
@Test
public void testSomthing(@Jit TestBean bean) 
```

!!! info
    Additional annotation required because you may use other junit extensions providing their own
    parameters, which guicey extension should not try to handle. That's why not annotated parameters
    verified with existing injector bindings.
    
Qualified and generified injections will also work:

```java
@Test
public void testSomthing(@Named("qual") SomeBean bean,
                         TestBean<String> generifiedBean,
                         Provider<OtherBean> provider) 
```    

Also, there are special objects available as parameters:

* `Application` or exact application class (`MyApplication`)
* `ObjectMapper`
* `ClientSupport` application web client helper

!!! note
    Parameter injection will work on test methods as well as lifecyle methods (beforeAll, afterEach etc.) 

Example:

```java
@TestDropwizardApp(AutoScanApplication.class)
public class ParametersInjectionDwTest {

    public ParametersInjectionDwTest(Environment env, DummyService service) {
        Preconditions.checkNotNull(env);
        Preconditions.checkNotNull(service);
    }

    @BeforeAll
    static void before(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @BeforeEach
    void setUp(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @AfterEach
    void tearDown(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @AfterAll
    static void after(Application app, DummyService service) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(service);
    }

    @Test
    void checkAllPossibleParams(Application app,
                                AutoScanApplication app2,
                                Configuration conf,
                                TestConfiguration conf2,
                                Environment env,
                                ObjectMapper mapper,
                                Injector injector,
                                ClientSupport client,
                                DummyService service,
                                @Jit JitService jit) {
        assertNotNull(app);
        assertNotNull(app2);
        assertNotNull(conf);
        assertNotNull(conf2);
        assertNotNull(env);
        assertNotNull(mapper);
        assertNotNull(injector);
        assertNotNull(client);
        assertNotNull(service);
        assertNotNull(jit);
        assertEquals(client.getPort(), 8080);
        assertEquals(client.getAdminPort(), 8081);
    }

    public static class JitService {

        private final DummyService service;

        @Inject
        public JitService(DummyService service) {
            this.service = service;
        }
    }
}
```

## Client

Both extensions prepare special jersey client instance which could be used for web calls.
It is mostly useful for complete web tests to call rest services and servlets.

```java
@Test
void checkRandomPorts(ClientSupport client) {
    Assertions.assertNotEquals(8080, client.getPort());
    Assertions.assertNotEquals(8081, client.getAdminPort());
}
```

Client object provides:

* Access to [JerseyClient](https://eclipse-ee4j.github.io/jersey.github.io/documentation/2.29.1/client.html) object (for raw calls)
* Shortcuts for querying main, admin or rest contexts (it will count the current configuration automatically)
* Shortcuts for base main, admin or rest contexts base urls (and application ports)

Example usages:

```java
// GET {rest path}/some
client.targetRest("some").request().buildGet().invoke()

// GET {main context path}/servlet
client.targetMain("servlet").request().buildGet().invoke()

// GET {admin context path}/adminServlet
client.targetAdmin("adminServlet").request().buildGet().invoke()
```

!!! tip
    All methods above accepts any number of strings which would be automatically combined into correct path:
    ```groovy
    client.targetRest("some", "other/", "/part")
    ``` 
    would be correctly combined as "/some/other/part/"

As you can see test code is abstracted from actual configuration: it may be default or simple server
with any contexts mapping on any ports - target urls will always be correct.

```java
Response res = client.targetRest("some").request().buildGet().invoke()

Assertions.assertEquals(200, res.getStatus())
Assertions.assertEquals("response text", res.readEntity(String)) 
```

Also, if you want to use other client, client object can simply provide required info:

```groovy
client.getPort()        // app port (8080)
client.getAdminPort()   // app admin port (8081)
client.basePathMain()   // main context path (http://localhost:8080/)
client.basePathAdmin()  // admin context path (http://localhost:8081/)
client.basePathRest()   // rest context path (http://localhost:8080/)
```

Raw client usage:

```java
// call completely external url
client.target("http://somedomain:8080/dummy/").request().buildGet().invoke()
```

!!! warning 
    Client object could be injected with both dropwizard and guicey extensions, but in case of guicey extension,
    only raw client could be used (because web part not started all other methods will throw NPE)

## Configuration

For both extensions you can configure application with external configuration file:

```java
@TestGuiceyApp(value = MyApplication.class,
    config = "path/to/my/config.yml"
public class ConfigOverrideTest {
```

Or just declare required values:

```java
@TestGuiceyApp(value = MyApplication.class,
    configOverride = {
            "foo: 2",
            "bar: 12"
    })
public class ConfigOverrideTest {
```

(note that overriding declaration follows yaml format "key: value")

Or use both at once (here overrides will override file values):

```java
@TestGuiceyApp(value = MyApplication.class,
    config = 'path/to/my/config.yml',
    configOverride = {
            "foo: 2",
            "bar: 12"
    })
class ConfigOverrideTest {
```

### Deferred configuration

If you need to configure value, supplied by some other extension, or value may be resolved only
after test start, then static overrides declaration is not an option. In this case use
[alternative extensions declaration](#alternative-declaration) which provides additional 
config override methods:

```java
@RegisterExtension
@Order(1)
static FooExtension ext = new FooExtension();

@RegisterExtension
@Order(2)
static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
        .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
        .configOverrides("foo: 1")
        .configOverride("bar", () -> ext.getValue())
        .configOverrides(new ConfigOverrideValue("baa", () -> "44"))
        .create();
```

In most cases `configOverride("bar", () -> ext.getValue())` would be enough to configure a supplier instead
of static value.

In more complex cases, you can use custom implementations of `ConfigOverride`. 

!!! warning ""
    Guicey have to accept only `ConfigOverride` objects implementing custom 
    `ru.vyarus.dropwizard.guice.test.util.ConfigurablePrefix` interface. 
    In order to support parallel tests guicey generates unique config prefix for each test
    (because all overrides eventually stored to system properties) and so it needs a way
    to set this prefix into custom `ConfigOverride` objects.

### Configuration from 3rd party extensions

If you have junit extension (e.g. which starts db for test) and you need 
to apply configuration overrides from that extension, then you should simply
store required values inside junit storage:

```java
public class ConfigExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // do something and then store value
        context.getStore(ExtensionContext.Namespace.GLOBAL).put("ext1", 10);
    }
}
```

And map overrides directly from store using `configOverrideByExtension` method:

```java
@ExtendWith(ConfigExtension.class)
public class SampleTest {
    
    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
            .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1")
            .create();
}
```

Here, value applied by extension under key `ext1` would be applied to configuration `ext1` path.
If you need to use different configuration key:

```java
.configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1", "key")
```

!!! tip
    You can use [setup objects](#test-environment-setup) instead of custom junit extensions for test environment setup

## Test environment setup

It is often required to prepare test environment before starting dropwizard application.
Normally, such cases require writing custom junit extensions. In order to simplify
environment setup, guicey provides `TestEnviromentSetup` interface.

Setup objects are called before application startup and could directly apply (through parameter)
configuration overrides and hooks.

For example, suppose you need to set up a database before test:

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

It is not required to return anything, only if something needs to be closed after application shutdown:
objects other than `Closable` (`AutoClosable`) or `org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource`
simply ignored.
This approach (only one method) simplifies interface usage with lambdas.

Setup object might be declared in extension annotation: 

```java
@TestGuiceyApp(value=App.class, setup=TestDbSetup.class)
```

Or in manual registration:

```java
@RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class)
        // as class
        .setup(TestDbSetup.class)
        // or as instance
        .setup(new TestDbSetup())
```

Or with lambda:

```java
.setup(ext -> {
        Db db = new Db();
        ext.configOverride("db.url", ()->db.getUrl())
        return db;
})
```

### Setup fields

Alternatively, setup objects might be declared simply in test fields:

```java
@EnableSetup
static TestEnvironmentSetup db = ext -> {
            Db db = new Db();
            ext.configOverride("db.url", ()->db.getUrl())
            return db;
        };
```

or 

```java
@EnableSetup
static TestDbSetup db = new TestDbSetup()
```

This could be extremely useful if you need to unify setup logic for multiple tests,
but use different extension declarations in test. In this case simply move field
declaration into base test class:

```java
public abstract class BaseTest {
    
    @EnableSetup
    static TestDbSetup db = new TestDbSetup();
}
```

!!! note
    To avoid confusion with guicey hooks: setup object required to prepare test environment before test (and apply
    required configurations) whereas hooks is a general mechanism for application customization (not only in tests).
    Setup objects are executed before application startup (before `DropwizardTestSupport` object creation) and hooks 
    are executed by started application.

## Application test modification

You can use [hooks to customize application](overview.md#configuration-hooks).

In both extensions annotation hooks could be declared with attribute:

```java
@TestDropwizardApp(value = MyApplication.class, hooks = MyHook.class)
```

or

```java
@TestGuiceyApp(value = MyApplication.class, hooks = MyHook.class)
```

Where MyHook is:

```java
public class MyHook implements GuiceyConfigurationHook {}
```

### Hook fields

Alternatively, you can declare hook directly in test field:

```java
@EnableHook
static GuiceyConfigurationHook HOOK = builder -> builder.modules(new DebugModule());
```

Any number of fields could be declared. The same way hook could be declared in base test class:

```java
public abstract class BaseTest {
    
    // hook in base class
    @EnableHook
    static GuiceyConfigurationHook BASE_HOOK = builder -> builder.modules(new DebugModule());
}

@TestGuiceyApp(value = App.class, hooks = SomeOtherHook.class)
public class SomeTest extends BaseTest {
    
    // Another hook
    @EnableHook
    static GuiceyConfigurationHook HOOK = builder -> builder.modules(new DebugModule2());
}
```

All 3 hooks will work.

## Extension configuration unification

It is a common need to run multiple tests with the same test application configuration
(same config overrides, same hooks etc.).
Do not configure it in each test, instead move extension configuration into base test class:

```java
@TestGuiceyApp(...)
public abstract class AbstractTest {
    // here might be helper methods
}
```

And now all test classes should simply extend it:

```java
public class Test1 extends AbstractTest {
    
    @Inject
    MyService service;
    
    @Test
    public void testSomething() { ... }
}
```

If you use manual extension configuration (through field), just replace annotation in base class with
manual declaration - approach would still work.

## Reuse application between tests

In some cases it is preferable to start application just once and use for all tests
(e.g. due to long startup or time-consuming environment preparation).

In order to use the same application instance, extension declaration must be performed in 
[base test class](#extension-configuration-unification) and `reuseApplication` flag must be enabled:

```java
@TestGuiceyApp(value = Application.class, reuseApplication = true)
public abstract class BaseTest {}
```

or

```java
public abstract class BaseTest {
    @RegisterExtension
    static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class)
            .reuseApplication()
            .create();
    
}
```

The same will work for dropwizard extension (`@TestDropwizardApp` and `TestDropwizardAppExtension`).

!!! important
    Application instance re-use is not enabled by default for backwards compatibility
    (for cases when base class declaration already used).

There might be multiple base test classes declaring reusable applications:
different global applications would be started for each declaration (allowing you
to group tests requiring different applications) 

Global application would be closed after all tests execution (with test engine shutdown).

In essence, reusable application "stick" to declaration in base class, so all tests,
extending base class "inherit" the same declaration and so the same application (when reuse enabled).

!!! tip
    Reusable applications may be used together with tests, not extending base class
    and using guicey extensions. Such tests would simply start a new application instance.
    Just be sure to avoid port clashes when using reusable dropwizard apps (by using `randomPorts` option).

`@EnableSetup` and `@EnableHook` fields are also supported for reusable applications.
But declare all such fields on base class level (or below) because otherwise only fields
declared on first started test would be used. Warning would be printed if such fields used
(or ignored because reusable app was already started by different test).

## Parallel execution
    
Junit [parallel tests execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution)
could be activated with properties file `junit-platform.properties` located at test resources root:

```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = concurrent
```

!!! note
    In order to avoid config overriding collisions (because all overrides eventually stored to system properties)
    guicey generates unique property prefixes in each test.

To avoid port collisions in dropwizard tests use [randomPorts option](#random-ports).

## Alternative declaration

Both extensions could be declared in fields:

```java
@RegisterExtension
static TestDropwizardAppExtension app = TestDropwizardAppExtension.forApp(AutoScanApplication.class)
        .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
        .configOverrides("foo: 2", "bar: 12")
        .randomPorts()
        .hooks(Hook.class)
        .hooks(builder -> builder.disableExtensions(DummyManaged.class))
        .create();
```

The only difference with annotations is that you can declare hooks and setup objects as lambdas directly 
(still hooks in static fields will also work).

```java
@RegisterExtension
static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
        ...
```

This alternative declaration is intended to be used in cases when guicey extensions need to be aligned with
other 3rd party extensions: in junit you can order extensions declared with annotations (by annotation order)
and extensions declared with `@RegisterExtension` (by declaration order). But there is no way
to order extension registered with `@RegisterExtension` before annotation extension.

So if you have 3rd party extension which needs to be executed BEFORE guicey extensions, you can use field declaration.

!!! note
    Junit 5 intentionally shuffle `@RegisterExtension` extensions order, but you can always order them with
    `@Order` annotation.

### Start application by test method

When you declare extensions with annotations or with `@RegisterExtension` in static fields,
application would be started before all test methods and shut down after last test method.

If you want to start application *for each test method* then declare extension in non-static field:

```java
RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class).create()

// injection would be re-newed for each test method
@Inject Bean bean;

@Test
public void test1() {
    Assertions.assertEquals(0, bean.value);
    // changing value to show that bean was reset between tests
    bean.value = 10    
}

@Test
public void test2() {
    Assertions.assertEquals(0, bean.value);
    bean.value = 10
}
```

Also, `@EnableHook` and `@EnableSetup` fields might also be not static (but static fields would also work) in this case:

```java
@RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class).create()

@EnableSetup
MySetup setup = new MySetup()
```

## Debug

Debug option could be activated on extensions in order to print

registered setup objects and hooks (registered in test):

```
Guicey test extensions (Test2.):

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

which prints registered objects in the execution order and with registration source in the right.

And applied configuration overrides:

```
Applied configuration overrides (Test1.): 

	                  foo = 1
```

!!! important
    Configuration overrides printed **after** application startup because they are
    extracted from system properties (to guarantee exact used value), which is possible
    to analyze only after `DropwizardTestSupport#before()` call.

!!! note
    Configuration prefix for system properties is shown in brackets: `(Test1.)`.
    It simplifies investigation in case of concurrent tests.

Debug could be activated by annotation:

```java
@TestGuiceyApp(value = App.class, debug = true)
```

By builder:

```java
@RegisterExtension
TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App)
        .debug()
        .create()
```

By setup object:

```java
@EnableSetup
static TestEnvironmentSetup db = ext -> {
            ext.debug();
        };
```

And using system property:

```
-Dguicey.extensions.debug=true
```

There is also a shortcut for enabling system property:

```java
TestSupport.debugExtensions()
```

## Junit nested classes

Junit natively supports [nested tests](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested).

Guicey extensions affects all nested tests below declaration (nesting level is not limited):

```java
@TestGuiceyApp(AutoScanApplication.class)
public class NestedPropagationTest {

    @Inject
    Environment environment;

    @Test
    void checkInjection() {
        Assertions.assertNotNull(environment);
    }

    @Nested
    class Inner {

        @Inject
        Environment env; // intentionally different name

        @Test
        void checkInjection() {
            Assertions.assertNotNull(env);
        }
    }
}
```

!!! note
    Nested tests will use exactly the same guice context as root test (application started only once).

Extension declared on nested test will affect all sub-tests:

```java
public class NestedTreeTest {

    @TestGuiceyApp(AutoScanApplication.class)
    @Nested
    class Level1 {

        @Inject
        Environment environment;

        @Test
        void checkExtensionApplied() {
            Assertions.assertNotNull(environment);
        }

        @Nested
        class Level2 {
            @Inject
            Environment env;

            @Test
            void checkExtensionApplied() {
                Assertions.assertNotNull(env);
            }

            @Nested
            class Level3 {

                @Inject
                Environment envr;

                @Test
                void checkExtensionApplied() {
                    Assertions.assertNotNull(envr);
                }
            }
        }
    }

    @Nested
    class NotAffected {
        @Inject
        Environment environment;

        @Test
        void extensionNotApplied() {
            Assertions.assertNull(environment);
        }
    }
}
```

This way nested tests allows you to use different extension configurations in one (root) class.

Note that extension declaration with `@RegisterExtension` on the root class field would also
be applied to nested tests. Even declaration in non-static field (start application for each method)
would also work.

### Use interfaces to share tests

This is just a tip on how to execute same test method in different environments.

```java
public class ClientSupportDwTest {

    interface ClientCallTest {
        // test to apply for multiple environments
        @Test
        default void callClient(ClientSupport client) {
            Assertions.assertEquals("main", client.targetMain("servlet")
                    .request().buildGet().invoke().readEntity(String.class));
        }
    }

    @TestDropwizardApp(App.class)
    @Nested
    class DefaultConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertEquals("http://localhost:8080/", client.basePathMain());
        }
    }

    @TestDropwizardApp(value = App.class, configOverride = {
            "server.applicationContextPath: /app",
            "server.adminContextPath: /admin",
    }, restMapping = "api")
    @Nested
    class ChangedDefaultConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertEquals("http://localhost:8080/app/", client.basePathMain());
        }
    }
}
```

Here test declared in `ClientCallTest` interface will be called for each nested test 
(one declaration - two executions in different environments).
 
## Meta annotation

You can prepare meta annotation (possibly combining multiple 3rd party extensions): 

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TestDropwizardApp(AutoScanApplication.class)
public @interface MyApp {
}

@MyApp
public class MetaAnnotationDwTest {

    @Test
    void checkAnnotationRecognized(Application app) {
        Assertions.assertNotNull(app);
    }   
}
```

OR you can simply use base test class and configure annotation there:

```java
@TestDropwizardApp(AutoScanApplication.class)
public class BaseTest {}

public class ActualTest extends BaseTest {} 
```

## Dropwizard startup error

!!! warning
    Tests written in such way CAN'T run in parallel due to `System.*` modifications.
       
To test application startup fails you can use [system stubs](https://github.com/webcompere/system-stubs) library

```groovy
testImplementation 'uk.org.webcompere:system-stubs-jupiter:2.0.1'
```

Testing app startup fail:

```java
@ExtendWith(SystemStubsExtension.class)
public class MyTest {
    @SystemStub
    SystemExit exit;
    @SystemStub
    SystemErr err;
    
    @Test
    public void testStartupError() {
        exit.execute(() -> new App().run('server'));
        
        Assertions.assertEquals(1, exit.getExitCode());
        Assertions.assertTrue(err.getTest().contains("Error message text"));
    }     
}
```

Note that you can also substitute environment variables and system properties and validate output:

```java
@ExtendWith(SystemStubsExtension.class)
public class MyTest {
    @SystemStub
    EnvironmentVariables ENV;
    @SystemStub
    SystemOut out;
    @SystemStub
    SystemProperties propsReset;
    
    @BeforeAll
    public void setup() {
        ENV.set("VAR", "1");
        System.setProperty("foo", "bar"); // OR propsReset.set("foo", "bar") - both works the same
    } 
    
    @Test
    public void test() {
        // here goes some test that requires custom environment and system property values
        
        // validating output
        Assertions.assertTrue(out.getTest().contains("some log message"));
    }
}
```

Pay attention that there is no need for cleanup: system properties and environment variables would be re-set automatically!

!!! note
    Use [test framework-agnostic utilities](general.md) to run application with configuration or to run
    application without web part (for faster test).

## 3rd party extensions integration

It is extremely simple in JUnit 5 to [write extensions](https://junit.org/junit5/docs/current/user-guide/#extensions).
If you do your own extension, you can easily integrate with guicey or dropwizard extensions: there
are special static methods allowing you to obtain main test objects:
   
* `GuiceyExtensionsSupport.lookupSupport(extensionContext)` -> `Optional<DropwizardTestSupport>`
* `GuiceyExtensionsSupport.lookupInjector(extensionContext)` -> `Optional<Injector>`
* `GuiceyExtensionsSupport.lookupClient(extensionContext)` -> `Optional<ClientSupport>`

For example:

```java
public class MyExtension implements BeforeEachCallback {
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Injector injector = GuiceyExtensionsSupport.lookupInjector(context).get();
        ...
    }
}
```

(guicey holds test state in junit test-specific storages and that's why test context is required)

!!! warning
    There is no way in junit to order extensions, so you will have to make sure that your extension
    will be declared after guicey extension (`@TestGuiceyApp` or `@TestDropwizardApp`).

There is intentionally no direct api for applying configuration overrides from
3rd party extensions because it would be not obvious. Instead, you should always 
declare overridden value in extension declaration. Either use instance getter:

```java
@RegisterExtension
static MyExtension ext = new MyExtension()

@RegisterExtension
static TestGuiceyAppExtension dw = TestGuiceyAppExtension.forApp(App.class)
        .configOverride("some.key", ()-> ext.getValue())
        .create()
```

Or store value [inside junit store](#configuration-from-3rd-party-extensions) and then reference it:

```java
@RegisterExtension
static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(App.class)
        .configOverrideByExtension(ExtensionContext.Namespace.GLOBAL, "ext1")
        .create();
```

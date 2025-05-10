# Testing application

Application could be started in 2 ways:

1. Core - create only guice injector (without starting web services) - ideal for business logic testing (pretty fast)
2. Web - full application start to test web endpoints (and complete flows)

The second case is handled by [DropwizardTestSupport](https://www.dropwizard.io/en/release-4.0.x/manual/testing.html#non-junit)
and the first one by `GuiceyTestSupport` object (extending `DropwizardTestSupport`).

There is a generic builder to simplify work with these objects (provides all possible configuration options):

```java
RunResult result = TestSupport.build(App.class)
        .config("src/test/resources/path/to/test/config.yml")
        .configOverrides("foo: 2", "bar: 12")
        .hooks(new MyHook())
        // run lightweight application (without web services)
        .runCore()
```

!!! tip
    `RunResult` contains both `DropwizardTestSupport` used for execution and `Injector` instance.
    In other words, everything required for performing assertions.

or with action:

```java
Object serviceValue = TestSupport.build(App.class)
        .config("src/test/resources/path/to/test/config.yml")
        .configOverrides("foo: 2", "bar: 12")
        .hooks(new MyHook())
        // run full application
        .runWeb(injector -> {
            return injector.getInstance(FooService.class).getSomething();
        })
```

!!! note
    Builder methods are almost equal to [junit 5 extension builder](../junit5/run.md#alternative-declaration)

!!! important
    All run methods declared as `throws Exception`. This was done to bypass original
    exceptions instead of wrapping them inside runtime exceptions.

    This should not be a problem: just add `throws Exception` into test method signature 

## Configuration

Configuration could be applied in a different ways:

```java
// with override values only
TestSupport.build(App.class)
        .configOverride("foo: 12")
    
// file with overrides
TestSupport.build(App.class)
        .config("src/test/resources/path/to/config.yml")
        .configOverride("foo: 12")

// file with config modifier
TestSupport.build(App.class)
        .config("src/test/resources/path/to/config.yml")
        .configModifiers(config -> config.setFoo(12))    
    
// direct config object 
MyConfig config = new MyConfig();         
TestSupport.build(App.class)
        .config(config)
```

!!! note
    Config override (`.configOverride()`) mechanism is provided by dropwizard: values are stored
    as system properties and applied in time of configuration parsing. It might not work for some
    properties (like collections) and it can't be used with manual configuration (last example).

    Config modifier (`.configModifiers()`) is a guicey concept: it could be used with either 
    configuration file or manual configuration object. It was added to simplify config modifications
    and overcome limitations of config override. The only downside: when configuration is parsed
    from file, modifier called after logging configuration, so to modify logging only config overrides
    could be used.


Also, configuration source provider could be modified:

```java
TestSupport.build(App.class)
        .config("path/in/classpath/config.yml")
        .configSourceProvider(new ResourceConfigurationSourceProvider())
```

There are also configuration shortcuts:

```java
TestSupport.build(App.class)
        .randomPorts()
        .restMapping("api")
```

To randomize used application ports (overrides config file values) and apply a different rest mapping path.

When config overrides are used, they are always stored as system properties. This could be a problem
for parallel tests execution. To overcome this, test-unique prefixes could be used:

```java
TestSupport.build(App.class)
        .configOverride("foo", "1")
        .propertyPrefix("something")
```

Junit 5 extensions use test class (and sometimes method) name to generate unique prefixes.

## Lifecycle listeners

Builder also support listeners registration in order to simulate setup/cleanup
lifecycle methods, common for test frameworks:

```java
TestSupport.build(App.class)
        .listen(new TestSupportBuilder.TestListener<>() {
            public void setup(final DropwizardTestSupport<C> support) throws Exception {
                // do before test
            }
            ...
        })
        .runCore();
```

All listener methods are default so only required methods could be overridden.

!!! warning
    Builder could be used for support objects creation (`buildCore()`, `buildWeb()`) -
    in this case listeners could not be used (only builder runner could properly process listeners).

## Shortcuts

For simple cases, there are many builder shortcuts in `TestSupport` class.

Support object construction:

```java
DropTestSupport support = TestSupport.webApp(App.class, 
        "path/to/config.yml", "prop: 1", "prop2: 2");

GuiceyTestSupport support = TestSupport.coreApp(App.class,
        "path/to/config.yml", "prop: 1", "prop2: 2"); 
```

Run:

```java
RunResult result = TestSupport.runWebApp(App.class);

RunResult result = TestSupport.runWebApp(App.class,
        "path/to/config.yml", "prop: 1", "prop2: 2");

Object value = TestSupport.runWebApp(App.class, injector - > {
            return injector.getInstance(Service.class).getSmth();
        });

Object value = TestSupport.runWebApp(App.class,
        "path/to/config.yml", injector - > {
        return injector.getInstance(Service.class).getSmth();
        }, "prop: 1", "prop2: 2");

// ... and same methods for "coreApp"
```

All these methods are builder shortcuts (suitable for simple cases).

!!! tip
    Context `DropwizardTestSupport` and `ClientSupport` objects could be statically referenced inside callback:

    ```java
    TestSupport.runWebApp(App.class, injector - > {
        DropwizardTestSupport support = TestSupport.getContext();
        ClientSupport client = TestSupport.getContextClient();
        return null;
    });
    ```

## Asserting execution

To assert configuration or any guicey bean it would be enough to use run without callback:

```java
RunResult<CfgType> result = TestSupport.build(App.class).runCore();

// direct configuratio instance
Assertions.assertEquals(12, result.getConfiguration().getProp1());
// any guice bean 
Assertions.assertEquals(12, result.getBean(Configuration.class).getProp1());
Assertions.assertNotNull(result.getEnvironment());
Assertions.assertNotNull(result.getApplication());
```

Web-related assertions could be done inside callback:

```java
SomeRsponseObject res = TestSupport.build(App.class).runWeb(injecor -> {
            ClientSupport client = TestSupport.getContextClient()
            return client.get("some", SomeRsponseObject.class);
        });

Assertions.assertEquals("something", res.getField1())
```

Or multiple assertions could be done directly inside callback.

## Managed lifecycle

Core tests (without web part) simulate `Managed` objects lifecycle (because often core logic
rely on pre-initializations).

If managed lifecycle is note required for test, it could be disabled with alternative run method:

```java
TestSupport.build(App.class).runCoreWithoutManaged(..)
```

On raw test support object: `new GuiceyTestSupport().disableManagedLifecycle()`

## Raw test support objects

It may be required to use `DropwizardTestSupport` objects directly: for example, when before() and after()
calls must be performed in different methods (some test framework integration).

Objects could be created with builder:

```java
GuiceyTestSupport core = TestSupport.build(App.class)
        .buildCore();

DropwizardTestSupport web = TestSupport.build(App.class)
        .buildWeb();
```

!!! note
    `GuiceyTestSupport extends DropwizardTestSupport`, so in both cases
    `DropwizardTestSupport` could be used as type.

There are also shortcut methods:

```java
DropwizardTestSupport supportCore = TestSupport.coreApp(App.class,
        "path/to/config.yml", 
        "prop: 1", "prop2: 2");

DropwizardTestSupport supportWeb = TestSupport.webApp(App.class,
        "path/to/config.yml",
        "prop: 1", "prop2: 2");
```

Support object usage:

```java
support.before()

// test

support.after()
```

This is equivalent to:

```java
TestSupport.run(support, injector -> {
    // test
});
```

Other helper methods for support object (executed while the support object is active):

* `TestSupport.getInjector(support)` - obtain application injector
* `TestSupport.getBean(support, Key/Class)` - get guice bean
* `TestSupport.injectBeans(support, target)` - inject annotated object fields
* `TestSupport.webClient(support)` - construct `ClientSupport` object

Support object provides references for dropwizard objects:

```java
support.getEnvironment();
support.getConfiguration();
support.getApplication();
```

Complete example using junit:

```java
public class RawTest {
    
    static DropwizardTestSupport support;
    
    @Inject MyService service;
    
    @BeforeAll
    public static void setup() {
        support = TestSupport.coreApp(App.class);
        // support = TestSupport.webApp(App.class);
        // start app
        support.before();
    }
    
    @BeforeEach
    public void before() {
        // inject services in test
        TestSupport.injectBeans(support, this);
    }
    
    @AfterAll
    public static void cleanup() {
        if (support != null) {
            support.after();
        }
    }
    
    @Test
    public void test() {
        Assertions.assertEquals("10", service.computeValue());
    }
}
```

!!! note
    `support.before()` would automatically call `after()` in case of startup error

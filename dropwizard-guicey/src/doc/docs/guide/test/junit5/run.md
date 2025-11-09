# Testing application

Guicey provides two junit extensions:

* [@TestGuiceyApp](#testing-core-logic) - for lightweight tests (without starting web part, only guice context)
* [@TestDropwizardApp](#testing-web-logic) - for complete integration tests

!!! note ""
    `@TestGuiceyApp` assumed to be used for the majority of tests as it only starts guice injector
    (which is much faster than complete application startup). Such tests are ideal for testing 
      business logic (services).
    
    `@TestDropwizardApp` (full integration test) used only to check web endpoints and full workflow
    (assuming all business logic was already tested with lightweight tests)

Both extensions:

* [Inject guice beans](inject.md) directly in test fields.
* Support [method parameters injection](inject.md#parameter-injection)
* Support [hooks](hooks.md) and [setup objects](setup-object.md) for test configuration
* Support [alternative declaration](#alternative-declaration) for [deferred configuration](#deferred-configuration)
  or [starting application for each test method](#start-application-by-test-method). 
* Provide pre-configured [http client](client.md) might be used for calling test application endpoints (or external).
* Support junit [parallel execution](#parallel-execution) (no side effects).


Field annotations:

* `@EnableHook` - [hooks](hooks.md#hook-fields) registration
* `@EnableSetup` - [setup objects](setup-object.md#setup-fields) registration
* `@StubBean` - guice bean [stubs](stubs.md) registration
* `@MockBean` - guice bean [mocks](mocks.md) registration (mockito)
* `@SpyBean` - guice bean [spies](spies.md) registration (mockito)
* `@TrackBean` - guice beans [execution tracking](tracks.md) (simpler then mockito spies; suitable for performance testing) 
* `@StubRest` - lightweight [REST testing](rest.md)
* `@RecordLogs` - [logs testing](logs.md)

Method parameter annotations:

* `@Jit` - for not declared [guice beans injection](inject.md#parameter-injection)
    
## Testing core logic

`@TestGuiceyApp` creates guice injector (runs all application services) without starting jetty (so resources, servlets and filters will not be available).
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

Also, [injections](inject.md) work as [method parameters](inject.md#parameter-injection):

```java
@TestGuiceyApp(MyApplication.class)
public class AutoScanModeTest {
    
    public void testMyService(MyService service) {        
        Assertions.assertEquals("hello", service.getSmth());     
    }
```

Application started before all tests in annotated class and stopped after them.

### Annotation options

| Option          | Description                                                                 | Default                    |
|-----------------|-----------------------------------------------------------------------------|----------------------------|
| config          | Configuration file path                                                     | ""                         |
| configOverride  | Configuration file overriding values                                        | {}                         |
| configModifiers | Configuration object modifier                                               | {}                         |
| hooks           | [Hooks](hooks.md) to apply                                      | {}                         |
| setup           | Setup objects to apply                                                      | {}                         |
| injectOnce      | Inject test fields just one for multiple test methods with one test instance | false                      |
| debug           | Enable extension debug output                                               | false                      |
| reuseApplication | Use the same application instance for multiple tests                        | false                      |
| useDefaultExtensions | Use default guicey field extensions                                         | true                       |
| clientFactory | Custom client factory to use                                                | `DefaultTestClientFactory` |
| managedLifecycle | Managed beans lifecycle simulation                                          | true                       |

### Managed lifecycle

Core application tests (`@TestGuiceyApp`) does not start web part and so lifecycle should not work,
but `Managed` objects often used to initialize core services.   

Guicey core test simulate `Managed` lifecycle (call start and stop methods).
For tests, not requiring lifecycle at all, it might be disabled with:

```java
@TestGuiceyApp(value = App.class, managedLifecycle = false)
```

or

```java
@RegisterExtension
static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(..)
        ...
        .disableManagedLifecycle()
```

!!! note
    Application lifecycle will remain: events like `onApplicationStartup` would still be
    working (and all registered `LifeCycle` objects would work). Only managed objects ignored.

### Inject test fields once

By default, guicey would inject test field values before every test method, even if the same
test instance used (`TestInstance.Lifecycle.PER_CLASS`). This should not be a problem
in the majority of cases because guice injection takes very little time.
Also, it is important for prototype beans, which will be refreshed for each test.

But it is possible to inject fields just once:

```java
@TestGuiceyApp(value = App.class, injectOnce = true)
// by default new test instance used for each method, so injectOnce option would be useless 
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PerClassInjectOnceGuiceyTest {
    @Inject
    Bean bean;
    
    @Test
    public test1() {..}

    @Test
    public test2() {..}
}
```

In this case, the same test instance used for both methods (`Lifecycle.PER_CLASS`)
and `Bean bean` field would be injected just once (`injectOnce = true`)

!!! tip
    To check the actual fields injection time enable debug (`debug = true`) and
    it will [print injection time](debug.md#startup-performance) before each test method:
    ```
    [Before each]                      : 2.05 ms
        Guice fields injection             : 1.58 ms    
    ```

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
            client.targetApp("servlet").request().buildGet().invoke().readEntity(String.class));
        
        Assertions.assertTrur(service.isCalled());
```

`@TestDropwizardApp` contains the same [annotation options](#annotation-options) as core test,
but without lifecycle simulation (lifecycle managed by started server).

### Random ports

In order to start application on random port you can use configuration shortcut:

```groovy
@TestDropwizardApp(value = MyApplication.class, randomPorts = true)
```

!!! note
    Random ports setting override exact ports in configuration:
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

### Start application per test method

When you declare extensions with annotations or with `@RegisterExtension` in static fields,
application would be started before all test methods and shut down after last test method.

If you want to start application *for each test method* then declare extension in non-static field:

```java
@RegisterExtension
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

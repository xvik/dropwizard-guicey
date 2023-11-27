# General test tools

Test framework-agnostic tools. 
Useful when:

 - There are no extensions for your test framework
 - Assertions must be performed after test app shutdown (or before startup)
 - Commands testing

Test utils:

 - `TestSupport` - root utilities class, providing easy access to other helpers
 - `DropwizardTestSupport` - [dropwizard native support](https://www.dropwizard.io/en/release-4.0.x/manual/testing.html#non-junit) for full integration tests
 - `GuiceyTestSupport` - guice context-only integration tests (without starting web part)
 - `CommandTestSupport` - general commands tests 
 - `ClientSupport` - web client helper (useful for calling application urls)

!!! important
    `TestSupport` assumed to be used as a universal shortcut: everything could be created/executed through it
    so just type `TestSupport.` and look available methods - *no need to remember other classes*. 

## Application run

Application could be started in 2 ways:

1. Core - create only guice injector (without starting web services) - ideal for business logic testing (pretty fast)
2. Web - full application start to test web endpoints (and complete flows) 

The second case is handled by [DropwizardTestSupport](https://www.dropwizard.io/en/release-4.0.x/manual/testing.html#non-junit)
and the first one by `GuiceyTestSupport` object (which is an extension for `DropwizardTestSupport`). 

There is a generic builder to simplify work with these objects (provides all possible options for configuring these objects):

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
    Builder methods are almost equal to [junit 5 extension builder](junit5.md#alternative-declaration)

!!! important
    All run methods declared as `throws Exception`. This was done to bypass original
    exceptions instead of wrapping them inside runtime exceptions.

    This should not be a problem: just add `throws Exception` into test method signature 

### Configuration

Configuration could be applied in a different ways: 

```java
// with override values only
TestSupport.build(App.class)
        .configOverride("foo: 12")

// file with overrides
TestSupport.build(App.class)
        .config("src/test/resources/path/to/config.yml")
        .configOverride("foo: 12")

// direct config object 
MyConfig config = new MyConfig();         
TestSupport.build(App.class)
        .config(config)
```

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

### Lifecycle listeners

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

### Shortcuts

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
I will use the builder call below, but it is equivalent to these shortcut methods
(they just appeared before builder and were preserved for compatibility). 

!!! tip
    Context `DropwizardTestSupport` and `ClientSupport` objects could be statically referenced inside callback:
    
    ```java
    TestSupport.runWebApp(App.class, injector - > {
        DropwizardTestSupport support = TestSupport.getContext();
        ClientSupport client = TestSupport.getContextClient();
        return null;
    });
    ```

### Asserting execution

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

## Raw test support objects

It may be required to use `DropwizardTestSuppor` objects directly: for example, when before() and after()
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

## Client

`ClientSupport` is a [JerseyClient](https://eclipse-ee4j.github.io/jersey.github.io/documentation/2.29.1/client.html)
aware of dropwizard configuration, so you can easily call admin/main/rest urls.

Creation:

```java
ClientSupport client = TestSupport.webClient(support);
```

where support is `DropwizardTestSupport` or `GuiceyTestSupport` (in later case it could be used only as generic client for calling external urls).

Example usage:

```java
// GET {rest path}/some
client.targetRest("some").request().buildGet().invoke()

// GET {main context path}/servlet
client.targetMain("servlet").request().buildGet().invoke()

// GET {admin context path}/adminServlet
client.targetAdmin("adminServlet").request().buildGet().invoke()

// General external url call
client.target("https://google.com").request().buildGet().invoke()
```

!!! tip
    All methods above accepts any number of strings which would be automatically combined into correct path:
    ```groovy
    client.targetRest("some", "other/", "/part")
    ```
    would be correctly combined as "/some/other/part/"

As you can see, test code is abstracted from actual configuration: it may be default or simple server
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
client.basePathRoot()   // root server path (http://localhost:8080/)
client.basePathMain()   // main context path (http://localhost:8080/)
client.basePathAdmin()  // admin context path (http://localhost:8081/)
client.basePathRest()   // rest context path (http://localhost:8080/)
```

### Simple REST methods

The client also contains simplified GET/POST/PUT/DELETE methods for path, relative to server root (everything after port):


```java
@Test
public void testWeb(ClientSupport client) {
    // get with result
    Result res = client.get("rest/sample", Result.class);
    
    // post without result (void)
    client.post("rest/action", new PostObject(), null);

   // post with result 
   Result res = client.post("rest/action", new PostObject(), Result.class);
}
```

All methods:

1. Methods accept paths relative to server root. In the example above: "http://localhost:8080/rest/sample"
2. Could return mapped response.
3. For void calls, use null instead of the result type. In this case, only 200 and 204 (no content) responses
   would be considered successful

POST and PUT also accept (body) object to send.
But methods does not allow multipart execution.

!!! tip
    These methods could be used as examples for jersey client usage.

### Customization

`JerseyClient` used in `ClientSupport` could be customized now using `TestClientFactory` implementation.

Simple factory example:

```java
public class SimpleTestClientFactory implements TestClientFactory {

    @Override
    public JerseyClient create(final DropwizardTestSupport<?> support) {
        return new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .build();
    }
}
```

!!! tip
    See `DefaultTestClientFactory` implementation (it's a bit more complex)

Custom factory could be specified directly in test annotation (junit 5, spock 2):

```java
@TestDropwizardApp(value = MyApp.class, clientFactory = CustomTestClientFactory.class)
```

All other builders also support client factory as an optional parameter.


### Default client

`JerseyClient` used inside `ClientSupport` is created by `DefaultTestClientFactory`.

Default implementation:

1. Enables multipart feature if `dropwizard-forms` is in classpath (so the client could be used
for sending multipart data).
2. Enables request and response logging to simplify writing (and debugging) tests.

By default, all request and response messages are written directly into console to guarantee client
actions visibility (logging might not be configured in tests).

Example output:

```

[Client action]---------------------------------------------{
1 * Sending client request on thread main
1 > GET http://localhost:8080/sample/get

}----------------------------------------------------------


[Client action]---------------------------------------------{
1 * Client response received on thread main
1 < 200
1 < Content-Length: 13
1 < Content-Type: application/json
1 < Date: Mon, 27 Nov 2023 10:00:40 GMT
1 < Vary: Accept-Encoding
{"foo":"get"}

}----------------------------------------------------------
```

Console output might be disabled with a system proprty:

```java
// shortcut sets DefaultTestClientFactory.USE_LOGGER property
DefaultTestClientFactory.disableConsoleLog()
```

With it, everything would be logged into `ClientSupport` logger (java.util) under INFO
(most likely, would be invisible in the most logger configurations, but could be enabled).


To reset property (and get logs back into console) use:

```java
DefaultTestClientFactory.enableConsoleLog()
```

!!! note
    Static methods added not directly into `ClientSupport` because this is
    the default client factory feature. You might use a completely different factory.


## Capture console output

There is now a utility to capture console output:

```java
String out = TestSupport.captureOutput(() -> {
        
    // run application inside
    TestSupport.runWebApp(App.class, injector -> {
        ClientSupport client = TestSupport.getContextClient();
        
        // call application api endpoint
        client.get("sample/get", null);

        return null;
    });
});

// uses assert4j, test that client was called (just an example) 
Assertions.assertThat(out)
    .contains("[Client action]---------------------------------------------{");
```

Returned output contains both `System.out` and `System.err` - same as it would be seen in console.

All output is also printed into console to simplify visual validation

!!! warning
    Such tests could not be run in parallel (due to system io overrides)

## Test commands

`CommandTestSupport` object is a commands test utility equivalent to `DropwizardTestSupport`.
It uses dropwizard `Cli` for arguments recognition and command selection.

The main difference with `DropwizardTestSupport` is that command execution is
a short-lived process and all assertions are possible only *after* the execution.
That's why command runner would include in the result all possible dropwizard objects,
created during execution (because it would be impossible to reference them after execution).

New builder (almost the same as application execution builder) simplify commands execution:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("simple", "-u", "user")

Assertions.assertTrue(result.isSuccessful());
```

This runner could be used to run *any* command type (simple, configured, environment).
The type of command would define what objects would be present ofter the command execution
(for example, `Injector` would be available only for `EnvironmentCommand`).

!!! important
    Such run *never fails* with an exception: any appeared exception would be
    stored inside the response:

    ```java
    Assertions.assertFalse(result.isSuccessful());  
    Assertions.assertEquals("Error message", result.getException().getMessage());
    ```

### IO

Runner use System.in/err/out replacement. All output is intercepted and could be
asserted:

```java
Assertions.assertTrue(result.getOutput().contains("some text"))
```

`result.getOutput()` contains both `out` and `err` streams together
(the same way as user would see it in console). Error output is also available
separately with `result.getErrorOutput()`.

!!! note
    All output is always printed to console, so you could always see it after test execution
    (without additional actions)

Commands requiring user input could also be tested (with mocked input):

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .consoleInputs("1", "two", "something else")
        .run("quiz")
```

At least, the required number of answers must be provided (otherwise error would be thrown,
indicating not enough inputs)

!!! warning
    Due to IO overrides, command tests could not run in parallel.   
    For junit 5, such tests could be annotated with [`@Isolated`](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution-synchronization)
    (to prevent execution in parallel with other tests)

### Configuration

Configuration options are the same as in run builder. For example:

```java
// override only
TestSupport.buildCommandRunner(App.class)
        .configOverride("foo: 12")
        .run("cfg");

// file with overrides
TestSupport.buildCommandRunner(App.class)
        .config("src/test/resources/path/to/config.yml")
        .configOverride("foo: 12")
        .run("cfg");

// direct config object
MyConfig config = new MyConfig();         
TestSupport.buildCommandRunner(App.class)
        .config(config)
        .run("cfg");
```

!!! note
    Config file should not be specified in command itself - builder would add it, if required.  
    But still, it would not be a mistake to use config file directly in command:

    ```java
    TestSupport.buildCommandRunner(App.class)
        // note .config("...") was not used (otherwise two files would appear)!
        .run("cfg", "path/to/config.yml");
    ```

    Using builder for config file configuration assumed to be a preferred way.

### Listener

There is a simple listener support (like in application run builder) for setup-cleanup actions:

```java
TestSupport.buildCommandRunner(App.class)
        .listen(new CommandRunBuilder.CommandListener<>() {
            public void setup(String[] args) { ... }
            public void cleanup(CommandResult<TestConfiguration> result) { ... }
        })
        .run("cmd")
```

## Test application startup fail

Command runner could also be used for application startup fail tests:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("server")
```

or with the shortcut:

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .runApp()
```

!!! note
    In case of application *successful* start, special check would immediately stop it
    by throwing exception (resulting object would contain it), so such test would never freeze.

No additional mocks or extensions required because running like this would not cause
`System.exist(1)` call, performed in `Application` class (see `Application.onFatalError`).

# General test tools

Test framework-agnostic tools. 
Useful when:

 - There is no extensions for your test framework
 - Assertions must be performed after test app shutdown (or before startup)

Test utils:

 - `TestSupport` - root utilities class, providing easy access to other helpers
 - `DropwizardTestSupport` - [dropwizard native support](https://www.dropwizard.io/en/release-2.0.x/manual/testing.html#non-junit) for full integration tests
 - `GuiceyTestSupport` - guice context only integration tests (without starting web part)
 - `ClientSupport` - web client helper (useful for calling application urls)

!!! important
    `TestSupport` assumed to be used as a universal shortcut: everything could be created/executed through it
    so just type `TestSupport.` and look available methods - no need to remember other classes. 

## Web app

Core [DropwizardTestSupport](https://www.dropwizard.io/en/release-2.0.x/manual/testing.html#non-junit) class
used to run complete application for testing:

```java
DropwizardTestSupport support = new DropwizardTestSupport(App.class, "path/to/test-config.yml");
// start
support.before();

// helpers
support.getEnvironment();
support.getConfiguration();
support.getApplication();

// stop
support.after();
```

Provides two lifecycle methods: `before()` and `after()` and utilities to access context environment and configuration objects.

See constructor for advanced configuration options.

## Core app

`GuiceyTestSupport` is an inheritor of `DropwizardTestSupport` (could be casted) starting only 
guice context without web part. Provides additional utility methods:

```java
GuiceyTestSupport support = new GuiceyTestSupport(App.class, "path/to/test-config.yml");
// start
support.before();

// additional method
support.getBean(Key/Class);

// stop
support.after();
```

Also provide shortcut `.run(callback)` method as an alternative to manual `before()` and `after()` calls.

## Client

`ClientSupport` is a [JerseyClient](https://eclipse-ee4j.github.io/jersey.github.io/documentation/2.29.1/client.html)
aware of dropwizard configuration, so you can easily call admin/main/rest urls.

Creation:

```java
ClientSupport client = new ClientSupport(support);
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
client.basePathMain()   // main context path (http://localhost:8080/)
client.basePathAdmin()  // admin context path (http://localhost:8081/)
client.basePathRest()   // rest context path (http://localhost:8080/)
```

## TestSupport

`TestSupport` class simplifies usage of all test utilities: it is the only class you'll need
to remember - all other utilities could be created (or found) through it. 

Methods inside it follow convention:

- `webApp` - complete application (`DropwizardTestSupport`)
- `coreApp` - guice-only part (`GuiceyTestSupport`)

Methods:

- `runWebApp(Class, String, Callback?)`
- `runCoreApp(Class, String, Callback?)`
- `webApp(Class, String)`
- `coreAoo(Class, String)`
- `webClient(support)`
- `getInjector(support)`
- `getBean(support, Key/Class)`
- `injectBeans(support, target)`
- `run(support, Callback)`

where `support` is an instance of `DropwizardTestSupport` (or `GuiceyTestSupport`).

### Simple run

If application must be just started and stopped (e.g. to test startup errors):

```java
TestSupport.runWebApp(App.class, "path/to/test-config.yml");
```

or without configuration:

```java
TestSupport.runWebApp(App.class, null);
```

More advanced version, using callback:  

```java
TestSupport.runWebApp(App.class, "path/to/test-config.yml", (injector) -> {
        injector.getInstance(MyService.class).doSomething();
});
```

Here we get injector object inside callback and can call any guice service and perform any assertions while application runs.
You can also use it to return a value:

```java
String value = TestSupport.runWebApp(App.class, "path/to/test-config.yml", (injector) -> {
        return injector.getInstance(MyService.class).computeValue()
});
```

All the same is available for lightweight guice-only testing:

```java
TestSupport.runCoreApp(App.class, "path/to/test-config.yml", (injector) -> {
    injector.getInstance(MyService.class).doSomething();
});
```

### Advanced usage

For more advanced usage, you'll need to construct `DropwizardTestSupport` or `GuiceyTestSupport` objects first:

```java
DropwizardTestSupport support = TestSupport.webApp(App.class, "path/to/test-config.yml");
```

or

```java
GuiceyTestSupport support = TestSupport.coreApp(App.class, "path/to/test-config.yml");
```

As before, configuration could be null:

```java
DropwizardTestSupport support = TestSupport.webApp(App.class, null);
```

!!! note
    This construction is suitable for the simplest cases, but you can always create
    `DropwizardTestSupport` object manually.
 
    Utility call just hides easy to forget no-config instantiation:

    ```java
    new DropwizardTestSupport(App.class, (String) null);
    ```

Instead of manual lifecycle methods call you can use:

```java
TestSupport.run(support, (injector) -> {
     // do somthing while app started
})
```

Inside callback the following shortcuts could be used:

- `TestSupport.getInjector(support)`
- `TestSupport.getBean(support, Key/Class)`
- `TestSupport.injectBeans(support, target)` 

Complete example using junit:

```java
public class RawTest {
    
    static DropwizardTestSupport support;
    
    @Inject MyService service;
    
    @BeforeAll
    static void setup() {
        support = TestSupport.coreApp(App.class);
        // support = TestSupport.webApp(App.class);
        // start app
        support.before();
    }
    
    @BeforeEach
    void before() {
        // inject services in test
        TestSupport.injectBeans(support, this);
    }
    
    @AfterAll
    static void cleanup() {
        if (support != null) {
            support.after();
        }
    }
    
    @Test
    void test() {
        Assertions.assertEquals("10", service.computeValue());
    }
}
```

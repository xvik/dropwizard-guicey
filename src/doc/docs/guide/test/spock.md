# Spock

[Spock framework](http://spockframework.org) allows you to write much clearer tests (comparing to junit) thanks to 
groovy language.

You will need the following dependencies (assuming BOM used for versions management):

```groovy
testImplementation 'io.dropwizard:dropwizard-testing'

testImplementation 'org.objenesis:objenesis:3.1'
testImplementation 'cglib:cglib-nodep:3.3.0'
testImplementation 'org.spockframework:spock-core'
```

OR you can use it with junit 5 vintage engine:

```groovy
testImplementation 'io.dropwizard:dropwizard-testing'

testImplementation 'org.objenesis:objenesis:3.1'
testImplementation 'cglib:cglib-nodep:3.3.0'
testImplementation 'org.spockframework:spock-core'

testImplementation 'org.junit.jupiter:junit-jupiter-api'
testRuntimeOnly 'org.junit.jupiter:junit-jupiter'
testRuntimeOnly 'org.junit.vintage:junit-vintage-engine'
```

This way you can write both spock (groovy) and junit 5 (java or groovy) tests.

## Extensions

Provided extensions:

* `@UseGuiceyApp` - for lightweight tests (without starting web part, only guice context)
* `@UseDropwizardApp` - for complete integration tests

Both extensions allow using injections directly in specifications (like spock-guice).

`@UseGuiceyHooks` extension could be used to apply [configuration hook](../hooks.md) 
common for all tests. But, it is deprecated in favor of native hooks support in main extensions.

!!! note
    Spock and junit5 extensions are almost equivalent in features and behaviour. Differences:
    
    * Obviously different extension annotations, but completely the same attributes and behaviour
    * Spock does not support test method parameters injections, so everything injected with fields
      (including [ClientSupport](#client) object).  
    * Config override syntax (spock extension use legacy syntax, which will change in the next breaking release)

## @UseGuiceyApp

`@UseGuiceyApp` runs all guice logic without starting jetty (so resources, servlets and filters will not be available).
`Managed` objects will still be handled correctly.

```groovy
@UseGuiceyApp(MyApplication)
class AutoScanModeTest extends Specification {

    @Inject MyService service

    def "My service test" {
    
        when: 'calling service'
        def res = service.getSmth()

        then: 'correct result returned'
        res == 'hello'
    }
```

Application started before all tests in annotated class and stopped after them.

## @UseDropwizardApp

`@UseDropwizardApp` is useful for complete integration testing (when web part is required):

```groovy
@UseDropwizardApp(MyApplication)
class WebModuleTest extends Specification {

    @Inject MyService service

    def "Check web bindings"() {

        when: "calling filter"
        def res = new URL("http://localhost:8080/dummyFilter").getText()

        then: "filter active"
        res == 'Sample filter and service called'
        service.isCalled()
```

### Random ports

In order to start application on random port you can use configuration shortcut:

```groovy
@UseDropwizardApp(value = MyApplication, randomPorts = true)
```

!!! note
    Random ports will be applied even if configuration with exact ports provided:
    ```groovy
    @UseDropwizardApp(value = MyApplication, 
                      config = 'path/to/my/config.yml', 
                      randomPorts = true)
    ```
    Also, random ports support both server types (default and simple)
    

Real ports could be resolved with [ClientSupport](#client) object.

### Rest mapping

Normally, rest mapping configured with `server.rootMapping=/something/*` configuration, but
if you don't use custom configuration class, but still want to re-map rest, shortcut could be used:

```groovy
@UseDropwizardApp(value = MyApplication, restMapping="something")
```

In contrast to config declaration, attribute value may not start with '/' and end with '/*' -
it would be appended automatically. 

This option is only intended to simplify cases when custom configuration file is not yet used in tests
(usually early PoC phase). It allows you to map servlet into application root in test (because rest is no
more resides in root). When used with existing configuration file, this parameter will override file definition.

## Guice injections

Any gucie bean may be injected directly into test field:

```groovy
@Inject
SomeBean bean
```

This may be even bean not declared in guice modules (JIT injection will occur).

To better understand injection scopes look the following test:

```groovy
@UseGuiceyApp(AutoScanApplication)
class InjectionTest extends Specification {

    // instance remain the same between tests
    @Shared @Inject TestBean sharedBean

    // new instance injected on each test
    @Inject TestBean bean

    // the same context used for all tests (in class), so the same bean instance inserted before each test
    @Inject TestSingletonBean singletonBean

    def "Check injection types"() {
        when: "changing state of injected beans"
        sharedBean.value = 10
        bean.value = 5
        singletonBean.value = 15

        then: "instances are different"
        sharedBean.value == 10
        bean.value == 5
        singletonBean.value == 15

    }

    def "Check shared state"() {

        expect: "shared bean instance is the same, whereas other one re-injected"
        sharedBean.value == 10
        bean.value == 0
        singletonBean.value == 15 // the same instance was set before second test
    }

    // bean is in prototype scope
    static class TestBean {
        int value
    }

    @Singleton
    static class TestSingletonBean {
        int value
    }
}
```

!!! note
    Guice AOP will not work on test methods (because test instances not created by guice).

## Client

Both extensions prepare special jersey client instance which could be used for web calls.
It is mostly useful for complete web tests to call rest services and servlets.

```groovy
@InjectClient
ClientSupport client
```

It will also work in static fields or `@Shared` fields.

Client object provides:

* Access to [JerseyClient](https://eclipse-ee4j.github.io/jersey.github.io/documentation/2.29.1/client.html) object (for raw calls)
* Shortcuts for querying main, admin or rest contexts (it will count the current configuration automatically)
* Shortcuts for base main, admin or rest contexts base urls (and application ports)

Example usages:

```groovy
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

```groovy
when: "calling rest service"
def res = client.targetRest("some").request().buildGet().invoke()

then: "response is correct"
res.status == 200
res.readEntity(String) == "response text"
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

```groovy
// call completely external url
client.target("http://somedomain:8080/dummy/").request().buildGet().invoke()
```

!!! warning 
    Client object could be injected with both dropwizard and guicey extensions, but in case of guicey extension,
    only raw client could be used (because web part not started all other methods will throw NPE)

## Configuration

For both extensions you can configure application with external configuration file:

```groovy
@UseGuiceyApp(value = MyApplication,
    config = 'path/to/my/config.yml'
class ConfigOverrideTest extends Specification {
```

Or just declare required values:

```groovy
@UseGuiceyApp(value = MyApplication,
    configOverride = [
            @ConfigOverride(key = "foo", value = "2"),
            @ConfigOverride(key = "bar", value = "12")
    ])
class ConfigOverrideTest extends Specification {
```

Or use both at once (here overrides will override file values):

```groovy
@UseGuiceyApp(value = MyApplication,
    config = 'path/to/my/config.yml',
    configOverride = [
            @ConfigOverride(key = "foo", value = "2"),
            @ConfigOverride(key = "bar", value = "12")
    ])
class ConfigOverrideTest extends Specification {
```

## Application test modification

You can use [hooks to customize application](overview.md#configuration-hooks).

In both extensions annotation hooks could be declared with attribute:

```java
@UseDropwizardApp(value = MyApplication, hooks = MyHook)
```

or

```java
@UseGuiceyApp(value = MyApplication, hooks = MyHook)
```

Where MyHook is:

```java
class MyHook implements GuiceyConfigurationHook {}
```

### Hook fields

Alternatively, you can declare hook directly in test static field:

```groovy
@EnableHook
static GuiceyConfigurationHook HOOK = { it.modules(new DebugModule()) }
```

Any number of fields could be declared. The same way hook could be declared in base test class:

```groovy
class BaseTest extends Specification {
    
    // hook in base class
    @EnableHook
    static GuiceyConfigurationHook BASE_HOOK = { it.modules(new DebugModule()) }
}

@UseGuiceyApp(value = App, hooks = SomeOtherHook)
class SomeTest extends BaseTest {
    
    // Another hook
    @EnableHook
    static GuiceyConfigurationHook HOOK = { it.modules(new DebugModule2()) }
}
```

All 3 hooks will work.

### Hooks extension

!!! warning
    This extension is deprecated in favour of field hooks declarations. 

```groovy
@UseGuiceyHooks(MyBaseHook)
class BaseTest extends Specification {
    
}

@UseGuiceyApp(App)
class SomeTest extends BaseTest {}
``` 

!!! note
    You **can still use** test specific hooks together with declared base hook
    (to apply some more test-specific configuration).

!!! warning
    Only one `@UseGuiceyHooks` declaration may be used in test hierarchy:
    for example, you can't declare it in base class and then another one on extended class
    - base for a group of tests. This is spock limitation (only one extension will actually work)
    but should not be an issue for most cases.

## Extension configuration unification

It is a common need to run multiple tests with the same test application configuration
(same config overrides, same hooks etc.).
Do not configure it in each test, instead move extension configuration into base test class:

```groovy
@UsetGuiceyApp(...)
abstract class AbstractTest extends Specification {
    // here might be helper methods
}
```

And now all test classes should simply extend it:

```groovy
class Test1 extends AbstractTest {
    
    @Inject
    MyService service
            
    def "Check something"() { ... }
}
```

## Dropwizard startup error

!!! warning
    StartupErrorRule is deprecated, but as spock still depends on junit4 (waiting for spock2)
    there are no alternatives.   

`StartupErrorRule` may be used to intercept dropwizard `#!java System.exit(1)` call.
But it will work different then for junit:
`then` section is always called with exception (`CheckExitCalled`). 
Also, `then` section may be used for assertion after exit calls and so there is 
no need to add custom assertion callbacks (required by junit tests).

```groovy
class ErrorTest extends Specification {

    @Rule StartupErrorRule RULE = StartupErrorRule.create()

    def "Check startup error"() {

        when: "starting app with error"
        new MyErrApp().main(['server'])
        
        then: "startup failed"
        thrown(RULE.indicatorExceptionType)
        RULE.output.contains('stating application')
        RULE.error.contains('some error occur')
```

## Spock lifecycle hooks

```groovy
class MyTest extends Specification {
    
    @ClassRule @Shared
    JunitRule sharedRule = new JunitRule()
    
    @Rule
    JunitRule2 rule = new JunitRule2()
    
    def setupSpec() {
    }
    
    def setup() {
    }
    
    def "Test method body" () {
        setup:
    }
}
```

!!! note ""
    Class rules are applied once per test class (same as `setupSpec`).
    Rules are applied per test method (same as `setup`).

Setup order:

* Class rule
* Setup spec method
* Rule
* Setup method
* Test method's setup section

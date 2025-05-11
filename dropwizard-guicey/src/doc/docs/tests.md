# Testing

Core [dropwziard testing support](https://www.dropwizard.io/en/stable/manual/testing.html)
proposes atomic testing approach (separate testing of each element, which you still could use when possible).

With DI (guice) we have to move towards **integration testing** because:

1. It is now harder to mock classes "manually" (because of DI "black box")
2. We have a core (guice injector, without web services), starting much faster than
   complete application.

## Guicey tests

Guicey intended to shine in integration tests: it provides [a lot of tools](guide/test/overview.md) for application modification.

The most important is [hooks mechanism](guide/hooks.md) which allows you to re-configure
existing application. There are two main testing approaches:

* Disable everything not required and register custom versions **instead**
* **Override** some bindings (pure guice `Modules.override()` method)

### Disable and replace

Suppose we have the following application:

```java 
public class App extends Application<MyConfig> {
    public void initialize(Bootstrap<MyConfig> bootstrap) {
        bootstrap.addBundle(GuiceBundle.builder()
            .bundles(new SecurityOnlyBundle())
            .modules(new ServiceModule(), new DaoModule())
            .build()
    }
}
```

Specifically for tests we create special module `MockDaoModule` which applies
all the same bindings, but with mock implementations.

Just for demonstration, suppose that application registers `SecurityOnlyBundle` which
do nothing except of additional security. Suppose we don't need this in tests.

```java
@TestGuiceyApp(App.class)
public class MyTest {   
    
    @EnableHook
    static GuiceyConfigurationHook HOOK = builder -> 
                                builder.disableBundles(SecurityOnlyBundle.class)
                                       .disableModules(DaoModule.class)
                                       .modules(new MockDaoModule());                
}
```

Here hook applied to:

- remove `SecurityOnlyBundle`
- remove `DaoModule`
- add `MockDaoModule`

This way you can [disable everything](guide/disables.md): module, extensions, guicey and
dropwizard bundles and installers.

!!! note
    Bundles (both guice and dropwizard) and guice modules are actually hierarchical (one bundle/module can register other bundle/module)
    and you can disable even exact bundle/module inside this hierarchy (not just directly registered).
    See more: about [guice transitive bundles](guide/guice/module-analysis.md#transitive-modules)
    and [dropwizard transitive bundles](guide/bundles.md#transitive-bundles-tracking)

All disables are shown on [diagnostic report](guide/diagnostic/configuration-report.md) - you can use it to verify 
configuration state. 
    
### Override bindings

We can do the same without replacing module, but [overriding bindings](guide/guice/override.md) using guice
`Modules.override()` feature. This is preferred in cases when modules are not so well
structured and you need to override just a subset of bindings (not all bindings in module).

Above example would look like:

```java
@TestDropwizardApp(App.class)
public class MyTest {    
    
    @EnableHook
    static GuiceyConfigurationHook HOOK = builder -> 
                                builder.disableBundles(SecurityOnlyBundle.class)                                       
                                       .modulesOverride(new MockDaoModule());       
}
```  

In the previous example all bindings from `DaoModule` were removed and here we just register
[overriding bindings](guide/guice/override.md) so bindings from `MockDaoModule` will be used
instead of (the same) bindings from `DaoModule`.

!!! note
    All overrides are visible on [guice report](guide/diagnostic/guice-report.md) - use it to verify
    override correctness.

### Configuration

For tests you can use custom configuration file (e.g. `src/test/resources/test-config.yml`).

```java
@TestDropwizardApp(value = MyApp.class, conifg="src/test/resources/test-config.yml")
``` 

Or just override exact values (without declaring config file):

```java
@TestDropwizardApp(value = MyApp.class, conifgOverride = "server.applicationConnectors[0].port: 0") 
```
    
## Lightweight tests

In many cases, you don't need the entire application, but just a working `Injector` to check core application logic.

For such cases, guicey provides lightweight extensions like [@TestGuiceyApp](guide/test/junit5/run.md#testing-core-logic):

- will not start jetty (no ports bind, no HK2 launched)
- start `Managed` objects to simulate lifecycle

These tests work *much* faster!     

```java
@TestGuiceyApp(App.class)
public class MyTest {    
    
    @EnableHook
    static GuiceyConfigurationHook HOOK = builder -> 
                            builder.disableBundles(SecurityOnlyBundle.class)                                       
                                   .modulesOverride(new MockDaoModule());    
    
    @Test
    public void test(MyService service) {
         service.doSomething();
         ...  
    }       
}
```

!!! tip
    There is also a special [lightweight REST](guide/test/junit5/rest.md) tests support to 
    avoid starting entire web server. 

## Spock

All examples above was for junit 5, but you can also use groovy-based [Spock framework 2](http://spockframework.org/). 
Spock tests are much easier to write (you can write less code) and more expressive. You'll use the same Junit 5 rules
(instead of writing custom spock extensions, junit 5 compatibility layer was [implemented](https://github.com/xvik/spock-junit5), 
so you can use any junit 5 extensions with spock).

See [this article](https://blog.vyarus.ru/using-junit-5-extensions-in-spock-2-tests#heading-why-spock) for spock usage motivation.

For example, the first junit example will look like in spock:

```groovy       
@TestDropwizardApp(App)
class MyTest extends Specification {    
    
    @EnableHook
    static GuiceyConfigurationHook HOOK = { it.disableBundles(SecurityOnlyBundle)
                                            .disableModules(DaoModule)
                                            .modules(new MockDaoModule()) }

    @Inject
    MyService service

    def 'Check service method'() {
    
        when: 'calling service method'
        def res = service.doSoomething()

        then: 'value is correct'
        res == 12
    }      
}
```

See [Spock 2 docs](guide/test/spock2.md) for more details.

## Testing commands

Guicey also provides special support for [testing dropwizard commands](guide/test/general/command.md):

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .run("simple", "-u", "user")

Assertions.assertTrue(result.isSuccessful());
```

* Such run never fails (in case of error exception would be inside result object)
* Result countain all required objects for assertions and contains 
* Full console output is accessible for assertions
* Could mock user input (for commands requiring interaction)

Also commands could be used to check application failures on startup (self-checks testing):

```java
CommandResult result = TestSupport.buildCommandRunner(App.class)
        .runApp()
```

Such test would fail in case of successful application start.
No additional mocks or extensions required because running like this would not cause
`System.exist(1)` call, performed in `Application` class (see `Application.onFatalError`).
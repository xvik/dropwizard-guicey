# Testing

You can use all existing [dropwizard arsenal](https://www.dropwizard.io/en/stable/manual/testing.html) for unit tests.

## Theory

Guicey provides [a lot of tools](guide/test.md) for writing integration tests.

The most important is [hooks mechanism](guide/hooks.md) which allows you to re-configure
existing application. There are two main testing approaches:

* Disable everything not required and register custom versions **instead**
* Override some bindings (pure guice `Modules.override()` method)

!!! warning
    Guicey currently provides Junit 4 and spock extensions. Junit 5 support will be added soon.

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
public class MyTest {    
    
    // NOTE! no rule marker here!
    static DropwizardAppRule<TestConfiguration> RULE = new DropwizardAppRule<>(App.class); 

    @ClassRule 
    static RuleChain chain = RuleChain
       .outerRule(new GuiceyHooksRule((builder) -> 
                                builder.disableBundles(SecurityOnlyBundle.class)
                                       .disableModules(DaoModule.class)
                                       .modules(new MockDaoModule())))
       .around(RULE);   
    
}
```

Here [dropwizard rule](https://www.dropwizard.io/en/stable/manual/testing.html#integration-testing) used for
application startup and [hooks rule](guide/test.md#customizing-guicey-configuration)
used **around** dropwizard rule to re-configure application:

- remove SecurityOnlyBundle
- remove DaoModule
- add DaoModule

Note that this way you can [disable everything](guide/disables.md): module, extensions, guicey and
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
public class MyTest {    
    
    // NOTE! no rule marker here!
    static DropwizardAppRule<TestConfiguration> RULE = new DropwizardAppRule<>(App.class); 

    @ClassRule 
    static RuleChain chain = RuleChain
       .outerRule(new GuiceyHooksRule((builder) -> 
                                builder.disableBundles(SecurityOnlyBundle.class)                                       
                                       .modulesOverride(new MockDaoModule())))
       .around(RULE);   
    
}
```  

In the previous example all bindings from `DaoModule` were removed and here we just register
[overriding bindings](guide/guice/override.md) so bindings from `MockDaoModule` will be used
instead of (the same) bindings from `DaoModule`.

!!! note
    All overrides are visible on [guice report](guide/diagnostic/guice-report.md) - use it to verify
    override correctness.

### Configuration

!!! note ""
    Pure dropwizard staff. Just to pay attention.

For tests use custom configuration file (e.g. `src/test/resources/test-config.yml`).

```java
@ClassRule
static DropwizardAppRule<TestConfiguration> RULE =
        new DropwizardAppRule<>(MyApp.class, ResourceHelpers.resourceFilePath("test-config.yaml"));
``` 

Override exact value:

```java
 static DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardTestSupport<>(MyApp.class,
                null,  // main config may or may not be declared
                ConfigOverride.config("server.applicationConnectors[0].port", "0") 
            );
```

## Access guice beans

When using `DropwizardAppRule` the only way to obtain guice managed beans is through:

```java
InjectorLookup.getInjector(RULE.getApplication()).getBean(MyService.class);
```
    
## Lightweight tests

`DropwizardAppRule` runs complete application, including web context (requires open ports).
But in many cases, you just need a working `Injector` to check core application logic.

For such cases, guicey provides lightweight rule [GuiceyAppRule](guide/test.md#testing-core-logic). In contrast
to `DropwizardAppRule` it:

- will not start jetty (no ports bind, no HK2 launched)
- start `Managed` objects to simulate lifecycle

These tests work *much*-faster!     

```java
public class MyTest {    
    
    // NOTE! no rule marker here!
    static GuiceyAppRule<TestConfiguration> RULE = new GuiceyAppRule<>(App.class); 

    @ClassRule 
    static RuleChain chain = RuleChain
       .outerRule(new GuiceyHooksRule((builder) -> 
                                builder.disableBundles(SecurityOnlyBundle.class)                                       
                                       .modulesOverride(new MockDaoModule())))
       .around(RULE);   

    
    @Test
    public void test() {
         RULE.getBean(MyService.class).doSomething();
         ...  
    }       
}
```

Note that `GuiceyAppRule` provides direct accessor for guice beans. 

## Startup fail tests

If you test all cases, even crashing application startup (e.g. due to mis-configuration)
then use special [startup errors rule](guide/test.md#dropwizard-startup-error) which intercepts
`System.exit(1)` call performed by dropwizard, allowing you to assert failure.

## Spock

You can use groovy-based [Spock framework](http://spockframework.org/) instead of junit. 
Spock tests are much easier to write (with you can write less code) and resulted tests are more expressive.

Guicey provides all [required extensions](guide/test.md#spock) to write tests above with spock.
For example, the first example will look like:

```groovy       
@UseDropwizardApp(App)
@UseGuiceyHooks(Hook)
class MyTest extends Specification {    
    
    @Inject
    MyService service

    def 'Check service method'() {
    
        when: 'calling service method'
        def res = service.doSoomething()
        then: 'value is correct'
        res == 12
    }
       
    // spock 2.0 will support java 8 lambdas (through just released groovy 3)
    // untill then using class-based declaration      
    static class Hook implements GuiceyConfigurationHook {
        void configure(GuiceBundle.Builder builder) {
            builder.disableBundles(SecurityOnlyBundle.class)
                   .disableModules(DaoModule.class)
                   .modules(new MockDaoModule())            
        }
    }    
}
```    

Note that in spock tests you can use injection of beans directly.

Lightweight guicey tests are available through [@UseGuiceyApp](guide/test.md#useguiceyapp) annotation.

Overriding configuration properties:

```groovy
@UseDropwizardApp(value = App,
        config = 'src/test/resources/test-config.yml',
        configOverride = [
                @ConfigOverride(key = "foo", value = "2"),
                @ConfigOverride(key = "bar", value = "12")
        ])
class DWConfigOverrideTest extends Specification { ... }
```

!!! hint
    All guicey tests written in spock, so you can [see them](https://github.com/xvik/dropwizard-guicey/tree/master/src/test/groovy/ru/vyarus/dropwizard/guice) 
    and decide what framework is better fits your needs.  
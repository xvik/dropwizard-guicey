# Testing

Test support require `io.dropwizard:dropwizard-testing:1.0.6` dependency.

## Junit

### Testing core logic

For integration testing of guice specific logic you can use `GuiceyAppRule`. It works almost like 
[DropwizardAppRule](http://www.dropwizard.io/1.0.6/docs/manual/testing.html),
but doesn't start jetty (and so jersey and guice web modules will not be initialized). 
Managed and lifecycle objects supported.

```java
public class MyTest {

    @Rule
    GuiceyAppRule<MyConfiguration> RULE = new GuiceyAppRule<>(MyApplication.class, "path/to/configuration.yaml")
    
    public void testSomething() {
        RULE.getBean(MyService.class).doSomething();
        ...
    }
}
```

As with dropwizard rule, configuration is optional

```java
new GuiceyAppRule<>(MyApplication.class, null)
```

### Testing web logic

For web component tests (servlets, filters, resources) use 
[DropwizardAppRule](http://www.dropwizard.io/1.0.6/docs/manual/testing.html).

To access guice beans use injector lookup:

```java
InjectorLookup.getInjector(RULE.getApplication()).getBean(MyService.class);
```

## Spock

If you use [spock framework](http://spockframework.org) you can use spock specific extensions:

* `@UseGuiceyApp` - internally use `GuiceyAppRule`
* `@UseDropwizardApp` - internally use `DropwizardAppRule`

Both extensions allows using injections directly in specifications (like spock-guice).

### @UseGuiceyApp

`@UseGuiceyApp` runs all guice logic without starting jetty (so resources, servlets and filters are not available).
Managed objects are handled correctly.

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

Annotation allows you to configure the same things as rules does: application class, configuration file (optional),
configuration overrides.

```groovy
@UseGuiceyApp(value = MyApplication,
    config = 'path/to/my/config.yml',
    configOverride = [
            @ConfigOverride(key = "foo", value = "2"),
            @ConfigOverride(key = "bar", value = "12")
    ])
class ConfigOverrideTest extends Specification {
```

As with rules, `configOverride` may be used without setting config file (simply to fill some configurations)

### @UseDropwizardApp

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

Annotation supports the same configuration options as `@UseGuiceyApp` (see above)

### Spock extensions details

Extensions follow spock-guice style - application started once for all tests in class. It's the same as using rule with
`@ClassRule` annotation. Rules may be used with spock too (the same way as in junit), but don't mix them with
annotation extensions.

There are two limitations comparing to rules:

* Application can't be created for each test separately (like with `@Rule` annotation). This is because of `@Shared` instances support.
* You can't customize application creation: application class must have no-args constructor (with rules you can extend rule class
and override `newApplication` method). But this should be rare requirement.

!!! note ""
    All guicey tests use spock, so you can see extensions usage examples there.

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

## Overriding beans

There is no direct support for bean overrides yet (it is planned), but you can use the following approach.

First, prepare custom [injector factory](injector.md#injector-factory):

```java
public class CustomInjectorFactory implements InjectorFactory {
     private static ThreadLocal<Modules[]> modules = new ThreadLocal<>();

     @Override
     public Injector createInjector(Stage stage, Iterable<Module> modules) {
          Modules[] customModules = modules.get();
          modules.remove();
          return Guice.createInjector(stage, customModules == null ? modules 
                               : Lists.newArrayList(Modules.override(modules).with(customModules)) ) 
     }

     public static void override(Module... modules) {
          customModules.set(modules);
     }
     
     public static void clear() {
         customModules.remove();
     }
}
```

It allows using guice `Module.overrides()` for bindings substitution.

Register factory in guice bundle:

```java
GuiceBundle.builder()
    .injectorFactory(new CustomInjectorFactory())
```

Guice Module.overrides forcefully override (substitute) all bindings from override modules.
So all modules in your application remain the same but you can create new test modules
with test specific replacements.

For example, suppose we have some service `CustomerService` and it's implementation, defined in appllication
`CustomerServiceImpl`. To override it we prepare new test module:

```java
public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CustomerService.class).to(TestCustomerServiceImpl.class);
    }
}
```

Now register overriding module:

```java
CustomInjectorFactory.override(new TestModule());
```

!!! warning
    Overriding modules registration must be performed before guicey start.
    
 After startup, application will use overridden customer service version.

Of course this approach is far from ideal, but it's the best option for now.

!!! note
    Thread local is used to hold overriding modules to allow concurrent tests run.
    This mean you have to manually clean state after each test to avoid side effects: 
    `#!java CustomInjectorFactory.clear()` (of course, if overriding is not used for all tests)
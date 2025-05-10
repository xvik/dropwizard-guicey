# Testing with stubs

Stubs are hand-made replacements of real application services ("manual" or "lazy" [mocks](mocks.md)).

Stubs declared in test class with a new `@StubBean` annotation.

!!! warning
    Stubs will not work for HK2 beans

There are two main cases:

1. Stub class extends existing service: `class ServiceStub extends Service`
2. Stub implements service interface: `class ServiceStub implements IService`

Stubs replace real application services (using guice [overriding modules](../overview.md#guice-bindings-override)),
so stub would be injected in all services instead of the real service.

For example, suppose we have a service:

```java
public class Service {
    public String foo() {
        ...
    }
}
```

where method foo implements some complex logic, not required in test.

Writing stub:

```java
public class ServiceStub extends Service {
    @Override
    public String foo() {
        return "static value";
    }
}
```

Using stub in test:

```java
@TestGuiceyApp(App.class)
public class Test {
    
    @StubBean(Service.class)
    ServiceStub stub;
    
    // injecting here to show that stub replaced real service
    @Inject
    Service service;
    
    @Test
    public void test(){
        // service is a stub
        Assertions.assertInstanceOf(ServiceStub.class, service);
        Assertions.assertEquals("static value", service.foo());
    }
}
```

!!! info
    In many cases, mockito [mocks](mocks.md) and [spies](spies.md) could be more useful,
    but stubs are simpler (easier to understand, especially comparing to spies).

In the example above, stub instance is created by guice. 
Stub could also be registered by instance:

```java
@StubBean(Service.class)
ServiceStub stub = new ServiceStub();
```

In this case, stub's `@Inject` fields would be processed (`requestInjection(stub)` would be called).

!!! note
    When stub is registered with instance, stub field must be static for per-test application run
    (default annotation). It may not be static for per-method application startup (with `@RegisterExtension`).

!!! note
    Guice AOP would apply only for stubs registered with class. So stub instance
    could be used (instead of class) exactly to avoid additional AOP logic for service.

## Stub lifecycle

More complex stubs may contain a test-related internal state, which must be cleared between tests.

In this case, stub could implement `StubLifecycle`:

```java
public class ServiceStub extends Service implements StubLifecycle {
    int calls;
    
    @Override 
    public void before() {
        calls = 0; 
    }

    @Override
    public void after() {
        calls = 0;
    }
}
```

(both methods optional)

Such methods would be called automatically before and after of each test method.

## Debug

When extension debug is active:

```java
@TestGucieyApp(value = App.class, debug = true)
public class Test 
```

All recognized stub fields would be logged:

```
Applied stubs (@StubBean) on StubsSimpleTest:

	StubsSimpleTest.stub2                    GUICE                  Service2 >> Service2Stub        
	StubsSimpleTest.stub                     GUICE                  Service1 >> Service1Stub 
```

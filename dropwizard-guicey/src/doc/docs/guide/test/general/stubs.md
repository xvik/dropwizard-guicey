# Testing with stubs

Stubs are hand-made replacements of real application services ("manual" or "lazy" [mocks](mocks.md)).

Guicey provides `StubsHook` for overriding guice beans with stub implementations.

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
StubsHook hook = new StubsHook();
// register stub (stub instance managed with guice)
hook.stub(Service.class, ServiceStub.class);

TestsSupport.build(App.class)
        .hooks(hook)
        .runCore(injector -> {
            Service service = injector.getInstance(Service.class);
            // service is a stub
            Assertions.assertInstanceOf(ServiceStub.class, service);
            Assertions.assertEquals("static value", service.foo());
        });
```

!!! info
    In many cases, mockito [mocks](mocks.md) and [spies](spies.md) could be more useful,
    but stubs are simpler (easier to understand, especially comparing to spies).

In the example above, stub instance is created by guice.
Stub could also be registered by instance:

```java
hook.stub(Service.class, new ServiceStub());
```

In this case, stub's `@Inject` fields would be processed (`requestInjection(stub)` would be called).

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

Then we can call these methods with hook:

```java

// call before() on all stubs
hook.before();
// test staff here
// call after after test
hook.after();
```

## Stub instance 

Stub instance could be obtained either from injector (using overridden service as a key):

```java
ServiceStub stub = (ServiceStub) injector.getInstance(Service.class);
```

or directly from hook:

```java
ServiceStub stub = hook.getStub(Service.class);
```
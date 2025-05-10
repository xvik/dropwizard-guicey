# Testing with spies

[Mockito](https://site.mockito.org/) spies allows dynamic modification of real objects behavior
(configured same as [mocks](mocks.md), but, by default, all methods work as in raw bean).

Guicey provides `SpiesHook` for overriding guice beans with mockito spies.

!!! important
    Spy creation requires real bean instance and so guicey use AOP to intercept real bean
    access and redirecting all calls through a dynamically created (on first access)
    spy object. This means that spies would only work with guice-managed beans.

    If you need to spy for a manual instance - use [partial mocks](mocks.md#partial-mocks)

!!! warning
    Spies will not work for HK2 beans

Mockito documentation is written in the `Mockito` class [javadoc](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html).  
Additional docs could be found in [mockito wiki](https://github.com/mockito/mockito/wiki/FAQ)  
Also, see official [mockito refcard](https://dzone.com/refcardz/mockito)
and [baeldung guides](https://www.baeldung.com/mockito-series).

## Setup

Requires mockito dependency (version may be omitted if dropwizard BOM used):

```groovy
testImplementation 'org.mockito:mockito-core'
```

## Usage

Suppose we have a service:

```java
public static class Service {

    public String get(int id) {
        return "Hello " + id;
    }
}
```

Spying it:

```java
SpiesHook hook = new SpiesHook();
// real spy could be created ONLY after injector startup
final SpyProxy proxy = hook.spy(Service.class);
// SpyProxy implements provider and so could be also used as: 
// Provider<Service> provider = hook.spy(Service.class)

TestSupport.build(App.class)
        .hooks(hook)
        .runCore(injector -> {
            // get spy object for configuration
            Service spy = proxy.getSpy();
            // IMPORTANT: spies configured in reverse order to avoid accidental method call            
            doReturn("bar1").when(spy).get(11);

            // real instance, injected everywhere in application (AOP proxy)
            Service service = injector.getInstance(Service.class);
            // stubbed result
            Assertions.assertEquals("bar1", s1.get(11));
            // real method result (because argument is different)
            Assertions.assertEquals("Hello 10", s1.get(10));
        })
```

Here `doReturn` refer to `Mockito.doReturn` used with static import.

!!! note
    As real guice bean used under the hood, all AOP, applied to the original bean, will work.

!!! tip
    Calling guice proxy `injector.getInstance(Service.class).get(11)` and spy object
    directly `spy.get(11)` is equivalent (because guice returns AOP proxy which redirects
    call to the spy)

See other examples in [mocks section](mocks.md#mocking-examples).

## Asserting calls

!!! tip
    If you want to use spies to track bean access (verify arguments and response) then
    try [trackers](tracks.md) which are better match for this case.

As [mocks](mocks.md#asserting-calls), spies could be used to assert calls:

```java
// method Service.get(11) called on mock just once
verify(spy, times(1)).get(11);
```

These assertions would fail if method was called more times or using different arguments.

## Method result capture

Verifying method return value with spies is a bit clumsy:

```java
public static class ResultCaptor<T> implements Answer {
    private T result = null;
    public T getResult() {
        return result;
    }

    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        result = (T) invocationOnMock.callRealMethod();
        return result;
    }
}

ResultCaptor<String> resultCaptor = new ResultCaptor<>();
// capture actual argument value (just to show how to do it)
ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
Mockito.doAnswer(resultCaptor).when(spy).get(argumentCaptor.capture());

// call method
Assertions.assertThat(spy.get(11)).isEqualTo("bar");
// result captured
Assertions.assertThat(resultCaptor.getResult()).isEqualTo("bar");
Assertions.assertThat(argumentCaptor.getValue()).isEqualTo(11);

Mockito.verify(spy, Mockito.times(1)).get(11);
```

Why would you need that? It is often useful when verifying indirect bean call.
For example, if we have `SuperService` which internally calls `Service` and so 
there is no other way to verify service call result correctness other than spying it (or use [tracker](tracks.md)).

## Pre initialization

As spy object creation is delayed until application startup, it is impossible to
configure spy before application startup (as with mocks). Usually it is not a problem,
if target bean is not called during startup.

If you need to modify behavior of spy, used during application startup (e.g. by some `Managed`),
then there is a delayed initialization mechanism:

```java
SpiesHook hook = new SpiesHook();
// real spy could be created ONLY after injector startup
final SpyProxy proxy = hook.spy(Service.class)
        .withInitializer(service -> doReturn("spied").when(service).get(11));
...
```

Here, configuration from `withInitializer` block would be called just after 
spy creation (on first access).

And so any `Managed`, calling it during startup would use completely configured spy:

```java
@Singleton
public static class Mng implements Managed {
    @Inject
    Service service;

    @Override
    public void start() throws Exception {
        // "spied" result
        service1.get(11);
    }
}
```

## Spies reset

If you run multiple tests with the same application, then it makes sense to re-configure
spies for each test and so the previous spy state must be reset.

Use `hook.resetSpies()` to reset all registered mocks

## Accessing spy

Spy instance (used to configure methods behavior) could be obtained:

1. After application startup:
    ```java
    SpyProxy proxy = hook.spy(Service.class);
    // after app startup  
    Service spy = proxy.getSpy();
    ```
3. From hook: `Service spy = hook.getSpy(Service.class)`
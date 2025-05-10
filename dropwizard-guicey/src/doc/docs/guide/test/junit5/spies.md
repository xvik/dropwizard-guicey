# Testing with spies
                
[Mockito](https://site.mockito.org/) spies allows dynamic modification of real objects behavior
(configured same as [mocks](mocks.md), but, by default, all methods work as in raw bean).

Spies declared with a `@SpyBean` annotation.

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
@TestGuiceyApp(App.class)
public class Test {
    
    @SpyBean
    Service spy;
    
    // NOT the same instance as spy (but calls on both objects are equivalent)
    @Inject
    Service service;
    
    @BeforeEach
    public void setUp() {
        // IMPORTANT: spies configured in reverse order to avoid accidental method call            
        doReturn("bar1").when(spy).get(11);
    }
    
    @Test
    public void test() {
        // stubbed result
        Assertions.assertEquals("bar1", s1.get(11));
        // real method result (because argument is different)
        Assertions.assertEquals("Hello 10", s1.get(10)); 
    }
}
```

Here `doReturn` refer to `Mockito.doReturn` used with static import.

!!! note
    As real guice bean used under the hood, all AOP, applied to the original bean, will work.

!!! tip
    Calling guice proxy `service.get(11)` and spy object
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

@TestGuiceyApp(App.class)
public class Test {
    ResultCaptor<String> resultCaptor = new ResultCaptor<>();
    // capture actual argument value (just to show how to do it)
    ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
    
    @SpyBean 
    Service spy;
    
    @BeforeAll
    public void setUp() {
        doAnswer(resultCaptor).when(spy).get(argumentCaptor.capture());
    }
    
    public void test() {
        // call method
        Assertions.assertThat(spy.get(11)).isEqualTo("bar");
        // result captured
        Assertions.assertThat(resultCaptor.getResult()).isEqualTo("bar");
        Assertions.assertThat(argumentCaptor.getValue()).isEqualTo(11);

        verify(spy, Mockito.times(1)).get(11);
    }
}
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
// extra class required to overcome annotation limitation
public class Initializer implements Consumer<Service> {
    // real spy could be created ONLY after injector startup
    @Override
    public void accept(Service spy) {
        doReturn("spied").when(service).get(11); 
    }
    
}

@TestGuiceyApp(App.class)
public class Test {
    
    @SpyBean(initializers = Initializer.class)
    Service spy;
    
    ...
}
```

Here, `Initializer` would be called just after spy creation (on first access).

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

Spies are re-set automatically after each test method (and that's why it makes
sense to declare mock behavior in test setup method - execured before each test method).

!!! note
    Spy could be reset manually at any time with `Mockito.reset(spy)`

Spies automatic reset could be disabled with `autoReset` option:

```java
@SpyBean(autoReset = false)
Service spy;
```

## Spies report

Same as for mocks, a usage report could be printed after each test `@SpyBean(printSummary = true)`

```
\\\------------------------------------------------------------/ test instance = 285bf5ac /
@SpyBean stats on [After each] for SpySummaryTest$Test1#test():

	[Mockito] Interactions of: ru.vyarus.dropwizard.guice.test.jupiter.setup.spy.SpySummaryTest$Service$$EnhancerByGuice$$60e90c@40fe8fd5
	 1. spySummaryTest$Service$$EnhancerByGuice$$60e90c.foo(
	    1
	);
	  -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.spy.SpySummaryTest$Test1.test(SpySummaryTest.java:50)
```

## Debug

When extension debug is active:

```java
@TestGucieyApp(value = App.class, debug = true)
public class Test 
```

All recognized spy fields would be logged:

```
Applied spies (@SpyBean) on SpySimpleTest:

	#spy2                          Service2                     (r.v.d.g.t.j.s.s.SpySimpleTest) 
	#spy1                          Service1                     (r.v.d.g.t.j.s.s.SpySimpleTest) 
```
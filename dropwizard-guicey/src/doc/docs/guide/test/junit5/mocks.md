# Testing with mocks
 
[Mockito](https://site.mockito.org/) mocks are essentially an automatic [stubs](stubs.md):
with the ability to dynamically declare method behavior (by default, all mock methods 
return default value: often null). 

Mocks declared with a `@MockBean` annotation.

!!! warning
    Stubs will not work for HK2 beans     

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

!!! note "Remember"
    * Do not mock types you don’t own
    * Don’t mock value objects
    * Don’t mock everything
    * Show love with your tests!

    [source](https://site.mockito.org/#more), [explanations](https://github.com/mockito/mockito/wiki/How-to-write-good-tests)

For example, suppose we have a service:

```java
public class Service {
    public String foo() {
        ...
    }
}
```

where method foo implements some complex logic, not required in test.

To override service with a mock:

```java
@TestGuiceyApp(App.class)
public class Test {

    // register mock (mock would be created automatically using Mockito.mock(Service.class)
    @MockBean
    Service mock;

    // injecting here to show that mock replaced real service
    @Inject
    Service service;
    
    @BeforeEach
    public void setUp() {
        // declaring behaviour
        when(mock.foo()).thenReturn("static value");
    }
    
    @Test
    public void test() {
        // mock instance instead of service
        Assertions.assertEquals(mock, service);
        // method overridden            
        Assertions.assertEquals("static value", service.foo());
    }
}
```

Here `when` refer to `Mockito.when()` used with static import.

!!! important 
    Guice AOP would not be applied to mocks (only guice-managed beans support AOP)

You can also provide a pre-created mock instance (useful if mock used during application startup or partial mocks):

```java
@MockBean
static Service mock = createMock();
```

!!! note
    When mock is registered with instance, mock field must be static for per-test application run
    (default annotation). It may not be static for per-method application startup (with `@RegisterExtension`).

## Mocking examples

Mocking answers for different arguments:

```java
when(mock.foo(10)).thenReturn(100);
when(mock.foo(20)).thenReturn(200);
when(mock.foo(30)).thenReturn(300);
```

Different method answers (for consequent calls):

```java
when(mock.foo(anyInt())).thenReturn(10, 20, 30);
```

Using actual argument in mock:

```java
 when(mock.getValue(anyInt())).thenAnswer(invocation -> {
        int argument = (int) invocation.getArguments()[0];
        int result;
        switch (argument) {
        case 10:
            result = 100;
            break;
        case 20:
            result = 200;
            break;
        case 30:
            result = 300;
            break;
        default:
            result = 0;
        }
        return result;
    });
```

## Asserting calls

Mock could also be used for calls verification:

```java
// method Service.foo() called on mock just once
verify(mock, times(1)).foo();
// method Service.bar(12) called just once (with exact argument value)
verify(mock, times(1)).bar(12);
```

These assertions would fail if method was called more times or using different arguments.

## Mock reset

Mocks are re-set automatically after each test method (and that's why it makes 
sense to declare mock behavior in test setup method - execured before each test method).

!!! note
    Mock could be reset manually at any time with `Mockito.reset(mock)` 

Mocks automatic reset could be disabled with `autoReset` option:

```java
@MockBean(autoReset = false)
Service mock;
```

## Partial mocks

If mock is applied for a class with implemented methods, these methods would
still be overridden with fake implementations. If you want to preserve this logic, then
use spies:

```java
public class AbstractService implements IService {
    public abstract String bar();
    
    public String foo() {
        return "value";
    }
}

@TestGuiceyApp(App.class)
public class Test {
    
    @MockBean
    static IService mock = Mockito.spy(AbstractService.class);
    
    @Inject 
    IService service;
    
    @Test
    public void test() {
        // default mock implementation for abstract method
        Assertions.assertNull(service.bar());
        // implemented method preserved
        Assertions.assertEquals("value", service.foo());
    }
}
```

!!! note
    The [spies](spies.md) section covers only spies, spying on real guice bean instance.
    Using spies for partial mocks is more related to pure mocking and so it's described here.

## Mocks report

Mockito provides a mock usage report (`Mockito.mockingDetails(value).printInvocations()`),
which could be enabled with `@MockBean(printSummary = true)` (report shown after each test method):

```
\\\------------------------------------------------------------/ test instance = 6d420cdd /
@MockBean stats on [After each] for MockSummaryTest$Test1#test():

	[Mockito] Interactions of: Mock for Service, hashCode: 1340267778
	 1. service.foo(1);
	  -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.mock.MockSummaryTest$Test1.test(MockSummaryTest.java:55)
	   - stubbed -> at ru.vyarus.dropwizard.guice.test.jupiter.setup.mock.MockSummaryTest$Test1.setUp(MockSummaryTest.java:50)
```

## Debug

When extension debug is active:

```java
@TestGucieyApp(value = App.class, debug = true)
public class Test 
```

All recognized mock fields would be logged:

```
Applied mocks (@MockBean) on MockSimpleTest:

	#mock2                         Service2                     (r.v.d.g.t.j.s.m.MockSimpleTest)  AUTO
	#mock1                         Service1                     (r.v.d.g.t.j.s.m.MockSimpleTest)  AUTO
```

## Mocking OpenAPI client

If you use some external API with a client, generated from openapi (swagger) declaration,
then you should be using it in code like this:

```java

@Inject
SomeApi api;

public void foo() {
    Some response = api.someGetCall(...)
}
```

Where `SomeApi` is a generated client class.

Usually, the simplest way is to record real service response (using swagger UI or other generated documentation)
or simply enabling client debug in the application (so all requests and responses would be logged).

Store such responses as json files in test resources: e.g. `src/test/resources/responses/someGet.json`

Now mocking `SomeApi` and configure it to return object, mapped from json file content, instead of the real call:

```java
@TestGuiceyApp(App.class)
public class Test {
    @MockBean
    SomeApi mock;

    // injecting here to show that mock replaced real service
    @Inject
    SomeService service;
    
    @Inject
    Environment environment;

    @BeforeEach
    public void setUp() throws Exception {
        // usually better than new ObjectMapper() (already pre-configured with extensions)
        ObjectMapper mapper = environment.getObjectMapper();
        when(mock.someGetCall(...)).thenReturn(mapper.readValue(
                new File("src/test/resources/responses/someGet.json"), Some.class));
    }

    @Test
    public void test() {
        // call some service using api internally (mock removes external call)
        service.doSomething();
    }
}
```

With it, object, mapped from json file, would be returned on service call, instead of
the real api.

!!! note
    In the example, direct file access used instead of classpath lookup because
    IDEA by default does not copy `.json` resources (it must be additionally configured) and
    so direct file access is more universal.
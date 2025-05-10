# Testing with mocks

[Mockito](https://site.mockito.org/) mocks are essentially an automatic [stubs](stubs.md):
with the ability to dynamically declare method behavior (by default, all mock methods 
return default value: often null). 

Guicey provides `MocksHook` for overriding guice beans with mockito mocks.

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
MocksHook hook = new MocksHook();
// register mock (mock would be created automatically using Mockito.mock(Service.class)
Service mock = hook.mock(Service.class);
// define method result
when(mock.foo()).thenReturn("static value");

TestsSupport.build(App.class)
        .hooks(hook)
        .runCore(injector -> {
            Service service = injector.getInstance(Service.class);

            // mock instance instead of service
            Assertions.assertEquals(mock, service);
            // method overridden            
            Assertions.assertEquals("static value", service.foo());
        });
```

Here `when` refer to `Mockito.when()` used with static import.

!!! important 
    Guice AOP would not be applied to mocks (only guice-managed beans support AOP)


You can also provide a pre-created mock instance (this does not make much sence, but possible):

```java
hook.mock(Service.class, mockInstance);
```

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

If you run multiple tests with the same application, then it makes sense to re-configure
mocks for each test and so the previous mock state must be reset.

Use `hook.resetMocks()` to reset all registered mocks

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

AbstractService mock = Mockito.spy(AbstractService.class);
hook.mock(IService.class, mock);


IService service = injector.getInstance(IService.class);
// default mock implementation for abstract method
Assertions.assertNull(service.bar());
// implemented method preserved
Assertions.assertEquals("value", service.foo());
```

!!! note
    The [spies](spies.md) section covers only spies, spying on real guice bean instance.
    Using spies for partial mocks is more related to pure mocking and so it's described here.

## Accessing mock

Mock instance (used to configure methods behavior) could be obtained:

1. On registration (`Service mock = hook.mock(Service.class)`)
2. From guice injector: `Service mock = injector.getInstance(Service.class)`
   (as hook is registered by instance, guice AOP could not be applied for it and so it 
   would always be a raw mock)
3. From hook: `Service mock = hook.getMock(Service.class)`

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
MocksHook hook = new MocksHook();
Service mock = hook.mock(SomeApi.class);
ObjectMapper mapper = new ObjectMapper();
// define method result
when(mock.someGetCall(...)).thenReturn(mapper.readValue(
        new File("src/test/resources/responses/someGet.json"), Some.class));
```

With it, object, mapped from json file, would be returned on service call, instead of
the real api.

!!! note
    In the example, direct file access used instead of classpath lookup because
    IDEA by default does not copy `.json` resources (it must be additionally configured) and
    so direct file access is more universal.
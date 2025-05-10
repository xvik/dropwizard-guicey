# Application modification

You can use [hooks to customize application](../overview.md#configuration-hooks).

!!! note
    Hook provides the same methods as the main `GuiceBundle.builder()` and
    so it could easily re-configure application (change options, add or disable modules, 
    enable reports, etc.)

In both extension annotations, hooks could be declared with attribute:

```java
@TestDropwizardApp(value = MyApplication.class, hooks = MyHook.class)
```

or

```java
@TestGuiceyApp(value = MyApplication.class, hooks = MyHook.class)
```

Where MyHook is:

```java
public class MyHook implements GuiceyConfigurationHook {
    @Override
    public void configure(GuiceBundle.Builder builder) throws Exception {
        
    }
}
```

Many test extensions could be written with hooks. For example, to implement deep mocks
support we can write hook like this:

```java
public class MockApiHook implements GuiceyConfigurationHook {
    private final Class<?>[] classes;
    
    public MockApiHook(final Class<?>... classes) {
        this.classes = classes;
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        builder.modulesOverride(binder -> {
            for (Class<?> clazz : classes) {
                bind(binder, clazz, Mockito.mock(clazz));
            }
        });
    }
}
```

Usage:

```java
// mocks created and registered as overriding original services 
@EnableHook
static MockApiHook mocks = new MockApiHook(SomeService.class, SomeOtherService.class);

@Inject
SomeService service;

@BeforeEach
public void setUp() {
    // In case of multiple test methods  Mockito.reset(service) required
    // Would work correctly only if AOP not used for service
    Mockito.when(service.getFoo()).thenReturn("12");
}

@test
public void test() {
     Assertions.assertEquals("12", service.getFoo());
}
```

!!! important
    This is just a simple example (not counting possible AOP usage) - just to show how hooks 
    could be used. Mocks support is already implemented: see `@MockBean` extension.

## Hook fields

Alternatively, you can declare hook directly in test field:

```java
@EnableHook
static GuiceyConfigurationHook HOOK = builder -> builder.modules(new DebugModule());
```

!!! tip
    Hook field could be used for guicey report activation in test:
    ```java
    @EnableHook
    static GuiceyConfigurationHook hook = GuiceBundle.Builder::printStartupTime;
    ```

Any number of hook fields could be declared.   
Hook fields could be also declared in base test class:

```java
public abstract class BaseTest {
    
    // hook in base class
    @EnableHook
    static GuiceyConfigurationHook BASE_HOOK = builder -> builder.modules(new DebugModule());
}

@TestGuiceyApp(value = App.class, hooks = SomeOtherHook.class)
public class SomeTest extends BaseTest {
    
    // Another hook
    @EnableHook
    static GuiceyConfigurationHook HOOK = builder -> builder.modules(new DebugModule2());
}
```

All 3 hooks will work (two in fields, one in annotation).
# Extension configuration unification

It is a common need to run multiple tests with the same test application configuration
(same config overrides, same hooks etc.).
Do not configure it in each test, instead move extension configuration into base test class:

```java
@TestGuiceyApp(...)
public abstract class AbstractTest {
    // here might be helper methods
}
```

And now all test classes should simply extend it:

```java
public class Test1 extends AbstractTest {
    
    @Inject
    MyService service;
    
    @Test
    public void testSomething() { ... }
}
```

If you use manual extension configuration (through field), just replace annotation in base class with
manual declaration - approach would still work.

## Meta annotation

You can prepare meta annotation (possibly combining multiple 3rd party extensions): 

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TestDropwizardApp(AutoScanApplication.class)
public @interface MyApp {
}

@MyApp
public class MetaAnnotationDwTest {

    @Test
    void checkAnnotationRecognized(Application app) {
        Assertions.assertNotNull(app);
    }   
}
```

OR you can simply use base test class and configure annotation there:

```java
@TestDropwizardApp(AutoScanApplication.class)
public class BaseTest {}

public class ActualTest extends BaseTest {} 
```

## Reuse application between tests

In some cases it is preferable to start application just once and use for all tests
(e.g. due to long startup or time-consuming environment preparation).

In order to use the same application instance, extension declaration must be performed in 
[base test class](#extension-configuration-unification) and `reuseApplication` flag must be enabled:

```java
@TestGuiceyApp(value = Application.class, reuseApplication = true)
public abstract class BaseTest {}
```

or

```java
public abstract class BaseTest {
    @RegisterExtension
    static TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class)
            .reuseApplication()
            .create();
    
}
```

The same will work for dropwizard extension (`@TestDropwizardApp` and `TestDropwizardAppExtension`).

!!! important
    Application instance re-use is not enabled by default for backwards compatibility
    (for cases when base class declaration already used).

There might be multiple base test classes declaring reusable applications:
different global applications would be started for each declaration (allowing you
to group tests requiring different applications) 

Global application would be closed after all tests execution (with test engine shutdown).

In essence, reusable application "stick" to declaration in base class, so all tests,
extending base class "inherit" the same declaration and so the same application (when reuse enabled).

!!! tip
    Reusable applications may be used together with tests, not extending base class
    and using guicey extensions. Such tests would simply start a new application instance.
    Just be sure to avoid port clashes when using reusable dropwizard apps (by using `randomPorts` option).

`@EnableSetup` and `@EnableHook` fields are also supported for reusable applications.
But declare all such fields on base class level (or below) because otherwise only fields
declared on first started test would be used. Warning would be printed if such fields used
(or ignored because reusable app was already started by different test).


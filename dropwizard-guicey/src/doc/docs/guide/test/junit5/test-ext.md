# Testing extensions

Extensions (like [setup objects](setup-object.md)) often rely on afterEach/afterAll methods
and so it is not possible to test extension completely using test extensions (like `@TestGuiceyApp`).

Junit provides a TestKit which could run unit tests inside usual unit test. This way 
full extension lifecycle could be tested.

Additional dependency required (version managed by dropwizard BOM):

```groovy
testImplementation 'org.junit.platform:junit-platform-testkit'
```

Prepare test class, using your extension (better inner class):

```java
public class Test {
    
    
    // IMPORTANT to skip this test for the main junit engine (don't let it run this test)
    @Disabled
    public static class TestCase1 {
        
        // custom extension
        @MyAnnotation
        Something field;
        
    }
}
```

Running test:

```java
Throwable th;
EngineTestKit
        .engine("junit-jupiter")
        // ignore @Disable annotation
        .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
        .selectors(DiscoverySelectors.selectClass(TestCase1.class))
        .execute().allEvents().failed().stream()
        .forEach(event -> {
            Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
            err.printStackTrace();
            th = err;
        });

if (th != null) {
    // success case
} else {
    // error case
}
```

## Testing console output

Full console output could be tracked wither with [output captor utility](output.md) or 
using `system-stubs-jupiter` library.

For example:

```java
public void run(Class test) {
    EngineTestKit
            .engine("junit-jupiter")
            ....
}

@Test
public void test() {
    String out = TestSupport.captureOutput(() -> {
        run(TestCase1.class);
    });
    
    // windows compatibility
    out = out.replace("\r","");
    
    Assertions.assertThat(out).contains("some probably long text");
}
```

Most likely, logs would contain some changing data (like logger time or performance measures),
so output would need to be pre-processed with regexps.

For example, to replace string like "20 ms", "112.3 ms":

```java
out.replaceAll("\\d+(\\.\\d+)? ms( +)?", "111 ms ");
```

To replace lambda identity in class name:

```java
out.replaceAll("\\$\\$Lambda\\$\\d+/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111")
    // jdk 21
    .replaceAll("\\$\\$Lambda/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111");
```

Logger time:

```java
out.replaceAll("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d+]", "[2025-22-22 11:11:11]")
```

And so on. You can see `AbstractPlatformTest` in guicey tests (dropwizard-guicey module) and
all related tests as examples.
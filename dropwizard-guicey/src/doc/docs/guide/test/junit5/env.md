# Environment variables

!!! warning
    Such modifications are not suitable for parallel tests execution!

    Use [`@Isolated`](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution-synchronization) 
    on such tests to prevent parallel execution with other tests

To modify environment variables for test use [system stubs](https://github.com/webcompere/system-stubs) library

```groovy
testImplementation 'uk.org.webcompere:system-stubs-jupiter:2.1.3'
testImplementation 'org.objenesis:objenesis:3.3'
```

```java
@ExtendWith(SystemStubsExtension.class)
public class MyTest {
    @SystemStub
    EnvironmentVariables ENV;
    @SystemStub
    SystemOut out;
    @SystemStub
    SystemProperties propsReset;
    
    @BeforeAll
    public void setup() {
        ENV.set("VAR", "1");
        System.setProperty("foo", "bar"); // OR propsReset.set("foo", "bar") - both works the same
    } 
    
    @Test
    public void test() {
        // here goes some test that requires custom environment and system property values
        
        // validating output
        Assertions.assertTrue(out.getTest().contains("some log message"));
    }
}
```

Pay attention that there is no need for cleanup: system properties and environment variables would be re-set automatically!


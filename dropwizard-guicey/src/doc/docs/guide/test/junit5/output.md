# Testing console output

There is a utility to capture console output:

```java
@Isolated
@TestWebApp(App.class)
public class Test {
    
    @Test
    public void testRestCall(ClientSupport client) {
        String out = TestSupport.captureOutput(() -> {
            // call application api endpoint
            client.get("sample/get", null);
        });

        // uses assert4j, test that client was called (just an example) 
        Assertions.assertThat(out)
                .contains("[Client action]---------------------------------------------{");
    }
}
```

Returned output contains both `System.out` and `System.err` - same as it would be seen in console.

All output is also printed into console to simplify visual validation

!!! warning
    Such tests could not be run in parallel (due to system io overrides) and so should be 
    annotated with `@Isolated`
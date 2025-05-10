# Testing console output

There is a utility to capture console output:

```java
String out = TestSupport.captureOutput(() -> {
        
    // run application inside
    TestSupport.runWebApp(App.class, injector -> {
        ClientSupport client = TestSupport.getContextClient();
        
        // call application api endpoint
        client.get("sample/get", null);

        return null;
    });
});

// uses assert4j, test that client was called (just an example) 
Assertions.assertThat(out)
    .contains("[Client action]---------------------------------------------{");
```

Returned output contains both `System.out` and `System.err` - same as it would be seen in console.

All output is also printed into console to simplify visual validation

!!! warning
    Such tests could not be run in parallel (due to system io overrides)

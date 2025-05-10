# JUnit 5

!!! note ""
    Junit 5 [user guide](https://junit.org/junit5/docs/current/user-guide/) | [Migration from JUnit 4](../junit4.md#migrating-to-junit-5)

## Setup

You will need the following dependencies (assuming BOM used for versions management):

```groovy
testImplementation 'io.dropwizard:dropwizard-testing'
testImplementation 'org.junit.jupiter:junit-jupiter-api'
testRuntimeOnly 'org.junit.jupiter:junit-jupiter'
```

!!! tip
    If you already have junit4 or spock tests, you can activate [vintage engine](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4) 
    so all tests could work  **together** with junit 5: 
    ```groovy    
    testRuntimeOnly 'org.junit.vintage:junit-vintage-engine'
    ```

!!! note
    In gradle you need to explicitly [activate junit 5 support](https://docs.gradle.org/current/userguide/java_testing.html#using_junit5) with
    ```groovy
    test {
        useJUnitPlatform()
        ...
    }                    
    ```
    
!!! warning
    Junit 5 annotations are **different** from junit4, so if you have both junit 5 and junit 4
    make sure correct classes (annotations) used for junit 5 tests:
    ```java
    import org.junit.jupiter.api.Assertions;
    import org.junit.jupiter.api.Test;
    ```    

## Dropwizard extensions compatibility

Guicey extensions *could be used together with dropwizard 
extensions*. It could be used to start multiple dropwizard applications.

For example:

```java
// run app (injector only)
@TestGuiceyApp(App.class)
// activate dropwizard extensions
@ExtendWith(DropwizardExtensionsSupport.class)
public class ClientSupportGuiceyTest {

    // Use dropwizard extension to start a separate server
    // It might be the same application or different 
    // (application instances would be different in any case)
    static DropwizardAppExtension app = new DropwizardAppExtension(App.class);

    @Test
    void testLimitedClient(ClientSupport client) {
        Assertions.assertEquals(200, client.target("http://localhost:8080/dummy/")
                .request().buildGet().invoke().getStatus());
    }
}
```

!!! info
    There is a difference in extensions implementation. 
    
    Dropwizard extensions work as:
    junit extension `@ExtendWith(DropwizardExtensionsSupport.class)` looks for fields 
    implementing `DropwizardExtension` (like `DropwizardAppExtension`) and start/stop them according to test lifecycle.
    
    Guicey extensions implemented as separate junit extensions (only some annotated fields are manually searched 
    (hooks, setup objects, special extensions). 
    Also, guciey extensions implement junit parameters injection (for test and lifecycle methods). 
    




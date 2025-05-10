# Testing web (HTTP client)

`ClientSupport` is a [JerseyClient](https://eclipse-ee4j.github.io/jersey.github.io/documentation/2.29.1/client.html)
aware of dropwizard configuration, so you can easily call admin/main/rest urls.

Creation:

```java
ClientSupport client = TestSupport.webClient(support);
```

where support is `DropwizardTestSupport` or `GuiceyTestSupport` (in later case it could be used only as generic client for calling external urls).

Example usage:

```java
// GET {rest path}/some
client.targetRest("some").request().buildGet().invoke()

// GET {main context path}/servlet
client.targetMain("servlet").request().buildGet().invoke()

// GET {admin context path}/adminServlet
client.targetAdmin("adminServlet").request().buildGet().invoke()

// General external url call
client.target("https://google.com").request().buildGet().invoke()
```

!!! tip
    All methods above accepts any number of strings which would be automatically combined into correct path:
    ```groovy
    client.targetRest("some", "other/", "/part")
    ```
    would be correctly combined as "/some/other/part/"

As you can see, test code is abstracted from actual configuration: it may be default or simple server
with any contexts mapping on any ports - target urls will always be correct.

```java
Response res = client.targetRest("some").request().buildGet().invoke()

Assertions.assertEquals(200, res.getStatus())
Assertions.assertEquals("response text", res.readEntity(String)) 
```

Also, if you want to use other client, client object can simply provide required info:

```groovy
client.getPort()        // app port (8080)
client.getAdminPort()   // app admin port (8081)
client.basePathRoot()   // root server path (http://localhost:8080/)
client.basePathMain()   // main context path (http://localhost:8080/)
client.basePathAdmin()  // admin context path (http://localhost:8081/)
client.basePathRest()   // rest context path (http://localhost:8080/)
```

## Simple REST methods

The client also contains simplified GET/POST/PUT/DELETE methods for path, relative to server root (everything after port):


```java
@Test
public void testWeb(ClientSupport client) {
    // get with result
    Result res = client.get("rest/sample", Result.class);
    
    // post without result (void)
    client.post("rest/action", new PostObject(), null);

   // post with result 
   Result res = client.post("rest/action", new PostObject(), Result.class);
}
```

All methods:

1. Methods accept paths relative to server root. In the example above: "http://localhost:8080/rest/sample"
2. Could return mapped response.
3. For void calls, use null instead of the result type. In this case, only 200 and 204 (no content) responses
   would be considered successful

POST and PUT also accept (body) object to send.
But methods does not allow multipart execution.

!!! tip
    These methods could be used as examples for jersey client usage.

## Customization

`JerseyClient` used in `ClientSupport` could be customized using `TestClientFactory` implementation.

Simple factory example:

```java
public class SimpleTestClientFactory implements TestClientFactory {

    @Override
    public JerseyClient create(final DropwizardTestSupport<?> support) {
        return new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .build();
    }
}
```

Default implementation (`DefaultTestClientFactory`) applies timeouts and auto-registers multipart support if `dropwizard-forms` module
if available in classpath.

All builders support `.clientFactory()` method for optional customization.


## Default client

`JerseyClient` used inside `ClientSupport` is created by `DefaultTestClientFactory`.

Default implementation:

1. Enables multipart feature if `dropwizard-forms` is in classpath (so the client could be used
   for sending multipart data).
2. Enables request and response logging to simplify writing (and debugging) tests.

By default, all request and response messages are written directly into console to guarantee client
actions visibility (logging might not be configured in tests).

Example output:

```

[Client action]---------------------------------------------{
1 * Sending client request on thread main
1 > GET http://localhost:8080/sample/get

}----------------------------------------------------------


[Client action]---------------------------------------------{
1 * Client response received on thread main
1 < 200
1 < Content-Length: 13
1 < Content-Type: application/json
1 < Date: Mon, 27 Nov 2023 10:00:40 GMT
1 < Vary: Accept-Encoding
{"foo":"get"}

}----------------------------------------------------------
```

Console output might be disabled with a system proprty:

```java
// shortcut sets DefaultTestClientFactory.USE_LOGGER property
DefaultTestClientFactory.disableConsoleLog()
```

With it, everything would be logged into `ClientSupport` logger (java.util) under INFO
(most likely, would be invisible in the most logger configurations, but could be enabled).


To reset property (and get logs back into console) use:

```java
DefaultTestClientFactory.enableConsoleLog()
```

!!! note
    Static methods added not directly into `ClientSupport` because this is
    the default client factory feature. You might use a completely different factory.

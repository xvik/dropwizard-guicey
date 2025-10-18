# Rest client

Rest client creation:

```java
    public void test (ClientSupport client) {
        // resource client 
        ResourceClient<ResourceClass> rest = client.restClient(RestResource.class);    
    }
```

or from root rest client:

```java
    ResourceClient<ResourceClass> rest = client.restClient().subClient(RestResource.class);
```

## Method call examples

```java
    @GET
    @Path("/get/{name}")
    public List<Integer> get(@PathParam("name") String param)
```

```java
   // path param applied
   assertThat(rest.method(r -> r.get("path")).as(String.class))
        .isEqualTo("something")
```

```java
    @Path("/entity")
    @POST
    public String entity(ModelType model) 
```

```java
    // post with entity
    assertThat(rest.method(r -> r.entity(new MediaType(...))).as(String.class))
        .isEqualTo("something")
```

### Response types

```java
    // execute request, no success status check
    TestClientResponse res = rest.method(RestResource::get).invoke()
    // throw exception if not success and AssertionError if not provided status
    TestClientResponse res = rest.method(RestResource::get).expectSuccess()
    // throw AssertionError if success or not provided status        
    TestClientResponse res = rest.method(RestResource::get).expectFailure()
    // throw AssertionError if not redirect or not provided status
    TestClientResponse res = rest.method(RestResource::get).expectRedirect()
    // execute, ignore response, throw error if not successful
    rest.method(RestResource::get).asVoid()
    // execute with successful result conversion, exception otherwise
    String res = rest.method(RestResource::get).asString()
    // execute with successful result conversion, exception otherwise
    MyType res = rest.method(RestResource::get).as(MyType.class)
    // execute with successful result conversion, exception otherwise
    List<MyType> res = rest.method(RestResource::get).as(new GenericType<>{})
```

### Response assertions

```java
   rest.method(RestResource::get).invoke()
        .assertSuccess()  // assertFaile() assertRedirect()
        .assertStatus(200)
        .assertVoidResponse()
        .assertMedia(MediaType.APPLICATION_JSON_TYPE)
        .assertLocale(Locale.EN)
        .assertheader("Name", "value")
        .assertCookie("Cookie", "value")
        .assertCacheControl(cc -> cc.isMustRevalidate())
```

```java
    MyType res = rest.method(RestResource::get).invoke()
        .assertLocale(Locale.EN)
        .assertheader("Name", "value")
        .as(MyType.class)
```

### Request assertions

```java
rest.defaultHeader("Token", "abc")
    
MyType res = rest.method(RestResource::get)
        .assertRequest(tracker -> assertThat(tracker.getHeaders().get("Token")).isEqualsTo("abc"))
        .as(MyType.class)
```


## Sub resources

### Sub resource by instance

```java
    @Path("/sub")
    public SubResource sub() {
        return new SubResource();
    }
```

```java
   // sub resource called
   assertThat(rest.method(r -> r.sub().get("path")).as(String.class))
        .isEqualTo("something")
```

### Sub resource by class

```java
    @Path("/sub")
    public Class<SubResource> sub() {
        return SubResource.class;
    }
```

```java
   // manual sub client creation (path resolved from locator method)
   assertThat(rest.subResourceClient(Resource::sub, SubResource.class)
                .method(SubResource::get).asString())
        .isEqualTo("something");
```

## File download

```java
    @GET
    @Path("/download")
    public Response download() {
        return Response.ok(getClass().getResourceAsStream("/some.txt"))
                .header(HttpHeader.CONTENT_DISPOSITION.toString(), "attachment; filename=some.txt")
                .build();
    }
```  

```java
    // load in directory, preserving file name
    File res = rest.method(FileResource::download).expectSuccess().asFile(temp);
```

## Exact status check

```java
    // fail for different status
    rest.method(r -> r.post(entity)).expectSuccess(201)
        // example response assertion
        .assertHeader("Some", "11");
```


## Error check

```java
    assertThatThrownBy(() -> rest.method(RestResource::get).expectFailure(401))
        .isInstanceOf(AssertionError.class)
        .hasMessageStartingWith("Unexpected response status 500 when expected 401");
```

## Redirects

```java
    @Path("/redirect")
    @GET
    public Response redirect() {
        return Response.seeOther(
                urlBuilder.rest(getClass()).method(RestResource::get).buildUri()
        ).build();
    }
```

```java
     rest.method(RestResource::redirect).expectRedirect()  // optional status
        .assertHeader("Location", s -> s.endsWith("/get"));
```

## Urlencoded forms

### Simple values

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post(@NotNull @FormParam("name") String value,
                     @NotNull @FormParam("date") String date)
```

```java
   // entity from parameters
   assertThat(rest.method(r -> r.post("1", "2")).as(String.class))
        .isEqualTo("something");

   // manual call
   assertThat(rest.buildForm("/post")
                .param("name", "1")
                .param("date", "2")
                .buildPost()
                .as(String.class))
        .isEqualTo("something");

    // manual entity building
    assertThat(rest.method(r -> r.post(null, null), 
                            rest.buildForm(null)
                            .param("name", "1")
                            .param("date", "2")
                            .buildEntity())
                    .as(String.class))
        .isEqualTo("something");
```

### Multiple values

```java
    @Path("/postMulti")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postMulti(@NotEmpty @FormParam("name") List<String> value,
                          @NotNull @FormParam("date") String date)
```

```java
    assertThat(rest.buildForm("/postMulti")
                .param("name", 1, 2, 3)
                .param("date", "2")
                .buildPost()
                .as(String.class)).isEqualTo("something");

    assertThat(rest.method(instance -> instance.postMulti(Arrays.asList("1", "2", "3"), "2"))
                .as(String.class)).isEqualTo("something");
```

### Not mapped fields

```java
    @Path("/post2")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post2(@NotEmpty MultivaluedMap<String, String> params)
```

```java
    final MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
    map.add("name", "1");
    map.add("date", "2");
    assertThat(rest.method(r -> r.post2(map)).as(String.class))
        .isEqualTo("somthing");
```

## Multipart forms

### File upload

```java
    @Path("/multipart")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart(
            @NotNull @FormDataParam("file") InputStream uploadedInputStream,
            @NotNull @FormDataParam("file") FormDataContentDisposition fileDetail)
```

```java
    // manual
    assertThat(rest.buildForm("/multipart")
            .param("file", new File("src/test/resources/some.txt"))
            .buildPost()
            .as(String.class)).isEqualTo("something");

    // from method call
    assertThat(rest.multipartMethod((r, multipart) ->
                    r.multipart(multipart.fromClasspath("/logback.xml"),
                            multipart.disposition("file", "logback.xml")))
            .as(String.class)).isEqualTo("something");
```

### File upload 2

```java
    @Path("/multipart2")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipart2(
            @NotNull @FormDataParam("file") FormDataBodyPart file)
```

```java
    assertThat(rest.multipartMethod((r, multipart) ->
        // from classpath
        r.multipart2(multipart.streamPart("file", "/some.txt")))
        .as(String.class)).isEqualTo("something");

    assertThat(rest.multipartMethod((r, multipart) ->
        // from fs (relative to work dir)
        r.multipart2(multipart.filePart("file", "src/test/resources/some.txt")))
        .as(String.class)).isEqualTo("something");
```

### Multiple files within one field

```java
    @Path("/multipartMulti")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartMulti(
            @NotNull @FormDataParam("file") List<FormDataBodyPart> file) 
```

```java
    // from method call
    assertThat(rest.multipartMethod((r, multipart) ->
        r.multipartMulti(Arrays.asList(
            multipart.filePart("file", "src/test/resources/some1.txt"),
            multipart.filePart("file", "src/test/resources/some2.txt"))))
        .as(String.class)).isEqualTo("something");

    // manual
    assertThat(rest.buildForm("/multipartMulti")
                .param("file", new File("src/test/resources/some1.txt"), new File("src/test/resources/some2.txt"))
                .buildPost()
        .as(String.class)).isEqualTo("something");
```

### Generic multipart

```java
    @Path("/multipartGeneric")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String multipartGeneric(@NotNull FormDataMultiPart multiPart) 
```

```java
    assertThat(rest.multipartMethod((r, multipart) ->
        r.multipartGeneric(multipart.multipart()
                                .field("foo", "bar")
                                .stream("file", "/some.txt")
                                .build()))
        .as(String.class)).isEqualTo("something");
```
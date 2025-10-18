
## Urlencoded forms

Single fields:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post(@NotNull @FormParam("field1") String field1,
                       @NotNull @FormParam("field2") String field1) {
    }
```

There might be multiple values with the same name:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post(@NotNull @FormParam("field") List<String> values) {
    }
```

All values:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String post(@NotEmpty MultivaluedMap<String, String> params) {
    }
```

## GET

Urlencoded forms might post values with GET using query parameters:

```java
    @Path("/get")
    @GET
    public String get(@NotNull @QueryParam("field1") String field1,
                    @NotNull @QueryParam("field1") String field2) {
    }
```

Multiple values are also possible:

```java
    @Path("/get")
    @GET
    public String get(@NotEmpty @QueryParam("field") List<String> values) {
    }
```

## Mutlipart

Simple parameters:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(
            @NotNull @FormDataParam("field1") String field1,
            @NotNull @FormDataParam("field2") String feild2) {
    }
```

Simple file upload:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(
            @NotNull @FormDataParam("file") InputStream uploadedInputStream,
            @NotNull @FormDataParam("file") FormDataContentDisposition fileDetail) {
    }
```

File (or any simple field):

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@NotNull @FormDataParam("file") FormDataBodyPart file) {
    }
```

Multiple values:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@NotNull @FormDataParam("file") List<FormDataBodyPart> files) {
    }
```

Also, content-dispositions might be aggregated:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@NotNull @FormDataParam("file") List<FormDataContentDisposition> files) {
    }
```

All multipart fields:

```java
    @Path("/post")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String post(@NotNull FormDataMultiPart multiPart) {
        Map<String, List<FormDataBodyPart>> fieldsMap = multiPart.getFields();
    }
```
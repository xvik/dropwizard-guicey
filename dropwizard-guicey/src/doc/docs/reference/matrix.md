# Matrix params

Matrix param are almost the same as query params:

/some;m=1?q=2

```java
    @Path("/some")
    public Response get(@MatrixParam("m") String m, @QueryParam("q") String q)
```

But matrix params could appear in the middle of path:

/some;m=1/path?q=2

```java
    @Path("{vars:some}/path")
    public Response get(@PathParam("vars") PathSegment vars, @QueryParam("1") String q)
```

`@MatrixParam` annotation can't be used here, instead using path param with static regex ("some").
Actual matrix parameters are available from `PathSegment.getMatrixParams()`

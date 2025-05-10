# AssertJ

It is highly recommended to use [AssertJ](https://assertj.github.io/doc/) instead of JUnit assertions
(dropwizard team use for testing dropwizard).

AssertJ version is already managed by dropwizard BOM:

```groovy
testImplementation 'org.assertj:assertj-core'
```

AssertJ assertions are reversed, which might be cumbersome for simple assertions:

```java
// Junit
Assertions.assertEquals(12, something);

// AssertJ
Assertions.assertThat(something).isEqualTo(12);
```

But, with AssertJ you can combine assertions:

```java
assertThat(frodo.getName()).startsWith("Fro")
                           .endsWith("do")
                           .isEqualToIgnoringCase("frodo");
```

!!! tip
    See [assertions guide](https://assertj.github.io/doc/#assertj-core-assertions-guide)

!!! important 
    In many cases, assertj assertion fail messages would be much more informative,
    which speeds up tests development and regressions investigation.

## Text assertions

AssertJ greatly simplifies large text comparisons (e.g. console output).
In Junit, to check if output contains some part you'll have to do:

```java
Assertions.assertTrue(output.contains("some large string chunk here"))
```

If assertion fails, you'll only:

```
org.opentest4j.AssertionFailedError: 
Expected :true
Actual   :false
```

For AssertJ assertion:

```java
Assertions.assertThat(output).contains("some large string chunk here");
```

You'll have all required info in the console:

```
java.lang.AssertionError: 
Expecting actual:
  "
  original text here
  
"
to contain:
  "some large string chunk here" 
```

This is extremely helpful because often output is pre-processed with regexps
(to remove windows "\r", to replace varying part (e.g. times), etc.) and
AssertJ error shows the processed text which greatly simplifies understanding the problem.

## Asserting collections

```java
List<TolkienCharacter> hobbits = list(frodo, sam, pippin);

// all elements must satisfy the given assertions
assertThat(hobbits).allSatisfy(character -> {
  assertThat(character.getRace()).isEqualTo(HOBBIT);
  assertThat(character.getName()).isNotEqualTo("Sauron");
});

// at least one element must satisfy the given assertions
assertThat(hobbits).anySatisfy(character -> {
  assertThat(character.getRace()).isEqualTo(HOBBIT);
  assertThat(character.getName()).isEqualTo("Sam");
});
```

Accessing elements:

```java
Iterable<TolkienCharacter> hobbits = list(frodo, sam, pippin);
assertThat(hobbits).first().isEqualTo(frodo);
assertThat(hobbits).element(1).isEqualTo(sam);
assertThat(hobbits).last().isEqualTo(pippin);
```

## Exception assertions

```java
assertThatExceptionOfType(RuntimeException.class)
         .isThrownBy(() -> { throw new RuntimeException(new IllegalArgumentException("boom!")); })
         .havingCause()
         .withMessage("boom!");
```

Or

```java
assertThatThrownBy(() -> throw new RuntimeException("boom!"))
        .hasMessage("boom!");
```

## Objects comparison

For data objects, not implementing equals simple comparison would not work:

```java
Object first = new Something();
Object second = new Something();

// different objects because equals not implemented
assertThat(first).isEqualTo(second);
```

AssertJ provides [recursive comparison](https://assertj.github.io/doc/#assertj-core-recursive-comparison)
to compare object fields (instead of using object equals):

```java
assertThat(first)
	.usingRecursiveComparison()
	.isEqualTo(second);
```

You can also exclude some fields from comparison:

```java
assertThat(first)
  .usingRecursiveComparison()
  .ignoringFields("birthdDate")
  .isEqualTo(second);
```

!!! tip
    Assert object does not contain null fields: 
    ```java
    assertThat(object).usingRecursiveAssertion().hasNoNullFields()
    ```


## Assumptions

[Assumption mechanism](https://assertj.github.io/doc/#assertj-core-assumptions) allows ignoring test if some 
condition does not met:

```java
@Test
public void when_an_assumption_is_not_met_the_test_is_ignored() {
  // since this assumption is obviously false ...
  Assumptions.assumeThat(frodo.getRace()).isEqualTo(ORC);
  // ... this assertion is not performed
  assertThat(fellowshipOfTheRing).contains(sauron);
}
```


## Soft assertions

[Soft assertions](https://assertj.github.io/doc/#assertj-core-soft-assertions)
allows showing all errors at once, instead of only the first one.

This might be useful for speeding up debugging long-running tests (avoid many run-fix-run cycles):

```java
SoftAssertions.assertSoftly(softly -> {
    softly.assertThat(frodo.name).isEqualTo("Samwise");
    softly.assertThat(sam.name).isEqualTo("Frodo");
});
```

```
Multiple Failures (2 failures)
 -- failure 1 --
 Expecting:
  <"Frodo">
 to be equal to:
  <"Samwise">
 but was not.
 -- failure 2 --
 Expecting:
  <"Samwise">
 to be equal to:
  <"Frodo">
 but was not.
```

## DB assertions

There is also an [assertj-db](https://assertj.github.io/doc/#assertj-db) extension which greatly
simplifies testing logic affecting JDBC database.

To use assertj-db add dependency:

```groovy
testImplementation 'org.assertj:assertj-db:3.0.0'
```

Assuming database is configured in application configuration:

```java

AssertDbConnection connection;

@BeforeAll
// here AppConfig would be injected as guice bean (assume junit extension used) 
static void beforeAll(AppConfig config) {
    final DataSourceFactory db = config.getDatabase();
    connection = AssertDbConnectionFactory
            .of(db.getUrl(), db.getUser(), db.getPassword())
            .create();
}
```

Now you can access any table:

```java
Table table = connection.table("table_name").build();
```

!!! important
    `Table` represents current database "snapshot" - it will not show modifications
    performed after table creation! So always create new table before assertions.


To output it to console (useful for modifications on empty or small tables):

```java
Outputs.output(table).toConsole();
```

Will print the entire table in console:

```
[MEMBERS table]
|-----------|---------|-----------|-----------|--------------|-----------|-----------|-----------|
|           |         | *         |           |              |           |           |           |
|           | PRIMARY | ID        | NAME      | FIRSTNAME    | SURNAME   | BIRTHDATE | SIZE      |
|           | KEY     | (NUMBER)  | (TEXT)    | (TEXT)       | (TEXT)    | (DATE)    | (NUMBER)  |
|           |         | Index : 0 | Index : 1 | Index : 2    | Index : 3 | Index : 4 | Index : 5 |
|-----------|---------|-----------|-----------|--------------|-----------|-----------|-----------|
| Index : 0 | 1       | 1         | Hewson    | Paul David   | Bono      | 05-10-60  | 1.75      |
| Index : 1 | 2       | 2         | Evans     | David Howell | The Edge  | 08-08-61  | 1.77      |
| Index : 2 | 3       | 3         | Clayton   | Adam         |           | 03-13-60  | 1.78      |
| Index : 4 | 4       | 4         | Mullen    | Larry        |           | 10-31-61  | 1.70      |
|-----------|---------|-----------|-----------|--------------|-----------|-----------|-----------|
```

Assert [table data](https://assertj.github.io/doc/#assertj-db-concepts-table):

```java
org.assertj.db.api.Assertions.assertThat(table).hasNumberOfRows(1);

org.assertj.db.api.Assertions.assertThat(table).column("name")
        .value().isEqualTo("Hewson")
```

Do direct [sql requests](https://assertj.github.io/doc/#assertj-db-concepts-request):

```java
Request request1 = connection.request("select name, firstname from members where id = 2 or id = 3").build();
```

!!! tip
    Read more in [concepts doc](https://assertj.github.io/doc/#assertj-db-concepts)
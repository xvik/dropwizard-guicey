### Guice-validator example

Example show [guice-validator](https://github.com/xvik/guice-validator) 3rd party library integration.

By default, dropwizard support javax.validation annotations usage on [rest resources](http://www.dropwizard.io/1.2.2/docs/manual/validation.html).
Guice-validator will allow using them on all guice beans and write custom guice-aware validators.

NOTE: Integration deserves special ext module, but it's not exists now (planned), so pure example only.

#### Setup 

Add guice-validator dependency:

```groovy
dependencies {
    implementation 'ru.vyarus:guice-validator:2.0.0'
}
```

Next we need to add validator guice module which will find and apply aop interceptors for
all validation annotations in guice beans:

```java
.modules(
        new ValidationModule(bootstrap.getValidatorFactory())
                .targetClasses(Matchers.not(Matchers.annotatedWith(Path.class)))
                .targetMethods(Matchers.not(Matchers.annotatedWith(Path.class))))
)
``` 

Note that dropwizard already applies validation to reast reasource, but they are also 
guice beans and so to avoid duplication excluding all resource classes from "guice-validator scope".

And the last thing is to substitute validator:

```java
environment.setValidator(InjectorLookup.getInjector(this).get().getInstance(Validator.class));
```

Explanation (you may skip this): dropwizard declares ValidatorFactory in bootstrap object. This factory is used then (on run phase)
to create Validator (used for validation). Guice-validator is also using dropwizard factory, but it has to
configure different ConstraintFactory, so validators creation could be delegated to guice (and so you can use injection in validators).
The problem here is that this configuration "forks" factory so dropwizard still use it's factory, but
guice will use "forked" factory. As the result, dropwizard creates Validator from it's factory, which is not aware of guice.
And guice use Validator, created from "forked" factory. In this case, custom guice-aware validators (custom annotations) will not 
work on resources. To fix this we simply set correct validator to dropwizard environment. 


#### Examples

Custom constraint: we want to use custom validation annotation `@CustomCondition` to apply some 
custom validation logic.

```java
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CustomValidator.class})
@Documented
public @interface CustomCondition {

    String message() default "Very specific case check failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
public class CustomValidator implements ConstraintValidator<CustomCondition, String> {

    @Inject
    SomeService service;

    @Override
    public void initialize(CustomCondition constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return /* do some check and return true/false */;
    }
}
```

Usage in guice bean:

```java
@Singleton
public class SomeService {

    public String customValidationParameter(@CustomCondition String value) ...

    @CustomCondition
    public String customValidationReturn(String value) ...
```

When one of these methods would be called, `CustomValidator` will be created with guice and used
for validation automatically.

The same way it will work on resources:

```java
@GET
@Path("/custom")
public String withCustomValidator(@CustomCondition @QueryParam("q") String something) ...
```

#### Bean example

In some cases it could be handy to tie custom validator for entity type (so you can be sure 
that entity is always valid):

```java
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {MyBeanValidator.class})
@Documented
public @interface MyBeanValid {

    String message() default "Bean is not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
public class MyBeanValidator implements ConstraintValidator<MyBeanValid, MyBean> {

    @Override
    public void initialize(MyBeanValid constraintAnnotation) {}

    @Override
    public boolean isValid(MyBean value, ConstraintValidatorContext context) {
        return /* some checks here (could be multiple checks) */;
    }
}
```

Adding validation to bean:

```java
@MyBeanValid
public class MyBean { ...
```

Now every time this bean appear under `@Valid` annotation, custom validator will work:

```java
public void customBeanCheck(@Valid MyBean bean) { ...
```


Read more in [guice-validator](https://github.com/xvik/guice-validator) documentation (for guice-related aspects) and 
[hibernate-validator](http://hibernate.org/validator/) documentation for javax.validation description.

See [Test](src/test/groovy/ru/vyarus/dropwizard/guice/examples/RestValidationTest.groovy) to make sure it works 
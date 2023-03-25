package ru.vyarus.guicey.validation;

import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guice.validator.ValidationModule;
import ru.vyarus.guice.validator.aop.DeclaredMethodMatcher;
import ru.vyarus.guicey.validation.util.RestMethodMatcher;

import javax.validation.Validator;
import javax.validation.executable.ValidateOnExecution;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Validation bundle activates implicit method validations for guice beans. It means that if method have
 * any javax.validtion annotations (contraints for parameters ot return value) then validations would be
 * performed. By default, dropwizard applies validation only to rest resources, this bundle activates validations
 * for all guice beans.
 * <p>
 * Bundle registered automatically by bundles lookup. But if you want to configure it, simply register it directly
 * (and lookup-provided instance would be ignored).
 * <p>
 * See {@link ValidationModule} for more info. Bundle essentially just provide shortcuts for module configurations.
 * <p>
 * Bundle also binds {@link Validator} and {@link javax.validation.executable.ExecutableValidator}, so they become
 * available for injection. Custom validators may use guice injections.
 * <p>
 * WARNING: do not obtain validator directly from {@link javax.validation.ValidatorFactory} because it will not
 * be able to wire guice injections for validators requiring it. Module substitute {@link Validator} instance in
 * dropwizard {@link io.dropwizard.core.setup.Environment} so custom guice-aware validators may be used on rest
 * resources too,
 *
 * @author Vyacheslav Rusakov
 * @since 26.12.2019
 */
public class ValidationBundle extends UniqueGuiceyBundle {
    private final Logger logger = LoggerFactory.getLogger(ValidationBundle.class);

    // most resources annotated directly
    private Matcher<? super Class> typeMatcher = Matchers.not(Matchers.annotatedWith(Path.class));

    // in complex declaration cases, avoid methods with @GET, @POST, etc. annotations
    private Matcher<? super Method> methodMatcher = new DeclaredMethodMatcher()
            .and(Matchers.not(new RestMethodMatcher()));
    private Class<? extends Annotation> targetAnnotation;
    private boolean strictGroups;


    /**
     * Customize target classes to apply validation on. By default, it would be all classes not annotated
     * with {@link Path}.
     * <p>
     * If you declare your own target matcher, make sure it also avoids rest services:
     * {@code yourMatcher.and(Matchers.not(Matchers.annotatedWith(Path.class)))}.
     * <p>
     * Shortcut for {@link ValidationModule#targetClasses(Matcher)}.
     *
     * @param matcher matcher
     * @return bundle instance
     */
    public ValidationBundle targetClasses(final Matcher<? super Class> matcher) {
        typeMatcher = matcher;
        return this;
    }

    /**
     * Customize target methods to apply validation on. By default, all methods except annotated with rest
     * annotations ({@link javax.ws.rs.GET}, link {@link javax.ws.rs.POST}, etc.) are allowed (see
     * {@link RestMethodMatcher}. Also, synthetic methods avoided.
     * <p>
     * It is better to also exclude synthetic and bridge methods from matching: you can simply add direct method
     * matcher: {@code yourMatcher.and(new DirectMethodMatcher())}.
     * <p>
     * Shortcut for {@link ValidationModule#targetMethods(Matcher)}.
     * <p>
     * Note: it is possible to "implement" explicit mode with this matcher (like
     * {@code Matchers.annotatedWith(MyAnn.class)}), but better use {@link #validateAnnotatedOnly(Class)}.
     * Method call will produce correct log and eventually will extend your matcher with annotation condition.
     * But, if you want to implement exclusion annotation, then method matcher is the best choice:
     * {@code Matchers.not(Matchers.annotatedWith(SuppressValidation.class))} will lead to validation
     * suppression on all annotated methods.
     *
     * @param matcher matcher
     * @return bundle instance
     */
    public ValidationBundle targetMethods(final Matcher<? super Method> matcher) {
        methodMatcher = matcher;
        return this;
    }

    /**
     * Activates explicit mode, when only {@link ValidateOnExecution} annotated methods (or all methods in
     * annotated class) are validated.
     * <p>
     * Shortcut for {@link ValidationModule#validateAnnotatedOnly()}.
     *
     * @return bundle instance
     */
    public ValidationBundle validateAnnotatedOnly() {
        return validateAnnotatedOnly(ValidateOnExecution.class);
    }

    /**
     * Same as {@link #validateAnnotatedOnly()}, but you can specify custom annotation.
     * <p>
     * Shortcut for {@link ValidationModule#validateAnnotatedOnly(Class)}.
     *
     * @param annotation annotation to trigger validation
     * @return bundle instance
     */
    public ValidationBundle validateAnnotatedOnly(final Class<? extends Annotation> annotation) {
        this.targetAnnotation = annotation;
        return this;
    }

    /**
     * By default, ({@link javax.validation.groups.Default}) group is always added to groups
     * defined with {@link ru.vyarus.guice.validator.group.annotation.ValidationGroups} annotation.
     * <p>
     * Calling this method disables default behavior: after calling it, {@link javax.validation.groups.Default}
     * must be explicitly declared.
     *
     * @return bundle instance
     */
    public ValidationBundle strictGroupsDeclaration() {
        this.strictGroups = true;
        return this;
    }

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        // excluding rest beans because dropwizard already applies validation support there
        final ValidationModule module = new ValidationModule(bootstrap.bootstrap().getValidatorFactory())
                .targetClasses(typeMatcher)
                .targetMethods(methodMatcher);

        if (targetAnnotation != null) {
            module.validateAnnotatedOnly(targetAnnotation);
        }

        if (strictGroups) {
            module.strictGroupsDeclaration();
        }

        bootstrap.modules(module);
    }

    @Override
    public void run(final GuiceyEnvironment environment) throws Exception {
        // substitute dropwizard validator with guice-aware validator in order to be able
        // to use custom (guice-aware) validators in resources
        environment.onGuiceyStartup((config, env, injector) -> {
            env.setValidator(injector.getInstance(Validator.class));
            if (targetAnnotation == null) {
                logger.info("Validation annotations support enabled on guice beans");
            } else {
                logger.info("Validation annotations support enabled on guice beans and methods, "
                        + "annotated with @{}", targetAnnotation.getSimpleName());
            }
        });
    }
}

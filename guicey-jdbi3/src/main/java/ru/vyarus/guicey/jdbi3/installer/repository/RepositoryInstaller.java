package ru.vyarus.guicey.jdbi3.installer.repository;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Stage;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelUtils;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.guice.ext.core.generator.DynamicClassGenerator;
import ru.vyarus.guicey.jdbi3.installer.repository.sql.SqlObjectProvider;
import ru.vyarus.guicey.jdbi3.module.NoSyntheticMatcher;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;
import ru.vyarus.guicey.jdbi3.tx.TransactionTemplate;
import ru.vyarus.guicey.jdbi3.unit.UnitManager;
import ru.vyarus.java.generics.resolver.GenericsResolver;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recognize classes annotated with {@link JdbiRepository} and register them. Such classes may be then
 * injected as usual beans and used as usual daos. All daos will participate in the thread-bound transaction,
 * declared by transaction annotation, transaction template or manually with unit manager.
 * <p>
 * Dao may use any guice-related annotations because beans participate in guice aop. This is done by creating
 * special guice-managed proxy class (where guice could apply aop). These proxies delegate all method calls to
 * JDBI-managed proxies.
 * <p>
 * Manual guice bindings are not allowed, except one case: {@code bind(Base.class).to(Repo.class)} where
 * {@code Repo.class} is a recognizable (annotated) repository. This case useful for generifying repositories
 * (so implementation with exact queries could be pluggable).
 *
 * @author Vyacheslav Rusakov
 * @see InTransaction default annotation
 * @see TransactionTemplate for template usage
 * @see UnitManager for low level usage without transaction
 * @since 31.08.2018
 */
public class RepositoryInstaller implements FeatureInstaller, BindingInstaller {

    private final Reporter reporter = new Reporter(RepositoryInstaller.class, "repositories = ");

    private final Set<Class> bound = new HashSet<>();

    @Override
    public boolean matches(final Class<?> type) {
        final boolean res = type.getAnnotation(JdbiRepository.class) != null;
        if (res) {
            validateCorrectness(type);
        }
        return res;
    }

    @Override
    public void bind(final Binder binder, final Class<?> type, final boolean lazy) {
        Preconditions.checkState(!lazy, "@LazyBinding not supported");

        generateRepository(binder, type);
    }

    @Override
    public <T> void manualBinding(final Binder binder, final Class<T> type, final Binding<T> binding) {
        if (binding.getKey().getTypeLiteral().getRawType() == type) {
            // bind(type) case - guice will fail in any case but this way the message would be better
            // bind(type).to(impl) - extension could be still found by classpath scan and registered failing guice
            // better use bindings override (more obvious)
            throw new UnsupportedOperationException(String.format(
                    "JDBI repository %s can't be installed from binding: %s",
                    type.getSimpleName(), GuiceModelUtils.getDeclarationSource(binding).toString()));
        } else {
            // bind(something).to(type) - binding of something to repo (in this case additional binding could be
            // added)
            generateRepository(binder, type);
        }
    }

    @Override
    public void extensionBound(final Stage stage, final Class<?> type) {
        if (stage != Stage.TOOL) {
            reporter.line(String.format("(%s)", type.getName()));
        }
    }

    @Override
    public void report() {
        reporter.report();
    }

    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("@" + JdbiRepository.class + " on class");
    }

    @SuppressWarnings({"unchecked", "checkstyle:Indentation"})
    private void generateRepository(final Binder binder, final Class<?> type) {
        // avoid duplicate bindings from classpath scan and binding
        if (bound.contains(type)) {
            return;
        }

        // jdbi on demand proxy creator: laziness required to wait for global configuration complete
        // to let proxy factory create method configs with all global configurations (mappers)
        final SqlObjectProvider jdbiProxy = new SqlObjectProvider(type);
        binder.requestInjection(jdbiProxy);

        // collect proxies to be able to eagerly bootstrap them (and avoid slow first proxy execution)
        Multibinder.newSetBinder(binder, SqlObjectProvider.class, Names.named("jdbi3.proxies"))
                .addBinding().toInstance(jdbiProxy);

        // prepare non abstract implementation class (instantiated by guice)
        final Class guiceType = DynamicClassGenerator.generate(type);
        binder.bind(type).to(guiceType).in(Singleton.class);

        // interceptor registered for each dao and redirect calls to actual jdbi proxy
        // (at this point all guice interceptors are already involved)
        binder.bindInterceptor(Matchers.subclassesOf(type), NoSyntheticMatcher.instance(),
                // exact class instead of compact lambda to make AOP report more informative
                new JdbiProxyRedirect(jdbiProxy));

        // without it, on reporting phase binding would be cached and not generated on real run
        if (binder.currentStage() != Stage.TOOL) {
            bound.add(type);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateCorrectness(final Class<?> type) {
        // repository base interfaces must not be annotated because in case of classpath scan they would
        // also be registered as repositories and will ruin AOP appliance
        for (Class check : GenericsResolver.resolve(type).getGenericsInfo().getComposingTypes()) {
            if (!check.equals(type) && check.isAnnotationPresent(JdbiRepository.class)) {
                throw new IllegalStateException(String.format(
                        "Incorrect repository %s declaration: base interface %s is also annotated with @%s which may "
                                + "break AOP mappings. Only root repository class must be annotated.",
                        type.getSimpleName(),
                        check.getSimpleName(),
                        JdbiRepository.class.getSimpleName()));
            }
        }
    }

    /**
     * Guice interceptor redirects calls from guice repository bean into jdbi proxy instance.
     */
    public static class JdbiProxyRedirect implements MethodInterceptor {

        private final Provider<Object> jdbiProxy;

        /**
         * Create jdbi proxy interceptor.
         *
         * @param jdbiProxy jdbi proxy provider
         */
        public JdbiProxyRedirect(final Provider<Object> jdbiProxy) {
            this.jdbiProxy = jdbiProxy;
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            try {
                return invocation.getMethod().invoke(jdbiProxy.get(), invocation.getArguments());
            } catch (InvocationTargetException th) {
                // avoid exception wrapping (simpler to handle outside)
                throw th.getCause();
            }
        }
    }
}

package ru.vyarus.guicey.jdbi3.module;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.TransactionalHandleSupplier;
import org.jdbi.v3.core.extension.Extensions;
import org.jdbi.v3.core.mapper.RowMapper;
import ru.vyarus.guicey.jdbi3.inject.InjectionHandlerFactory;
import ru.vyarus.guicey.jdbi3.installer.repository.RepositoryInstaller;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;
import ru.vyarus.guicey.jdbi3.tx.TransactionTemplate;
import ru.vyarus.guicey.jdbi3.tx.aop.TransactionalInterceptor;
import ru.vyarus.guicey.jdbi3.unit.UnitManager;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Jdbi support guice module. Binds {@link Jdbi} for injection.
 * Introduce unit of work concept for JDBI: thread-bound handle must be created in order to access db (work with db
 * only inside unit of work). Assumed that repositories will be installed with
 * {@link RepositoryInstaller}, which will customize instances to
 * support unit of work. Also, customized instances support guice aop.
 * <p>
 * It is assumed that in most cases unit of work will be defined together with transaction using transaction
 * annotation (one or more). By default, only {@link InTransaction} annotation will be
 * recognized.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
public class JdbiModule extends AbstractModule {
    private final Jdbi jdbi;
    private final List<Class<? extends Annotation>> txAnnotations;

    /**
     * Create jdbi module.
     *
     * @param jdbi          jdbi instance
     * @param txAnnotations transaction annotations
     */
    public JdbiModule(final Jdbi jdbi, final List<Class<? extends Annotation>> txAnnotations) {
        Preconditions.checkState(!txAnnotations.isEmpty(),
                "Provide at least one transactional annotation");
        this.jdbi = jdbi;
        this.txAnnotations = txAnnotations;
    }

    @Override
    protected void configure() {
        // avoid handlers registration under tool stage execution - could lead to NPEs
        if (binder().currentStage() != Stage.TOOL) {
            // allow using guice beans inside proxies with getters, annotated by @Inject
            final InjectionHandlerFactory gettersInjector = new InjectionHandlerFactory();
            requestInjection(gettersInjector);
            jdbi.getConfig(Extensions.class).registerHandlerFactory(gettersInjector);
        }

        bind(Jdbi.class).toInstance(jdbi);

        // init empty collection for case when no mappers registered
        Multibinder.newSetBinder(binder(), RowMapper.class);
        bind(MapperBinder.class).asEagerSingleton();

        // unit of work support
        bind(UnitManager.class);
        bind(Handle.class).toProvider(UnitManager.class);
        // transactions support
        //      supplier provides correct handler into jdbi sql proxies
        bind(TransactionalHandleSupplier.class);
        bind(TransactionTemplate.class);

        bindAnnotationsSupport();
    }

    private void bindAnnotationsSupport() {
        final TransactionalInterceptor interceptor = new TransactionalInterceptor(txAnnotations);
        requestInjection(interceptor);
        txAnnotations.forEach(it -> {
            bindInterceptor(Matchers.annotatedWith(it), NoSyntheticMatcher.instance(), interceptor);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(it), interceptor);
        });
    }
}

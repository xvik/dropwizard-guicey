package ru.vyarus.guicey.jdbi.module;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import ru.vyarus.guicey.jdbi.tx.TransactionTemplate;
import ru.vyarus.guicey.jdbi.tx.aop.TransactionalInterceptor;
import ru.vyarus.guicey.jdbi.unit.UnitManager;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Jdbi support guice module. Binds {@link DBI} for injection.
 * Introduce unit of work concept for JDBI: thread-bound handle must be created in order to access db (work with db
 * only inside unit of work). Assumed that repositories will be installed with
 * {@link ru.vyarus.guicey.jdbi.installer.repository.RepositoryInstaller}, which will customize instances to
 * support unit of work. Also, customized instances support guice aop.
 * <p>
 * It is assumed that in most cases unit of work will be defined together with transaction using transaction
 * annotation (on or more). By default, only {@link ru.vyarus.guicey.jdbi.tx.InTransaction} annotation will be
 * recognized.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2016
 */
public class JdbiModule extends AbstractModule {
    private final DBI jdbi;
    private final List<Class<? extends Annotation>> txAnnotations;

    public JdbiModule(final DBI jdbi, final List<Class<? extends Annotation>> txAnnotations) {
        Preconditions.checkState(!txAnnotations.isEmpty(),
                "Provide at least one transactional annotation");
        this.jdbi = jdbi;
        this.txAnnotations = txAnnotations;
    }

    @Override
    protected void configure() {
        bind(DBI.class).toInstance(jdbi);

        // init empty collection for case when no mappers registered
        Multibinder.newSetBinder(binder(), ResultSetMapper.class);
        bind(MapperBinder.class).asEagerSingleton();

        // unit of work support
        bind(UnitManager.class);
        bind(Handle.class).toProvider(UnitManager.class);
        // transactions support
        bind(TransactionTemplate.class);

        bindAnnotationsSupport();
    }

    private void bindAnnotationsSupport() {
        final TransactionalInterceptor interceptor = new TransactionalInterceptor();
        requestInjection(interceptor);
        txAnnotations.forEach(it -> {
            bindInterceptor(Matchers.annotatedWith(it), NoSyntheticMatcher.instance(), interceptor);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(it), interceptor);
        });
    }
}

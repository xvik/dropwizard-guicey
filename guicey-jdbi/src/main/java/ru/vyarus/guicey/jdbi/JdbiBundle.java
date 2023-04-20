package ru.vyarus.guicey.jdbi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.dropwizard.Configuration;
import io.dropwizard.db.PooledDataSourceFactory;
import org.skife.jdbi.v2.DBI;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guicey.jdbi.dbi.ConfigAwareProvider;
import ru.vyarus.guicey.jdbi.dbi.SimpleDbiProvider;
import ru.vyarus.guicey.jdbi.installer.MapperInstaller;
import ru.vyarus.guicey.jdbi.installer.repository.RepositoryInstaller;
import ru.vyarus.guicey.jdbi.module.JdbiModule;
import ru.vyarus.guicey.jdbi.tx.InTransaction;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Bundle activates JDBI support. To construct bundle use static builders with dbi or database config providers.
 * For example: {@code JdbiBundle.forDatabase((env, conf) -> conf.getDatabase())}.
 * <p>
 * Bundle introduce unit of work concept for JDBI. All actions must perform inside unit of work. You may use
 * {@link InTransaction} annotation to annotate classes or methods in order to wrap logic with unit of work
 * (and actual transaction). Annotations could be nested: in this case upper most annotation declares transaction and
 * nested are ignored (logic executed inside upper transaction). To declare unit of work without transaction
 * use {@link ru.vyarus.guicey.jdbi.unit.UnitManager}.
 * <p>
 * To manually declare transaction use {@link ru.vyarus.guicey.jdbi.tx.TransactionTemplate} bean.
 * <p>
 * Custom installations:
 * <ul>
 * <li>Classes annotated with {@link ru.vyarus.guicey.jdbi.installer.repository.JdbiRepository} are installed
 * as guice beans, but provides usual functionality as JDBI sql proxies (just no need to always combine them).</li>
 * <li>Classes implementing {@link org.skife.jdbi.v2.tweak.ResultSetMapper} are registered
 * automatically.</li>
 * </ul>
 * <p>
 * Only one bundle instance will be actually used (in case of multiple registrations).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guicey.jdbi.unit.UnitManager for manual unit of work definition
 * @see ru.vyarus.guicey.jdbi.tx.TransactionTemplate for manual work with transactions
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller for sql object
 * customization details
 * @since 4.12.2016
 */
public final class JdbiBundle extends UniqueGuiceyBundle {

    private final ConfigAwareProvider<DBI, ?> dbi;
    private List<Class<? extends Annotation>> txAnnotations = ImmutableList
            .<Class<? extends Annotation>>builder()
            .add(InTransaction.class)
            .build();

    private JdbiBundle(final ConfigAwareProvider<DBI, ?> dbi) {
        this.dbi = dbi;
    }

    /**
     * By default, {@link InTransaction} annotation registered. If you need to use different or more annotations
     * provide all of them. Note, that you will need to provide {@link InTransaction} too if you want to use it too,
     * otherwise it would not be supported.
     *
     * @param txAnnotations annotations to use as transaction annotations
     * @return bundle instance for chained calls
     */
    @SafeVarargs
    public final JdbiBundle withTxAnnotations(final Class<? extends Annotation>... txAnnotations) {
        this.txAnnotations = Lists.newArrayList(txAnnotations);
        return this;
    }

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap.installers(
                RepositoryInstaller.class,
                MapperInstaller.class);
    }

    @Override
    public void run(final GuiceyEnvironment environment) {
        final DBI jdbi = this.dbi.get(environment.configuration(), environment.environment());
        environment.modules(new JdbiModule(jdbi, txAnnotations));
    }

    /**
     * Builds bundle for custom JDBI instance.
     *
     * @param dbi JDBI instance provider
     * @param <C> configuration type
     * @return bundle instance
     */
    public static <C extends Configuration> JdbiBundle forDbi(final ConfigAwareProvider<DBI, C> dbi) {
        return new JdbiBundle(dbi);
    }

    /**
     * Builds bundle, by using only database factory from configuration.
     *
     * @param db  database configuration provider
     * @param <C> configuration type
     * @return bundle instance
     */
    public static <C extends Configuration> JdbiBundle forDatabase(
            final ConfigAwareProvider<PooledDataSourceFactory, C> db) {
        return forDbi(new SimpleDbiProvider<C>(db));
    }
}

package ru.vyarus.guicey.jdbi3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.PooledDataSourceFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guicey.jdbi3.dbi.ConfigAwareProvider;
import ru.vyarus.guicey.jdbi3.dbi.SimpleDbiProvider;
import ru.vyarus.guicey.jdbi3.installer.MapperInstaller;
import ru.vyarus.guicey.jdbi3.installer.repository.JdbiRepository;
import ru.vyarus.guicey.jdbi3.installer.repository.RepositoryInstaller;
import ru.vyarus.guicey.jdbi3.installer.repository.sql.SqlObjectProvider;
import ru.vyarus.guicey.jdbi3.module.JdbiModule;
import ru.vyarus.guicey.jdbi3.tx.InTransaction;
import ru.vyarus.guicey.jdbi3.tx.TransactionTemplate;
import ru.vyarus.guicey.jdbi3.unit.UnitManager;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Bundle activates JDBI3 support. To construct bundle use static builders with jdbi or database config providers.
 * For example: {@code JdbiBundle.forDatabase((env, conf) -> conf.getDatabase())}.
 * <p>
 * Bundle introduce unit of work concept for JDBI. All actions must perform inside unit of work. You may use
 * {@link InTransaction} annotation to annotate classes or methods in order to wrap logic with unit of work
 * (and actual transaction). Annotations could be nested: in this case upper most annotation declares transaction and
 * nested are ignored (logic executed inside upper transaction). To declare unit of work without transaction
 * use {@link UnitManager}.
 * <p>
 * To manually declare transaction use {@link TransactionTemplate} bean.
 * <p>
 * Custom installations:
 * <ul>
 * <li>Classes annotated with {@link JdbiRepository} are installed
 * as guice beans, but provides usual functionality as JDBI sql proxies (just no need to always combine them).</li>
 * <li>Classes implementing {@link org.jdbi.v3.core.mapper.RowMapper} are registered
 * automatically.</li>
 * </ul>
 * <p>
 *  Only one bundle instance will be actually used (in case of multiple registrations).
 *
 * @author Vyacheslav Rusakov
 * @see UnitManager for manual unit of work definition
 * @see TransactionTemplate for manual work with transactions
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller for sql object
 * customization details
 * @since 31.08.2018
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class JdbiBundle extends UniqueGuiceyBundle {

    private final ConfigAwareProvider<Jdbi, ?> jdbi;
    private List<Class<? extends Annotation>> txAnnotations = ImmutableList
            .<Class<? extends Annotation>>builder()
            .add(InTransaction.class)
            .build();
    private List<JdbiPlugin> plugins = Collections.emptyList();
    private Consumer<Jdbi> configurer;
    private boolean eagerInit;

    private JdbiBundle(final ConfigAwareProvider<Jdbi, ?> jdbi) {
        this.jdbi = jdbi;
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

    /**
     * Note that dropwizard registers some plugins (sql objects, guava and jodatime).
     *
     * @param plugins extra jdbi plugins to register
     * @return bundle instance for chained calls
     */
    public JdbiBundle withPlugins(final JdbiPlugin... plugins) {
        this.plugins = Arrays.asList(plugins);
        return this;
    }

    /**
     * Manual jdbi instance configuration. Configuration will be called just after jdbi object creation
     * (on run dropwizard phase), but before guice injector creation.
     *
     * @param configurer configuration action
     * @return bundle instance for chained calls
     */
    public JdbiBundle withConfig(final Consumer<Jdbi> configurer) {
        this.configurer = configurer;
        return this;
    }

    /**
     * By default, repository beans (annotated with {@link JdbiRepository}) are initialized on first method call.
     * Lazy initialization is required to properly add all registered jdbi extensions. Also, this slightly speed
     * up startup.
     * <p>
     * This option will enable eager repositories initialization after application startup. It may be important if
     * execution time of first method call is important (e.g. due to some metrics).
     *
     * @return bundle instance for chained calls
     */
    public JdbiBundle withEagerInitialization() {
        this.eagerInit = true;
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
        final Jdbi jdbi = this.jdbi.get(environment.configuration(), environment.environment());
        plugins.forEach(jdbi::installPlugin);
        if (configurer != null) {
            configurer.accept(jdbi);
        }

        environment.modules(new JdbiModule(jdbi, txAnnotations));
        if (eagerInit) {
            // eager repository proxies creation
            environment.onApplicationStartup(this::performEagerInitialization);
        }
    }

    /**
     * Builds bundle for custom JDBI instance.
     *
     * @param dbi JDBI instance provider
     * @param <C> configuration type
     * @return bundle instance
     */
    public static <C extends Configuration> JdbiBundle forDbi(final ConfigAwareProvider<Jdbi, C> dbi) {
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

    private void performEagerInitialization(final Injector injector) {
        final Set<SqlObjectProvider> proxies = injector.getInstance(
                Key.get(new TypeLiteral<Set<SqlObjectProvider>>() { }, Names.named("jdbi3.proxies")));
        for (Provider<?> proxy : proxies) {
            proxy.get();
        }
    }
}

package ru.vyarus.dropwizard.guice.injector.jersey;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.servlet.RequestScoped;
import org.glassfish.jersey.internal.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.injector.jersey.contract.BindingContractsStorage;
import ru.vyarus.dropwizard.guice.injector.jersey.scope.ThreadScopeModule;
import ru.vyarus.dropwizard.guice.injector.jersey.scope.ThreadScoped;
import ru.vyarus.dropwizard.guice.injector.jersey.util.BindingUtils;
import ru.vyarus.dropwizard.guice.injector.jersey.util.PreInjector;
import ru.vyarus.dropwizard.guice.injector.jersey.util.ProviderSupplierAdapter;
import ru.vyarus.dropwizard.guice.injector.jersey.util.SupplierProvider;
import ru.vyarus.java.generics.resolver.util.GenericsUtils;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import javax.inject.Singleton;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2019
 */
public class JerseyBindingsModule extends AbstractModule {
    private final AbstractBinder bindings;
    private final BindingContractsStorage contracts;
    private final PreInjector preInjector;
    private Logger logger = LoggerFactory.getLogger(JerseyBindingsModule.class);

    private final List<Class<?>> NEVER_BIND_CONTRACT = ImmutableList.of(
            ReaderInterceptor.class,
            WriterInterceptor.class
    ); // todo add other types


    public JerseyBindingsModule(AbstractBinder bindings,
                                BindingContractsStorage contracts,
                                PreInjector preInjector) {
        this.bindings = bindings;
        this.contracts = contracts;
        this.preInjector = preInjector;
    }

    /**
     * Jersey beans could use custom constructors, not annotated with @Inject, so have to manually search for
     * appropriate constructor.
     * <p>
     * todo such constructors use @Context annotated injection and proxies must go there
     * check if request scoped bean could override global bean and if so register all proxies by default as singletons
     *
     * @param type class to analyze
     * @return constructor to use or null
     */
    private static Constructor findConstructor(Class type) {
        if (type.getConstructors().length == 1) {
            return type.getConstructors()[0];
        } else {
            return null;
        }
        // todo detect @context injection points
    }

    @Override
    protected void configure() {
        // thread scope required for jersey
        install(new ThreadScopeModule());

        Collection<Binding> bindings = this.bindings.getBindings();

        for (Binding<?, ?> binding : bindings) {
            // note binding already validated during registration - no need to re-validate (e.g. qualifiers)
            bind(binding);
        }
    }

    private void bind(Binding<?, ? extends Binding> binding) {
        if (logger.isDebugEnabled()) {
            logger.debug("BIND {}", BindingUtils.toStringBinding(binding));
        }

        Key key = BindingUtils.buildKey(binding);
        boolean checkContracts = true;

        ScopedBindingBuilder builder = null;

        if (ClassBinding.class.isAssignableFrom(binding.getClass())) {
            ClassBinding bind = (ClassBinding) binding;

            final Class service = bind.getService();

            if (preInjector.isManuallyInstantiated(service)) {
                // service was forced to be created manually (before injector creation)
                Object instance = preInjector.getManualInstance(service);
                logger.debug("guice binding: bind({}).toInstance(<{}>) // due to pre-creation",
                        BindingUtils.toStringKey(key), TypeToStringUtils.toStringType(instance.getClass()));
                bind(key).toInstance(instance);
            } else {
                // look if no default constructor
                Constructor ctor = findConstructor(service);
                if (ctor != null) {
                    logger.debug("guice binding: bind({}).toConstructor({})", BindingUtils.toStringKey(key), ctor);
                    builder = bind(key).toConstructor(ctor);
                } else {
                    if (binding.getImplementationType() != service) {
                        logger.debug("guice binding: bind({}).to({})", BindingUtils.toStringKey(key), service.getSimpleName());
                        builder = bind(key).to(service);

                    } else {
                        logger.debug("guice binding: bind({})", BindingUtils.toStringKey(key));
                        builder = bind(key);
                    }
                }
            }

        } else if (InstanceBinding.class.isAssignableFrom(binding.getClass())) {
            InstanceBinding bind = (InstanceBinding) binding;

            logger.debug("guice binding: bind({}).toInstance(<{}>)",
                    BindingUtils.toStringKey(key), TypeToStringUtils.toStringType(bind.getService().getClass()));
            bind(key).toInstance(bind.getService());

        } else if (SupplierClassBinding.class.isAssignableFrom(binding.getClass())) {
            SupplierClassBinding bind = (SupplierClassBinding) binding;

            // todo check if it's the only case and always use contract without type guess!!!!
            // for supplier it is possible that contract declares returned type
            checkContracts = false;
            Class supplier = bind.getSupplierClass();

            final SupplierProvider provider = new SupplierProvider(supplier);
            logger.debug("guice binding: bind({}).toProvider(<{}>)",
                    BindingUtils.toStringKey(key), TypeToStringUtils.toStringType(supplier));
            // self-binding is required becuase supplier will get instance inside SupplierProvider
            // and it will fail if requireExplicitBindings() enabled
            bind(supplier);
            builder = bind(key).toProvider(provider);

            for (Type contract : binding.getContracts()) {
                Class boundType = key.getTypeLiteral().getRawType();
                Class contrType = GenericsUtils.resolveClass(contract, EmptyGenericsMap.getInstance());

                // todo contract must be qualified?
                // if not already bound
                if (boundType != contrType && !NEVER_BIND_CONTRACT.contains(contrType)) {
                    final Key key1 = Key.get(contrType);
                    logger.debug("guice binding (supplier contract): bind({}).to(<{}>)", BindingUtils.toStringKey(key1), BindingUtils.toStringKey(key));
                    bind(key1).to(key);
                }
            }

        } else if (SupplierInstanceBinding.class.isAssignableFrom(binding.getClass())) {
            SupplierInstanceBinding bind = (SupplierInstanceBinding) binding;

            Class supplier = bind.getSupplier().getClass();

            checkContracts = false;
            // todo supplier scope

            final ProviderSupplierAdapter provider = new ProviderSupplierAdapter(bind.getSupplier());
            logger.debug("guice binding: bind({}).toProvider(<{}>)", BindingUtils.toStringKey(key), supplier.getSimpleName());
            builder = bind(key).toProvider(provider);

            for (Type contract : binding.getContracts()) {
                Class boundType = key.getTypeLiteral().getRawType();
                Class contrType = GenericsUtils.resolveClass(contract, EmptyGenericsMap.getInstance());

                // todo contract must be qualified?
                // if not already bound
                if (boundType != contrType && !NEVER_BIND_CONTRACT.contains(contrType)) {
                    final Key key1 = Key.get(contrType);
                    logger.debug("guice binding (supplier contract): bind({}).to(<{}>)", BindingUtils.toStringKey(key1), BindingUtils.toStringKey(key));
                    bind(key1).to(key);
                }
            }
        } else if (InjectionResolverBinding.class.isAssignableFrom(binding.getClass())) {
            InjectionResolverBinding bind = (InjectionResolverBinding) binding;

            logger.debug("guice binding: bind({}).toInstance(<{}>)", BindingUtils.toStringKey(key), bind.getResolver().getClass().getSimpleName());
            // bind as InjectionResolver<Something> to instance
            bind(key).toInstance(bind.getResolver());
        }

        if (binding.getScope() != null) {
            Class<? extends Annotation> scope = mapScope(binding.getScope());
            if (scope != null && builder != null) {
                builder.in(scope);
            }
        }

        // bind unique interfaces
        if (checkContracts) {
            for (Type contract : binding.getContracts()) {
                final List<Key> byContract = contracts.findByContract(contract);
                if (byContract.size() == 1) {
                    if (!byContract.get(0).equals(key)) {
                        throw new IllegalStateException("Stored contract key contradict with computed: "
                                + BindingUtils.toStringKey(byContract.get(0)) + " " + BindingUtils.toStringKey(key));
                    }
                    Class target = key.getTypeLiteral().getRawType();
                    Class point = GenericsUtils.resolveClass(contract, EmptyGenericsMap.getInstance());
                    // avoid self-binding
                    if (target != point && !NEVER_BIND_CONTRACT.contains(point)) {
                        final Key<?> key1 = Key.get(contract);
                        logger.debug("guice binding (single contract): bind({}).to(<{}>)", BindingUtils.toStringKey(key1), BindingUtils.toStringKey(key));
                        bind(key1).to(key);
                    }
                }
            }
        }
    }

    private Class<? extends Annotation> mapScope(Class<? extends Annotation> scope) {
        Class<? extends Annotation> res;
        if (scope == PerLookup.class) {
            // default scope in guice
            res = null;
        } else if (scope == PerThread.class) {
            res = ThreadScoped.class;
        } else if (scope == org.glassfish.jersey.process.internal.RequestScoped.class) {
            res = RequestScoped.class;
        } else if (scope == Singleton.class) {
            res = Singleton.class;
        } else {
            throw new IllegalArgumentException("Unknown scope: " + scope);
        }

        return res;
    }
}

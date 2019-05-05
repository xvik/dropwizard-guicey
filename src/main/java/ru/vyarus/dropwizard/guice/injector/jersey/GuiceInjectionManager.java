package ru.vyarus.dropwizard.guice.injector.jersey;

import com.google.common.collect.Iterables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.ServletModule;
import org.glassfish.jersey.internal.inject.*;
import org.glassfish.jersey.process.internal.RequestScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.injector.jersey.context.GuiceContextInjectionResolver;
import ru.vyarus.dropwizard.guice.injector.jersey.contract.BindingContractsStorage;
import ru.vyarus.dropwizard.guice.injector.jersey.util.BindingUtils;
import ru.vyarus.dropwizard.guice.injector.jersey.util.PreInjector;
import ru.vyarus.dropwizard.guice.injector.jersey.web.GuiceRequestScope;
import ru.vyarus.java.generics.resolver.util.GenericsUtils;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import javax.annotation.Priority;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Jersey injection model has a few critical differences with guice injection:
 * 1. There is a notion of contracts, which has to be implemented manually for guice ({@link BindingContractsStorage}).
 * On beans registration contracts are remembered and later instances could be found by contract. Moreover
 * this way allows to find unique contracts, when only one implementation is bound to contract type and so it
 * could be bound directly in guice context (moreover direct injection by interface is often used for such cases).
 * 2. jersey need to inject fields to instances and creates some beans BEFORE registration process finalization.
 * This has to be performed manually with {@link PreInjector}. It is able to inject only already registered instances
 * and create and inject proxies for {@link javax.ws.rs.core.Context} injection points.
 * 3. jersey relies on proxies for @Context injections. This behaviour is simulated.
 *
 * @author Vyacheslav Rusakov
 * @since 23.04.2019
 */
@Priority(1000) // even if hk2 present in classpath this one will be used
public class GuiceInjectionManager implements InjectionManager {
    final BindingContractsStorage contracts = new BindingContractsStorage();
    private final Logger logger = LoggerFactory.getLogger(GuiceInjectionManager.class);
    // Keeps all binders and bindings added to the InjectionManager during the bootstrap.
    private final AbstractBinder bindings = new AbstractBinder() {
        @Override
        protected void configure() {
        }
    };

    private PreInjector preInjector = new PreInjector(this::getInjector);


    private Injector injector;

    @Override
    public void completeRegistration() {
        register(Bindings.service(GuiceRequestScope.class).to(RequestScope.class).in(Singleton.class));
        register(Bindings.service(this).to(InjectionManager.class));
        register(new GuiceContextInjectionResolver.Binder(this::getInjector));
        logger.debug("Injection registration completed");

        // todo servlet module temporary here
        injector = Guice.createInjector(new JerseyBindingsModule(bindings, contracts), new ServletModule());
    }

    @Override
    public void shutdown() {
        logger.debug("Shutdown");
        //todo integrate shutdown notifiaction
    }

    @Override
    public void register(Binding binding) {
        if (injector != null) {
            throw new IllegalStateException("Can't register new bindings after injector creation");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("REGISTER {}", BindingUtils.toStringBinding(binding));
        }
        // there may be duplicate bindings, which is ok for HK2, but not ok for guice
        final Key key = BindingUtils.buildKey(binding);
        if (!contracts.containsKey(key)) {
            bindings.bind(binding);

            if (binding instanceof InstanceBinding) {
                // remember instances for pre-creation injection
                preInjector.register(((InstanceBinding) binding).getService());
            }
        } else {
            logger.debug("Ignore duplicate binding (except contracts)");
        }

        // register contracts (just in case, remember contracts even from duplicate bindings)
        contracts.register(key, binding.getContracts());
    }

    @Override
    public void register(Iterable<Binding> descriptors) {
        if (injector != null) {
            throw new IllegalStateException("Can't register new bindings after injector creation");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("REGISTER bindings collection with {} bindings", Iterables.size(descriptors));
        }
        for (Binding binding : descriptors) {
            register(binding);
        }
        logger.debug("-- Done bindings collection registration");
    }

    @Override
    public void register(Binder binder) {
        if (injector != null) {
            throw new IllegalStateException("Can't register new bindings after injector creation");
        }
        logger.debug("REGISTER binder {} with {} bindings", binder.getClass().getName(), binder.getBindings().size());
        register(Bindings.getBindings(this, binder));
    }

    @Override
    public void register(Object provider) throws IllegalArgumentException {
        if (provider instanceof Class) {
            throw new IllegalArgumentException("Provider can't be class: " + ((Class) provider).getName());
        }
        if (isRegistrable(provider.getClass())) {
            register((Binder)provider);
        } else {
            throw new UnsupportedOperationException("Provider not supported: " + provider.getClass().getName());
        }
    }

    @Override
    public boolean isRegistrable(Class<?> clazz) {
        final boolean res = Binder.class.isAssignableFrom(clazz);
        logger.debug("isRegistrable: {}: {}", clazz.getName(), res);
        return res;
    }

    @Override
    public <T> T createAndInitialize(Class<T> createMe) {
        logger.debug("CREATE AND INIT: {}", createMe.getName());
        T instance;
        if (injector == null) {
            instance = (T) preInjector.create(createMe);
        } else {
            instance = injector.getInstance(createMe);
        }
        // todo post construct call required
        return instance;
    }

    @Override
    public <T> List<ServiceHolder<T>> getAllServiceHolders(Class<T> contractOrImpl, Annotation... qualifiers) {
        if (qualifiers.length > 1) {
            throw new IllegalStateException("Multiple qualifiers not supported: "
                    + BindingUtils.toStringKey(contractOrImpl, qualifiers));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("GET ALL SERVICE HOLDERS for {}", BindingUtils.toStringKey(contractOrImpl, qualifiers));
        }
        Class<? extends Annotation> ann = qualifiers.length > 0 ? qualifiers[0].annotationType() : null;
        Key rootKey = ann != null ? Key.get(contractOrImpl, ann) : Key.get(contractOrImpl);
        List<Key> target = contracts.findByContract(contractOrImpl);
        if (!target.contains(rootKey) && injector.getExistingBinding(rootKey) != null) {
            target.add(rootKey);
        }
        BindingUtils.filterNotQualifiedKeys(target, ann);

        List<ServiceHolder<T>> res = new ArrayList<>();

        for (Key key : target) {
            logger.debug("Obtain gucice instance by key {}", BindingUtils.toStringKey(key));
            final Object instance = injector.getInstance(key);
            // todo rank?
            res.add(new ServiceHolderImpl(instance, key.getTypeLiteral().getRawType(), contracts.getContracts(key), 0));
        }
        return res;
    }

    @Override
    public ForeignDescriptor createForeignDescriptor(Binding binding) {
        if (injector != null) {      //todo sure?
            throw new IllegalStateException("Can't register new bindings after injector creation: "
                    + BindingUtils.toStringBinding(binding));
        }

        if (binding.getQualifiers().size() > 1) {
            throw new IllegalStateException("Multiple qualifiers not supported: "
                    + BindingUtils.toStringBinding(binding));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("CREATE FOREIGN DESCRIPTOR for {}", BindingUtils.toStringBinding(binding));
        }

        Class<? extends Annotation> ann = BindingUtils.extractQualifier(binding.getQualifiers());
        Class<?> clazz;
        if (ClassBinding.class.isAssignableFrom(binding.getClass())) {
            clazz = ((ClassBinding<?>) binding).getService();
        } else if (InstanceBinding.class.isAssignableFrom(binding.getClass())) {
            clazz = ((InstanceBinding<?>) binding).getService().getClass();
        } else {
            throw new RuntimeException(
                    org.glassfish.jersey.internal.LocalizationMessages
                            .UNKNOWN_DESCRIPTOR_TYPE(binding.getClass().getSimpleName()));
        }
        List<Key> keys = contracts.findByContract(clazz);
        BindingUtils.filterNotQualifiedKeys(keys, ann);
        Key key = keys.isEmpty() ? (ann == null ? Key.get(clazz) : Key.get(clazz, ann)) : keys.get(0);
        // todo add impl class itself?
        contracts.register(key, binding.getContracts());
        // todo what about scope?
        return ForeignDescriptor.wrap(key, new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                // here instance could be disposed, which is not supported by guice
            }
        });
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, Annotation... qualifiers) {
        if (logger.isDebugEnabled()) {
            logger.debug("GET INSTANCE {}", BindingUtils.toStringKey(contractOrImpl, qualifiers));
        }
        if (injector == null) {
            throw new IllegalStateException("Can't get instance when injector is not yet created: "
                    + BindingUtils.toStringKey(contractOrImpl, qualifiers));
        }
        if (qualifiers.length > 1) {
            throw new IllegalStateException("Multiple qualifiers not supported: "
                    + BindingUtils.toStringKey(contractOrImpl, qualifiers));
        }
        final Class<? extends Annotation> ann = qualifiers[0].annotationType();
        List<Key> keys = contracts.findByContract(contractOrImpl);
        BindingUtils.filterNotQualifiedKeys(keys, ann);

        Key key = keys.isEmpty() ? Key.get(contractOrImpl, ann) : keys.get(0);
        return (T) injector.getInstance(key);
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl) {
        logger.debug("GET INSTANCE {}", contractOrImpl.getName());
        if (injector == null) {
            return (T) preInjector.create(contractOrImpl);
        }
        List<Key> keys = contracts.findByContract(contractOrImpl);
        Key key = keys.isEmpty() ? Key.get(contractOrImpl) : keys.get(0);
        return (T) injector.getInstance(key);
    }

    @Override
    public <T> T getInstance(Type contractOrImpl) {
        if (logger.isDebugEnabled()) {
            logger.debug("GET INSTANCE {}",
                    TypeToStringUtils.toStringType(contractOrImpl, EmptyGenericsMap.getInstance()));
        }
        if (injector == null) {
            return (T) preInjector.create(GenericsUtils.resolveClass(contractOrImpl, EmptyGenericsMap.getInstance()));
        }
        final Key<?> key = Key.get(contractOrImpl);
        return injector.getExistingBinding(key) != null ? (T) injector.getInstance(key) : null;
    }

    @Override
    public Object getInstance(ForeignDescriptor foreignDescriptor) {
        logger.debug("GET INSTANCE FROM DESCRIPTOR {}", foreignDescriptor.get());
        if (injector == null) {
            throw new IllegalStateException("Can't get instance when injector is not yet created: "
                    + foreignDescriptor.get());
        }
        // todo?
        Key key = (Key) foreignDescriptor.get();
        return injector.getInstance(key);
    }

    @Override
    public <T> List<T> getAllInstances(Type contractOrImpl) {
        if (logger.isDebugEnabled()) {
            logger.debug("GET ALL INSTANCES {}",
                    TypeToStringUtils.toStringType(contractOrImpl, EmptyGenericsMap.getInstance()));
        }
        if (injector == null) {
            throw new IllegalStateException("Can't get instance when injector is not yet created:"
                    + TypeToStringUtils.toStringType(contractOrImpl, EmptyGenericsMap.getInstance()));
        }
        List<T> res = new ArrayList<>();

        Class<?> type = GenericsUtils.resolveClass(contractOrImpl, EmptyGenericsMap.getInstance());

        List<Key> keys = contracts.findByContract(type);
        if (keys.isEmpty()) {
            keys.add(Key.get(contractOrImpl));
        }
        for (Key key : keys) {
            if (injector.getExistingBinding(key) != null) {
                res.add((T) injector.getInstance(key));
            }
        }
        return res;
    }

    @Override
    public void inject(Object injectMe) {
        logger.debug("INJECT {}", injectMe.getClass().getName());
        if (injector == null) {
            preInjector.inject(injectMe);
        } else {
            injector.injectMembers(injectMe);
        }
    }

    @Override
    public void preDestroy(Object preDestroyMe) {
        logger.debug("PRE DESTROY {}", preDestroyMe.getClass().getName());
        // todo?
    }

    @Override
    public void inject(Object injectMe, String classAnalyzer) {
        // TODO: Used only in legacy CDI integration.
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, String classAnalyzer) {
        // TODO: Used only in legacy CDI integration.
        throw new UnsupportedOperationException();
    }

    private Injector getInjector() {
        return injector;
    }

    private Injector get() {return injector;}
}


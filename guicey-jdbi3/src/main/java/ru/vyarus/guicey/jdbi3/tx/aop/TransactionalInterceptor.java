package ru.vyarus.guicey.jdbi3.tx.aop;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ru.vyarus.guicey.jdbi3.tx.TransactionTemplate;
import ru.vyarus.guicey.jdbi3.tx.TxConfig;
import ru.vyarus.guicey.jdbi3.tx.aop.config.TxConfigFactory;
import ru.vyarus.guicey.jdbi3.tx.aop.config.TxConfigSupport;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Intercept transaction annotations usage and applies {@link TransactionTemplate} around method call.
 * Transaction config could be obtained from annotation, if it supports it.
 *
 * @author Vyacheslav Rusakov
 * @since 31.08.2018
 */
public class TransactionalInterceptor implements MethodInterceptor {

    private static final ReentrantLock LOCK = new ReentrantLock();

    private final Map<Class<? extends Annotation>, Class<? extends TxConfigFactory>> txConfigFactories
            = new HashMap<>();
    // cache used to avoid annotations introspection on each call
    private final Map<String, TxConfig> methodCache = new HashMap<>();

    @Inject
    private TransactionTemplate template;
    @Inject
    private Injector injector;

    /**
     * Create a transactional interceptor.
     *
     * @param txAnnotations transactional annotations
     */
    public TransactionalInterceptor(final List<Class<? extends Annotation>> txAnnotations) {
        findConfigurableAnnotations(txAnnotations);
    }

    @Override
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final TxConfig config = checkTxConfig(invocation.getMethod());
        return template.inTransaction(config, handle -> {
            try {
                return invocation.proceed();
            } catch (Throwable throwable) {
                Throwables.throwIfUnchecked(throwable);
                throw new RuntimeException(throwable);
            }
        });
    }

    private void findConfigurableAnnotations(final List<Class<? extends Annotation>> txAnnotations) {
        for (Class<? extends Annotation> ann : txAnnotations) {
            if (ann.isAnnotationPresent(TxConfigSupport.class)) {
                txConfigFactories.put(ann, ann.getAnnotation(TxConfigSupport.class).value());
            }
        }
    }

    private TxConfig checkTxConfig(final Method method) {
        final String methodIdentity = (method.getDeclaringClass().getName() + " " + method.toString()).intern();
        TxConfig cfg = methodCache.get(methodIdentity);
        if (cfg == null) {
            LOCK.lock();
            try {
                if (methodCache.get(methodIdentity) != null) {
                    // cfg could be stored while waiting for lock
                    cfg = methodCache.get(methodIdentity);
                } else {
                    cfg = buildConfig(method);
                    methodCache.put(methodIdentity, cfg);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return cfg;
    }

    @SuppressWarnings("unchecked")
    private TxConfig buildConfig(final Method method) {
        TxConfig res = null;
        if (!txConfigFactories.isEmpty()) {
            // search on method first
            Annotation txAnn = findAnnotation(method, txConfigFactories.keySet());

            if (txAnn == null) {
                // look on type
                txAnn = findAnnotation(method.getDeclaringClass(), txConfigFactories.keySet());
            }

            if (txAnn != null) {
                final TxConfigFactory factory = injector.getInstance(txConfigFactories.get(txAnn.annotationType()));
                res = factory.build(txAnn);
            }
        }
        // using default config to avoid re-introspection
        return res == null ? new TxConfig() : res;
    }

    private Annotation findAnnotation(final AnnotatedElement obj,
                                      final Collection<Class<? extends Annotation>> anns) {
        for (Class<? extends Annotation> ann : anns) {
            if (obj.isAnnotationPresent(ann)) {
                return obj.getAnnotation(ann);
            }
        }
        return null;
    }
}

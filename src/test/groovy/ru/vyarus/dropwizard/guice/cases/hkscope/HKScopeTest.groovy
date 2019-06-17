package ru.vyarus.dropwizard.guice.cases.hkscope

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Scope
import com.google.inject.Scopes
import com.google.inject.spi.DefaultBindingScopingVisitor
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.glassfish.hk2.api.Descriptor
import org.glassfish.hk2.api.Filter
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.inject.hk2.InstanceSupplierFactoryBridge
import org.glassfish.jersey.internal.inject.InjectionManager
import org.glassfish.jersey.internal.inject.InjectionResolver
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.ExtensionsHolder
import ru.vyarus.dropwizard.guice.module.jersey.debug.service.ContextDebugService
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.ParamConverterProvider
import javax.ws.rs.ext.Providers
import java.lang.annotation.Annotation

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@UseDropwizardApp(ScopeApplication)
class HKScopeTest extends AbstractTest {

    @Inject
    ContextDebugService debugService
    @Inject
    Provider<InjectionManager> locator
    @Inject
    Injector injector
    @Inject
    ExtensionsHolder holder

    def "Check jersey extensions registration"() {

        setup: "need to request hk2 resource to force instantiation"
        new URL("http://localhost:8080/hk/foo").getText()
        new URL("http://localhost:8080/guice/foo").getText()

        and: "force jersey extensions load"
        //force jersey to load custom HKContextResolver
        Providers providers = locator.get().getInstance(Providers)
        providers.getContextResolver(null, null)
        locator.get().getAllInstances(ExceptionMapper)
        locator.get().getAllInstances(ParamConverterProvider)
        locator.get().getAllInstances(InjectionResolver)

        expect: "app launched successfully"
        debugService.guiceManaged.size() == debugService.hkManaged.size()

        and: "all guice beans are in forced singleton scope"
        debugService.guiceManaged.find {
            !injector.getExistingBinding(Key.get(it)).acceptScopingVisitor(new DefaultBindingScopingVisitor<Boolean>() {
                @Override
                Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
                    return scopeAnnotation == Singleton
                }

                @Override
                Boolean visitScope(Scope scope) {
                    return scope == Scopes.SINGLETON
                }
            })
        } == null

        and: "all hk2 beans are in forced singleton scope"
        debugService.hkManaged.find {
            def r = locator.get().<ServiceLocator>getInstance(ServiceLocator.class).getBestDescriptor(new Filter() {
                @Override
                boolean matches(Descriptor d) {
                    d.getImplementation() == it.name
                }
            })
            // types self-registered as contract
            assert r.getAdvertisedContracts().contains(it.name)
            r.scopeAnnotation != Singleton
        } == null


        and: "all guice beans are registered in hk2 context as singletons"
        def list = {
            def r = holder.getExtensions(JerseyProviderInstaller)
            r.removeAll(debugService.hkManaged)
            r
        }.call()
        list.find {
            def r = locator.get().<ServiceLocator>getInstance(ServiceLocator.class).getBestDescriptor(new Filter() {
                @Override
                boolean matches(Descriptor d) {
                    d.getAdvertisedContracts().contains(it.name)
                }
            })
            println r.getAdvertisedContracts().join(', ')
            // actually GuiceComponentFactory or LazyGuiceFactory used, but hk2 wraps them into
            assert r.implementation == InstanceSupplierFactoryBridge.name
            r.scope != Singleton.name
        } == null
    }

    static class ScopeApplication extends Application<TestConfiguration> {

        @Override
        void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig("ru.vyarus.dropwizard.guice.cases.hkscope.support")
                    .build())
        }

        @Override
        void run(TestConfiguration configuration, Environment environment) throws Exception {
        }
    }
}
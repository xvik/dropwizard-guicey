package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.util.visitor;

import com.google.inject.Binding;
import com.google.inject.spi.*;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.model.DeclarationType;

import java.util.Collections;

/**
 * Guice SPI model elements visitor.
 *
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class GuiceElementVisitor implements ElementVisitor<BindingDeclaration> {
    private static final GuiceBindingVisitor BINDING_VISITOR = new GuiceBindingVisitor();

    @Override
    public <T> BindingDeclaration visit(final Binding<T> binding) {
        return binding.acceptTargetVisitor(BINDING_VISITOR);
    }

    @Override
    public BindingDeclaration visit(final InterceptorBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.Aop, binding);
        res.setSpecial(binding.getInterceptors());
        return res;
    }

    @Override
    public BindingDeclaration visit(final ScopeBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.Scope, binding);
        res.setScope(binding.getAnnotationType());
        return res;
    }

    @Override
    public BindingDeclaration visit(final TypeConverterBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.TypeConverter, binding);
        res.setSpecial(Collections.singletonList(binding.getTypeConverter()));
        return res;
    }

    @Override
    public BindingDeclaration visit(final InjectionRequest<?> request) {
        // not structure
        return null;
    }

    @Override
    public BindingDeclaration visit(final StaticInjectionRequest request) {
        // not structure
        return null;
    }

    @Override
    public <T> BindingDeclaration visit(final ProviderLookup<T> lookup) {
        // not structure
        return null;
    }

    @Override
    public <T> BindingDeclaration visit(final MembersInjectorLookup<T> lookup) {
        // not structure
        return null;
    }

    @Override
    public BindingDeclaration visit(final Message message) {
        // not structure
        return null;
    }

    @Override
    public BindingDeclaration visit(final PrivateElements elements) {
        // analyze private modules in parallel
        throw new PrivateModuleException(elements);
    }

    @Override
    public BindingDeclaration visit(final TypeListenerBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.TypeListener, binding);
        res.setSpecial(Collections.singletonList(binding.getListener()));
        return res;
    }

    @Override
    public BindingDeclaration visit(final ProvisionListenerBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.ProvisionListener, binding);
        res.setSpecial(binding.getListeners());
        return res;
    }

    @Override
    public BindingDeclaration visit(final RequireExplicitBindingsOption option) {
        // ignore options
        return null;
    }

    @Override
    public BindingDeclaration visit(final DisableCircularProxiesOption option) {
        // ignore options
        return null;
    }

    @Override
    public BindingDeclaration visit(final RequireAtInjectOnConstructorsOption option) {
        // ignore options
        return null;
    }

    @Override
    public BindingDeclaration visit(final RequireExactBindingAnnotationsOption option) {
        // ignore options
        return null;
    }

    @Override
    public BindingDeclaration visit(final ModuleAnnotatedMethodScannerBinding binding) {
        // ignore options
        return null;
    }
}

package ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor;

import com.google.inject.Key;
import com.google.inject.internal.UniqueAnnotations;
import com.google.inject.servlet.*;
import com.google.inject.spi.*;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.DeclarationType;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelUtils;

import java.util.Collections;

/**
 * Guice SPI model bindings visitor.
 * <p>
 * NOTE: Multibindings specific visitor is not implemented because it's useless.
 *
 * @author Vyacheslav Rusakov
 * @since 20.08.2019
 */
public class GuiceBindingVisitor implements ServletModuleTargetVisitor<Object, BindingDeclaration> {

    @Override
    public BindingDeclaration visit(final InstanceBinding binding) {
        // Filter extension bindings, for example created for filter. Such bindings are marker as @Internal
        final Class annotationType = binding.getKey().getAnnotationType();
        if (annotationType != null && annotationType.getDeclaringClass() != null
                && annotationType.getDeclaringClass().equals(UniqueAnnotations.class)) {
            return null;
        }
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.Instance, binding);
        res.setKey(binding.getKey());
        return res;
    }

    @Override
    public BindingDeclaration visit(final ProviderInstanceBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.ProviderInstance, binding);
        res.setKey(binding.getKey());
        res.setProvidedBy(binding.getUserSuppliedProvider().toString());
        return res;
    }

    @Override
    public BindingDeclaration visit(final ProviderKeyBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.ProviderKey, binding);
        res.setKey(binding.getKey());
        res.setProvidedBy(GuiceModelUtils.renderKey(binding.getProviderKey()));
        return res;
    }

    @Override
    public BindingDeclaration visit(final LinkedKeyBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.LinkedKey, binding);
        res.setKey(binding.getKey());
        res.setTarget(binding.getLinkedKey());
        return res;
    }

    @Override
    public BindingDeclaration visit(final ExposedBinding binding) {
        // exposed binding from private module may appear only within injector bindings, but not within elements
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.Exposed, binding);
        res.setKey(binding.getKey());
        return res;
    }

    @Override
    public BindingDeclaration visit(final UntargettedBinding binding) {
        // appear only for elements (module analysis)
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.Untargetted, binding);
        res.setKey(binding.getKey());
        return res;
    }

    @Override
    public BindingDeclaration visit(final ConstructorBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.Binding, binding);
        res.setKey(binding.getKey());
        return res;
    }

    @Override
    public BindingDeclaration visit(final ConvertedConstantBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.ConvertedConstant, binding);
        res.setKey(binding.getKey());
        res.setTarget(binding.getSourceKey());
        // store converter name
        res.setSpecial(Collections.singletonList("converted by "
                + binding.getTypeConverterBinding().getTypeConverter().getClass().getName()));
        return res;
    }

    @Override
    public BindingDeclaration visit(final ProviderBinding binding) {
        /*
         Synthetic provider binding appearing from declaration like {@code bind(..).toProvider(..)} (for right part).
         Ignored because only direct bindings are reported.
         */
        return null;
    }

    // --------------------------------------------------------- Servlets

    @Override
    public BindingDeclaration visit(final LinkedFilterBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.FilterKey, binding);
        res.setKey(binding.getLinkedKey());
        res.setSpecial(Collections.singletonList(binding.getPattern()));
        return res;
    }

    @Override
    public BindingDeclaration visit(final InstanceFilterBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.FilterInstance, binding);
        res.setKey(Key.get(binding.getFilterInstance().getClass()));
        res.setSpecial(Collections.singletonList(binding.getPattern()));
        return res;
    }

    @Override
    public BindingDeclaration visit(final LinkedServletBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.ServletKey, binding);
        res.setKey(binding.getLinkedKey());
        res.setSpecial(Collections.singletonList(binding.getPattern()));
        return res;
    }

    @Override
    public BindingDeclaration visit(final InstanceServletBinding binding) {
        final BindingDeclaration res = new BindingDeclaration(DeclarationType.ServletInstance, binding);
        res.setKey(Key.get(binding.getServletInstance().getClass()));
        res.setSpecial(Collections.singletonList(binding.getPattern()));
        return res;
    }
}

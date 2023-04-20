package ru.vyarus.dropwizard.guice.debug.report.web.util;

import com.google.inject.Key;
import com.google.inject.servlet.*;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import ru.vyarus.dropwizard.guice.debug.report.web.model.WebElementModel;
import ru.vyarus.dropwizard.guice.debug.report.web.model.WebElementType;

/**
 * Visitor for guice servlet and filter bindings detection.
 */
public class ServletVisitor extends DefaultBindingTargetVisitor<Object, WebElementModel>
        implements ServletModuleTargetVisitor<Object, WebElementModel> {

    @Override
    public WebElementModel visit(final LinkedFilterBinding binding) {
        return fillModel(binding, new WebElementModel(WebElementType.FILTER,
                binding.getLinkedKey(), false));
    }

    @Override
    public WebElementModel visit(final InstanceFilterBinding binding) {
        return fillModel(binding, new WebElementModel(WebElementType.FILTER,
                Key.get(binding.getFilterInstance().getClass()), true));
    }

    @Override
    public WebElementModel visit(final LinkedServletBinding binding) {
        return fillModel(binding, new WebElementModel(WebElementType.SERVLET,
                binding.getLinkedKey(), false));
    }

    @Override
    public WebElementModel visit(final InstanceServletBinding binding) {
        return fillModel(binding, new WebElementModel(WebElementType.SERVLET,
                Key.get(binding.getServletInstance().getClass()), true));
    }

    private WebElementModel fillModel(final ServletModuleBinding binding,
                                      final WebElementModel model) {
        model.setPattern(binding.getPattern());
        model.setPatternType(binding.getUriPatternType());
        return model;
    }
}

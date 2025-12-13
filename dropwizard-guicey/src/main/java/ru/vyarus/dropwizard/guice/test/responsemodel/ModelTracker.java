package ru.vyarus.dropwizard.guice.test.responsemodel;

import ru.vyarus.dropwizard.guice.test.responsemodel.intercept.ResponseInterceptorFilter;
import ru.vyarus.dropwizard.guice.test.responsemodel.model.ResponseModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class provides access for intercepted response models. Collected models could be cleared with {@link #clear()}.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
public class ModelTracker {

    private final ResponseInterceptorFilter filter;

    /**
     * Creates a tracker instance.
     *
     * @param filter rest filter instance
     */
    public ModelTracker(final ResponseInterceptorFilter filter) {
        this.filter = filter;
    }

    /**
     * @return all intercepted view models
     */
    public List<ResponseModel> getViewModels() {
        return filter.getInterceptedModels();
    }

    /**
     * @param resource resource to filter models
     * @return all intercepted models for resource
     */
    public List<ResponseModel> getViewModels(final Class<?> resource) {
        return filter.getInterceptedModels().stream()
                .filter(model -> model.getResourceClass().equals(resource))
                .collect(Collectors.toList());
    }

    /**
     * @return last intercepted model
     */
    public ResponseModel getLastModel() {
        return filter.getInterceptedModels().isEmpty() ? null
                : filter.getInterceptedModels().get(filter.getInterceptedModels().size() - 1);
    }

    /**
     * @param resource target resource
     * @return last intercepted model for resource
     */
    public ResponseModel getLastModel(final Class<?> resource) {
        return filter.getInterceptedModels().stream()
                .filter(model -> model.getResourceClass().equals(resource))
                // last element required
                .reduce((viewModel, viewModel2) -> viewModel2)
                .orElse(null);
    }

    /**
     * Clear all collected models.
     */
    public void clear() {
        filter.getInterceptedModels().clear();
    }
}

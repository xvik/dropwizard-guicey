package ru.vyarus.guicey.gsp.views.test.ext;

import ru.vyarus.guicey.gsp.views.test.ext.interceptor.ViewModelFilter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class provides access for intercepted view models. Collected models could be cleared with {@link #clear()}.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2025
 */
public class ViewModelTracker {

    private final ViewModelFilter filter;

    /**
     * Creates a tracker instance.
     *
     * @param filter rest filter instance
     */
    public ViewModelTracker(final ViewModelFilter filter) {
        this.filter = filter;
    }

    /**
     * @return all intercepted view models
     */
    public List<ViewModel> getViewModels() {
        return filter.getInterceptedModels();
    }

    /**
     * @param resource resource to filter models
     * @return all intercepted models for resource
     */
    public List<ViewModel> getViewModels(final Class<?> resource) {
        return filter.getInterceptedModels().stream()
                .filter(model -> model.getResourceClass().equals(resource))
                .collect(Collectors.toList());
    }

    /**
     * @return last intercepted model
     */
    public ViewModel getLastModel() {
        return filter.getInterceptedModels().get(filter.getInterceptedModels().size() - 1);
    }

    /**
     * @param resource target resource
     * @return last intercepted model for resource
     */
    public ViewModel getLastModel(final Class<?> resource) {
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

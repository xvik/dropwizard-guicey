package ru.vyarus.dropwizard.guice.debug.report.web;

import com.google.common.base.Throwables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.UriPatternType;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.core.setup.Environment;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelUtils;
import ru.vyarus.dropwizard.guice.debug.report.web.model.WebElementModel;
import ru.vyarus.dropwizard.guice.debug.report.web.model.WebElementType;
import ru.vyarus.dropwizard.guice.debug.report.web.util.ServletVisitor;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.debug.util.TreeNode;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ModuleItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.jersey.GuiceWebModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;

/**
 * Web mappings report (servlets and filters).
 *
 * @author Vyacheslav Rusakov
 * @since 22.10.2019
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.GodClass"})
public class WebMappingsRenderer implements ReportRenderer<MappingsConfig> {

    private static final ServletVisitor VISITOR = new ServletVisitor();
    private static final String STOPPED = "STOPPED";
    private static final String ASYNC = "async";
    private static final String IDEM = "--\"--";

    private final Environment environment;
    private final List<Module> modules;

    public WebMappingsRenderer(final Environment environment,
                               final GuiceyConfigurationInfo info) {
        this.environment = environment;
        // lookup through all modules
        this.modules = info.getModuleIds().stream()
                .map(it -> info.getData().<ModuleItemInfo>getInfo(it).getInstance())
                .collect(Collectors.toList());
    }

    @Override
    public String renderReport(final MappingsConfig config) {
        final StringBuilder res = new StringBuilder();

        if (config.isMainContext()) {
            renderContext(config, environment.getApplicationContext(), "MAIN", res);
        }
        if (config.isAdminContext()) {
            renderContext(config, environment.getAdminContext(), "ADMIN", res);
        }
        return res.toString();
    }

    private void renderContext(final MappingsConfig config,
                               final MutableServletContextHandler handler,
                               final String name,
                               final StringBuilder res) {
        final TreeNode root = new TreeNode("%s %s", name, handler.getContextPath());
        try {
            final Multimap<String, FilterReference> servletFilters = renderContextFilters(config, handler, root);

            // may be null if server not started and no servlets were registered (Guice Filter is always registered)
            if (handler.getServletHandler().getServletMappings() != null) {
                for (ServletMapping mapping : handler.getServletHandler().getServletMappings()) {
                    final ServletHolder servlet = handler.getServletHandler().getServlet(mapping.getServletName());
                    if (isAllowed(servlet.getClassName(), config)) {
                        renderServlet(mapping,
                                servlet,
                                servletFilters,
                                root);
                    }
                }
            }
        } catch (Exception ex) {
            Throwables.throwIfUnchecked(ex);
            throw new IllegalStateException(ex);
        }
        if (root.hasChildren()) {
            res.append(NEWLINE).append(NEWLINE);
            root.render(res);
        }
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Multimap<String, FilterReference> renderContextFilters(final MappingsConfig config,
                                                                   final MutableServletContextHandler handler,
                                                                   final TreeNode root) throws Exception {
        final Multimap<String, FilterReference> servletFilters = LinkedHashMultimap.create();
        for (FilterMapping mapping : handler.getServletHandler().getFilterMappings()) {
            final FilterHolder holder = handler.getServletHandler().getFilter(mapping.getFilterName());
            // single filter instance used for both contexts and so the name is also the same
            final boolean isGuiceFilter = GuiceWebModule.GUICE_FILTER.equals(mapping.getFilterName());
            if ((isGuiceFilter && !config.isGuiceMappings())
                    || !isAllowed(holder.getClassName(), config)) {
                continue;
            }
            if (mapping.getServletNames() != null && mapping.getServletNames().length > 0) {
                // filters targeting exact servlet are only remembered to be shown below target servlet
                for (String servlet : mapping.getServletNames()) {
                    servletFilters.put(servlet, new FilterReference(mapping, holder));
                }
            } else {
                final TreeNode filter = renderFilter(mapping, holder, root);
                if (isGuiceFilter) {
                    renderGuiceWeb(filter);
                }
            }
        }
        return servletFilters;
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void renderServlet(final ServletMapping mapping,
                               final ServletHolder holder,
                               final Multimap<String, FilterReference> servletFilters,
                               final TreeNode root) throws Exception {
        final List<String> markers = new ArrayList<>();
        boolean first = true;
        for (String path : mapping.getPathSpecs()) {
            markers.clear();
            if (first && !holder.isEnabled()) {
                markers.add("DISABLED");
            }
            // indicate multiple mappings of the same servlet
            String type = IDEM;
            String async = "";
            String stopped = "";
            String name = "";
            if (first) {
                type = RenderUtils.renderClassLine(Class.forName(holder.getClassName()), markers);
                if (holder.isStopped()) {
                    stopped = STOPPED;
                }
                if (holder.isAsyncSupported()) {
                    async = ASYNC;
                }
                name = mapping.getServletName();
            }
            final TreeNode servlet = root.child("servlet    %-20s %-7s %-70s %-10s    %-15s %s",
                    // blank placeholder to match with filters output
                    path, async, type, stopped, "", name);
            if (first) {
                for (FilterReference filter : servletFilters.get(mapping.getServletName())) {
                    renderFilter(filter.getMapping(), filter.getHolder(), servlet);
                }
            }
            first = false;
        }
    }

    private TreeNode renderFilter(final FilterMapping mapping,
                                  final FilterHolder holder,
                                  final TreeNode root) throws Exception {
        // required only guice filter
        TreeNode last = null;
        boolean first = true;
        final boolean servletMapping = mapping.getPathSpecs() == null || mapping.getPathSpecs().length == 0;
        for (String path : servletMapping ? mapping.getServletNames() : mapping.getPathSpecs()) {
            // indicate multiple urls or servlets mapping
            String type = IDEM;
            String async = "";
            String stopped = "";
            String dispatches = "";
            String name = "";
            if (first) {
                type = RenderUtils.renderClassLine(Class.forName(holder.getClassName()));
                if (holder.isStopped()) {
                    stopped = STOPPED;
                }
                if (holder.isAsyncSupported()) {
                    async = ASYNC;
                }
                dispatches = mapping.getDispatcherTypes().toString();
                name = mapping.getFilterName();
            }
            last = root.child("filter     %-20s %-7s %-70s %-10s    %-15s %s",
                    servletMapping ? "" : path, async, type, stopped, dispatches, name);
            first = false;
        }
        return last;
    }

    private void renderGuiceWeb(final TreeNode filter) throws Exception {
        final List<String> servlets = new ArrayList<>();
        final List<String> filters = new ArrayList<>();

        for (Element element : Elements.getElements(Stage.TOOL, modules)) {
            if (!(element instanceof Binding)) {
                continue;
            }
            @SuppressWarnings("unchecked") final WebElementModel model =
                    (WebElementModel) ((Binding) element).acceptTargetVisitor(VISITOR);
            if (model == null) {
                continue;
            }
            final String line = renderGuiceWebElement(model, element);
            if (model.getType().equals(WebElementType.FILTER)) {
                filters.add(line);
            } else {
                servlets.add(line);
            }
        }
        renderGucieWebElements(servlets, filters, filter);
    }

    private String renderGuiceWebElement(final WebElementModel model, final Element element) throws Exception {
        return String.format("%-15s %-20s %-7s %-30s %s",
                model.getType().equals(WebElementType.FILTER) ? "guicefilter" : "guiceservlet",
                model.getPattern(),
                model.getPatternType() == UriPatternType.REGEX ? "regex" : "",
                (model.isInstance() ? "instance of " : "") + GuiceModelUtils.renderKey(model.getKey()),
                RenderUtils.renderClass(Class.forName(BindingUtils.getModules(element).get(0))));
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void renderGucieWebElements(final List<String> servlets,
                                        final List<String> filters,
                                        final TreeNode root) {
        if (servlets.isEmpty() && filters.isEmpty()) {
            return;
        }

        if (!filters.isEmpty()) {
            for (String line : filters) {
                root.child(line);
            }
        }
        if (!servlets.isEmpty()) {
            for (String line : servlets) {
                root.child(line);
            }
        }
    }

    private boolean isAllowed(final String type, final MappingsConfig config) {
        return config.isDropwizardMappings()
                || !(type.startsWith("org.eclipse.jetty")
                || type.startsWith("io.dropwizard") || type.startsWith("com.codahale.metrics"));
    }

    /**
     * Filter mapping and declaration objects holder.
     */
    private static class FilterReference {
        private final FilterMapping mapping;
        private final FilterHolder holder;

        FilterReference(final FilterMapping mapping, final FilterHolder holder) {
            this.mapping = mapping;
            this.holder = holder;
        }

        public FilterMapping getMapping() {
            return mapping;
        }

        public FilterHolder getHolder() {
            return holder;
        }
    }
}

package ru.vyarus.dropwizard.guice.debug.report.web;

import com.google.common.base.Throwables;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.*;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Environment;
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
public class WebMappingsRenderer implements ReportRenderer<Void> {

    private static final ServletVisitor VISITOR = new ServletVisitor();
    private static final String STOPPED = "STOPPED";
    private static final String ASYNC = "async";

    private final Environment environment;
    private final List<Module> modules;

    public WebMappingsRenderer(final Environment environment,
                               final GuiceyConfigurationInfo info) {
        this.environment = environment;
        // lookup through all modules
        this.modules = info.getModules().stream()
                .map(it -> (Module) ((ModuleItemInfo) info.getInfo(it)).getInstance())
                .collect(Collectors.toList());
    }

    @Override
    public String renderReport(final Void config) {
        final StringBuilder res = new StringBuilder();

        renderContext(environment.getApplicationContext(), "MAIN", res);
        renderContext(environment.getAdminContext(), "ADMIN", res);
        return res.toString();
    }

    private void renderContext(final MutableServletContextHandler handler,
                               final String name,
                               final StringBuilder res) {
        final TreeNode root = new TreeNode("%s %s", name, handler.getContextPath());
        try {
            for (FilterMapping mapping : handler.getServletHandler().getFilterMappings()) {
                final TreeNode filter = renderFilter(mapping,
                        handler.getServletHandler().getFilter(mapping.getFilterName()), root);
                // single filter instance used for both contexts and so the name is also the same
                if (mapping.getFilterName().equals(GuiceWebModule.GUICE_FILTER)) {
                    renderGuiceWeb(filter);
                }
            }

            for (ServletMapping mapping : handler.getServletHandler().getServletMappings()) {
                renderServlet(mapping,
                        handler.getServletHandler().getServlet(mapping.getServletName()), root);
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

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_INFERRED")
    private void renderServlet(final ServletMapping mapping,
                               final ServletHolder holder,
                               final TreeNode root) throws Exception {
        final List<String> markers = new ArrayList<>();
        for (String path : mapping.getPathSpecs()) {
            markers.clear();
            if (!holder.isEnabled()) {
                markers.add("DISABLED");
            }
            if (!holder.isAvailable()) {
                markers.add(STOPPED);
            }
            root.child("servlet    %-20s %-7s %s",
                    path,
                    holder.isAsyncSupported() ? ASYNC : "",
                    RenderUtils.renderClassLine(Class.forName(holder.getClassName()), markers));
        }
    }

    private TreeNode renderFilter(final FilterMapping mapping,
                                  final FilterHolder holder,
                                  final TreeNode root) throws Exception {
        TreeNode last = null;
        final List<String> markers = new ArrayList<>();
        for (String path : mapping.getPathSpecs().length == 0 ? mapping.getServletNames() : mapping.getPathSpecs()) {
            markers.clear();
            if (!holder.isStarted()) {
                markers.add(STOPPED);
            }
            last = root.child("filter     %-20s %-7s %-50s    %s",
                    path,
                    holder.isAsyncSupported() ? ASYNC : "",
                    RenderUtils.renderClassLine(Class.forName(holder.getClassName()), markers),
                    mapping.getDispatcherTypes());
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
            final WebElementModel model = (WebElementModel) ((Binding) element).acceptTargetVisitor(VISITOR);
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
        return String.format("%-10s %-20s %-7s %-30s %s",
                model.getType().equals(WebElementType.FILTER) ? "filter" : "servlet",
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
}

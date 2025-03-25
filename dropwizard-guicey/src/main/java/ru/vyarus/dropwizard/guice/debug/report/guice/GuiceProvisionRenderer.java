package ru.vyarus.dropwizard.guice.debug.report.guice;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Binding;
import ru.vyarus.dropwizard.guice.debug.report.guice.model.BindingDeclaration;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.GuiceModelUtils;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor.GuiceBindingVisitor;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;
import ru.vyarus.java.generics.resolver.util.map.EmptyGenericsMap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders guice beans provision (creation) time. The report shows all created beans (including multiple times)
 * with all related binding keys. The report highlights JIT bindings to simplify searching for injection point
 * declaration mistakes (forgotten qualifier).
 * <p>
 * The report tries to guess incorrect JIT bindings: if there are qualified bindings (with annotation or generic)
 * of the same type exists, then such JIT binding considered suspicious and rendered before the main report.
 *
 * @author Vyacheslav Rusakov
 * @since 24.03.2025
 */
public class GuiceProvisionRenderer {
    private static final GuiceBindingVisitor BINDING_VISITOR = new GuiceBindingVisitor();
    private static final int MAX_PROVISIONS = 5;

    public String render(final ListMultimap<Binding<?>, Duration> provisions) {
        final List<Provision> parsed = process(provisions);
        final StringBuilder res = new StringBuilder(1000);
        res.append('\n');

        // reveal mappings of the same type, but with different modifiers
        // (as example, injecting a configuration object without @Config annotation)
        final Multimap<Class<?>, Provision> suspicious = findSuspicious(parsed);
        if (!suspicious.isEmpty()) {
            res.append("\n\tPossible mistakes (unqualified JIT bindings):\n");

            suspicious.keySet().stream()
                    .sorted(Comparator.comparing(Class::getSimpleName))
                    .forEach(type -> {
                        res.append("\n\t\t @Inject ")
                                .append(TypeToStringUtils.toStringType(type, EmptyGenericsMap.getInstance()))
                                .append(":\n");

                        suspicious.get(type).stream()
                                .sorted(Comparator.comparing(Provision::getKey))
                                .forEach(provision -> res
                                        .append(String.format("\t\t\t%s ",
                                                provision.getDeclaration().getSource() == null ? '>' : ' '))
                                        .append(renderProvision("", provision)));
                    });
        }

        res.append("\n\tOverall ").append(countBeans(parsed)).append(" provisions took ")
                .append(PrintUtils.ms(countOverall(parsed))).append('\n');
        for (Provision provision : parsed) {
            res.append(renderProvision("\t\t", provision));
        }

        return res.toString();
    }

    private List<Provision> process(final ListMultimap<Binding<?>, Duration> provisions) {
        final List<Provision> res = new ArrayList<>();

        for (Binding<?> binding : provisions.keySet()) {
            final BindingDeclaration declaration = binding.acceptTargetVisitor(BINDING_VISITOR);
            declaration.setScope(GuiceModelUtils.getScope(binding));
            declaration.setSource(GuiceModelUtils.renderSource(binding));

            res.add(new Provision(GuiceModelUtils.renderKey(binding.getKey()),
                    binding, new ArrayList<>(provisions.get(binding)), declaration));
        }

        // the slowest provisions go first
        // in case of multiple provisions, the first one would be the slowest
        final Comparator<Provision> comparing = Comparator
                .comparing(Provision::getOverall);
        res.sort(comparing.reversed());

        return res;
    }

    private Duration countOverall(final List<Provision> provisions) {
        Duration total = Duration.ZERO;
        for (Provision provision : provisions) {
            total = total.plus(provision.getOverall());
        }
        return total;
    }

    private int countBeans(final List<Provision> provisions) {
        int total = 0;
        for (Provision provision : provisions) {
            total += provision.getProvisions().size();
        }
        return total;
    }

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    private String renderProvision(final String prefix, final Provision provision) {
        final BindingDeclaration dec = provision.getDeclaration();
        String time = PrintUtils.ms(provision.getOverall());
        if (provision.getProvisions().size() > 1) {
            time += " (" + provision.getProvisions().stream()
                    .limit(MAX_PROVISIONS)
                    .map(PrintUtils::ms)
                    .collect(Collectors.joining(" + "));
            if (provision.getProvisions().size() > MAX_PROVISIONS) {
                time += " + ...";
            }
            time += ")";
        }
        return String.format("%s%-20s %-16s %-80s %-4s : %-10s \t\t %s%n",
                prefix,
                dec.getSource() == null ? "JIT" : dec.getType().name().toLowerCase(),
                dec.getScope() != null ? "[@" + dec.getScope().getSimpleName() + ']' : "",
                provision.getKey(),
                provision.getProvisions().size() > 1 ? "x" + provision.getProvisions().size() : "",
                time,
                dec.getSource() != null ? dec.getSource() : "");
    }

    private Multimap<Class<?>, Provision> findSuspicious(final List<Provision> provisions) {
        final Multimap<Class<?>, Provision> res = HashMultimap.create();

        provisions.forEach(provision ->
                res.put(provision.getBinding().getKey().getTypeLiteral().getRawType(), provision));

        res.keySet().removeIf(type -> {
            if (res.get(type).size() == 1) {
                return true;
            }

            // at least one is a jit binding (others would be annotated)
            boolean notAnnotated = true;
            for (Provision provision : res.get(type)) {
                notAnnotated = notAnnotated
                        // jit with annotation is not possible
                        && provision.getBinding().getKey().getAnnotation() != null;
            }
            return notAnnotated;
        });

        return res;
    }

    private static class Provision {
        private final String key;
        private final Binding<?> binding;
        private final List<Duration> provisions;
        private final BindingDeclaration declaration;
        private final Duration overall;

        Provision(final String key,
                  final Binding<?> binding,
                  final List<Duration> provisions,
                  final BindingDeclaration declaration) {
            this.key = key;
            this.binding = binding;
            this.provisions = provisions;
            this.declaration = declaration;
            Duration overall = Duration.ZERO;
            for (Duration duration : provisions) {
                overall = overall.plus(duration);
            }
            this.overall = overall;
        }

        public String getKey() {
            return key;
        }

        public Binding<?> getBinding() {
            return binding;
        }

        public List<Duration> getProvisions() {
            return provisions;
        }

        public BindingDeclaration getDeclaration() {
            return declaration;
        }

        public Duration getOverall() {
            return overall;
        }
    }
}

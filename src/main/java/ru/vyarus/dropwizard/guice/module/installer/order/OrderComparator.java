package ru.vyarus.dropwizard.guice.module.installer.order;

import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for classes annotated with {@link Order} annotation.
 * If class doesn't annotated then class placed at the end.
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
public class OrderComparator implements Comparator<Class>, Serializable {

    @Override
    public int compare(final Class o1, final Class o2) {
        final Order orderAnnotation1 = FeatureUtils.getAnnotation(o1, Order.class);
        final Order orderAnnotation2 = FeatureUtils.getAnnotation(o2, Order.class);
        final Integer order1 = orderAnnotation1 != null ? orderAnnotation1.value() : Integer.MAX_VALUE;
        final Integer order2 = orderAnnotation2 != null ? orderAnnotation2.value() : Integer.MAX_VALUE;
        return order1.compareTo(order2);
    }
}

package ru.vyarus.dropwizard.guice.unit;

import com.google.common.primitives.Primitives;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Vyacheslav Rusakov
 * @since 14.12.2025
 */
public class InstanceUtilsTest {

    @Test
    void testDummyObjects() {
        test(boolean.class);
        test(int.class);
        test(char.class);
        test(double.class);
        test(float.class);
        test(byte.class);
        test(short.class);
        test(long.class);

        test(boolean[].class);
        test(int[].class);
        test(char[].class);
        test(double[].class);
        test(float[].class);
        test(byte[].class);
        test(short[].class);
        test(long[].class);

        test(String.class);
        test(String[].class);

        test(Boolean.class);
        test(Integer.class);
        test(Character.class);
        test(Double.class);
        test(Float.class);
        test(Byte.class);
        test(Short.class);
        test(Long.class);

        test(Boolean[].class);
        test(Integer[].class);
        test(Character[].class);
        test(Double[].class);
        test(Float[].class);
        test(Byte[].class);
        test(Short[].class);
        test(Long[].class);

        test(Object.class);
        test(Object[].class);

        test(Obj.class);
        test(Obj[].class);

        test(Iface.class);
        test(Iface[].class);

        test(Abstr.class);
        test(Abstr[].class);

        test(List.class);
        test(Set.class);
        test(Collection.class);

        test(BigDecimal.class);
    }

    @Test
    void testConstructors() {
        
        InstanceUtils.createWithDummyArgs(SampleCtor.class, byte.class);
        InstanceUtils.createWithDummyArgs(SampleCtor.class, Byte.class);
        InstanceUtils.createWithDummyArgs(SampleCtor.class, List.class);
        InstanceUtils.createWithDummyArgs(SampleCtor.class, Obj.class);
        InstanceUtils.createWithDummyArgs(SampleCtor.class, Iface.class);
        InstanceUtils.createWithDummyArgs(SampleCtor.class, Abstr.class);
    }

    @Test
    void testBadObject() {
        final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class,
                () -> InstanceUtils.createDummyInstance(BadObject.class));
        Assertions.assertEquals("Failed to create dummy instance of ru.vyarus.dropwizard.guice.unit.InstanceUtilsTest$BadObject " +
                "with constructor (String)", ex.getMessage());
    }

    public void test(Class<?> type) {
        Object res = InstanceUtils.createDummyInstance(type);
        Assertions.assertNotNull(res);
        Class<?> sourceType = Primitives.wrap(type);
        Class<?> targetType = res.getClass();
        if (type.isArray()) {
            sourceType = sourceType.getComponentType();
            Assertions.assertTrue(targetType.isArray());
            targetType = targetType.getComponentType();
        }
        System.out.println(type.getSimpleName() + " = " + res
                + "  (" + sourceType.getName() + " | " + targetType.getName() + ")");
        Assertions.assertTrue(sourceType.isAssignableFrom(targetType));
    }

    public static class Obj {
        private String name;

        public Obj(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public interface Iface {

        void doSomething();
    }

    public static abstract class Abstr {}

    public static class SampleCtor {

        public SampleCtor(byte b) {
        }

        public SampleCtor(Byte b) {
        }

        public SampleCtor(List<Integer> b) {}

        public SampleCtor(Obj b) {}

        public SampleCtor(Iface b) {}

        public SampleCtor(Abstr b) {}
    }

    public static class BadObject {

        public BadObject(String num) {
            Integer.parseInt(num);
        }
    }
}

package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.TestFieldUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;

import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 19.02.2025
 */
public class FieldsSearchTest {

    @Test
    void testAnnotatedFieldsSearch() {

        final List<AnnotatedField<MockBean, Object>> fields = TestFieldUtils
                .findAnnotatedFields(Root.class, MockBean.class, Object.class);

        Assertions.assertEquals(6, fields.size());

        final List<AnnotatedField<MockBean, Object>> own = TestFieldUtils.getTestOwnFields(fields);
        Assertions.assertEquals(3, own.size());
        Assertions.assertEquals(0, own.stream()
                .filter(field -> field.getDeclaringClass() == Base.class).count());

        final List<AnnotatedField<MockBean, Object>> base = TestFieldUtils.getInheritedFields(fields);
        Assertions.assertEquals(3, base.size());
        Assertions.assertEquals(0, base.stream()
                .filter(field -> field.getDeclaringClass() == Root.class).count());
    }

    @Test
    void testBaseTypeValidation() {

        try {
            TestFieldUtils.findAnnotatedFields(Root.class, MockBean.class, Base.class);
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock1 annotated with @MockBean, but its type is not Base", e.getMessage());
        }
    }

    @Test
    void testFieldValidations() {
        final List<AnnotatedField<MockBean, Object>> fields = TestFieldUtils
                .findAnnotatedFields(Base.class, MockBean.class, Object.class);

        AnnotatedField<MockBean, Object> bmock1 = fields.stream()
                .filter(field -> field.getName().equals("bmock1"))
                .findFirst().get();

        Assertions.assertEquals("r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock1 (@MockBean static Service)", bmock1.toString());

        bmock1.requireStatic();

        try {
            bmock1.requireNonStatic();
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock1 annotated with @MockBean, must not be static", e.getMessage());
        }


        AnnotatedField<MockBean, Object> bmock2 = fields.stream()
                .filter(field -> field.getName().equals("bmock2"))
                .findFirst().get();

        Assertions.assertEquals("r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock2 (@MockBean Service)", bmock2.toString());

        bmock2.requireNonStatic();

        try {
            bmock2.requireStatic();
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock2 annotated with @MockBean, must be static", e.getMessage());
        }
    }

    @Test
    void testStaticValueValidation() {
        final List<AnnotatedField<MockBean, Object>> fields = TestFieldUtils
                .findAnnotatedFields(Base.class, MockBean.class, Object.class);

        AnnotatedField<MockBean, Object> bmock1 = fields.stream()
                .filter(field -> field.getName().equals("bmock1"))
                .findFirst().get();

        bmock1.setValue(null, new Service());
        // instance ignored
        bmock1.setValue(new Base(), new Service());
        bmock1.setValue(new Object(), new Service());

        Assertions.assertNotNull(bmock1.getValue(null));
        Assertions.assertNotNull(bmock1.getValue(new Object()));

        try {
            bmock1.setValue(null, new Object());
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Can not set static ru.vyarus.dropwizard.guice.test.jupiter.setup.FieldsSearchTest$Service field ru.vyarus.dropwizard.guice.test.jupiter.setup.FieldsSearchTest$Base.bmock1 to java.lang.Object", e.getMessage());
        }

    }

    @Test
    void testNonStaticValueValidation() {
        final List<AnnotatedField<MockBean, Object>> fields = TestFieldUtils
                .findAnnotatedFields(Base.class, MockBean.class, Object.class);

        AnnotatedField<MockBean, Object> bmock2 = fields.stream()
                .filter(field -> field.getName().equals("bmock2"))
                .findFirst().get();

        try {
            bmock2.setValue(null, new Service());
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock2 is not static: test instance required for setting value", e.getMessage());
        }

        bmock2.setValue(new Base(), new Service());

        try {
            bmock2.setValue(new Object(), new Service());
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Invalid instance provided: class java.lang.Object for field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock2", e.getMessage());
        }

        try {
            Assertions.assertNotNull(bmock2.getValue(null));
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock2 is not static: test instance required for obtaining value", e.getMessage());
        }

        try {
            Assertions.assertNotNull(bmock2.getValue(new Object()));
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Invalid instance provided: class java.lang.Object for field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock2", e.getMessage());
        }
    }

    @Test
    void testFiledValueChangeDetection() throws Exception {
        final List<AnnotatedField<MockBean, Object>> fields = TestFieldUtils
                .findAnnotatedFields(Base.class, MockBean.class, Object.class);

        AnnotatedField<MockBean, Object> bmock1 = fields.stream()
                .filter(field -> field.getName().equals("bmock1"))
                .findFirst().get();

        bmock1.setValue(null, new Service());

        // manual override
        bmock1.getField().set(null, new Service());

        try {
            bmock1.checkValueNotChanged(null);
            Assertions.fail();
        } catch (IllegalStateException e) {
            Assertions.assertEquals("Field r.v.d.g.t.j.s.FieldsSearchTest$Base.bmock1 annotated with @MockBean value was changed: " +
                    "most likely, it happen in test setup method, which is called after Injector startup and so too late to change " +
                    "binding values. Manual initialization is possible in field directly.", e.getMessage());
        }
    }


    @Test
    void testIgnoreChanges() {
        final List<AnnotatedField<MockBean, Object>> fields = TestFieldUtils
                .findAnnotatedFields(Base.class, MockBean.class, Object.class);

        AnnotatedField<MockBean, Object> bmock1 = fields.stream()
                .filter(field -> field.getName().equals("bmock1"))
                .findFirst().get();

        bmock1.setValue(null, new Service());

        bmock1.setIgnoreChanges(true);
        Assertions.assertTrue(bmock1.isIgnoreChanges());
        bmock1.setValue(null, null);
        Assertions.assertNotNull(bmock1.getValue(null));

        bmock1.setIgnoreChanges(false);
        bmock1.setValue(null, null);
        Assertions.assertNull(bmock1.getValue(null));

        bmock1.setIgnoreChanges(true);
        bmock1.setCustomData("TEST", 1);
        Assertions.assertNull(bmock1.getCustomData("TEST"));

        bmock1.setIgnoreChanges(false);
        bmock1.setCustomData("TEST", 1);
        Assertions.assertEquals(1, bmock1.<Integer>getCustomData("TEST"));

        bmock1.setIgnoreChanges(true);
        bmock1.clearCustomData();
        Assertions.assertEquals(1, bmock1.<Integer>getCustomData("TEST"));

        bmock1.setIgnoreChanges(false);
        bmock1.clearCustomData();
        Assertions.assertNull(bmock1.<Integer>getCustomData("TEST"));

    }

    public static class Base {

        @MockBean
        static Service bmock1;

        @MockBean
        Service bmock2;

        @MockBean
        private static Service bmock3;
    }

    public static class Root extends Base {
        @MockBean
        static Service mock1;

        @MockBean
        Service mock2;

        @MockBean
        private static Service mock3;
    }

    public static class Service {}
}

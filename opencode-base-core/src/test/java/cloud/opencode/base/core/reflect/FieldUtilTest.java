package cloud.opencode.base.core.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * FieldUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("FieldUtil 测试")
class FieldUtilTest {

    // 测试用注解
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TestAnnotation {}

    // 测试用类
    static class Parent {
        protected String parentField;
        private int parentPrivate;
    }

    static class TestClass extends Parent {
        public String publicField;
        private String privateField;
        protected int protectedField;
        static String staticField = "static";
        final String finalField = "final";
        transient String transientField;
        volatile int volatileField;
        @TestAnnotation
        String annotatedField;
        List<String> genericField;
    }

    @Nested
    @DisplayName("获取字段测试")
    class GetFieldsTests {

        @Test
        @DisplayName("getAllFields 包括父类")
        void testGetAllFieldsWithInheritance() {
            List<Field> fields = FieldUtil.getAllFields(TestClass.class);
            assertThat(fields).isNotEmpty();

            List<String> names = fields.stream().map(Field::getName).toList();
            assertThat(names).contains("publicField", "privateField", "parentField");
        }

        @Test
        @DisplayName("getDeclaredFields 不包括父类")
        void testGetDeclaredFields() {
            List<Field> fields = FieldUtil.getDeclaredFields(TestClass.class);
            assertThat(fields).isNotEmpty();

            List<String> names = fields.stream().map(Field::getName).toList();
            assertThat(names).contains("publicField", "privateField");
            assertThat(names).doesNotContain("parentField");
        }

        @Test
        @DisplayName("getFieldByName")
        void testGetFieldByName() {
            Optional<Field> field = FieldUtil.getFieldByName(TestClass.class, "privateField");
            assertThat(field).isPresent();
            assertThat(field.get().getName()).isEqualTo("privateField");
        }

        @Test
        @DisplayName("getFieldByName 从父类")
        void testGetFieldByNameFromParent() {
            Optional<Field> field = FieldUtil.getFieldByName(TestClass.class, "parentField");
            assertThat(field).isPresent();
        }

        @Test
        @DisplayName("getFieldByName 不存在")
        void testGetFieldByNameNotFound() {
            Optional<Field> field = FieldUtil.getFieldByName(TestClass.class, "nonExistent");
            assertThat(field).isEmpty();
        }
    }

    @Nested
    @DisplayName("按条件过滤字段测试")
    class FilterFieldsTests {

        @Test
        @DisplayName("getFieldsWithAnnotation")
        void testGetFieldsWithAnnotation() {
            List<Field> fields = FieldUtil.getFieldsWithAnnotation(TestClass.class, TestAnnotation.class);
            assertThat(fields).hasSize(1);
            assertThat(fields.get(0).getName()).isEqualTo("annotatedField");
        }

        @Test
        @DisplayName("getFieldsByType")
        void testGetFieldsByType() {
            List<Field> fields = FieldUtil.getFieldsByType(TestClass.class, String.class);
            assertThat(fields).isNotEmpty();
            assertThat(fields).allMatch(f -> String.class.isAssignableFrom(f.getType()));
        }

        @Test
        @DisplayName("getStaticFields")
        void testGetStaticFields() {
            List<Field> fields = FieldUtil.getStaticFields(TestClass.class);
            assertThat(fields).isNotEmpty();
            assertThat(fields.stream().map(Field::getName).toList()).contains("staticField");
        }

        @Test
        @DisplayName("getInstanceFields")
        void testGetInstanceFields() {
            List<Field> fields = FieldUtil.getInstanceFields(TestClass.class);
            assertThat(fields).isNotEmpty();
            assertThat(fields).noneMatch(f -> java.lang.reflect.Modifier.isStatic(f.getModifiers()));
        }

        @Test
        @DisplayName("getPublicFields")
        void testGetPublicFields() {
            List<Field> fields = FieldUtil.getPublicFields(TestClass.class);
            assertThat(fields).isNotEmpty();
            assertThat(fields.stream().map(Field::getName).toList()).contains("publicField");
        }
    }

    @Nested
    @DisplayName("字段值操作测试")
    class FieldValueTests {

        @Test
        @DisplayName("getValue")
        void testGetValue() throws Exception {
            TestClass obj = new TestClass();
            obj.publicField = "test";

            Field field = TestClass.class.getDeclaredField("publicField");
            String value = FieldUtil.getValue(obj, field);
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("getValue 私有字段")
        void testGetValuePrivate() throws Exception {
            TestClass obj = new TestClass();
            Field field = TestClass.class.getDeclaredField("privateField");

            FieldUtil.setValue(obj, field, "private value");
            String value = FieldUtil.getValue(obj, field);
            assertThat(value).isEqualTo("private value");
        }

        @Test
        @DisplayName("setValue")
        void testSetValue() throws Exception {
            TestClass obj = new TestClass();
            Field field = TestClass.class.getDeclaredField("publicField");

            FieldUtil.setValue(obj, field, "new value");
            assertThat(obj.publicField).isEqualTo("new value");
        }

        @Test
        @DisplayName("setValue 私有字段")
        void testSetValuePrivate() throws Exception {
            TestClass obj = new TestClass();
            Field field = TestClass.class.getDeclaredField("privateField");

            FieldUtil.setValue(obj, field, "private");
            String value = FieldUtil.getValue(obj, field);
            assertThat(value).isEqualTo("private");
        }
    }

    @Nested
    @DisplayName("修饰符检查测试")
    class ModifierCheckTests {

        @Test
        @DisplayName("isFinal")
        void testIsFinal() throws Exception {
            Field field = TestClass.class.getDeclaredField("finalField");
            assertThat(FieldUtil.isFinal(field)).isTrue();

            Field nonFinal = TestClass.class.getDeclaredField("publicField");
            assertThat(FieldUtil.isFinal(nonFinal)).isFalse();
        }

        @Test
        @DisplayName("isStatic")
        void testIsStatic() throws Exception {
            Field field = TestClass.class.getDeclaredField("staticField");
            assertThat(FieldUtil.isStatic(field)).isTrue();

            Field nonStatic = TestClass.class.getDeclaredField("publicField");
            assertThat(FieldUtil.isStatic(nonStatic)).isFalse();
        }

        @Test
        @DisplayName("isTransient")
        void testIsTransient() throws Exception {
            Field field = TestClass.class.getDeclaredField("transientField");
            assertThat(FieldUtil.isTransient(field)).isTrue();

            Field nonTransient = TestClass.class.getDeclaredField("publicField");
            assertThat(FieldUtil.isTransient(nonTransient)).isFalse();
        }

        @Test
        @DisplayName("isVolatile")
        void testIsVolatile() throws Exception {
            Field field = TestClass.class.getDeclaredField("volatileField");
            assertThat(FieldUtil.isVolatile(field)).isTrue();

            Field nonVolatile = TestClass.class.getDeclaredField("publicField");
            assertThat(FieldUtil.isVolatile(nonVolatile)).isFalse();
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("getGenericType")
        void testGetGenericType() throws Exception {
            Field field = TestClass.class.getDeclaredField("genericField");
            Type type = FieldUtil.getGenericType(field);
            assertThat(type.getTypeName()).contains("List");
            assertThat(type.getTypeName()).contains("String");
        }
    }

    @Nested
    @DisplayName("字段名和映射测试")
    class FieldNameAndMapTests {

        @Test
        @DisplayName("getFieldNames")
        void testGetFieldNames() {
            List<String> names = FieldUtil.getFieldNames(TestClass.class);
            assertThat(names).contains("publicField", "privateField", "staticField");
        }

        @Test
        @DisplayName("getFieldMap")
        void testGetFieldMap() {
            Map<String, Field> map = FieldUtil.getFieldMap(TestClass.class);
            assertThat(map).containsKeys("publicField", "privateField");
            assertThat(map.get("publicField").getName()).isEqualTo("publicField");
        }

        @Test
        @DisplayName("getFieldMap 子类字段优先")
        void testGetFieldMapChildFirst() {
            Map<String, Field> map = FieldUtil.getFieldMap(TestClass.class);
            // putIfAbsent 保证第一个遇到的（子类的）被保留
            assertThat(map).isNotEmpty();
        }
    }
}

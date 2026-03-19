package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenFieldTest Tests
 * OpenFieldTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenField 测试")
class OpenFieldTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenField.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getField方法测试")
    class GetFieldTests {

        @Test
        @DisplayName("获取字段")
        void testGetField() {
            Field field = OpenField.getField(TestClass.class, "name");
            assertThat(field).isNotNull();
            assertThat(field.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("获取继承的字段")
        void testGetFieldInherited() {
            Field field = OpenField.getField(ChildClass.class, "parentField");
            assertThat(field).isNotNull();
        }

        @Test
        @DisplayName("字段不存在抛出异常")
        void testGetFieldNotFound() {
            assertThatThrownBy(() -> OpenField.getField(TestClass.class, "nonexistent"))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("不强制访问时获取字段")
        void testGetFieldNoForceAccess() {
            Field field = OpenField.getField(TestClass.class, "name", false);
            assertThat(field).isNotNull();
        }
    }

    @Nested
    @DisplayName("getDeclaredField方法测试")
    class GetDeclaredFieldTests {

        @Test
        @DisplayName("获取声明的字段")
        void testGetDeclaredField() {
            Field field = OpenField.getDeclaredField(TestClass.class, "name");
            assertThat(field).isNotNull();
        }

        @Test
        @DisplayName("获取继承字段抛出异常")
        void testGetDeclaredFieldInherited() {
            assertThatThrownBy(() -> OpenField.getDeclaredField(ChildClass.class, "parentField"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getAllFields方法测试")
    class GetAllFieldsTests {

        @Test
        @DisplayName("获取所有字段")
        void testGetAllFields() {
            List<Field> fields = OpenField.getAllFields(ChildClass.class);
            List<String> names = fields.stream().map(Field::getName).toList();
            assertThat(names).contains("childField", "parentField");
        }
    }

    @Nested
    @DisplayName("getDeclaredFields方法测试")
    class GetDeclaredFieldsTests {

        @Test
        @DisplayName("获取声明的字段列表")
        void testGetDeclaredFields() {
            List<Field> fields = OpenField.getDeclaredFields(TestClass.class);
            assertThat(fields).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getFieldsWithAnnotation方法测试")
    class GetFieldsWithAnnotationTests {

        @Test
        @DisplayName("获取带注解的字段")
        void testGetFieldsWithAnnotation() {
            List<Field> fields = OpenField.getFieldsWithAnnotation(AnnotatedClass.class, Deprecated.class);
            assertThat(fields).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getFieldsOfType方法测试")
    class GetFieldsOfTypeTests {

        @Test
        @DisplayName("获取指定类型的字段")
        void testGetFieldsOfType() {
            List<Field> fields = OpenField.getFieldsOfType(TestClass.class, String.class);
            assertThat(fields).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getFieldsWithModifiers方法测试")
    class GetFieldsWithModifiersTests {

        @Test
        @DisplayName("获取指定修饰符的字段")
        void testGetFieldsWithModifiers() {
            List<Field> fields = OpenField.getFieldsWithModifiers(ModifierClass.class, Modifier.STATIC);
            assertThat(fields).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("readField方法测试")
    class ReadFieldTests {

        @Test
        @DisplayName("读取字段值")
        void testReadField() {
            TestClass obj = new TestClass();
            obj.name = "test";
            Object value = OpenField.readField(obj, "name");
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("读取字段值（带类型）")
        void testReadFieldWithType() {
            TestClass obj = new TestClass();
            obj.name = "test";
            String value = OpenField.readField(obj, "name", String.class);
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("读取null字段值")
        void testReadFieldNull() {
            TestClass obj = new TestClass();
            String value = OpenField.readField(obj, "name", String.class);
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("类型转换失败抛出异常")
        void testReadFieldTypeMismatch() {
            TestClass obj = new TestClass();
            obj.name = "test";
            assertThatThrownBy(() -> OpenField.readField(obj, "name", Integer.class))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("使用Field对象读取")
        void testReadFieldByFieldObject() throws Exception {
            TestClass obj = new TestClass();
            obj.name = "test";
            Field field = TestClass.class.getDeclaredField("name");
            Object value = OpenField.readField(field, obj);
            assertThat(value).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("readStaticField方法测试")
    class ReadStaticFieldTests {

        @Test
        @DisplayName("读取静态字段值")
        void testReadStaticField() {
            Object value = OpenField.readStaticField(ModifierClass.class, "CONSTANT");
            assertThat(value).isEqualTo("constant");
        }

        @Test
        @DisplayName("读取静态字段值（带类型）")
        void testReadStaticFieldWithType() {
            String value = OpenField.readStaticField(ModifierClass.class, "CONSTANT", String.class);
            assertThat(value).isEqualTo("constant");
        }
    }

    @Nested
    @DisplayName("writeField方法测试")
    class WriteFieldTests {

        @Test
        @DisplayName("写入字段值")
        void testWriteField() {
            TestClass obj = new TestClass();
            OpenField.writeField(obj, "name", "newValue");
            assertThat(obj.name).isEqualTo("newValue");
        }

        @Test
        @DisplayName("使用Field对象写入")
        void testWriteFieldByFieldObject() throws Exception {
            TestClass obj = new TestClass();
            Field field = TestClass.class.getDeclaredField("name");
            OpenField.writeField(field, obj, "newValue");
            assertThat(obj.name).isEqualTo("newValue");
        }
    }

    @Nested
    @DisplayName("writeStaticField方法测试")
    class WriteStaticFieldTests {

        @Test
        @DisplayName("写入静态字段值")
        void testWriteStaticField() {
            String original = ModifierClass.staticField;
            OpenField.writeStaticField(ModifierClass.class, "staticField", "newStatic");
            assertThat(ModifierClass.staticField).isEqualTo("newStatic");
            ModifierClass.staticField = original;
        }
    }

    @Nested
    @DisplayName("字段信息测试")
    class FieldInfoTests {

        @Test
        @DisplayName("获取字段类型")
        void testGetFieldType() {
            Class<?> type = OpenField.getFieldType(TestClass.class, "name");
            assertThat(type).isEqualTo(String.class);
        }

        @Test
        @DisplayName("获取字段泛型类型")
        void testGetFieldGenericType() {
            var type = OpenField.getFieldGenericType(GenericFieldClass.class, "list");
            assertThat(type.getTypeName()).contains("List");
        }

        @Test
        @DisplayName("获取字段TypeToken")
        void testGetFieldTypeToken() {
            var token = OpenField.getFieldTypeToken(TestClass.class, "name");
            assertThat(token.getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("检查字段是否存在")
        void testHasField() {
            assertThat(OpenField.hasField(TestClass.class, "name")).isTrue();
            assertThat(OpenField.hasField(TestClass.class, "nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("字段遍历测试")
    class IterationTests {

        @Test
        @DisplayName("forEach遍历")
        void testForEach() {
            int[] count = {0};
            OpenField.forEach(TestClass.class, f -> count[0]++);
            assertThat(count[0]).isGreaterThan(0);
        }

        @Test
        @DisplayName("findFirst查找")
        void testFindFirst() {
            Optional<Field> field = OpenField.findFirst(TestClass.class, f -> f.getName().equals("name"));
            assertThat(field).isPresent();
        }

        @Test
        @DisplayName("stream流操作")
        void testStream() {
            long count = OpenField.stream(TestClass.class).count();
            assertThat(count).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("批量操作测试")
    class BatchOperationsTests {

        @Test
        @DisplayName("读取所有字段到Map")
        void testReadFieldsToMap() {
            TestClass obj = new TestClass();
            obj.name = "test";
            obj.age = 25;
            Map<String, Object> values = OpenField.readFieldsToMap(obj);
            assertThat(values).containsEntry("name", "test");
            assertThat(values).containsEntry("age", 25);
        }

        @Test
        @DisplayName("读取指定字段")
        void testReadFields() {
            TestClass obj = new TestClass();
            obj.name = "test";
            Map<String, Object> values = OpenField.readFields(obj, "name");
            assertThat(values).containsEntry("name", "test");
        }

        @Test
        @DisplayName("从Map写入字段")
        void testWriteFieldsFromMap() {
            TestClass obj = new TestClass();
            Map<String, Object> values = Map.of("name", "test", "age", 30);
            OpenField.writeFieldsFromMap(obj, values);
            assertThat(obj.name).isEqualTo("test");
            assertThat(obj.age).isEqualTo(30);
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestClass {
        String name;
        int age;
    }

    @SuppressWarnings("unused")
    static class ParentClass {
        String parentField;
    }

    static class ChildClass extends ParentClass {
        @SuppressWarnings("unused")
        String childField;
    }

    @SuppressWarnings("unused")
    static class AnnotatedClass {
        @Deprecated
        String annotatedField;
        String normalField;
    }

    @SuppressWarnings("unused")
    static class ModifierClass {
        static final String CONSTANT = "constant";
        static String staticField = "static";
        String instanceField;
    }

    @SuppressWarnings("unused")
    static class GenericFieldClass {
        java.util.List<String> list;
    }
}

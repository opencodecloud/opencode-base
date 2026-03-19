package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FieldUtilTest Tests
 * FieldUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("FieldUtil 测试")
class FieldUtilTest {

    @BeforeEach
    void setUp() {
        FieldUtil.clearCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = FieldUtil.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getDeclaredFields方法测试")
    class GetDeclaredFieldsTests {

        @Test
        @DisplayName("获取声明的字段")
        void testGetDeclaredFields() {
            Field[] fields = FieldUtil.getDeclaredFields(TestClass.class);
            assertThat(fields).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getAllFields方法测试")
    class GetAllFieldsTests {

        @Test
        @DisplayName("获取所有字段包括继承的")
        void testGetAllFields() {
            List<Field> fields = FieldUtil.getAllFields(ChildClass.class);
            // Should include both child and parent fields
            assertThat(fields).isNotEmpty();
            List<String> fieldNames = fields.stream().map(Field::getName).toList();
            assertThat(fieldNames).contains("childField", "parentField");
        }

        @Test
        @DisplayName("结果被缓存")
        void testGetAllFieldsCached() {
            List<Field> fields1 = FieldUtil.getAllFields(TestClass.class);
            List<Field> fields2 = FieldUtil.getAllFields(TestClass.class);
            assertThat(fields1).isSameAs(fields2);
        }
    }

    @Nested
    @DisplayName("getField方法测试")
    class GetFieldTests {

        @Test
        @DisplayName("按名称获取字段")
        void testGetField() {
            Field field = FieldUtil.getField(TestClass.class, "name");
            assertThat(field).isNotNull();
            assertThat(field.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("获取继承的字段")
        void testGetFieldInherited() {
            Field field = FieldUtil.getField(ChildClass.class, "parentField");
            assertThat(field).isNotNull();
        }

        @Test
        @DisplayName("字段不存在返回null")
        void testGetFieldNotFound() {
            Field field = FieldUtil.getField(TestClass.class, "nonexistent");
            assertThat(field).isNull();
        }
    }

    @Nested
    @DisplayName("getFieldOrThrow方法测试")
    class GetFieldOrThrowTests {

        @Test
        @DisplayName("获取存在的字段")
        void testGetFieldOrThrowExists() {
            Field field = FieldUtil.getFieldOrThrow(TestClass.class, "name");
            assertThat(field).isNotNull();
        }

        @Test
        @DisplayName("字段不存在抛出异常")
        void testGetFieldOrThrowNotFound() {
            assertThatThrownBy(() -> FieldUtil.getFieldOrThrow(TestClass.class, "nonexistent"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getFields方法测试")
    class GetFieldsPredicateTests {

        @Test
        @DisplayName("按条件过滤字段")
        void testGetFieldsWithPredicate() {
            List<Field> fields = FieldUtil.getFields(TestClass.class,
                    f -> f.getType() == String.class);
            assertThat(fields).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getFieldsWithAnnotation方法测试")
    class GetFieldsWithAnnotationTests {

        @Test
        @DisplayName("获取带注解的字段")
        void testGetFieldsWithAnnotation() {
            List<Field> fields = FieldUtil.getFieldsWithAnnotation(AnnotatedClass.class, Deprecated.class);
            assertThat(fields).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getFieldsOfType方法测试")
    class GetFieldsOfTypeTests {

        @Test
        @DisplayName("获取特定类型的字段")
        void testGetFieldsOfType() {
            List<Field> fields = FieldUtil.getFieldsOfType(TestClass.class, String.class);
            assertThat(fields).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getInstanceFields方法测试")
    class GetInstanceFieldsTests {

        @Test
        @DisplayName("获取实例字段")
        void testGetInstanceFields() {
            List<Field> fields = FieldUtil.getInstanceFields(TestClassWithStatic.class);
            assertThat(fields).allSatisfy(f ->
                    assertThat(java.lang.reflect.Modifier.isStatic(f.getModifiers())).isFalse()
            );
        }
    }

    @Nested
    @DisplayName("getStaticFields方法测试")
    class GetStaticFieldsTests {

        @Test
        @DisplayName("获取静态字段")
        void testGetStaticFields() {
            List<Field> fields = FieldUtil.getStaticFields(TestClassWithStatic.class);
            assertThat(fields).allSatisfy(f ->
                    assertThat(java.lang.reflect.Modifier.isStatic(f.getModifiers())).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("getValue方法测试")
    class GetValueTests {

        @Test
        @DisplayName("获取字段值")
        void testGetValue() throws Exception {
            TestClass obj = new TestClass();
            obj.name = "test";

            Field field = TestClass.class.getDeclaredField("name");
            Object value = FieldUtil.getValue(field, obj);

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("获取字段值并转型")
        void testGetValueWithType() throws Exception {
            TestClass obj = new TestClass();
            obj.name = "test";

            Field field = TestClass.class.getDeclaredField("name");
            String value = FieldUtil.getValue(field, obj, String.class);

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("类型转换失败抛出异常")
        void testGetValueTypeCastFailed() throws Exception {
            TestClass obj = new TestClass();
            obj.name = "test";

            Field field = TestClass.class.getDeclaredField("name");

            assertThatThrownBy(() -> FieldUtil.getValue(field, obj, Integer.class))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("setValue方法测试")
    class SetValueTests {

        @Test
        @DisplayName("设置字段值")
        void testSetValue() throws Exception {
            TestClass obj = new TestClass();
            Field field = TestClass.class.getDeclaredField("name");

            FieldUtil.setValue(field, obj, "newValue");

            assertThat(obj.name).isEqualTo("newValue");
        }
    }

    @Nested
    @DisplayName("getStaticValue方法测试")
    class GetStaticValueTests {

        @Test
        @DisplayName("获取静态字段值")
        void testGetStaticValue() throws Exception {
            Field field = TestClassWithStatic.class.getDeclaredField("CONSTANT");
            Object value = FieldUtil.getStaticValue(field);
            assertThat(value).isEqualTo("constant");
        }
    }

    @Nested
    @DisplayName("setStaticValue方法测试")
    class SetStaticValueTests {

        @Test
        @DisplayName("设置静态字段值")
        void testSetStaticValue() throws Exception {
            Field field = TestClassWithStatic.class.getDeclaredField("staticField");
            FieldUtil.setStaticValue(field, "newStatic");
            assertThat(TestClassWithStatic.staticField).isEqualTo("newStatic");
            // Reset
            TestClassWithStatic.staticField = "static";
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取字段类型")
        void testGetType() throws Exception {
            Field field = TestClass.class.getDeclaredField("name");
            assertThat(FieldUtil.getType(field)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getGenericType方法测试")
    class GetGenericTypeTests {

        @Test
        @DisplayName("获取字段泛型类型")
        void testGetGenericType() throws Exception {
            Field field = GenericFieldClass.class.getDeclaredField("list");
            Type genericType = FieldUtil.getGenericType(field);
            assertThat(genericType.getTypeName()).contains("List");
        }
    }

    @Nested
    @DisplayName("isStatic方法测试")
    class IsStaticTests {

        @Test
        @DisplayName("检查静态字段")
        void testIsStatic() throws Exception {
            Field staticField = TestClassWithStatic.class.getDeclaredField("staticField");
            Field instanceField = TestClassWithStatic.class.getDeclaredField("instanceField");

            assertThat(FieldUtil.isStatic(staticField)).isTrue();
            assertThat(FieldUtil.isStatic(instanceField)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFinal方法测试")
    class IsFinalTests {

        @Test
        @DisplayName("检查final字段")
        void testIsFinal() throws Exception {
            Field finalField = TestClassWithStatic.class.getDeclaredField("CONSTANT");
            Field nonFinalField = TestClassWithStatic.class.getDeclaredField("instanceField");

            assertThat(FieldUtil.isFinal(finalField)).isTrue();
            assertThat(FieldUtil.isFinal(nonFinalField)).isFalse();
        }
    }

    @Nested
    @DisplayName("isTransient方法测试")
    class IsTransientTests {

        @Test
        @DisplayName("检查transient字段")
        void testIsTransient() throws Exception {
            Field transientField = TransientClass.class.getDeclaredField("transientField");
            Field normalField = TransientClass.class.getDeclaredField("normalField");

            assertThat(FieldUtil.isTransient(transientField)).isTrue();
            assertThat(FieldUtil.isTransient(normalField)).isFalse();
        }
    }

    @Nested
    @DisplayName("isVolatile方法测试")
    class IsVolatileTests {

        @Test
        @DisplayName("检查volatile字段")
        void testIsVolatile() throws Exception {
            Field volatileField = VolatileClass.class.getDeclaredField("volatileField");
            Field normalField = VolatileClass.class.getDeclaredField("normalField");

            assertThat(FieldUtil.isVolatile(volatileField)).isTrue();
            assertThat(FieldUtil.isVolatile(normalField)).isFalse();
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除所有缓存")
        void testClearCache() {
            FieldUtil.getAllFields(TestClass.class);
            FieldUtil.clearCache();
            // Should not throw
        }

        @Test
        @DisplayName("清除特定类的缓存")
        void testClearCacheForClass() {
            FieldUtil.getAllFields(TestClass.class);
            FieldUtil.clearCache(TestClass.class);
            // Should not throw
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
    static class TestClassWithStatic {
        static final String CONSTANT = "constant";
        static String staticField = "static";
        String instanceField;
    }

    @SuppressWarnings("unused")
    static class GenericFieldClass {
        List<String> list;
    }

    @SuppressWarnings("unused")
    static class TransientClass {
        transient String transientField;
        String normalField;
    }

    @SuppressWarnings("unused")
    static class VolatileClass {
        volatile String volatileField;
        String normalField;
    }
}

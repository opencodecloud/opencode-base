package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import cloud.opencode.base.reflect.type.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenReflectTest Tests
 * OpenReflectTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenReflect 测试")
class OpenReflectTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenReflect.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("类操作测试")
    class ClassOperationsTests {

        @Test
        @DisplayName("按名称加载类")
        void testForName() {
            Class<?> clazz = OpenReflect.forName("java.lang.String");
            assertThat(clazz).isEqualTo(String.class);
        }

        @Test
        @DisplayName("安全加载类")
        void testForNameSafe() {
            Optional<Class<?>> clazz = OpenReflect.forNameSafe("java.lang.String");
            assertThat(clazz).isPresent();
            assertThat(clazz.get()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("类不存在时forNameSafe返回空")
        void testForNameSafeNotFound() {
            Optional<Class<?>> clazz = OpenReflect.forNameSafe("com.nonexistent.Class");
            assertThat(clazz).isEmpty();
        }

        @Test
        @DisplayName("检查类是否存在")
        void testClassExists() {
            assertThat(OpenReflect.classExists("java.lang.String")).isTrue();
            assertThat(OpenReflect.classExists("com.nonexistent.Class")).isFalse();
        }

        @Test
        @DisplayName("创建TypeToken")
        void testTypeOf() {
            TypeToken<String> token = OpenReflect.typeOf(String.class);
            assertThat(token.getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("字段操作测试")
    class FieldOperationsTests {

        @Test
        @DisplayName("获取字段")
        void testGetField() {
            Field field = OpenReflect.getField(TestClass.class, "name");
            assertThat(field).isNotNull();
        }

        @Test
        @DisplayName("获取所有字段")
        void testGetAllFields() {
            List<Field> fields = OpenReflect.getAllFields(TestClass.class);
            assertThat(fields).isNotEmpty();
        }

        @Test
        @DisplayName("读取字段值")
        void testReadField() {
            TestClass obj = new TestClass();
            obj.name = "test";
            Object value = OpenReflect.readField(obj, "name");
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("写入字段值")
        void testWriteField() {
            TestClass obj = new TestClass();
            OpenReflect.writeField(obj, "name", "newValue");
            assertThat(obj.name).isEqualTo("newValue");
        }
    }

    @Nested
    @DisplayName("方法操作测试")
    class MethodOperationsTests {

        @Test
        @DisplayName("获取方法")
        void testGetMethod() {
            Method method = OpenReflect.getMethod(TestClass.class, "getName");
            assertThat(method).isNotNull();
        }

        @Test
        @DisplayName("获取所有方法")
        void testGetAllMethods() {
            List<Method> methods = OpenReflect.getAllMethods(TestClass.class);
            assertThat(methods).isNotEmpty();
        }

        @Test
        @DisplayName("调用方法")
        void testInvokeMethod() {
            TestClass obj = new TestClass();
            obj.name = "test";
            Object result = OpenReflect.invokeMethod(obj, "getName");
            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("调用静态方法")
        void testInvokeStaticMethod() {
            Object result = OpenReflect.invokeStaticMethod(StaticMethodClass.class, "staticMethod");
            assertThat(result).isEqualTo("static");
        }
    }

    @Nested
    @DisplayName("构造器操作测试")
    class ConstructorOperationsTests {

        @Test
        @DisplayName("获取构造器")
        void testGetConstructor() {
            Constructor<TestClass> constructor = OpenReflect.getConstructor(TestClass.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("创建实例")
        void testNewInstance() {
            TestClass instance = OpenReflect.newInstance(TestClass.class);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("创建实例（带参数）")
        void testNewInstanceWithArgs() {
            TestClass instance = OpenReflect.newInstance(TestClass.class, "test");
            assertThat(instance).isNotNull();
            assertThat(instance.name).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("注解操作测试")
    class AnnotationOperationsTests {

        @Test
        @DisplayName("获取注解")
        void testGetAnnotation() {
            Deprecated annotation = OpenReflect.getAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("查找注解")
        void testFindAnnotation() {
            Optional<Deprecated> annotation = OpenReflect.findAnnotation(DeprecatedClass.class, Deprecated.class);
            assertThat(annotation).isPresent();
        }

        @Test
        @DisplayName("检查注解存在")
        void testHasAnnotation() {
            assertThat(OpenReflect.hasAnnotation(DeprecatedClass.class, Deprecated.class)).isTrue();
            assertThat(OpenReflect.hasAnnotation(TestClass.class, Deprecated.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("修饰符操作测试")
    class ModifierOperationsTests {

        @Test
        @DisplayName("检查是否public")
        void testIsPublic() throws Exception {
            Field publicField = ModifierClass.class.getDeclaredField("publicField");
            Field privateField = ModifierClass.class.getDeclaredField("privateField");
            assertThat(OpenReflect.isPublic(publicField)).isTrue();
            assertThat(OpenReflect.isPublic(privateField)).isFalse();
        }

        @Test
        @DisplayName("检查是否private")
        void testIsPrivate() throws Exception {
            Field privateField = ModifierClass.class.getDeclaredField("privateField");
            assertThat(OpenReflect.isPrivate(privateField)).isTrue();
        }

        @Test
        @DisplayName("检查是否static")
        void testIsStatic() throws Exception {
            Field staticField = ModifierClass.class.getDeclaredField("staticField");
            assertThat(OpenReflect.isStatic(staticField)).isTrue();
        }

        @Test
        @DisplayName("检查是否final")
        void testIsFinal() throws Exception {
            Field finalField = ModifierClass.class.getDeclaredField("CONSTANT");
            assertThat(OpenReflect.isFinal(finalField)).isTrue();
        }
    }

    @Nested
    @DisplayName("类型信息测试")
    class TypeInfoTests {

        @Test
        @DisplayName("检查是否原始类型")
        void testIsPrimitive() {
            assertThat(OpenReflect.isPrimitive(int.class)).isTrue();
            assertThat(OpenReflect.isPrimitive(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("检查是否包装类型")
        void testIsWrapper() {
            assertThat(OpenReflect.isWrapper(Integer.class)).isTrue();
            assertThat(OpenReflect.isWrapper(int.class)).isFalse();
        }

        @Test
        @DisplayName("检查是否Record")
        void testIsRecord() {
            record TestRecord(String name) {
            }
            assertThat(OpenReflect.isRecord(TestRecord.class)).isTrue();
            assertThat(OpenReflect.isRecord(String.class)).isFalse();
        }

        @Test
        @DisplayName("检查是否密封类")
        void testIsSealed() {
            assertThat(OpenReflect.isSealed(String.class)).isFalse();
        }

        @Test
        @DisplayName("原始类型转包装类型")
        void testPrimitiveToWrapper() {
            assertThat(OpenReflect.primitiveToWrapper(int.class)).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("包装类型转原始类型")
        void testWrapperToPrimitive() {
            assertThat(OpenReflect.wrapperToPrimitive(Integer.class)).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("类层次测试")
    class ClassHierarchyTests {

        @Test
        @DisplayName("获取所有父类")
        void testGetAllSuperclasses() {
            List<Class<?>> superclasses = OpenReflect.getAllSuperclasses(Integer.class);
            assertThat(superclasses).contains(Number.class);
        }

        @Test
        @DisplayName("获取所有接口")
        void testGetAllInterfaces() {
            List<Class<?>> interfaces = OpenReflect.getAllInterfaces(Integer.class);
            assertThat(interfaces).contains(Comparable.class);
        }

        @Test
        @DisplayName("获取类层次结构")
        void testGetClassHierarchy() {
            List<Class<?>> hierarchy = OpenReflect.getClassHierarchy(Integer.class);
            assertThat(hierarchy).contains(Integer.class, Number.class);
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityTests {

        @Test
        @DisplayName("设置可访问")
        void testMakeAccessible() throws Exception {
            // Use a Constructor to avoid canAccess(null) issue with instance members
            Constructor<TestClass> constructor = TestClass.class.getDeclaredConstructor(String.class);
            Constructor<TestClass> accessible = OpenReflect.makeAccessible(constructor);
            // Verify it's accessible by invoking it
            TestClass obj = accessible.newInstance("test");
            assertThat(obj.getName()).isEqualTo("test");
        }

        @Test
        @DisplayName("检查是否同包")
        void testIsSamePackage() {
            assertThat(OpenReflect.isSamePackage(String.class, Integer.class)).isTrue();
            assertThat(OpenReflect.isSamePackage(String.class, java.util.List.class)).isFalse();
        }

        @Test
        @DisplayName("获取简单名称")
        void testGetSimpleName() {
            assertThat(OpenReflect.getSimpleName(String.class)).isEqualTo("String");
            assertThat(OpenReflect.getSimpleName(String[].class)).isEqualTo("String[]");
        }

        @Test
        @DisplayName("获取规范名称")
        void testGetCanonicalNameOrName() {
            assertThat(OpenReflect.getCanonicalNameOrName(String.class)).isEqualTo("java.lang.String");
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestClass {
        String name;

        public TestClass() {
        }

        public TestClass(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Deprecated
    static class DeprecatedClass {
    }

    @SuppressWarnings("unused")
    static class StaticMethodClass {
        public static String staticMethod() {
            return "static";
        }
    }

    @SuppressWarnings("unused")
    static class ModifierClass {
        public static final String CONSTANT = "const";
        public static String staticField;
        public String publicField;
        private String privateField;
    }
}

package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenConstructorTest Tests
 * OpenConstructorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenConstructor 测试")
class OpenConstructorTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenConstructor.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getConstructor方法测试")
    class GetConstructorTests {

        @Test
        @DisplayName("获取默认构造器")
        void testGetDefaultConstructor() {
            Constructor<TestClass> constructor = OpenConstructor.getConstructor(TestClass.class);
            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("获取带参数的构造器")
        void testGetConstructorWithParams() {
            Constructor<TestClass> constructor = OpenConstructor.getConstructor(TestClass.class, String.class);
            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("构造器不存在抛出异常")
        void testGetConstructorNotFound() {
            assertThatThrownBy(() -> OpenConstructor.getConstructor(TestClass.class, Long.class))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("不强制访问时获取构造器")
        void testGetConstructorNoForceAccess() {
            Constructor<TestClass> constructor = OpenConstructor.getConstructor(TestClass.class, false);
            assertThat(constructor).isNotNull();
        }
    }

    @Nested
    @DisplayName("getMatchingConstructor方法测试")
    class GetMatchingConstructorTests {

        @Test
        @DisplayName("精确匹配")
        void testGetMatchingConstructorExact() {
            Constructor<TestClass> constructor = OpenConstructor.getMatchingConstructor(TestClass.class, String.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("兼容匹配")
        void testGetMatchingConstructorCompatible() {
            Constructor<TestClass> constructor = OpenConstructor.getMatchingConstructor(TestClass.class, Integer.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("无匹配抛出异常")
        void testGetMatchingConstructorNotFound() {
            assertThatThrownBy(() -> OpenConstructor.getMatchingConstructor(TestClass.class, Double.class))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getDefaultConstructor方法测试")
    class GetDefaultConstructorTests {

        @Test
        @DisplayName("获取默认构造器")
        void testGetDefaultConstructor() {
            Constructor<TestClass> constructor = OpenConstructor.getDefaultConstructor(TestClass.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("无默认构造器抛出异常")
        void testGetDefaultConstructorNotFound() {
            assertThatThrownBy(() -> OpenConstructor.getDefaultConstructor(NoDefaultConstructorClass.class))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getConstructors方法测试")
    class GetConstructorsTests {

        @Test
        @DisplayName("获取所有构造器")
        void testGetConstructors() {
            List<Constructor<TestClass>> constructors = OpenConstructor.getConstructors(TestClass.class);
            assertThat(constructors).hasSizeGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getConstructorsWithAnnotation方法测试")
    class GetConstructorsWithAnnotationTests {

        @Test
        @DisplayName("获取带注解的构造器")
        void testGetConstructorsWithAnnotation() {
            List<Constructor<AnnotatedConstructorClass>> constructors =
                    OpenConstructor.getConstructorsWithAnnotation(AnnotatedConstructorClass.class, Deprecated.class);
            assertThat(constructors).hasSize(1);
        }
    }

    @Nested
    @DisplayName("newInstance方法测试")
    class NewInstanceTests {

        @Test
        @DisplayName("使用默认构造器创建实例")
        void testNewInstanceDefault() {
            TestClass instance = OpenConstructor.newInstance(TestClass.class);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("使用参数创建实例")
        void testNewInstanceWithArgs() {
            TestClass instance = OpenConstructor.newInstance(TestClass.class, "test");
            assertThat(instance).isNotNull();
            assertThat(instance.getValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("使用指定参数类型创建实例")
        void testNewInstanceWithParamTypes() {
            TestClass instance = OpenConstructor.newInstance(TestClass.class, new Class[]{String.class}, "test");
            assertThat(instance).isNotNull();
            assertThat(instance.getValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("使用构造器对象创建实例")
        void testNewInstanceWithConstructor() {
            Constructor<TestClass> ctor = OpenConstructor.getConstructor(TestClass.class, String.class);
            TestClass instance = OpenConstructor.newInstance(ctor, "test");
            assertThat(instance).isNotNull();
            assertThat(instance.getValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("强制创建实例")
        void testNewInstanceForced() {
            PrivateConstructorClass instance = OpenConstructor.newInstanceForced(PrivateConstructorClass.class, "test");
            assertThat(instance).isNotNull();
        }
    }

    @Nested
    @DisplayName("hasDefaultConstructor方法测试")
    class HasDefaultConstructorTests {

        @Test
        @DisplayName("检查是否有默认构造器")
        void testHasDefaultConstructor() {
            assertThat(OpenConstructor.hasDefaultConstructor(TestClass.class)).isTrue();
            assertThat(OpenConstructor.hasDefaultConstructor(NoDefaultConstructorClass.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasConstructor方法测试")
    class HasConstructorTests {

        @Test
        @DisplayName("检查是否有指定构造器")
        void testHasConstructor() {
            assertThat(OpenConstructor.hasConstructor(TestClass.class, String.class)).isTrue();
            assertThat(OpenConstructor.hasConstructor(TestClass.class, Long.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getParameterNames方法测试")
    class GetParameterNamesTests {

        @Test
        @DisplayName("获取参数名称")
        void testGetParameterNames() {
            Constructor<TestClass> ctor = OpenConstructor.getConstructor(TestClass.class, String.class);
            List<String> names = OpenConstructor.getParameterNames(ctor);
            assertThat(names).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getParameterTypes方法测试")
    class GetParameterTypesTests {

        @Test
        @DisplayName("获取参数类型")
        void testGetParameterTypes() {
            Constructor<TestClass> ctor = OpenConstructor.getConstructor(TestClass.class, String.class);
            Class<?>[] types = OpenConstructor.getParameterTypes(ctor);
            assertThat(types).containsExactly(String.class);
        }
    }

    @Nested
    @DisplayName("findFactoryMethod方法测试")
    class FindFactoryMethodTests {

        @Test
        @DisplayName("查找工厂方法")
        void testFindFactoryMethod() {
            Optional<Method> method = OpenConstructor.findFactoryMethod(FactoryClass.class, "create");
            assertThat(method).isPresent();
        }

        @Test
        @DisplayName("工厂方法不存在")
        void testFindFactoryMethodNotFound() {
            Optional<Method> method = OpenConstructor.findFactoryMethod(TestClass.class, "create");
            assertThat(method).isEmpty();
        }
    }

    @Nested
    @DisplayName("findFactoryMethods方法测试")
    class FindFactoryMethodsTests {

        @Test
        @DisplayName("查找所有工厂方法")
        void testFindFactoryMethods() {
            List<Method> methods = OpenConstructor.findFactoryMethods(FactoryClass.class);
            assertThat(methods).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("newInstanceByFactory方法测试")
    class NewInstanceByFactoryTests {

        @Test
        @DisplayName("通过工厂方法创建实例")
        void testNewInstanceByFactory() {
            FactoryClass instance = OpenConstructor.newInstanceByFactory(FactoryClass.class, "create");
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("工厂方法不存在抛出异常")
        void testNewInstanceByFactoryNotFound() {
            assertThatThrownBy(() -> OpenConstructor.newInstanceByFactory(TestClass.class, "create"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestClass {
        private String value;

        public TestClass() {
        }

        public TestClass(String value) {
            this.value = value;
        }

        public TestClass(int number) {
            this.value = String.valueOf(number);
        }

        public String getValue() {
            return value;
        }
    }

    static class NoDefaultConstructorClass {
        @SuppressWarnings("unused")
        public NoDefaultConstructorClass(String required) {
        }
    }

    @SuppressWarnings("unused")
    static class AnnotatedConstructorClass {
        public AnnotatedConstructorClass() {
        }

        @Deprecated
        public AnnotatedConstructorClass(String value) {
        }
    }

    static class PrivateConstructorClass {
        @SuppressWarnings("unused")
        private PrivateConstructorClass(String value) {
        }
    }

    @SuppressWarnings("unused")
    static class FactoryClass {
        private FactoryClass() {
        }

        public static FactoryClass create() {
            return new FactoryClass();
        }

        public static FactoryClass createWith(String value) {
            return new FactoryClass();
        }
    }
}

package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ConstructorUtilTest Tests
 * ConstructorUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ConstructorUtil 测试")
class ConstructorUtilTest {

    @BeforeEach
    void setUp() {
        ConstructorUtil.clearCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = ConstructorUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getDeclaredConstructors方法测试")
    class GetDeclaredConstructorsTests {

        @Test
        @DisplayName("获取所有声明的构造器")
        void testGetDeclaredConstructors() {
            Constructor<?>[] constructors = ConstructorUtil.getDeclaredConstructors(TestClass.class);
            assertThat(constructors).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("getConstructor方法测试")
    class GetConstructorTests {

        @Test
        @DisplayName("按参数类型获取构造器")
        void testGetConstructor() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructor(TestClass.class, String.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("获取默认构造器")
        void testGetDefaultConstructor() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructor(TestClass.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("构造器不存在返回null")
        void testGetConstructorNotFound() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructor(TestClass.class, Long.class);
            assertThat(constructor).isNull();
        }

        @Test
        @DisplayName("结果被缓存")
        void testGetConstructorCached() {
            Constructor<TestClass> constructor1 = ConstructorUtil.getConstructor(TestClass.class, String.class);
            Constructor<TestClass> constructor2 = ConstructorUtil.getConstructor(TestClass.class, String.class);
            assertThat(constructor1).isSameAs(constructor2);
        }
    }

    @Nested
    @DisplayName("getConstructorOrThrow方法测试")
    class GetConstructorOrThrowTests {

        @Test
        @DisplayName("获取存在的构造器")
        void testGetConstructorOrThrowExists() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructorOrThrow(TestClass.class, String.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("构造器不存在抛出异常")
        void testGetConstructorOrThrowNotFound() {
            assertThatThrownBy(() -> ConstructorUtil.getConstructorOrThrow(TestClass.class, Long.class))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getDefaultConstructor方法测试")
    class GetDefaultConstructorTests {

        @Test
        @DisplayName("获取默认构造器")
        void testGetDefaultConstructor() {
            Constructor<TestClass> constructor = ConstructorUtil.getDefaultConstructor(TestClass.class);
            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("无默认构造器返回null")
        void testGetDefaultConstructorNotExists() {
            Constructor<NoDefaultConstructorClass> constructor =
                    ConstructorUtil.getDefaultConstructor(NoDefaultConstructorClass.class);
            assertThat(constructor).isNull();
        }
    }

    @Nested
    @DisplayName("findMatchingConstructor方法测试")
    class FindMatchingConstructorTests {

        @Test
        @DisplayName("精确匹配")
        void testFindMatchingConstructorExact() {
            Constructor<TestClass> constructor = ConstructorUtil.findMatchingConstructor(TestClass.class, "test");
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("兼容匹配")
        void testFindMatchingConstructorCompatible() {
            Constructor<TestClass> constructor = ConstructorUtil.findMatchingConstructor(TestClass.class, 42);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("无参数匹配默认构造器")
        void testFindMatchingConstructorNoArgs() {
            Constructor<TestClass> constructor = ConstructorUtil.findMatchingConstructor(TestClass.class);
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("无匹配返回null")
        void testFindMatchingConstructorNotFound() {
            Constructor<TestClass> constructor = ConstructorUtil.findMatchingConstructor(TestClass.class, 1.0);
            assertThat(constructor).isNull();
        }
    }

    @Nested
    @DisplayName("getConstructors方法测试")
    class GetConstructorsTests {

        @Test
        @DisplayName("按条件过滤构造器")
        void testGetConstructorsWithPredicate() {
            List<Constructor<TestClass>> constructors = ConstructorUtil.getConstructors(TestClass.class,
                    c -> c.getParameterCount() == 1);
            assertThat(constructors).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getConstructorsWithAnnotation方法测试")
    class GetConstructorsWithAnnotationTests {

        @Test
        @DisplayName("获取带注解的构造器")
        void testGetConstructorsWithAnnotation() {
            List<Constructor<AnnotatedConstructorClass>> constructors =
                    ConstructorUtil.getConstructorsWithAnnotation(AnnotatedConstructorClass.class, Deprecated.class);
            assertThat(constructors).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getPublicConstructors方法测试")
    class GetPublicConstructorsTests {

        @Test
        @DisplayName("获取公共构造器")
        void testGetPublicConstructors() {
            List<Constructor<MixedAccessClass>> constructors =
                    ConstructorUtil.getPublicConstructors(MixedAccessClass.class);
            assertThat(constructors).hasSize(1);
        }
    }

    @Nested
    @DisplayName("newInstance方法测试")
    class NewInstanceTests {

        @Test
        @DisplayName("使用构造器创建实例")
        void testNewInstanceWithConstructor() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructor(TestClass.class, String.class);
            TestClass instance = ConstructorUtil.newInstance(constructor, "test");
            assertThat(instance).isNotNull();
            assertThat(instance.getValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("使用默认构造器创建实例")
        void testNewInstanceDefault() {
            TestClass instance = ConstructorUtil.newInstance(TestClass.class);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("使用参数自动匹配创建实例")
        void testNewInstanceWithArgs() {
            TestClass instance = ConstructorUtil.newInstance(TestClass.class, "test");
            assertThat(instance).isNotNull();
            assertThat(instance.getValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("无默认构造器抛出异常")
        void testNewInstanceNoDefaultConstructor() {
            assertThatThrownBy(() -> ConstructorUtil.newInstance(NoDefaultConstructorClass.class))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("无匹配构造器抛出异常")
        void testNewInstanceNoMatchingConstructor() {
            assertThatThrownBy(() -> ConstructorUtil.newInstance(TestClass.class, 1.0))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("newInstanceSafe方法测试")
    class NewInstanceSafeTests {

        @Test
        @DisplayName("安全创建实例")
        void testNewInstanceSafe() {
            Optional<TestClass> instance = ConstructorUtil.newInstanceSafe(TestClass.class);
            assertThat(instance).isPresent();
        }

        @Test
        @DisplayName("创建失败返回空")
        void testNewInstanceSafeFailed() {
            Optional<NoDefaultConstructorClass> instance =
                    ConstructorUtil.newInstanceSafe(NoDefaultConstructorClass.class);
            assertThat(instance).isEmpty();
        }
    }

    @Nested
    @DisplayName("构造器信息测试")
    class ConstructorInfoTests {

        @Test
        @DisplayName("获取参数类型")
        void testGetParameterTypes() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructor(TestClass.class, String.class);
            Class<?>[] types = ConstructorUtil.getParameterTypes(constructor);
            assertThat(types).containsExactly(String.class);
        }

        @Test
        @DisplayName("获取参数数量")
        void testGetParameterCount() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructor(TestClass.class, String.class);
            assertThat(ConstructorUtil.getParameterCount(constructor)).isEqualTo(1);
        }

        @Test
        @DisplayName("检查是否为公共")
        void testIsPublic() {
            Constructor<TestClass> constructor = ConstructorUtil.getConstructor(TestClass.class);
            assertThat(ConstructorUtil.isPublic(constructor)).isTrue();
        }

        @Test
        @DisplayName("检查是否为私有")
        void testIsPrivate() {
            Constructor<MixedAccessClass> constructor =
                    ConstructorUtil.getConstructor(MixedAccessClass.class, String.class);
            assertThat(ConstructorUtil.isPrivate(constructor)).isTrue();
        }

        @Test
        @DisplayName("检查是否有默认构造器")
        void testHasDefaultConstructor() {
            assertThat(ConstructorUtil.hasDefaultConstructor(TestClass.class)).isTrue();
            assertThat(ConstructorUtil.hasDefaultConstructor(NoDefaultConstructorClass.class)).isFalse();
        }

        @Test
        @DisplayName("检查是否可实例化")
        void testIsInstantiable() {
            assertThat(ConstructorUtil.isInstantiable(TestClass.class)).isTrue();
            assertThat(ConstructorUtil.isInstantiable(Runnable.class)).isFalse();
            assertThat(ConstructorUtil.isInstantiable(AbstractClass.class)).isFalse();
            assertThat(ConstructorUtil.isInstantiable(int.class)).isFalse();
            assertThat(ConstructorUtil.isInstantiable(String[].class)).isFalse();
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除所有缓存")
        void testClearCache() {
            ConstructorUtil.getConstructor(TestClass.class, String.class);
            ConstructorUtil.clearCache();
            // Should not throw
        }

        @Test
        @DisplayName("清除特定类的缓存")
        void testClearCacheForClass() {
            ConstructorUtil.getConstructor(TestClass.class, String.class);
            ConstructorUtil.clearCache(TestClass.class);
            // Should not throw
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

    @SuppressWarnings("unused")
    static class MixedAccessClass {
        public MixedAccessClass() {
        }

        private MixedAccessClass(String value) {
        }
    }

    static abstract class AbstractClass {
    }
}

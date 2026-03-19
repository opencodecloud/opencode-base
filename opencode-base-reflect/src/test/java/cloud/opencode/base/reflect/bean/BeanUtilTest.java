package cloud.opencode.base.reflect.bean;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanUtilTest Tests
 * BeanUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("BeanUtil 测试")
class BeanUtilTest {

    @BeforeEach
    void setUp() {
        BeanUtil.clearCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = BeanUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getGetters方法测试")
    class GetGettersTests {

        @Test
        @DisplayName("获取所有getter")
        void testGetGetters() {
            List<Method> getters = BeanUtil.getGetters(TestBean.class);
            assertThat(getters).isNotEmpty();
            assertThat(getters.stream().map(Method::getName))
                    .contains("getName", "getAge");
        }

        @Test
        @DisplayName("缓存getter")
        void testGetGettersCached() {
            List<Method> g1 = BeanUtil.getGetters(TestBean.class);
            List<Method> g2 = BeanUtil.getGetters(TestBean.class);
            assertThat(g1).isSameAs(g2);
        }
    }

    @Nested
    @DisplayName("getSetters方法测试")
    class GetSettersTests {

        @Test
        @DisplayName("获取所有setter")
        void testGetSetters() {
            List<Method> setters = BeanUtil.getSetters(TestBean.class);
            assertThat(setters).isNotEmpty();
            assertThat(setters.stream().map(Method::getName))
                    .contains("setName", "setAge");
        }
    }

    @Nested
    @DisplayName("getGetter方法测试")
    class GetGetterTests {

        @Test
        @DisplayName("获取指定属性getter")
        void testGetGetter() {
            Method getter = BeanUtil.getGetter(TestBean.class, "name");
            assertThat(getter).isNotNull();
            assertThat(getter.getName()).isEqualTo("getName");
        }

        @Test
        @DisplayName("获取boolean属性getter")
        void testGetGetterBoolean() {
            Method getter = BeanUtil.getGetter(TestBean.class, "active");
            assertThat(getter).isNotNull();
            assertThat(getter.getName()).isEqualTo("isActive");
        }

        @Test
        @DisplayName("不存在的属性返回null")
        void testGetGetterNotFound() {
            Method getter = BeanUtil.getGetter(TestBean.class, "nonexistent");
            assertThat(getter).isNull();
        }
    }

    @Nested
    @DisplayName("getSetter方法测试")
    class GetSetterTests {

        @Test
        @DisplayName("获取指定属性setter")
        void testGetSetter() {
            Method setter = BeanUtil.getSetter(TestBean.class, "name");
            assertThat(setter).isNotNull();
            assertThat(setter.getName()).isEqualTo("setName");
        }

        @Test
        @DisplayName("不存在的属性返回null")
        void testGetSetterNotFound() {
            Method setter = BeanUtil.getSetter(TestBean.class, "nonexistent");
            assertThat(setter).isNull();
        }
    }

    @Nested
    @DisplayName("getPropertyValue方法测试")
    class GetPropertyValueTests {

        @Test
        @DisplayName("获取属性值")
        void testGetPropertyValue() {
            TestBean bean = new TestBean();
            bean.setName("John");
            Object value = BeanUtil.getPropertyValue(bean, "name");
            assertThat(value).isEqualTo("John");
        }

        @Test
        @DisplayName("不存在的属性抛出异常")
        void testGetPropertyValueNotFound() {
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> BeanUtil.getPropertyValue(bean, "nonexistent"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getPropertyValueSafe方法测试")
    class GetPropertyValueSafeTests {

        @Test
        @DisplayName("获取属性值返回Optional")
        void testGetPropertyValueSafe() {
            TestBean bean = new TestBean();
            bean.setName("John");
            Optional<Object> value = BeanUtil.getPropertyValueSafe(bean, "name");
            assertThat(value).isPresent().contains("John");
        }

        @Test
        @DisplayName("不存在的属性返回empty")
        void testGetPropertyValueSafeNotFound() {
            TestBean bean = new TestBean();
            Optional<Object> value = BeanUtil.getPropertyValueSafe(bean, "nonexistent");
            assertThat(value).isEmpty();
        }
    }

    @Nested
    @DisplayName("setPropertyValue方法测试")
    class SetPropertyValueTests {

        @Test
        @DisplayName("设置属性值")
        void testSetPropertyValue() {
            TestBean bean = new TestBean();
            BeanUtil.setPropertyValue(bean, "name", "John");
            assertThat(bean.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("不存在的属性抛出异常")
        void testSetPropertyValueNotFound() {
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> BeanUtil.setPropertyValue(bean, "nonexistent", "value"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("setPropertyValueSafe方法测试")
    class SetPropertyValueSafeTests {

        @Test
        @DisplayName("设置成功返回true")
        void testSetPropertyValueSafeSuccess() {
            TestBean bean = new TestBean();
            boolean result = BeanUtil.setPropertyValueSafe(bean, "name", "John");
            assertThat(result).isTrue();
            assertThat(bean.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("设置失败返回false")
        void testSetPropertyValueSafeFailed() {
            TestBean bean = new TestBean();
            boolean result = BeanUtil.setPropertyValueSafe(bean, "nonexistent", "value");
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isGetter方法测试")
    class IsGetterTests {

        @Test
        @DisplayName("getter方法返回true")
        void testIsGetterTrue() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("getName");
            assertThat(BeanUtil.isGetter(method)).isTrue();
        }

        @Test
        @DisplayName("is前缀getter返回true")
        void testIsGetterIsPrefix() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("isActive");
            assertThat(BeanUtil.isGetter(method)).isTrue();
        }

        @Test
        @DisplayName("非getter方法返回false")
        void testIsGetterFalse() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("setName", String.class);
            assertThat(BeanUtil.isGetter(method)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSetter方法测试")
    class IsSetterTests {

        @Test
        @DisplayName("setter方法返回true")
        void testIsSetterTrue() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("setName", String.class);
            assertThat(BeanUtil.isSetter(method)).isTrue();
        }

        @Test
        @DisplayName("非setter方法返回false")
        void testIsSetterFalse() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("getName");
            assertThat(BeanUtil.isSetter(method)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractPropertyName方法测试")
    class ExtractPropertyNameTests {

        @Test
        @DisplayName("从get方法提取")
        void testExtractFromGetter() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("getName");
            assertThat(BeanUtil.extractPropertyName(method)).isEqualTo("name");
        }

        @Test
        @DisplayName("从is方法提取")
        void testExtractFromIsGetter() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("isActive");
            assertThat(BeanUtil.extractPropertyName(method)).isEqualTo("active");
        }

        @Test
        @DisplayName("从set方法提取")
        void testExtractFromSetter() throws NoSuchMethodException {
            Method method = TestBean.class.getMethod("setName", String.class);
            assertThat(BeanUtil.extractPropertyName(method)).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getReadablePropertyNames方法测试")
    class GetReadablePropertyNamesTests {

        @Test
        @DisplayName("获取可读属性名")
        void testGetReadablePropertyNames() {
            Set<String> names = BeanUtil.getReadablePropertyNames(TestBean.class);
            assertThat(names).contains("name", "age", "active");
        }
    }

    @Nested
    @DisplayName("getWritablePropertyNames方法测试")
    class GetWritablePropertyNamesTests {

        @Test
        @DisplayName("获取可写属性名")
        void testGetWritablePropertyNames() {
            Set<String> names = BeanUtil.getWritablePropertyNames(TestBean.class);
            assertThat(names).contains("name", "age", "active");
        }
    }

    @Nested
    @DisplayName("getAllPropertyNames方法测试")
    class GetAllPropertyNamesTests {

        @Test
        @DisplayName("获取所有属性名")
        void testGetAllPropertyNames() {
            Set<String> names = BeanUtil.getAllPropertyNames(TestBean.class);
            assertThat(names).contains("name", "age", "active");
        }
    }

    @Nested
    @DisplayName("getPropertyType方法测试")
    class GetPropertyTypeTests {

        @Test
        @DisplayName("获取属性类型")
        void testGetPropertyType() {
            Class<?> type = BeanUtil.getPropertyType(TestBean.class, "name");
            assertThat(type).isEqualTo(String.class);
        }

        @Test
        @DisplayName("不存在的属性返回null")
        void testGetPropertyTypeNotFound() {
            Class<?> type = BeanUtil.getPropertyType(TestBean.class, "nonexistent");
            assertThat(type).isNull();
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("可读属性返回true")
        void testIsReadableTrue() {
            assertThat(BeanUtil.isReadable(TestBean.class, "name")).isTrue();
        }

        @Test
        @DisplayName("不可读属性返回false")
        void testIsReadableFalse() {
            assertThat(BeanUtil.isReadable(TestBean.class, "nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("isWritable方法测试")
    class IsWritableTests {

        @Test
        @DisplayName("可写属性返回true")
        void testIsWritableTrue() {
            assertThat(BeanUtil.isWritable(TestBean.class, "name")).isTrue();
        }

        @Test
        @DisplayName("不可写属性返回false")
        void testIsWritableFalse() {
            assertThat(BeanUtil.isWritable(TestBean.class, "nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除所有缓存")
        void testClearCache() {
            BeanUtil.getGetters(TestBean.class);
            BeanUtil.clearCache();
            // No assertion, just verify no exception
        }

        @Test
        @DisplayName("清除类缓存")
        void testClearCacheClass() {
            BeanUtil.getGetters(TestBean.class);
            BeanUtil.clearCache(TestBean.class);
            // No assertion, just verify no exception
        }
    }

    // Test helper class
    public static class TestBean {
        private String name;
        private int age;
        private boolean active;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}

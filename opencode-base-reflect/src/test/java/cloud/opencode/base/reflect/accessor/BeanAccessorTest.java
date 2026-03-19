package cloud.opencode.base.reflect.accessor;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanAccessorTest Tests
 * BeanAccessorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("BeanAccessor 测试")
class BeanAccessorTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("从getter和setter创建")
        void testCreate() throws NoSuchMethodException {
            Method getter = TestBean.class.getMethod("getName");
            Method setter = TestBean.class.getMethod("setName", String.class);
            BeanAccessor<TestBean> accessor = new BeanAccessor<>("name", TestBean.class, getter, setter);
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("仅getter")
        void testCreateGetterOnly() throws NoSuchMethodException {
            Method getter = TestBean.class.getMethod("getName");
            BeanAccessor<TestBean> accessor = new BeanAccessor<>("name", TestBean.class, getter, null);
            assertThat(accessor.isReadable()).isTrue();
            assertThat(accessor.isWritable()).isFalse();
        }

        @Test
        @DisplayName("仅setter")
        void testCreateSetterOnly() throws NoSuchMethodException {
            Method setter = TestBean.class.getMethod("setName", String.class);
            BeanAccessor<TestBean> accessor = new BeanAccessor<>("name", TestBean.class, null, setter);
            assertThat(accessor.isReadable()).isFalse();
            assertThat(accessor.isWritable()).isTrue();
        }

        @Test
        @DisplayName("getter和setter都为null抛出异常")
        void testCreateBothNull() {
            assertThatThrownBy(() -> new BeanAccessor<>("name", TestBean.class, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("of静态方法测试")
    class OfTests {

        @Test
        @DisplayName("按类和属性名创建")
        void testOf() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("boolean属性使用is前缀")
        void testOfBoolean() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "active");
            assertThat(accessor).isNotNull();
            assertThat(accessor.isReadable()).isTrue();
        }

        @Test
        @DisplayName("不存在的属性抛出异常")
        void testOfNotFound() {
            assertThatThrownBy(() -> BeanAccessor.of(TestBean.class, "nonexistent"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getGetter方法测试")
    class GetGetterTests {

        @Test
        @DisplayName("获取getter方法")
        void testGetGetter() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.getGetter()).isNotNull();
            assertThat(accessor.getGetter().getName()).isEqualTo("getName");
        }
    }

    @Nested
    @DisplayName("getSetter方法测试")
    class GetSetterTests {

        @Test
        @DisplayName("获取setter方法")
        void testGetSetter() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.getSetter()).isNotNull();
            assertThat(accessor.getSetter().getName()).isEqualTo("setName");
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取属性名")
        void testGetName() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取属性类型")
        void testGetType() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.getType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类")
        void testGetDeclaringClass() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.getDeclaringClass()).isEqualTo(TestBean.class);
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("有getter返回true")
        void testIsReadableTrue() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.isReadable()).isTrue();
        }
    }

    @Nested
    @DisplayName("isWritable方法测试")
    class IsWritableTests {

        @Test
        @DisplayName("有setter返回true")
        void testIsWritableTrue() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.isWritable()).isTrue();
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取属性值")
        void testGet() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            bean.setName("test");
            assertThat(accessor.get(bean)).isEqualTo("test");
        }

        @Test
        @DisplayName("无getter抛出异常")
        void testGetNoGetter() throws NoSuchMethodException {
            Method setter = TestBean.class.getMethod("setName", String.class);
            BeanAccessor<TestBean> accessor = new BeanAccessor<>("name", TestBean.class, null, setter);
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> accessor.get(bean))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("set方法测试")
    class SetTests {

        @Test
        @DisplayName("设置属性值")
        void testSet() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            accessor.set(bean, "newValue");
            assertThat(bean.getName()).isEqualTo("newValue");
        }

        @Test
        @DisplayName("无setter抛出异常")
        void testSetNoSetter() throws NoSuchMethodException {
            Method getter = TestBean.class.getMethod("getName");
            BeanAccessor<TestBean> accessor = new BeanAccessor<>("name", TestBean.class, getter, null);
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> accessor.set(bean, "value"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同属性相等")
        void testEquals() {
            BeanAccessor<TestBean> a1 = BeanAccessor.of(TestBean.class, "name");
            BeanAccessor<TestBean> a2 = BeanAccessor.of(TestBean.class, "name");
            assertThat(a1).isEqualTo(a2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor).isEqualTo(accessor);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同属性有相同hashCode")
        void testHashCode() {
            BeanAccessor<TestBean> a1 = BeanAccessor.of(TestBean.class, "name");
            BeanAccessor<TestBean> a2 = BeanAccessor.of(TestBean.class, "name");
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            BeanAccessor<TestBean> accessor = BeanAccessor.of(TestBean.class, "name");
            assertThat(accessor.toString()).contains("BeanAccessor");
            assertThat(accessor.toString()).contains("name");
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

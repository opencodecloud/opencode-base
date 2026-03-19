package cloud.opencode.base.reflect.accessor;

import org.junit.jupiter.api.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodHandleAccessorTest Tests
 * MethodHandleAccessorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("MethodHandleAccessor 测试")
class MethodHandleAccessorTest {

    @Nested
    @DisplayName("fromField静态方法测试")
    class FromFieldTests {

        @Test
        @DisplayName("从Field创建")
        void testFromField() throws NoSuchFieldException {
            Field field = TestBean.class.getDeclaredField("name");
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.fromField(field);
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("null字段抛出异常")
        void testFromFieldNull() {
            assertThatThrownBy(() -> MethodHandleAccessor.fromField(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fromMethods静态方法测试")
    class FromMethodsTests {

        @Test
        @DisplayName("从getter和setter创建")
        void testFromMethods() throws NoSuchMethodException {
            Method getter = TestBean.class.getMethod("getName");
            Method setter = TestBean.class.getMethod("setName", String.class);
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.fromMethods("name", TestBean.class, getter, setter);
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("仅getter")
        void testFromMethodsGetterOnly() throws NoSuchMethodException {
            Method getter = TestBean.class.getMethod("getName");
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.fromMethods("name", TestBean.class, getter, null);
            assertThat(accessor.isReadable()).isTrue();
            assertThat(accessor.isWritable()).isFalse();
        }

        @Test
        @DisplayName("仅setter")
        void testFromMethodsSetterOnly() throws NoSuchMethodException {
            Method setter = TestBean.class.getMethod("setName", String.class);
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.fromMethods("name", TestBean.class, null, setter);
            assertThat(accessor.isReadable()).isFalse();
            assertThat(accessor.isWritable()).isTrue();
        }

        @Test
        @DisplayName("getter和setter都为null抛出异常")
        void testFromMethodsBothNull() {
            assertThatThrownBy(() -> MethodHandleAccessor.fromMethods("name", TestBean.class, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("of静态方法测试")
    class OfTests {

        @Test
        @DisplayName("按类和字段名创建")
        void testOf() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("不存在的字段抛出异常")
        void testOfNotFound() {
            assertThatThrownBy(() -> MethodHandleAccessor.of(TestBean.class, "nonexistent"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取属性名")
        void testGetName() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor.getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取属性类型")
        void testGetType() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor.getType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类")
        void testGetDeclaringClass() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor.getDeclaringClass()).isEqualTo(TestBean.class);
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("有getter返回true")
        void testIsReadableTrue() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor.isReadable()).isTrue();
        }
    }

    @Nested
    @DisplayName("isWritable方法测试")
    class IsWritableTests {

        @Test
        @DisplayName("非final字段可写")
        void testIsWritableTrue() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor.isWritable()).isTrue();
        }

        @Test
        @DisplayName("final字段不可写")
        void testIsWritableFalse() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "CONSTANT");
            assertThat(accessor.isWritable()).isFalse();
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取属性值")
        void testGet() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            bean.name = "test";
            assertThat(accessor.get(bean)).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("set方法测试")
    class SetTests {

        @Test
        @DisplayName("设置属性值")
        void testSet() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            accessor.set(bean, "newValue");
            assertThat(bean.name).isEqualTo("newValue");
        }

        @Test
        @DisplayName("不可写字段抛出异常")
        void testSetNotWritable() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "CONSTANT");
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> accessor.set(bean, "value"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("getGetterHandle方法测试")
    class GetGetterHandleTests {

        @Test
        @DisplayName("获取getter MethodHandle")
        void testGetGetterHandle() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            MethodHandle handle = accessor.getGetterHandle();
            assertThat(handle).isNotNull();
        }
    }

    @Nested
    @DisplayName("getSetterHandle方法测试")
    class GetSetterHandleTests {

        @Test
        @DisplayName("获取setter MethodHandle")
        void testGetSetterHandle() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            MethodHandle handle = accessor.getSetterHandle();
            assertThat(handle).isNotNull();
        }

        @Test
        @DisplayName("final字段返回null")
        void testGetSetterHandleNull() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "CONSTANT");
            assertThat(accessor.getSetterHandle()).isNull();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同字段相等")
        void testEquals() {
            MethodHandleAccessor<TestBean> a1 = MethodHandleAccessor.of(TestBean.class, "name");
            MethodHandleAccessor<TestBean> a2 = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(a1).isEqualTo(a2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor).isEqualTo(accessor);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同字段有相同hashCode")
        void testHashCode() {
            MethodHandleAccessor<TestBean> a1 = MethodHandleAccessor.of(TestBean.class, "name");
            MethodHandleAccessor<TestBean> a2 = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            MethodHandleAccessor<TestBean> accessor = MethodHandleAccessor.of(TestBean.class, "name");
            assertThat(accessor.toString()).contains("MethodHandleAccessor");
            assertThat(accessor.toString()).contains("name");
        }
    }

    // Test helper class
    static class TestBean {
        String name;
        int age;
        final String CONSTANT = "constant";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}

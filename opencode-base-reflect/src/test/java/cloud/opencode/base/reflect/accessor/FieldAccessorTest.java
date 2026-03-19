package cloud.opencode.base.reflect.accessor;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.*;

/**
 * FieldAccessorTest Tests
 * FieldAccessorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("FieldAccessor 测试")
class FieldAccessorTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("从Field创建")
        void testCreate() throws NoSuchFieldException {
            Field field = TestBean.class.getDeclaredField("name");
            FieldAccessor<TestBean> accessor = new FieldAccessor<>(field);
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("null字段抛出异常")
        void testCreateNull() {
            assertThatThrownBy(() -> new FieldAccessor<>(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("of静态方法测试")
    class OfTests {

        @Test
        @DisplayName("按类和字段名创建")
        void testOf() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("不存在的字段抛出异常")
        void testOfNotFound() {
            assertThatThrownBy(() -> FieldAccessor.of(TestBean.class, "nonexistent"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getField方法测试")
    class GetFieldTests {

        @Test
        @DisplayName("获取底层Field")
        void testGetField() throws NoSuchFieldException {
            Field field = TestBean.class.getDeclaredField("name");
            FieldAccessor<TestBean> accessor = new FieldAccessor<>(field);
            assertThat(accessor.getField()).isSameAs(field);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取字段名")
        void testGetName() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor.getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取字段类型")
        void testGetType() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor.getType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getGenericType方法测试")
    class GetGenericTypeTests {

        @Test
        @DisplayName("获取泛型类型")
        void testGetGenericType() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            Type genericType = accessor.getGenericType();
            assertThat(genericType).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类")
        void testGetDeclaringClass() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor.getDeclaringClass()).isEqualTo(TestBean.class);
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("总是返回true")
        void testIsReadable() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor.isReadable()).isTrue();
        }
    }

    @Nested
    @DisplayName("isWritable方法测试")
    class IsWritableTests {

        @Test
        @DisplayName("非final字段可写")
        void testIsWritableTrue() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor.isWritable()).isTrue();
        }

        @Test
        @DisplayName("final字段不可写")
        void testIsWritableFalse() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "CONSTANT");
            assertThat(accessor.isWritable()).isFalse();
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取字段值")
        void testGet() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            bean.name = "test";
            assertThat(accessor.get(bean)).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("set方法测试")
    class SetTests {

        @Test
        @DisplayName("设置字段值")
        void testSet() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            accessor.set(bean, "newValue");
            assertThat(bean.name).isEqualTo("newValue");
        }

        @Test
        @DisplayName("final字段抛出异常")
        void testSetFinal() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "CONSTANT");
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> accessor.set(bean, "value"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("getStatic方法测试")
    class GetStaticTests {

        @Test
        @DisplayName("获取静态字段值")
        void testGetStatic() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "staticField");
            TestBean.staticField = "staticValue";
            assertThat(accessor.getStatic()).isEqualTo("staticValue");
        }
    }

    @Nested
    @DisplayName("setStatic方法测试")
    class SetStaticTests {

        @Test
        @DisplayName("设置静态字段值")
        void testSetStatic() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "staticField");
            accessor.setStatic("newStaticValue");
            assertThat(TestBean.staticField).isEqualTo("newStaticValue");
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同字段相等")
        void testEquals() {
            FieldAccessor<TestBean> a1 = FieldAccessor.of(TestBean.class, "name");
            FieldAccessor<TestBean> a2 = FieldAccessor.of(TestBean.class, "name");
            assertThat(a1).isEqualTo(a2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor).isEqualTo(accessor);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同字段有相同hashCode")
        void testHashCode() {
            FieldAccessor<TestBean> a1 = FieldAccessor.of(TestBean.class, "name");
            FieldAccessor<TestBean> a2 = FieldAccessor.of(TestBean.class, "name");
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            assertThat(accessor.toString()).contains("FieldAccessor");
            assertThat(accessor.toString()).contains("name");
        }
    }

    // Test helper class
    static class TestBean {
        String name;
        int age;
        final String CONSTANT = "constant";
        static String staticField;
    }
}

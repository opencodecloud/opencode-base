package cloud.opencode.base.reflect.bean;

import org.junit.jupiter.api.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyDescriptorTest Tests
 * PropertyDescriptorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("PropertyDescriptor 测试")
class PropertyDescriptorTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建PropertyDescriptor")
        void testCreate() throws Exception {
            Method getter = TestBean.class.getMethod("getName");
            Method setter = TestBean.class.getMethod("setName", String.class);
            Field field = TestBean.class.getDeclaredField("name");

            PropertyDescriptor descriptor = new PropertyDescriptor(
                    "name", String.class, String.class, getter, setter, field, TestBean.class);
            assertThat(descriptor).isNotNull();
            assertThat(descriptor.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testCreateNullName() {
            assertThatThrownBy(() -> new PropertyDescriptor(
                    null, String.class, String.class, null, null, null, TestBean.class))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null类型抛出异常")
        void testCreateNullType() {
            assertThatThrownBy(() -> new PropertyDescriptor(
                    "name", null, String.class, null, null, null, TestBean.class))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取属性名")
        void testGetName() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getPropertyType方法测试")
    class GetPropertyTypeTests {

        @Test
        @DisplayName("获取属性类型")
        void testGetPropertyType() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getPropertyType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getGenericType方法测试")
    class GetGenericTypeTests {

        @Test
        @DisplayName("获取泛型类型")
        void testGetGenericType() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getGenericType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getReadMethod方法测试")
    class GetReadMethodTests {

        @Test
        @DisplayName("获取getter方法")
        void testGetReadMethod() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getReadMethod()).isNotNull();
            assertThat(descriptor.getReadMethod().getName()).isEqualTo("getName");
        }
    }

    @Nested
    @DisplayName("getWriteMethod方法测试")
    class GetWriteMethodTests {

        @Test
        @DisplayName("获取setter方法")
        void testGetWriteMethod() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getWriteMethod()).isNotNull();
            assertThat(descriptor.getWriteMethod().getName()).isEqualTo("setName");
        }
    }

    @Nested
    @DisplayName("getField方法测试")
    class GetFieldTests {

        @Test
        @DisplayName("获取字段")
        void testGetField() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getField()).isNotNull();
            assertThat(descriptor.getField().getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类")
        void testGetDeclaringClass() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getDeclaringClass()).isEqualTo(TestBean.class);
        }
    }

    @Nested
    @DisplayName("isReadable方法测试")
    class IsReadableTests {

        @Test
        @DisplayName("有getter可读")
        void testIsReadable() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.isReadable()).isTrue();
        }
    }

    @Nested
    @DisplayName("isWritable方法测试")
    class IsWritableTests {

        @Test
        @DisplayName("有setter可写")
        void testIsWritable() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.isWritable()).isTrue();
        }
    }

    @Nested
    @DisplayName("getValue方法测试")
    class GetValueTests {

        @Test
        @DisplayName("获取属性值")
        void testGetValue() {
            TestBean bean = new TestBean();
            bean.setName("test");
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getValue(bean)).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("setValue方法测试")
    class SetValueTests {

        @Test
        @DisplayName("设置属性值")
        void testSetValue() {
            TestBean bean = new TestBean();
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            descriptor.setValue(bean, "newValue");
            assertThat(bean.getName()).isEqualTo("newValue");
        }
    }

    @Nested
    @DisplayName("getAnnotation方法测试")
    class GetAnnotationTests {

        @Test
        @DisplayName("获取字段注解")
        void testGetAnnotation() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(AnnotatedBean.class, "value");
            assertThat(descriptor.getAnnotation(TestAnnotation.class)).isPresent();
        }

        @Test
        @DisplayName("无注解返回empty")
        void testGetAnnotationEmpty() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.getAnnotation(TestAnnotation.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasAnnotation方法测试")
    class HasAnnotationTests {

        @Test
        @DisplayName("有注解返回true")
        void testHasAnnotation() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(AnnotatedBean.class, "value");
            assertThat(descriptor.hasAnnotation(TestAnnotation.class)).isTrue();
        }

        @Test
        @DisplayName("无注解返回false")
        void testHasAnnotationFalse() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.hasAnnotation(TestAnnotation.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同属性相等")
        void testEquals() {
            PropertyDescriptor d1 = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            PropertyDescriptor d2 = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(d1).isEqualTo(d2);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同属性有相同hashCode")
        void testHashCode() {
            PropertyDescriptor d1 = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            PropertyDescriptor d2 = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor.toString()).contains("PropertyDescriptor");
            assertThat(descriptor.toString()).contains("name");
        }
    }

    // Test helpers
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {}

    public static class TestBean {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class AnnotatedBean {
        @TestAnnotation
        private String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}

package cloud.opencode.base.reflect.accessor;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyAccessorTest Tests
 * PropertyAccessorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("PropertyAccessor 测试")
class PropertyAccessorTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("get带类型")
        void testGetTyped() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            bean.setName("test");
            String value = accessor.get(bean, String.class);
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("setIfWritable可写字段")
        void testSetIfWritableTrue() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            boolean result = accessor.setIfWritable(bean, "newValue");
            assertThat(result).isTrue();
            assertThat(bean.getName()).isEqualTo("newValue");
        }

        @Test
        @DisplayName("getOrDefault有值")
        void testGetOrDefaultWithValue() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            bean.setName("test");
            String value = accessor.getOrDefault(bean, "default");
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("getOrDefault无值返回默认")
        void testGetOrDefaultWithNull() {
            FieldAccessor<TestBean> accessor = FieldAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            String value = accessor.getOrDefault(bean, "default");
            assertThat(value).isEqualTo("default");
        }
    }

    // Test helper class
    public static class TestBean {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}

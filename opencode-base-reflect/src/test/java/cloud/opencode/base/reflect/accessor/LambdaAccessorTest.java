package cloud.opencode.base.reflect.accessor;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LambdaAccessor Tests
 * LambdaAccessor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
@DisplayName("LambdaAccessor 测试")
class LambdaAccessorTest {

    @Nested
    @DisplayName("通过getter/setter方法读写测试")
    class GetterSetterTests {

        @Test
        @DisplayName("通过getter读取属性值")
        void testReadViaGetter() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            bean.setName("Alice");
            assertThat(accessor.get(bean)).isEqualTo("Alice");
        }

        @Test
        @DisplayName("通过setter写入属性值")
        void testWriteViaSetter() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            TestBean bean = new TestBean();
            accessor.set(bean, "Bob");
            assertThat(bean.getName()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("读写int类型属性")
        void testReadWriteInt() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "age");
            TestBean bean = new TestBean();
            accessor.set(bean, 25);
            assertThat(accessor.get(bean)).isEqualTo(25);
        }

        @Test
        @DisplayName("读写boolean类型属性（isXxx）")
        void testReadWriteBoolean() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "active");
            TestBean bean = new TestBean();
            accessor.set(bean, true);
            assertThat(accessor.get(bean)).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("通过字段直接访问测试")
    class FieldAccessTests {

        @Test
        @DisplayName("无getter/setter时通过字段读取")
        void testReadViaField() {
            LambdaAccessor<FieldOnlyBean> accessor = LambdaAccessor.of(FieldOnlyBean.class, "secret");
            FieldOnlyBean bean = new FieldOnlyBean();
            bean.secret = "hidden";
            assertThat(accessor.get(bean)).isEqualTo("hidden");
        }

        @Test
        @DisplayName("无getter/setter时通过字段写入")
        void testWriteViaField() {
            LambdaAccessor<FieldOnlyBean> accessor = LambdaAccessor.of(FieldOnlyBean.class, "secret");
            FieldOnlyBean bean = new FieldOnlyBean();
            accessor.set(bean, "revealed");
            assertThat(bean.secret).isEqualTo("revealed");
        }
    }

    @Nested
    @DisplayName("isReadable/isWritable测试")
    class ReadableWritableTests {

        @Test
        @DisplayName("有getter的属性可读")
        void testIsReadableWithGetter() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThat(accessor.isReadable()).isTrue();
        }

        @Test
        @DisplayName("有setter的属性可写")
        void testIsWritableWithSetter() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThat(accessor.isWritable()).isTrue();
        }

        @Test
        @DisplayName("有字段的属性可读")
        void testIsReadableWithField() {
            LambdaAccessor<FieldOnlyBean> accessor = LambdaAccessor.of(FieldOnlyBean.class, "secret");
            assertThat(accessor.isReadable()).isTrue();
        }

        @Test
        @DisplayName("非final字段可写")
        void testIsWritableWithField() {
            LambdaAccessor<FieldOnlyBean> accessor = LambdaAccessor.of(FieldOnlyBean.class, "secret");
            assertThat(accessor.isWritable()).isTrue();
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取属性名")
        void testGetName() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThat(accessor.getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取String属性类型")
        void testGetTypeString() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThat(accessor.getType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("获取int属性类型")
        void testGetTypeInt() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "age");
            assertThat(accessor.getType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("getGenericType方法测试")
    class GetGenericTypeTests {

        @Test
        @DisplayName("获取泛型属性类型")
        void testGetGenericType() {
            LambdaAccessor<GenericBean> accessor = LambdaAccessor.of(GenericBean.class, "items");
            Type genericType = accessor.getGenericType();
            assertThat(genericType).isNotNull();
            assertThat(genericType.getTypeName()).contains("List");
            assertThat(genericType.getTypeName()).contains("String");
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类")
        void testGetDeclaringClass() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThat(accessor.getDeclaringClass()).isEqualTo(TestBean.class);
        }
    }

    @Nested
    @DisplayName("null目标处理测试")
    class NullTargetTests {

        @Test
        @DisplayName("get传null目标抛出NullPointerException")
        void testGetNullTarget() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThatThrownBy(() -> accessor.get(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("set传null目标抛出NullPointerException")
        void testSetNullTarget() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThatThrownBy(() -> accessor.set(null, "value"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("只读属性测试")
    class ReadOnlyTests {

        @Test
        @DisplayName("final字段不可写")
        void testFinalFieldNotWritable() {
            LambdaAccessor<ReadOnlyBean> accessor = LambdaAccessor.of(ReadOnlyBean.class, "id");
            assertThat(accessor.isReadable()).isTrue();
            assertThat(accessor.isWritable()).isFalse();
        }

        @Test
        @DisplayName("final字段可读")
        void testFinalFieldReadable() {
            LambdaAccessor<ReadOnlyBean> accessor = LambdaAccessor.of(ReadOnlyBean.class, "id");
            ReadOnlyBean bean = new ReadOnlyBean("test-id");
            assertThat(accessor.get(bean)).isEqualTo("test-id");
        }

        @Test
        @DisplayName("写入只读属性抛出异常")
        void testWriteToReadOnly() {
            LambdaAccessor<ReadOnlyBean> accessor = LambdaAccessor.of(ReadOnlyBean.class, "id");
            ReadOnlyBean bean = new ReadOnlyBean("test-id");
            assertThatThrownBy(() -> accessor.set(bean, "new-id"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfFactoryTests {

        @Test
        @DisplayName("null类抛出NullPointerException")
        void testNullClass() {
            assertThatThrownBy(() -> LambdaAccessor.of(null, "name"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null属性名抛出NullPointerException")
        void testNullPropertyName() {
            assertThatThrownBy(() -> LambdaAccessor.of(TestBean.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("不存在的属性抛出异常")
        void testNonexistentProperty() {
            assertThatThrownBy(() -> LambdaAccessor.of(TestBean.class, "nonexistent"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("PropertyAccessors集成测试")
    class PropertyAccessorsIntegrationTests {

        @Test
        @DisplayName("通过LAMBDA策略创建访问器")
        void testCreateWithLambdaStrategy() {
            PropertyAccessor<TestBean> accessor = PropertyAccessors.create(
                    TestBean.class, "name", PropertyAccessors.Strategy.LAMBDA);
            assertThat(accessor).isInstanceOf(LambdaAccessor.class);

            TestBean bean = new TestBean();
            accessor.set(bean, "Lambda");
            assertThat(accessor.get(bean)).isEqualTo("Lambda");
        }

        @Test
        @DisplayName("AUTO策略优先选择LambdaAccessor")
        void testAutoStrategyPrefersLambda() {
            PropertyAccessor<TestBean> accessor = PropertyAccessors.create(
                    TestBean.class, "name", PropertyAccessors.Strategy.AUTO);
            assertThat(accessor).isInstanceOf(LambdaAccessor.class);
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同属性相等")
        void testEquals() {
            LambdaAccessor<TestBean> a1 = LambdaAccessor.of(TestBean.class, "name");
            LambdaAccessor<TestBean> a2 = LambdaAccessor.of(TestBean.class, "name");
            assertThat(a1).isEqualTo(a2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThat(accessor).isEqualTo(accessor);
        }

        @Test
        @DisplayName("不同属性不相等")
        void testNotEquals() {
            LambdaAccessor<TestBean> a1 = LambdaAccessor.of(TestBean.class, "name");
            LambdaAccessor<TestBean> a2 = LambdaAccessor.of(TestBean.class, "age");
            assertThat(a1).isNotEqualTo(a2);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同属性有相同hashCode")
        void testHashCode() {
            LambdaAccessor<TestBean> a1 = LambdaAccessor.of(TestBean.class, "name");
            LambdaAccessor<TestBean> a2 = LambdaAccessor.of(TestBean.class, "name");
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含类名和属性名")
        void testToString() {
            LambdaAccessor<TestBean> accessor = LambdaAccessor.of(TestBean.class, "name");
            assertThat(accessor.toString()).contains("LambdaAccessor");
            assertThat(accessor.toString()).contains("TestBean");
            assertThat(accessor.toString()).contains("name");
        }
    }

    // ==================== Test Helper Classes | 测试辅助类 ====================

    static class TestBean {
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

    static class FieldOnlyBean {
        String secret;
    }

    static class ReadOnlyBean {
        private final String id;

        ReadOnlyBean(String id) {
            this.id = id;
        }
    }

    static class GenericBean {
        private List<String> items;

        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }
}

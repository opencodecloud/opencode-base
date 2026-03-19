package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyAccessNode Tests
 * PropertyAccessNode 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("PropertyAccessNode Tests | PropertyAccessNode 测试")
class PropertyAccessNodeTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of creates standard access | of 创建标准访问")
        void testOf() {
            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("obj"), "name");
            assertThat(node.target()).isInstanceOf(IdentifierNode.class);
            assertThat(node.property()).isEqualTo("name");
            assertThat(node.nullSafe()).isFalse();
        }

        @Test
        @DisplayName("of with nullSafe | of 带 nullSafe")
        void testOfWithNullSafe() {
            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("obj"), "name", true);
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("nullSafe factory | nullSafe 工厂方法")
        void testNullSafeFactory() {
            PropertyAccessNode node = PropertyAccessNode.nullSafe(IdentifierNode.of("obj"), "name");
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("Constructor validation | 构造函数验证")
        void testConstructorValidation() {
            assertThatThrownBy(() -> new PropertyAccessNode(null, "name", false))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new PropertyAccessNode(IdentifierNode.of("obj"), null, false))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Map Property Access Tests | Map 属性访问测试")
    class MapPropertyAccessTests {

        @Test
        @DisplayName("Access map property | 访问 Map 属性")
        void testAccessMapProperty() {
            Map<String, Object> map = Map.of("name", "John", "age", 30);
            StandardContext ctx = new StandardContext();
            ctx.setVariable("obj", map);

            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("obj"), "name");
            assertThat(node.evaluate(ctx)).isEqualTo("John");
        }

        @Test
        @DisplayName("Access missing map key | 访问不存在的 Map 键")
        void testAccessMissingMapKey() {
            Map<String, Object> map = Map.of("name", "John");
            StandardContext ctx = new StandardContext();
            ctx.setVariable("obj", map);

            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("obj"), "missing");
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("Bean Property Access Tests | Bean 属性访问测试")
    class BeanPropertyAccessTests {

        @Test
        @DisplayName("Access getter property | 访问 getter 属性")
        void testAccessGetterProperty() {
            TestBean bean = new TestBean("John", 30);
            StandardContext ctx = new StandardContext();
            ctx.setVariable("bean", bean);

            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("bean"), "name");
            assertThat(node.evaluate(ctx)).isEqualTo("John");
        }

        @Test
        @DisplayName("Access boolean getter | 访问布尔 getter")
        void testAccessBooleanGetter() {
            TestBean bean = new TestBean("John", 30);
            bean.setActive(true);
            StandardContext ctx = new StandardContext();
            ctx.setVariable("bean", bean);

            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("bean"), "active");
            assertThat(node.evaluate(ctx)).isEqualTo(true);
        }

        @Test
        @DisplayName("Property not found throws | 属性未找到抛出异常")
        void testPropertyNotFound() {
            TestBean bean = new TestBean("John", 30);
            StandardContext ctx = new StandardContext();
            ctx.setVariable("bean", bean);

            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("bean"), "nonexistent");
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class);
        }
    }

    @Nested
    @DisplayName("Record Property Access Tests | Record 属性访问测试")
    class RecordPropertyAccessTests {

        record Person(String name, int age) {}

        @Test
        @DisplayName("Access record property | 访问 record 属性")
        void testAccessRecordProperty() {
            Person person = new Person("John", 30);
            StandardContext ctx = new StandardContext();
            ctx.setVariable("person", person);

            PropertyAccessNode nameNode = PropertyAccessNode.of(IdentifierNode.of("person"), "name");
            PropertyAccessNode ageNode = PropertyAccessNode.of(IdentifierNode.of("person"), "age");

            assertThat(nameNode.evaluate(ctx)).isEqualTo("John");
            assertThat(ageNode.evaluate(ctx)).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("Null Safety Tests | 空安全测试")
    class NullSafetyTests {

        @Test
        @DisplayName("Standard access on null throws | 标准访问 null 抛出异常")
        void testStandardAccessOnNull() {
            StandardContext ctx = new StandardContext();
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    LiteralNode.of("value"),
                    LiteralNode.ofNull()
            );

            PropertyAccessNode node = PropertyAccessNode.of(nullProducer, "name");
            assertThatThrownBy(() -> node.evaluate(ctx))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Null-safe access on null returns null | 空安全访问 null 返回 null")
        void testNullSafeAccessOnNull() {
            StandardContext ctx = new StandardContext();
            // Use TernaryOpNode to produce null value
            TernaryOpNode nullProducer = TernaryOpNode.of(
                    LiteralNode.of(false),
                    LiteralNode.of("value"),
                    LiteralNode.ofNull()
            );

            PropertyAccessNode node = PropertyAccessNode.nullSafe(nullProducer, "name");
            assertThat(node.evaluate(ctx)).isNull();
        }
    }

    @Nested
    @DisplayName("Static getPropertyValue Tests | 静态 getPropertyValue 测试")
    class StaticGetPropertyValueTests {

        @Test
        @DisplayName("Get property from map | 从 Map 获取属性")
        void testGetPropertyFromMap() {
            Map<String, Object> map = Map.of("key", "value");
            StandardContext ctx = new StandardContext();

            Object result = PropertyAccessNode.getPropertyValue(map, "key", ctx);
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("Get property from bean | 从 Bean 获取属性")
        void testGetPropertyFromBean() {
            TestBean bean = new TestBean("John", 30);
            StandardContext ctx = new StandardContext();

            Object result = PropertyAccessNode.getPropertyValue(bean, "name", ctx);
            assertThat(result).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("ToExpressionString Tests | 表达式字符串测试")
    class ToExpressionStringTests {

        @Test
        @DisplayName("Standard access format | 标准访问格式")
        void testStandardAccessFormat() {
            PropertyAccessNode node = PropertyAccessNode.of(IdentifierNode.of("obj"), "name");
            assertThat(node.toExpressionString()).isEqualTo("obj.name");
        }

        @Test
        @DisplayName("Null-safe access format | 空安全访问格式")
        void testNullSafeAccessFormat() {
            PropertyAccessNode node = PropertyAccessNode.nullSafe(IdentifierNode.of("obj"), "name");
            assertThat(node.toExpressionString()).isEqualTo("obj?.name");
        }
    }

    @Nested
    @DisplayName("Record Tests | Record 测试")
    class RecordTestsInner {

        @Test
        @DisplayName("Accessors | 访问器")
        void testAccessors() {
            PropertyAccessNode node = new PropertyAccessNode(IdentifierNode.of("obj"), "name", true);
            assertThat(node.target()).isEqualTo(IdentifierNode.of("obj"));
            assertThat(node.property()).isEqualTo("name");
            assertThat(node.nullSafe()).isTrue();
        }

        @Test
        @DisplayName("getTypeName | 获取类型名称")
        void testGetTypeName() {
            assertThat(PropertyAccessNode.of(IdentifierNode.of("obj"), "name").getTypeName())
                    .isEqualTo("PropertyAccess");
        }
    }

    // Test helper class
    public static class TestBean {
        private String name;
        private int age;
        private boolean active;

        public TestBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}

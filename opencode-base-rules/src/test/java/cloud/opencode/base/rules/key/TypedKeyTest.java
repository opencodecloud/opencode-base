package cloud.opencode.base.rules.key;

import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.DefaultFactStore;
import cloud.opencode.base.rules.model.FactStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for TypedKey
 * TypedKey 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("TypedKey - Type-Safe Key | 类型安全键")
class TypedKeyTest {

    @Nested
    @DisplayName("Creation | 创建")
    class CreationTest {

        @Test
        @DisplayName("of() creates typed key | of() 创建类型化键")
        void ofCreatesTypedKey() {
            TypedKey<String> key = TypedKey.of("name", String.class);
            assertThat(key.name()).isEqualTo("name");
            assertThat(key.type()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Constructor creates typed key | 构造函数创建类型化键")
        void constructorCreatesTypedKey() {
            TypedKey<Integer> key = new TypedKey<>("age", Integer.class);
            assertThat(key.name()).isEqualTo("age");
            assertThat(key.type()).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("Validation | 验证")
    class ValidationTest {

        @Test
        @DisplayName("Null name throws NullPointerException | null名称抛出NullPointerException")
        void nullNameThrows() {
            assertThatThrownBy(() -> TypedKey.of(null, String.class))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("Null type throws NullPointerException | null类型抛出NullPointerException")
        void nullTypeThrows() {
            assertThatThrownBy(() -> TypedKey.of("key", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("type");
        }
    }

    @Nested
    @DisplayName("Equality | 相等性")
    class EqualityTest {

        @Test
        @DisplayName("Same name and type are equal | 相同名称和类型相等")
        void sameNameAndTypeAreEqual() {
            TypedKey<String> key1 = TypedKey.of("name", String.class);
            TypedKey<String> key2 = TypedKey.of("name", String.class);
            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        @DisplayName("Different name not equal | 不同名称不相等")
        void differentNameNotEqual() {
            TypedKey<String> key1 = TypedKey.of("name1", String.class);
            TypedKey<String> key2 = TypedKey.of("name2", String.class);
            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("Different type not equal | 不同类型不相等")
        void differentTypeNotEqual() {
            TypedKey<String> key1 = TypedKey.of("key", String.class);
            TypedKey<Integer> key2 = TypedKey.of("key", Integer.class);
            assertThat(key1).isNotEqualTo(key2);
        }
    }

    @Nested
    @DisplayName("toString | 字符串表示")
    class ToStringTest {

        @Test
        @DisplayName("toString returns expected format | toString返回预期格式")
        void toStringFormat() {
            TypedKey<String> key = TypedKey.of("customerType", String.class);
            assertThat(key.toString())
                    .isEqualTo("TypedKey{name='customerType',type=java.lang.String}");
        }
    }

    @Nested
    @DisplayName("FactStore integration | FactStore集成")
    class FactStoreIntegrationTest {

        @Test
        @DisplayName("put and get with TypedKey | 使用TypedKey存取")
        void putAndGetWithTypedKey() {
            FactStore store = new DefaultFactStore();
            TypedKey<String> key = TypedKey.of("customer", String.class);

            store.put(key, "VIP");
            Optional<String> result = store.get(key);

            assertThat(result).isPresent().contains("VIP");
        }

        @Test
        @DisplayName("get returns empty for wrong type | 类型不匹配返回空")
        void getReturnsEmptyForWrongType() {
            FactStore store = new DefaultFactStore();
            store.add("amount", "not-a-number");

            TypedKey<Integer> key = TypedKey.of("amount", Integer.class);
            Optional<Integer> result = store.get(key);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("get returns empty for missing key | 不存在的键返回空")
        void getReturnsEmptyForMissingKey() {
            FactStore store = new DefaultFactStore();
            TypedKey<String> key = TypedKey.of("missing", String.class);
            Optional<String> result = store.get(key);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("contains with TypedKey | 使用TypedKey检查包含")
        void containsWithTypedKey() {
            FactStore store = new DefaultFactStore();
            TypedKey<String> key = TypedKey.of("name", String.class);

            assertThat(store.contains(key)).isFalse();
            store.put(key, "Alice");
            assertThat(store.contains(key)).isTrue();
        }
    }

    @Nested
    @DisplayName("RuleContext integration | RuleContext集成")
    class RuleContextIntegrationTest {

        @Test
        @DisplayName("put and get with TypedKey | 使用TypedKey存取")
        void putAndGetWithTypedKey() {
            RuleContext context = RuleContext.create();
            TypedKey<Double> amount = TypedKey.of("amount", Double.class);

            context.put(amount, 99.95);
            Double result = context.get(amount);

            assertThat(result).isEqualTo(99.95);
        }

        @Test
        @DisplayName("get with default value | 带默认值获取")
        void getWithDefaultValue() {
            RuleContext context = RuleContext.create();
            TypedKey<String> key = TypedKey.of("missing", String.class);

            String result = context.get(key, "default");
            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("get returns null for missing key | 不存在的键返回null")
        void getReturnsNullForMissing() {
            RuleContext context = RuleContext.create();
            TypedKey<String> key = TypedKey.of("missing", String.class);

            assertThat(context.get(key)).isNull();
        }

        @Test
        @DisplayName("contains with TypedKey | 使用TypedKey检查包含")
        void containsWithTypedKey() {
            RuleContext context = RuleContext.create();
            TypedKey<Integer> key = TypedKey.of("count", Integer.class);

            assertThat(context.contains(key)).isFalse();
            context.put(key, 42);
            assertThat(context.contains(key)).isTrue();
        }

        @Test
        @DisplayName("get falls back to facts | get回退到事实")
        void getFallsBackToFacts() {
            RuleContext context = RuleContext.create();
            TypedKey<String> key = TypedKey.of("factKey", String.class);

            context.addFact("factKey", "factValue");
            String result = context.get(key);

            assertThat(result).isEqualTo("factValue");
        }

        @Test
        @DisplayName("variables take precedence over facts | 变量优先于事实")
        void variablesTakePrecedenceOverFacts() {
            RuleContext context = RuleContext.create();
            TypedKey<String> key = TypedKey.of("key", String.class);

            context.addFact("key", "fromFact");
            context.put(key, "fromVariable");

            assertThat(context.get(key)).isEqualTo("fromVariable");
        }
    }
}

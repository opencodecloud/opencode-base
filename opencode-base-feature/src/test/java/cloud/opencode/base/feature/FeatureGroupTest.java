package cloud.opencode.base.feature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureGroup 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureGroup 测试")
class FeatureGroupTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建")
        void testNormalConstruction() {
            FeatureGroup group = new FeatureGroup("payment", "Payment features",
                    Set.of("pay-v2", "pay-refund"));

            assertThat(group.name()).isEqualTo("payment");
            assertThat(group.description()).isEqualTo("Payment features");
            assertThat(group.featureKeys()).containsExactlyInAnyOrder("pay-v2", "pay-refund");
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testNullName() {
            assertThatThrownBy(() -> new FeatureGroup(null, "desc", Set.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("空白名称抛出异常")
        void testBlankName() {
            assertThatThrownBy(() -> new FeatureGroup("   ", "desc", Set.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("null featureKeys使用空集合")
        void testNullFeatureKeys() {
            FeatureGroup group = new FeatureGroup("test", "desc", null);

            assertThat(group.featureKeys()).isEmpty();
        }

        @Test
        @DisplayName("featureKeys不可变")
        void testImmutableFeatureKeys() {
            FeatureGroup group = new FeatureGroup("test", "desc", Set.of("key1"));

            assertThatThrownBy(() -> group.featureKeys().add("key2"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("contains() 测试")
    class ContainsTests {

        @Test
        @DisplayName("包含已注册的键")
        void testContainsRegisteredKey() {
            FeatureGroup group = new FeatureGroup("test", "desc", Set.of("key1", "key2"));

            assertThat(group.contains("key1")).isTrue();
            assertThat(group.contains("key2")).isTrue();
        }

        @Test
        @DisplayName("不包含未注册的键")
        void testDoesNotContainUnregisteredKey() {
            FeatureGroup group = new FeatureGroup("test", "desc", Set.of("key1"));

            assertThat(group.contains("key2")).isFalse();
        }
    }

    @Nested
    @DisplayName("size() 测试")
    class SizeTests {

        @Test
        @DisplayName("返回正确大小")
        void testSize() {
            FeatureGroup group = new FeatureGroup("test", "desc", Set.of("k1", "k2", "k3"));

            assertThat(group.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("空组大小为0")
        void testEmptySize() {
            FeatureGroup group = new FeatureGroup("test", "desc", Set.of());

            assertThat(group.size()).isZero();
        }
    }
}

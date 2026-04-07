package cloud.opencode.base.feature.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureLifecycle 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureLifecycle 测试")
class FeatureLifecycleTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("包含所有生命周期状态")
        void testAllValues() {
            assertThat(FeatureLifecycle.values())
                    .containsExactly(
                            FeatureLifecycle.CREATED,
                            FeatureLifecycle.ACTIVE,
                            FeatureLifecycle.DEPRECATED,
                            FeatureLifecycle.ARCHIVED
                    );
        }

        @Test
        @DisplayName("valueOf正确解析")
        void testValueOf() {
            assertThat(FeatureLifecycle.valueOf("CREATED")).isEqualTo(FeatureLifecycle.CREATED);
            assertThat(FeatureLifecycle.valueOf("ACTIVE")).isEqualTo(FeatureLifecycle.ACTIVE);
            assertThat(FeatureLifecycle.valueOf("DEPRECATED")).isEqualTo(FeatureLifecycle.DEPRECATED);
            assertThat(FeatureLifecycle.valueOf("ARCHIVED")).isEqualTo(FeatureLifecycle.ARCHIVED);
        }
    }

    @Nested
    @DisplayName("getDescription() 测试")
    class DescriptionTests {

        @Test
        @DisplayName("英文描述不为空")
        void testDescriptions() {
            for (FeatureLifecycle lifecycle : FeatureLifecycle.values()) {
                assertThat(lifecycle.getDescription()).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("中文描述不为空")
        void testDescriptionsZh() {
            for (FeatureLifecycle lifecycle : FeatureLifecycle.values()) {
                assertThat(lifecycle.getDescriptionZh()).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("isUsable() 测试")
    class IsUsableTests {

        @Test
        @DisplayName("CREATED可用")
        void testCreatedIsUsable() {
            assertThat(FeatureLifecycle.CREATED.isUsable()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE可用")
        void testActiveIsUsable() {
            assertThat(FeatureLifecycle.ACTIVE.isUsable()).isTrue();
        }

        @Test
        @DisplayName("DEPRECATED不可用")
        void testDeprecatedNotUsable() {
            assertThat(FeatureLifecycle.DEPRECATED.isUsable()).isFalse();
        }

        @Test
        @DisplayName("ARCHIVED不可用")
        void testArchivedNotUsable() {
            assertThat(FeatureLifecycle.ARCHIVED.isUsable()).isFalse();
        }
    }
}

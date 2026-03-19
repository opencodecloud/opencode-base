package cloud.opencode.base.feature;

import cloud.opencode.base.feature.strategy.AlwaysOffStrategy;
import cloud.opencode.base.feature.strategy.AlwaysOnStrategy;
import cloud.opencode.base.feature.strategy.PercentageStrategy;
import cloud.opencode.base.feature.strategy.UserListStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Feature 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("Feature 测试")
class FeatureTest {

    @Nested
    @DisplayName("builder() 测试")
    class BuilderTests {

        @Test
        @DisplayName("使用key创建builder")
        void testBuilderWithKey() {
            Feature feature = Feature.builder("test-feature").build();

            assertThat(feature.key()).isEqualTo("test-feature");
            assertThat(feature.name()).isEqualTo("test-feature");
            assertThat(feature.description()).isNull();
            assertThat(feature.defaultEnabled()).isFalse();
            assertThat(feature.strategy()).isNull();
        }

        @Test
        @DisplayName("null key抛出异常")
        void testBuilderWithNullKey() {
            assertThatThrownBy(() -> Feature.builder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("空白key抛出异常")
        void testBuilderWithBlankKey() {
            assertThatThrownBy(() -> Feature.builder("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null or blank");
        }

        @Test
        @DisplayName("设置名称")
        void testBuilderWithName() {
            Feature feature = Feature.builder("key")
                    .name("Display Name")
                    .build();

            assertThat(feature.name()).isEqualTo("Display Name");
        }

        @Test
        @DisplayName("设置描述")
        void testBuilderWithDescription() {
            Feature feature = Feature.builder("key")
                    .description("Feature description")
                    .build();

            assertThat(feature.description()).isEqualTo("Feature description");
        }

        @Test
        @DisplayName("设置默认启用状态")
        void testBuilderWithDefaultEnabled() {
            Feature feature = Feature.builder("key")
                    .defaultEnabled(true)
                    .build();

            assertThat(feature.defaultEnabled()).isTrue();
        }

        @Test
        @DisplayName("设置策略")
        void testBuilderWithStrategy() {
            Feature feature = Feature.builder("key")
                    .strategy(AlwaysOnStrategy.INSTANCE)
                    .build();

            assertThat(feature.strategy()).isEqualTo(AlwaysOnStrategy.INSTANCE);
        }

        @Test
        @DisplayName("alwaysOn()快捷方法")
        void testBuilderAlwaysOn() {
            Feature feature = Feature.builder("key")
                    .alwaysOn()
                    .build();

            assertThat(feature.strategy()).isEqualTo(AlwaysOnStrategy.INSTANCE);
        }

        @Test
        @DisplayName("alwaysOff()快捷方法")
        void testBuilderAlwaysOff() {
            Feature feature = Feature.builder("key")
                    .alwaysOff()
                    .build();

            assertThat(feature.strategy()).isEqualTo(AlwaysOffStrategy.INSTANCE);
        }

        @Test
        @DisplayName("percentage()快捷方法")
        void testBuilderPercentage() {
            Feature feature = Feature.builder("key")
                    .percentage(50)
                    .build();

            assertThat(feature.strategy()).isInstanceOf(PercentageStrategy.class);
            assertThat(((PercentageStrategy) feature.strategy()).getPercentage()).isEqualTo(50);
        }

        @Test
        @DisplayName("forUsers(String...)快捷方法")
        void testBuilderForUsersVarargs() {
            Feature feature = Feature.builder("key")
                    .forUsers("user1", "user2")
                    .build();

            assertThat(feature.strategy()).isInstanceOf(UserListStrategy.class);
            assertThat(((UserListStrategy) feature.strategy()).getAllowedUsers())
                    .containsExactlyInAnyOrder("user1", "user2");
        }

        @Test
        @DisplayName("forUsers(Set)快捷方法")
        void testBuilderForUsersSet() {
            Feature feature = Feature.builder("key")
                    .forUsers(Set.of("user1", "user2"))
                    .build();

            assertThat(feature.strategy()).isInstanceOf(UserListStrategy.class);
        }

        @Test
        @DisplayName("添加单个元数据")
        void testBuilderWithSingleMetadata() {
            Feature feature = Feature.builder("key")
                    .metadata("version", "1.0")
                    .build();

            assertThat(feature.metadata()).containsEntry("version", "1.0");
        }

        @Test
        @DisplayName("添加多个元数据")
        void testBuilderWithMultipleMetadata() {
            Feature feature = Feature.builder("key")
                    .metadata("key1", "value1")
                    .metadata("key2", 123)
                    .build();

            assertThat(feature.metadata())
                    .containsEntry("key1", "value1")
                    .containsEntry("key2", 123);
        }

        @Test
        @DisplayName("从Map添加元数据")
        void testBuilderWithMetadataMap() {
            Feature feature = Feature.builder("key")
                    .metadata(Map.of("k1", "v1", "k2", "v2"))
                    .build();

            assertThat(feature.metadata()).hasSize(2);
        }

        @Test
        @DisplayName("null元数据Map不抛异常")
        void testBuilderWithNullMetadataMap() {
            Feature feature = Feature.builder("key")
                    .metadata((Map<String, Object>) null)
                    .build();

            assertThat(feature.metadata()).isEmpty();
        }

        @Test
        @DisplayName("时间戳自动设置")
        void testBuilderTimestamps() {
            Instant before = Instant.now();
            Feature feature = Feature.builder("key").build();
            Instant after = Instant.now();

            assertThat(feature.createdAt()).isBetween(before, after);
            assertThat(feature.updatedAt()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("无策略使用默认值false")
        void testIsEnabledWithoutStrategyDefaultFalse() {
            Feature feature = Feature.builder("key")
                    .defaultEnabled(false)
                    .build();

            assertThat(feature.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("无策略使用默认值true")
        void testIsEnabledWithoutStrategyDefaultTrue() {
            Feature feature = Feature.builder("key")
                    .defaultEnabled(true)
                    .build();

            assertThat(feature.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("AlwaysOn策略返回true")
        void testIsEnabledWithAlwaysOnStrategy() {
            Feature feature = Feature.builder("key")
                    .alwaysOn()
                    .build();

            assertThat(feature.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("AlwaysOff策略返回false")
        void testIsEnabledWithAlwaysOffStrategy() {
            Feature feature = Feature.builder("key")
                    .alwaysOff()
                    .build();

            assertThat(feature.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("带上下文检查")
        void testIsEnabledWithContext() {
            Feature feature = Feature.builder("key")
                    .forUsers("user1")
                    .build();

            assertThat(feature.isEnabled(FeatureContext.ofUser("user1"))).isTrue();
            assertThat(feature.isEnabled(FeatureContext.ofUser("user2"))).isFalse();
        }
    }

    @Nested
    @DisplayName("getMetadata() 测试")
    class GetMetadataTests {

        @Test
        @DisplayName("获取存在的元数据")
        void testGetExistingMetadata() {
            Feature feature = Feature.builder("key")
                    .metadata("version", "1.0")
                    .build();

            String version = feature.getMetadata("version");
            assertThat(version).isEqualTo("1.0");
        }

        @Test
        @DisplayName("获取不存在的元数据返回null")
        void testGetNonExistingMetadata() {
            Feature feature = Feature.builder("key").build();

            String value = feature.getMetadata("nonexistent");
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("获取元数据带默认值")
        void testGetMetadataWithDefault() {
            Feature feature = Feature.builder("key").build();

            String value = feature.getMetadata("key", "default");
            assertThat(value).isEqualTo("default");
        }

        @Test
        @DisplayName("存在值时不使用默认值")
        void testGetMetadataExistsNoDefault() {
            Feature feature = Feature.builder("key")
                    .metadata("key", "actual")
                    .build();

            String value = feature.getMetadata("key", "default");
            assertThat(value).isEqualTo("actual");
        }
    }

    @Nested
    @DisplayName("withStrategy() 测试")
    class WithStrategyTests {

        @Test
        @DisplayName("创建新策略副本")
        void testWithStrategy() {
            Feature original = Feature.builder("key")
                    .name("Test")
                    .alwaysOff()
                    .build();

            Feature updated = original.withStrategy(AlwaysOnStrategy.INSTANCE);

            assertThat(updated.key()).isEqualTo(original.key());
            assertThat(updated.name()).isEqualTo(original.name());
            assertThat(updated.strategy()).isEqualTo(AlwaysOnStrategy.INSTANCE);
            assertThat(original.strategy()).isEqualTo(AlwaysOffStrategy.INSTANCE);
        }

        @Test
        @DisplayName("更新时间戳")
        void testWithStrategyUpdatesTimestamp() throws InterruptedException {
            Feature original = Feature.builder("key").build();
            Thread.sleep(10);
            Feature updated = original.withStrategy(AlwaysOnStrategy.INSTANCE);

            assertThat(updated.updatedAt()).isAfter(original.updatedAt());
        }
    }

    @Nested
    @DisplayName("Record组件测试")
    class RecordComponentTests {

        @Test
        @DisplayName("所有record组件可访问")
        void testRecordComponents() {
            Instant now = Instant.now();
            Feature feature = new Feature(
                    "key", "name", "desc", true,
                    AlwaysOnStrategy.INSTANCE,
                    Map.of("k", "v"),
                    now, now
            );

            assertThat(feature.key()).isEqualTo("key");
            assertThat(feature.name()).isEqualTo("name");
            assertThat(feature.description()).isEqualTo("desc");
            assertThat(feature.defaultEnabled()).isTrue();
            assertThat(feature.strategy()).isEqualTo(AlwaysOnStrategy.INSTANCE);
            assertThat(feature.metadata()).containsEntry("k", "v");
            assertThat(feature.createdAt()).isEqualTo(now);
            assertThat(feature.updatedAt()).isEqualTo(now);
        }
    }
}

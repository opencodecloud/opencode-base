package cloud.opencode.base.json;

import cloud.opencode.base.json.annotation.JsonNaming;
import cloud.opencode.base.json.spi.JsonFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonConfig 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonConfig 测试")
class JsonConfigTest {

    @Nested
    @DisplayName("DEFAULT常量测试")
    class DefaultConstantTests {

        @Test
        @DisplayName("DEFAULT不为null")
        void testDefaultNotNull() {
            assertThat(JsonConfig.DEFAULT).isNotNull();
        }

        @Test
        @DisplayName("DEFAULT有默认值")
        void testDefaultValues() {
            JsonConfig config = JsonConfig.DEFAULT;

            assertThat(config.getNamingStrategy()).isEqualTo(JsonNaming.Strategy.IDENTITY);
            assertThat(config.getMaxDepth()).isEqualTo(1000);
            assertThat(config.getMaxStringLength()).isEqualTo(20_000_000);
            assertThat(config.getMaxSize()).isEqualTo(100_000);
            assertThat(config.getIndent()).isEqualTo("  ");
        }
    }

    @Nested
    @DisplayName("builder方法测试")
    class BuilderTests {

        @Test
        @DisplayName("创建构建器")
        void testBuilder() {
            JsonConfig.Builder builder = JsonConfig.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("构建配置")
        void testBuild() {
            JsonConfig config = JsonConfig.builder().build();

            assertThat(config).isNotNull();
        }
    }

    @Nested
    @DisplayName("enable方法测试")
    class EnableTests {

        @Test
        @DisplayName("启用单个特性")
        void testEnableSingle() {
            JsonConfig config = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT)
                .build();

            assertThat(config.isEnabled(JsonFeature.PRETTY_PRINT)).isTrue();
        }

        @Test
        @DisplayName("启用多个特性")
        void testEnableMultiple() {
            JsonConfig config = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT, JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
                .build();

            assertThat(config.isEnabled(JsonFeature.PRETTY_PRINT)).isTrue();
            assertThat(config.isEnabled(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)).isTrue();
        }

        @Test
        @DisplayName("启用覆盖禁用")
        void testEnableOverridesDisable() {
            JsonConfig config = JsonConfig.builder()
                .disable(JsonFeature.PRETTY_PRINT)
                .enable(JsonFeature.PRETTY_PRINT)
                .build();

            assertThat(config.isEnabled(JsonFeature.PRETTY_PRINT)).isTrue();
        }
    }

    @Nested
    @DisplayName("disable方法测试")
    class DisableTests {

        @Test
        @DisplayName("禁用单个特性")
        void testDisableSingle() {
            JsonConfig config = JsonConfig.builder()
                .disable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
                .build();

            assertThat(config.isEnabled(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)).isFalse();
        }

        @Test
        @DisplayName("禁用多个特性")
        void testDisableMultiple() {
            JsonConfig config = JsonConfig.builder()
                .disable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES, JsonFeature.INCLUDE_NULL_PROPERTIES)
                .build();

            assertThat(config.isEnabled(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)).isFalse();
            assertThat(config.isEnabled(JsonFeature.INCLUDE_NULL_PROPERTIES)).isFalse();
        }

        @Test
        @DisplayName("禁用覆盖启用")
        void testDisableOverridesEnable() {
            JsonConfig config = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT)
                .disable(JsonFeature.PRETTY_PRINT)
                .build();

            assertThat(config.isEnabled(JsonFeature.PRETTY_PRINT)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEnabled方法测试")
    class IsEnabledTests {

        @Test
        @DisplayName("检查启用的特性")
        void testIsEnabledTrue() {
            JsonConfig config = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT)
                .build();

            assertThat(config.isEnabled(JsonFeature.PRETTY_PRINT)).isTrue();
        }

        @Test
        @DisplayName("检查禁用的特性")
        void testIsEnabledFalse() {
            JsonConfig config = JsonConfig.builder()
                .disable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
                .build();

            assertThat(config.isEnabled(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)).isFalse();
        }

        @Test
        @DisplayName("未设置特性使用默认值")
        void testIsEnabledDefault() {
            JsonConfig config = JsonConfig.builder().build();

            // Check default-enabled feature
            for (JsonFeature feature : JsonFeature.values()) {
                if (feature.isEnabledByDefault()) {
                    assertThat(config.isEnabled(feature)).isTrue();
                }
            }
        }
    }

    @Nested
    @DisplayName("namingStrategy方法测试")
    class NamingStrategyTests {

        @Test
        @DisplayName("设置命名策略")
        void testSetNamingStrategy() {
            JsonConfig config = JsonConfig.builder()
                .namingStrategy(JsonNaming.Strategy.SNAKE_CASE)
                .build();

            assertThat(config.getNamingStrategy()).isEqualTo(JsonNaming.Strategy.SNAKE_CASE);
        }

        @Test
        @DisplayName("null策略抛出异常")
        void testNullNamingStrategy() {
            assertThatThrownBy(() -> JsonConfig.builder().namingStrategy(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("dateFormat方法测试")
    class DateFormatTests {

        @Test
        @DisplayName("设置日期格式")
        void testSetDateFormat() {
            JsonConfig config = JsonConfig.builder()
                .dateFormat("yyyy-MM-dd HH:mm:ss")
                .build();

            assertThat(config.getDateFormat()).isEqualTo("yyyy-MM-dd HH:mm:ss");
        }

        @Test
        @DisplayName("null格式抛出异常")
        void testNullDateFormat() {
            assertThatThrownBy(() -> JsonConfig.builder().dateFormat(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("timezone方法测试")
    class TimezoneTests {

        @Test
        @DisplayName("设置时区(ZoneId)")
        void testSetTimezoneZoneId() {
            ZoneId zone = ZoneId.of("Asia/Shanghai");
            JsonConfig config = JsonConfig.builder()
                .timezone(zone)
                .build();

            assertThat(config.getTimezone()).isEqualTo(zone);
        }

        @Test
        @DisplayName("设置时区(String)")
        void testSetTimezoneString() {
            JsonConfig config = JsonConfig.builder()
                .timezone("Europe/London")
                .build();

            assertThat(config.getTimezone()).isEqualTo(ZoneId.of("Europe/London"));
        }

        @Test
        @DisplayName("null时区抛出异常")
        void testNullTimezone() {
            assertThatThrownBy(() -> JsonConfig.builder().timezone((ZoneId) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("maxDepth方法测试")
    class MaxDepthTests {

        @Test
        @DisplayName("设置最大深度")
        void testSetMaxDepth() {
            JsonConfig config = JsonConfig.builder()
                .maxDepth(500)
                .build();

            assertThat(config.getMaxDepth()).isEqualTo(500);
        }

        @Test
        @DisplayName("非正值抛出异常")
        void testNonPositiveMaxDepth() {
            assertThatThrownBy(() -> JsonConfig.builder().maxDepth(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

            assertThatThrownBy(() -> JsonConfig.builder().maxDepth(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("maxStringLength方法测试")
    class MaxStringLengthTests {

        @Test
        @DisplayName("设置最大字符串长度")
        void testSetMaxStringLength() {
            JsonConfig config = JsonConfig.builder()
                .maxStringLength(10000)
                .build();

            assertThat(config.getMaxStringLength()).isEqualTo(10000);
        }

        @Test
        @DisplayName("非正值抛出异常")
        void testNonPositiveMaxStringLength() {
            assertThatThrownBy(() -> JsonConfig.builder().maxStringLength(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("maxSize方法测试")
    class MaxSizeTests {

        @Test
        @DisplayName("设置最大大小")
        void testSetMaxSize() {
            JsonConfig config = JsonConfig.builder()
                .maxSize(50000)
                .build();

            assertThat(config.getMaxSize()).isEqualTo(50000);
        }

        @Test
        @DisplayName("非正值抛出异常")
        void testNonPositiveMaxSize() {
            assertThatThrownBy(() -> JsonConfig.builder().maxSize(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("indent方法测试")
    class IndentTests {

        @Test
        @DisplayName("设置缩进")
        void testSetIndent() {
            JsonConfig config = JsonConfig.builder()
                .indent("\t")
                .build();

            assertThat(config.getIndent()).isEqualTo("\t");
        }

        @Test
        @DisplayName("null缩进抛出异常")
        void testNullIndent() {
            assertThatThrownBy(() -> JsonConfig.builder().indent(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("prettyPrint方法测试")
    class PrettyPrintTests {

        @Test
        @DisplayName("启用美化打印")
        void testPrettyPrint() {
            JsonConfig config = JsonConfig.builder()
                .prettyPrint()
                .build();

            assertThat(config.isEnabled(JsonFeature.PRETTY_PRINT)).isTrue();
        }
    }

    @Nested
    @DisplayName("toBuilder方法测试")
    class ToBuilderTests {

        @Test
        @DisplayName("从配置创建构建器")
        void testToBuilder() {
            JsonConfig original = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT)
                .namingStrategy(JsonNaming.Strategy.SNAKE_CASE)
                .maxDepth(500)
                .build();

            JsonConfig copy = original.toBuilder().build();

            assertThat(copy.isEnabled(JsonFeature.PRETTY_PRINT)).isTrue();
            assertThat(copy.getNamingStrategy()).isEqualTo(JsonNaming.Strategy.SNAKE_CASE);
            assertThat(copy.getMaxDepth()).isEqualTo(500);
        }

        @Test
        @DisplayName("修改副本不影响原始")
        void testToBuilderModification() {
            JsonConfig original = JsonConfig.builder()
                .maxDepth(500)
                .build();

            JsonConfig modified = original.toBuilder()
                .maxDepth(1000)
                .build();

            assertThat(original.getMaxDepth()).isEqualTo(500);
            assertThat(modified.getMaxDepth()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("getEnabledFeatures方法测试")
    class GetEnabledFeaturesTests {

        @Test
        @DisplayName("返回启用的特性集合")
        void testGetEnabledFeatures() {
            JsonConfig config = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT)
                .enable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
                .build();

            assertThat(config.getEnabledFeatures())
                .contains(JsonFeature.PRETTY_PRINT, JsonFeature.IGNORE_UNKNOWN_PROPERTIES);
        }

        @Test
        @DisplayName("返回不可修改集合")
        void testUnmodifiableSet() {
            JsonConfig config = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT)
                .build();

            assertThatThrownBy(() -> config.getEnabledFeatures().add(JsonFeature.INCLUDE_NULL_PROPERTIES))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class ChainedCallTests {

        @Test
        @DisplayName("完整链式配置")
        void testFullChainedConfiguration() {
            JsonConfig config = JsonConfig.builder()
                .enable(JsonFeature.PRETTY_PRINT)
                .disable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
                .namingStrategy(JsonNaming.Strategy.SNAKE_CASE)
                .dateFormat("yyyy-MM-dd")
                .timezone("UTC")
                .maxDepth(100)
                .maxStringLength(1000000)
                .maxSize(10000)
                .indent("    ")
                .build();

            assertThat(config.isEnabled(JsonFeature.PRETTY_PRINT)).isTrue();
            assertThat(config.isEnabled(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)).isFalse();
            assertThat(config.getNamingStrategy()).isEqualTo(JsonNaming.Strategy.SNAKE_CASE);
            assertThat(config.getDateFormat()).isEqualTo("yyyy-MM-dd");
            assertThat(config.getTimezone()).isEqualTo(ZoneId.of("UTC"));
            assertThat(config.getMaxDepth()).isEqualTo(100);
            assertThat(config.getMaxStringLength()).isEqualTo(1000000);
            assertThat(config.getMaxSize()).isEqualTo(10000);
            assertThat(config.getIndent()).isEqualTo("    ");
        }
    }
}

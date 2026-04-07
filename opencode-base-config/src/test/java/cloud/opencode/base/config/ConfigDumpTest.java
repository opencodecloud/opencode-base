package cloud.opencode.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigDump 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
@DisplayName("ConfigDump 测试")
class ConfigDumpTest {

    private Config buildConfig(Map<String, String> props) {
        return OpenConfig.builder()
                .addProperties(props)
                .build();
    }

    @Nested
    @DisplayName("dump 默认模式测试")
    class DumpDefaultPatternsTests {

        @Test
        @DisplayName("默认模式掩码 password 键")
        void testMasksPasswordKey() {
            Config config = buildConfig(Map.of("db.password", "secret123"));
            Map<String, String> result = ConfigDump.dump(config);
            assertThat(result).containsEntry("db.password", "***");
        }

        @Test
        @DisplayName("默认模式掩码 secret 键")
        void testMasksSecretKey() {
            Config config = buildConfig(Map.of("app.secret", "mysecret"));
            Map<String, String> result = ConfigDump.dump(config);
            assertThat(result).containsEntry("app.secret", "***");
        }

        @Test
        @DisplayName("默认模式掩码 token 键")
        void testMasksTokenKey() {
            Config config = buildConfig(Map.of("auth.token", "abc123"));
            Map<String, String> result = ConfigDump.dump(config);
            assertThat(result).containsEntry("auth.token", "***");
        }

        @Test
        @DisplayName("非敏感键不被掩码")
        void testNonSensitiveKeyNotMasked() {
            Config config = buildConfig(Map.of("db.url", "jdbc:mysql://localhost"));
            Map<String, String> result = ConfigDump.dump(config);
            assertThat(result).containsEntry("db.url", "jdbc:mysql://localhost");
        }

        @Test
        @DisplayName("嵌套键中包含 password 段被掩码")
        void testNestedPasswordSegmentMasked() {
            Config config = buildConfig(Map.of("db.connection.password", "pass123"));
            Map<String, String> result = ConfigDump.dump(config);
            assertThat(result).containsEntry("db.connection.password", "***");
        }

        @Test
        @DisplayName("混合敏感和非敏感键")
        void testMixedKeys() {
            Config config = buildConfig(Map.of(
                    "db.url", "jdbc:mysql://localhost",
                    "db.password", "secret",
                    "app.name", "myapp"
            ));
            Map<String, String> result = ConfigDump.dump(config);
            assertThat(result).containsEntry("db.url", "jdbc:mysql://localhost");
            assertThat(result).containsEntry("db.password", "***");
            assertThat(result).containsEntry("app.name", "myapp");
        }
    }

    @Nested
    @DisplayName("dump 自定义模式测试")
    class DumpCustomPatternsTests {

        @Test
        @DisplayName("自定义模式仅掩码指定模式")
        void testCustomPatternsOnlyMaskSpecified() {
            Config config = buildConfig(Map.of(
                    "db.password", "secret",
                    "db.url", "jdbc:mysql://localhost"
            ));
            Map<String, String> result = ConfigDump.dump(config, Set.of("url"));
            assertThat(result).containsEntry("db.url", "***");
            assertThat(result).containsEntry("db.password", "secret");
        }

        @Test
        @DisplayName("空自定义模式不掩码任何键")
        void testEmptyCustomPatternsNoMasking() {
            Config config = buildConfig(Map.of("db.password", "secret"));
            Map<String, String> result = ConfigDump.dump(config, Set.of());
            assertThat(result).containsEntry("db.password", "secret");
        }
    }

    @Nested
    @DisplayName("dumpToString 测试")
    class DumpToStringTests {

        @Test
        @DisplayName("格式化输出：排序且每行一个键值对")
        void testFormattedOutput() {
            Config config = buildConfig(Map.of(
                    "z.name", "last",
                    "a.name", "first"
            ));
            String result = ConfigDump.dumpToString(config);
            assertThat(result).isEqualTo("a.name=first\nz.name=last");
        }

        @Test
        @DisplayName("格式化输出中敏感值被掩码")
        void testFormattedOutputMasksSensitive() {
            Config config = buildConfig(Map.of(
                    "app.password", "secret"
            ));
            String result = ConfigDump.dumpToString(config);
            assertThat(result).contains("app.password=***");
        }

        @Test
        @DisplayName("自定义模式格式化输出")
        void testFormattedOutputCustomPatterns() {
            Config config = buildConfig(Map.of(
                    "app.name", "myapp",
                    "app.password", "secret"
            ));
            String result = ConfigDump.dumpToString(config, Set.of("name"));
            assertThat(result).contains("app.name=***");
            assertThat(result).contains("app.password=secret");
        }
    }

    @Nested
    @DisplayName("isSensitive 测试")
    class IsSensitiveTests {

        @Test
        @DisplayName("包含敏感模式的键返回 true")
        void testPositiveMatch() {
            assertThat(ConfigDump.isSensitive("db.password", Set.of("password"))).isTrue();
        }

        @Test
        @DisplayName("不包含敏感模式的键返回 false")
        void testNegativeMatch() {
            assertThat(ConfigDump.isSensitive("db.url", Set.of("password"))).isFalse();
        }

        @Test
        @DisplayName("大小写不敏感匹配")
        void testCaseInsensitive() {
            assertThat(ConfigDump.isSensitive("db.PASSWORD", Set.of("password"))).isTrue();
            assertThat(ConfigDump.isSensitive("db.Password", Set.of("PASSWORD"))).isTrue();
        }

        @Test
        @DisplayName("多段键中任一段匹配即返回 true")
        void testMultiSegmentMatch() {
            assertThat(ConfigDump.isSensitive("a.secret.b", Set.of("secret"))).isTrue();
        }

        @Test
        @DisplayName("完整键匹配")
        void testFullKeyMatch() {
            assertThat(ConfigDump.isSensitive("apikey", Set.of("apikey"))).isTrue();
        }
    }

    @Nested
    @DisplayName("mask 测试")
    class MaskTests {

        @Test
        @DisplayName("mask 返回 ***")
        void testMaskReturnsStars() {
            assertThat(ConfigDump.mask("any value")).isEqualTo("***");
        }

        @Test
        @DisplayName("mask null 值也返回 ***")
        void testMaskNullReturnsStars() {
            assertThat(ConfigDump.mask(null)).isEqualTo("***");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("null config 抛出 NullPointerException")
        void testNullConfigThrowsNpe() {
            assertThatNullPointerException().isThrownBy(() -> ConfigDump.dump(null));
        }

        @Test
        @DisplayName("null config dumpToString 抛出 NullPointerException")
        void testNullConfigDumpToStringThrowsNpe() {
            assertThatNullPointerException().isThrownBy(() -> ConfigDump.dumpToString(null));
        }

        @Test
        @DisplayName("空配置返回空结果")
        void testEmptyConfig() {
            Config config = buildConfig(Map.of());
            Map<String, String> result = ConfigDump.dump(config);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("空配置 dumpToString 返回空字符串")
        void testEmptyConfigDumpToString() {
            Config config = buildConfig(Map.of());
            String result = ConfigDump.dumpToString(config);
            assertThat(result).isEmpty();
        }
    }
}

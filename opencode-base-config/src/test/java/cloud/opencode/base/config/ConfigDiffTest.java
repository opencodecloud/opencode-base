package cloud.opencode.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigDiff 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
@DisplayName("ConfigDiff 测试")
class ConfigDiffTest {

    private Config buildConfig(Map<String, String> props) {
        return OpenConfig.builder()
                .addProperties(props)
                .build();
    }

    @Nested
    @DisplayName("diff(Config, Config) 测试")
    class DiffConfigTests {

        @Test
        @DisplayName("相同配置返回空差异")
        void testIdenticalConfigs() {
            Config config = buildConfig(Map.of("a", "1", "b", "2"));
            List<ConfigChangeEvent> changes = ConfigDiff.diff(config, config);
            assertThat(changes).isEmpty();
        }

        @Test
        @DisplayName("检测新增的键")
        void testAddedKeys() {
            Config before = buildConfig(Map.of("a", "1"));
            Config after = buildConfig(Map.of("a", "1", "b", "2"));
            List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);

            assertThat(changes).hasSize(1);
            assertThat(changes.getFirst().key()).isEqualTo("b");
            assertThat(changes.getFirst().changeType()).isEqualTo(ConfigChangeEvent.ChangeType.ADDED);
            assertThat(changes.getFirst().newValue()).isEqualTo("2");
            assertThat(changes.getFirst().oldValue()).isNull();
        }

        @Test
        @DisplayName("检测删除的键")
        void testRemovedKeys() {
            Config before = buildConfig(Map.of("a", "1", "b", "2"));
            Config after = buildConfig(Map.of("a", "1"));
            List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);

            assertThat(changes).hasSize(1);
            assertThat(changes.getFirst().key()).isEqualTo("b");
            assertThat(changes.getFirst().changeType()).isEqualTo(ConfigChangeEvent.ChangeType.REMOVED);
            assertThat(changes.getFirst().oldValue()).isEqualTo("2");
            assertThat(changes.getFirst().newValue()).isNull();
        }

        @Test
        @DisplayName("检测修改的键")
        void testModifiedKeys() {
            Config before = buildConfig(Map.of("db.port", "3306"));
            Config after = buildConfig(Map.of("db.port", "5432"));
            List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);

            assertThat(changes).hasSize(1);
            assertThat(changes.getFirst().key()).isEqualTo("db.port");
            assertThat(changes.getFirst().changeType()).isEqualTo(ConfigChangeEvent.ChangeType.MODIFIED);
            assertThat(changes.getFirst().oldValue()).isEqualTo("3306");
            assertThat(changes.getFirst().newValue()).isEqualTo("5432");
        }

        @Test
        @DisplayName("混合变更: 新增 + 删除 + 修改")
        void testMixedChanges() {
            Config before = buildConfig(Map.of(
                    "db.port", "3306",
                    "old.key", "abc",
                    "unchanged", "same"
            ));
            Config after = buildConfig(Map.of(
                    "db.port", "5432",
                    "cache.ttl", "60",
                    "unchanged", "same"
            ));
            List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);

            assertThat(changes).hasSize(3);

            // Sorted by key: cache.ttl, db.port, old.key
            assertThat(changes.get(0).key()).isEqualTo("cache.ttl");
            assertThat(changes.get(0).changeType()).isEqualTo(ConfigChangeEvent.ChangeType.ADDED);

            assertThat(changes.get(1).key()).isEqualTo("db.port");
            assertThat(changes.get(1).changeType()).isEqualTo(ConfigChangeEvent.ChangeType.MODIFIED);

            assertThat(changes.get(2).key()).isEqualTo("old.key");
            assertThat(changes.get(2).changeType()).isEqualTo(ConfigChangeEvent.ChangeType.REMOVED);
        }
    }

    @Nested
    @DisplayName("diff(Map, Map) 测试")
    class DiffMapTests {

        @Test
        @DisplayName("Map 差异检测新增")
        void testMapDiffAdded() {
            Map<String, String> before = Map.of("a", "1");
            Map<String, String> after = Map.of("a", "1", "b", "2");
            List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);

            assertThat(changes).hasSize(1);
            assertThat(changes.getFirst().changeType()).isEqualTo(ConfigChangeEvent.ChangeType.ADDED);
        }

        @Test
        @DisplayName("空 before 全部为新增")
        void testEmptyBefore() {
            Map<String, String> before = Map.of();
            Map<String, String> after = Map.of("a", "1", "b", "2");
            List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);

            assertThat(changes).hasSize(2);
            assertThat(changes).allMatch(ConfigChangeEvent::isAdded);
        }

        @Test
        @DisplayName("空 after 全部为删除")
        void testEmptyAfter() {
            Map<String, String> before = Map.of("a", "1", "b", "2");
            Map<String, String> after = Map.of();
            List<ConfigChangeEvent> changes = ConfigDiff.diff(before, after);

            assertThat(changes).hasSize(2);
            assertThat(changes).allMatch(ConfigChangeEvent::isRemoved);
        }

        @Test
        @DisplayName("返回的列表不可修改")
        void testUnmodifiableResult() {
            List<ConfigChangeEvent> changes = ConfigDiff.diff(Map.of("a", "1"), Map.of("b", "2"));
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> changes.add(ConfigChangeEvent.added("x", "y")));
        }
    }

    @Nested
    @DisplayName("format 测试")
    class FormatTests {

        @Test
        @DisplayName("格式化修改事件")
        void testFormatModified() {
            List<ConfigChangeEvent> changes = List.of(
                    ConfigChangeEvent.modified("db.port", "3306", "5432")
            );
            String result = ConfigDiff.format(changes);
            assertThat(result).contains("~ db.port: 3306 -> 5432");
        }

        @Test
        @DisplayName("格式化新增事件")
        void testFormatAdded() {
            List<ConfigChangeEvent> changes = List.of(
                    ConfigChangeEvent.added("cache.ttl", "60")
            );
            String result = ConfigDiff.format(changes);
            assertThat(result).contains("+ cache.ttl = 60");
        }

        @Test
        @DisplayName("格式化删除事件")
        void testFormatRemoved() {
            List<ConfigChangeEvent> changes = List.of(
                    ConfigChangeEvent.removed("old.key", "abc")
            );
            String result = ConfigDiff.format(changes);
            assertThat(result).contains("- old.key = abc");
        }

        @Test
        @DisplayName("格式化混合事件")
        void testFormatMixed() {
            List<ConfigChangeEvent> changes = List.of(
                    ConfigChangeEvent.added("cache.ttl", "60"),
                    ConfigChangeEvent.modified("db.port", "3306", "5432"),
                    ConfigChangeEvent.removed("old.key", "abc")
            );
            String result = ConfigDiff.format(changes);
            assertThat(result).contains("+ cache.ttl = 60");
            assertThat(result).contains("~ db.port: 3306 -> 5432");
            assertThat(result).contains("- old.key = abc");
        }

        @Test
        @DisplayName("空变更列表返回空字符串")
        void testFormatEmpty() {
            String result = ConfigDiff.format(List.of());
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("null before Config 抛出 NullPointerException")
        void testNullBeforeConfigThrowsNpe() {
            Config after = buildConfig(Map.of("a", "1"));
            assertThatNullPointerException().isThrownBy(() -> ConfigDiff.diff((Config) null, after));
        }

        @Test
        @DisplayName("null after Config 抛出 NullPointerException")
        void testNullAfterConfigThrowsNpe() {
            Config before = buildConfig(Map.of("a", "1"));
            assertThatNullPointerException().isThrownBy(() -> ConfigDiff.diff(before, (Config) null));
        }

        @Test
        @DisplayName("null before Map 抛出 NullPointerException")
        void testNullBeforeMapThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ConfigDiff.diff((Map<String, String>) null, Map.of()));
        }

        @Test
        @DisplayName("null after Map 抛出 NullPointerException")
        void testNullAfterMapThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ConfigDiff.diff(Map.of(), (Map<String, String>) null));
        }

        @Test
        @DisplayName("null changes 抛出 NullPointerException")
        void testNullChangesFormatThrowsNpe() {
            assertThatNullPointerException().isThrownBy(() -> ConfigDiff.format(null));
        }
    }
}

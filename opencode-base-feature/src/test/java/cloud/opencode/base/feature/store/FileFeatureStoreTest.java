package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * FileFeatureStore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FileFeatureStore 测试")
class FileFeatureStoreTest {

    @TempDir
    Path tempDir;

    private Path filePath;
    private FileFeatureStore store;

    @BeforeEach
    void setUp() {
        filePath = tempDir.resolve("features.properties");
        store = new FileFeatureStore(filePath);
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建FileFeatureStore")
        void testConstructor() {
            assertThat(store.getFilePath()).isEqualTo(filePath);
        }

        @Test
        @DisplayName("从已存在文件加载")
        void testLoadExistingFile() throws IOException {
            String content = """
                    feature.test.name=Test
                    feature.test.defaultEnabled=true
                    feature.test.strategy=always-on
                    """;
            Files.writeString(filePath, content);

            FileFeatureStore s = new FileFeatureStore(filePath);

            assertThat(s.exists("test")).isTrue();
        }
    }

    @Nested
    @DisplayName("save() 测试")
    class SaveTests {

        @Test
        @DisplayName("保存功能到文件")
        void testSave() {
            Feature feature = Feature.builder("test")
                    .name("Test Feature")
                    .alwaysOn()
                    .build();

            store.save(feature);

            assertThat(store.exists("test")).isTrue();
            assertThat(Files.exists(filePath)).isTrue();
        }

        @Test
        @DisplayName("null功能抛出异常")
        void testSaveNullFeature() {
            assertThatThrownBy(() -> store.save(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("保存不同策略")
        void testSaveWithDifferentStrategies() {
            store.save(Feature.builder("always-on").alwaysOn().build());
            store.save(Feature.builder("always-off").alwaysOff().build());
            store.save(Feature.builder("percentage").percentage(50).build());
            store.save(Feature.builder("user-list").forUsers("u1", "u2").build());
            store.save(Feature.builder("date-range")
                    .strategy(new DateRangeStrategy(Instant.now(), Instant.now().plusSeconds(3600)))
                    .build());

            assertThat(store.count()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("find() 测试")
    class FindTests {

        @Test
        @DisplayName("找到功能返回Optional.of")
        void testFindExisting() {
            store.save(Feature.builder("test").build());

            Optional<Feature> result = store.find("test");

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("未找到返回Optional.empty")
        void testFindNonExisting() {
            Optional<Feature> result = store.find("nonexistent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null key返回empty")
        void testFindNullKey() {
            Optional<Feature> result = store.find(null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll() 测试")
    class FindAllTests {

        @Test
        @DisplayName("返回所有功能")
        void testFindAll() {
            store.save(Feature.builder("f1").build());
            store.save(Feature.builder("f2").build());

            List<Feature> all = store.findAll();

            assertThat(all).hasSize(2);
        }
    }

    @Nested
    @DisplayName("delete() 测试")
    class DeleteTests {

        @Test
        @DisplayName("删除功能")
        void testDelete() {
            store.save(Feature.builder("test").build());

            boolean result = store.delete("test");

            assertThat(result).isTrue();
            assertThat(store.exists("test")).isFalse();
        }

        @Test
        @DisplayName("删除不存在的功能返回false")
        void testDeleteNonExisting() {
            boolean result = store.delete("nonexistent");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null key返回false")
        void testDeleteNullKey() {
            boolean result = store.delete(null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("clear() 测试")
    class ClearTests {

        @Test
        @DisplayName("清空所有功能")
        void testClear() {
            store.save(Feature.builder("f1").build());
            store.save(Feature.builder("f2").build());

            store.clear();

            assertThat(store.count()).isZero();
        }
    }

    @Nested
    @DisplayName("reload() 测试")
    class ReloadTests {

        @Test
        @DisplayName("从文件重新加载")
        void testReload() throws IOException {
            store.save(Feature.builder("original").build());

            // 直接修改文件
            String content = """
                    feature.new.name=New
                    feature.new.defaultEnabled=true
                    feature.new.strategy=default
                    """;
            Files.writeString(filePath, content);

            store.reload();

            assertThat(store.exists("original")).isFalse();
            assertThat(store.exists("new")).isTrue();
        }
    }

    @Nested
    @DisplayName("策略解析测试")
    class StrategyParsingTests {

        @Test
        @DisplayName("解析always-on策略")
        void testParseAlwaysOn() throws IOException {
            String content = """
                    feature.test.name=Test
                    feature.test.strategy=always-on
                    """;
            Files.writeString(filePath, content);

            FileFeatureStore s = new FileFeatureStore(filePath);
            Feature f = s.find("test").orElseThrow();

            assertThat(f.strategy()).isEqualTo(AlwaysOnStrategy.INSTANCE);
        }

        @Test
        @DisplayName("解析always-off策略")
        void testParseAlwaysOff() throws IOException {
            String content = """
                    feature.test.name=Test
                    feature.test.strategy=always-off
                    """;
            Files.writeString(filePath, content);

            FileFeatureStore s = new FileFeatureStore(filePath);
            Feature f = s.find("test").orElseThrow();

            assertThat(f.strategy()).isEqualTo(AlwaysOffStrategy.INSTANCE);
        }

        @Test
        @DisplayName("解析percentage策略")
        void testParsePercentage() throws IOException {
            String content = """
                    feature.test.name=Test
                    feature.test.strategy=percentage
                    feature.test.percentage=50
                    """;
            Files.writeString(filePath, content);

            FileFeatureStore s = new FileFeatureStore(filePath);
            Feature f = s.find("test").orElseThrow();

            assertThat(f.strategy()).isInstanceOf(PercentageStrategy.class);
            assertThat(((PercentageStrategy) f.strategy()).getPercentage()).isEqualTo(50);
        }

        @Test
        @DisplayName("解析user-list策略")
        void testParseUserList() throws IOException {
            String content = """
                    feature.test.name=Test
                    feature.test.strategy=user-list
                    feature.test.users=user1,user2,user3
                    """;
            Files.writeString(filePath, content);

            FileFeatureStore s = new FileFeatureStore(filePath);
            Feature f = s.find("test").orElseThrow();

            assertThat(f.strategy()).isInstanceOf(UserListStrategy.class);
        }

        @Test
        @DisplayName("解析date-range策略")
        void testParseDateRange() throws IOException {
            String content = """
                    feature.test.name=Test
                    feature.test.strategy=date-range
                    feature.test.startTime=2024-01-01T00:00:00Z
                    feature.test.endTime=2024-12-31T23:59:59Z
                    """;
            Files.writeString(filePath, content);

            FileFeatureStore s = new FileFeatureStore(filePath);
            Feature f = s.find("test").orElseThrow();

            assertThat(f.strategy()).isInstanceOf(DateRangeStrategy.class);
        }

        @Test
        @DisplayName("未知策略使用默认值")
        void testParseUnknownStrategy() throws IOException {
            String content = """
                    feature.test.name=Test
                    feature.test.strategy=unknown-strategy
                    feature.test.defaultEnabled=true
                    """;
            Files.writeString(filePath, content);

            FileFeatureStore s = new FileFeatureStore(filePath);
            Feature f = s.find("test").orElseThrow();

            assertThat(f.strategy()).isNull();
            assertThat(f.defaultEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("getFilePath() 测试")
    class GetFilePathTests {

        @Test
        @DisplayName("返回文件路径")
        void testGetFilePath() {
            assertThat(store.getFilePath()).isEqualTo(filePath);
        }
    }
}

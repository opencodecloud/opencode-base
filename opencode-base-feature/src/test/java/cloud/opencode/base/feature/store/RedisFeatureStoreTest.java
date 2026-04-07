package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * RedisFeatureStore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("RedisFeatureStore 测试")
class RedisFeatureStoreTest {

    private Map<String, Feature> mockRedis;
    private RedisFeatureStore store;

    @BeforeEach
    void setUp() {
        mockRedis = new HashMap<>();
        store = new RedisFeatureStore(
                "feature:",
                Duration.ofHours(1),
                (key, feature) -> mockRedis.put(key, feature),
                key -> mockRedis.get(key),
                () -> new ArrayList<>(mockRedis.values()),
                key -> mockRedis.remove(key) != null,
                () -> mockRedis.clear()
        );
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("设置键前缀")
        void testKeyPrefix() {
            assertThat(store.getKeyPrefix()).isEqualTo("feature:");
        }

        @Test
        @DisplayName("设置TTL")
        void testTtl() {
            assertThat(store.getTtl()).isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("null前缀使用默认值")
        void testNullPrefixUsesDefault() {
            RedisFeatureStore s = new RedisFeatureStore(
                    null, null,
                    (k, f) -> {}, k -> null, List::of, k -> false, () -> {}
            );

            assertThat(s.getKeyPrefix()).isEqualTo("feature:");
        }

        @Test
        @DisplayName("null TTL使用默认值")
        void testNullTtlUsesDefault() {
            RedisFeatureStore s = new RedisFeatureStore(
                    "prefix:", null,
                    (k, f) -> {}, k -> null, List::of, k -> false, () -> {}
            );

            assertThat(s.getTtl()).isEqualTo(Duration.ofHours(1));
        }
    }

    @Nested
    @DisplayName("save() 测试")
    class SaveTests {

        @Test
        @DisplayName("保存功能")
        void testSave() {
            Feature feature = Feature.builder("test").build();

            store.save(feature);

            assertThat(mockRedis).containsKey("feature:test");
        }

        @Test
        @DisplayName("null功能抛出异常")
        void testSaveNullFeature() {
            assertThatThrownBy(() -> store.save(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null key抛出异常")
        void testSaveNullKey() {
            assertThatThrownBy(() -> new Feature(null, null, null, false, null, null, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
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
            assertThat(mockRedis).doesNotContainKey("feature:test");
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

            assertThat(mockRedis).isEmpty();
        }
    }

    @Nested
    @DisplayName("getKeyPrefix() 测试")
    class GetKeyPrefixTests {

        @Test
        @DisplayName("返回键前缀")
        void testGetKeyPrefix() {
            assertThat(store.getKeyPrefix()).isEqualTo("feature:");
        }
    }

    @Nested
    @DisplayName("getTtl() 测试")
    class GetTtlTests {

        @Test
        @DisplayName("返回TTL")
        void testGetTtl() {
            assertThat(store.getTtl()).isEqualTo(Duration.ofHours(1));
        }
    }
}

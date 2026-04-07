package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LruFeatureStore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("LruFeatureStore 测试")
class LruFeatureStoreTest {

    private LruFeatureStore store;

    @BeforeEach
    void setUp() {
        store = new LruFeatureStore(3);
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("设置最大容量")
        void testMaxSize() {
            LruFeatureStore s = new LruFeatureStore(100);

            assertThat(s.getMaxSize()).isEqualTo(100);
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

            assertThat(store.exists("test")).isTrue();
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
    @DisplayName("LRU淘汰测试")
    class LruEvictionTests {

        @Test
        @DisplayName("超过容量淘汰最久未使用")
        void testEviction() {
            store.save(Feature.builder("f1").build());
            store.save(Feature.builder("f2").build());
            store.save(Feature.builder("f3").build());
            // f1 should be evicted after adding f4
            store.save(Feature.builder("f4").build());

            assertThat(store.count()).isEqualTo(3);
            assertThat(store.exists("f1")).isFalse();
            assertThat(store.exists("f4")).isTrue();
        }

        @Test
        @DisplayName("访问更新LRU顺序")
        void testAccessUpdatesLru() {
            store.save(Feature.builder("f1").build());
            store.save(Feature.builder("f2").build());
            store.save(Feature.builder("f3").build());

            // Access f1 to make it recently used
            store.find("f1");

            // Add f4, f2 should be evicted (oldest)
            store.save(Feature.builder("f4").build());

            assertThat(store.exists("f1")).isTrue();
            assertThat(store.exists("f2")).isFalse();
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
        @DisplayName("删除存在的功能返回true")
        void testDeleteExisting() {
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
    @DisplayName("exists() 测试")
    class ExistsTests {

        @Test
        @DisplayName("存在返回true")
        void testExistsTrue() {
            store.save(Feature.builder("test").build());

            assertThat(store.exists("test")).isTrue();
        }

        @Test
        @DisplayName("不存在返回false")
        void testExistsFalse() {
            assertThat(store.exists("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("null key返回false")
        void testExistsNullKey() {
            assertThat(store.exists(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("count() 测试")
    class CountTests {

        @Test
        @DisplayName("返回功能数量")
        void testCount() {
            store.save(Feature.builder("f1").build());
            store.save(Feature.builder("f2").build());

            assertThat(store.count()).isEqualTo(2);
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
    @DisplayName("getMaxSize() 测试")
    class GetMaxSizeTests {

        @Test
        @DisplayName("返回最大容量")
        void testGetMaxSize() {
            assertThat(store.getMaxSize()).isEqualTo(3);
        }
    }
}

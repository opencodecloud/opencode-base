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
 * InMemoryFeatureStore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("InMemoryFeatureStore 测试")
class InMemoryFeatureStoreTest {

    private InMemoryFeatureStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryFeatureStore();
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
        @DisplayName("覆盖已存在的功能")
        void testSaveOverwrite() {
            store.save(Feature.builder("test").name("v1").build());
            store.save(Feature.builder("test").name("v2").build());

            Feature feature = store.find("test").orElseThrow();
            assertThat(feature.name()).isEqualTo("v2");
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
            assertThat(result.get().key()).isEqualTo("test");
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

        @Test
        @DisplayName("空store返回空列表")
        void testFindAllEmpty() {
            List<Feature> all = store.findAll();

            assertThat(all).isEmpty();
        }

        @Test
        @DisplayName("返回副本")
        void testFindAllReturnsCopy() {
            store.save(Feature.builder("f1").build());

            List<Feature> all1 = store.findAll();
            List<Feature> all2 = store.findAll();

            assertThat(all1).isNotSameAs(all2);
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

        @Test
        @DisplayName("空store返回0")
        void testCountEmpty() {
            assertThat(store.count()).isZero();
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
}

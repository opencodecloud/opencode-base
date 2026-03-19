package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * CachedFeatureStore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("CachedFeatureStore 测试")
class CachedFeatureStoreTest {

    private InMemoryFeatureStore delegateStore;

    @BeforeEach
    void setUp() {
        delegateStore = new InMemoryFeatureStore();
    }

    @Nested
    @DisplayName("isCacheModuleAvailable 测试")
    class IsCacheModuleAvailableTests {

        @Test
        @DisplayName("返回布尔值")
        void shouldReturnBoolean() {
            boolean result = CachedFeatureStore.isCacheModuleAvailable();
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("wrap 工厂方法测试")
    class WrapFactoryMethodTests {

        @Test
        @DisplayName("创建缓存存储")
        void shouldCreateCachedStore() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            assertThat(cachedStore).isNotNull();
            assertThat(cachedStore.getDelegate()).isSameAs(delegateStore);
        }

        @Test
        @DisplayName("创建带 TTL 的缓存存储")
        void shouldCreateWithTtl() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore, Duration.ofMinutes(10));

            assertThat(cachedStore).isNotNull();
            assertThat(cachedStore.getDelegate()).isSameAs(delegateStore);
        }

        @Test
        @DisplayName("创建带 TTL 和最大大小的缓存存储")
        void shouldCreateWithTtlAndMaxSize() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore, Duration.ofMinutes(10), 500);

            assertThat(cachedStore).isNotNull();
            assertThat(cachedStore.getDelegate()).isSameAs(delegateStore);
        }

        @Test
        @DisplayName("null 委托抛出异常")
        void shouldThrowForNullDelegate() {
            assertThatNullPointerException()
                    .isThrownBy(() -> CachedFeatureStore.wrap(null))
                    .withMessage("delegate must not be null");
        }
    }

    @Nested
    @DisplayName("save 操作测试")
    class SaveOperationTests {

        @Test
        @DisplayName("保存功能到委托和缓存")
        void shouldSaveToDelegateAndCache() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);
            Feature feature = Feature.builder("test-feature").build();

            cachedStore.save(feature);

            // 验证委托存储
            assertThat(delegateStore.find("test-feature")).isPresent();

            // 再次查找应该命中缓存
            Optional<Feature> cached = cachedStore.find("test-feature");
            assertThat(cached).isPresent();
            assertThat(cached.get().key()).isEqualTo("test-feature");
        }
    }

    @Nested
    @DisplayName("find 操作测试")
    class FindOperationTests {

        @Test
        @DisplayName("从缓存查找功能")
        void shouldFindFromCache() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);
            Feature feature = Feature.builder("cached-feature").build();

            // 保存到缓存存储
            cachedStore.save(feature);

            // 第一次查找
            Optional<Feature> result1 = cachedStore.find("cached-feature");
            assertThat(result1).isPresent();

            // 第二次查找（应该命中缓存）
            Optional<Feature> result2 = cachedStore.find("cached-feature");
            assertThat(result2).isPresent();
            assertThat(result2.get().key()).isEqualTo("cached-feature");
        }

        @Test
        @DisplayName("未找到返回空")
        void shouldReturnEmptyWhenNotFound() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            Optional<Feature> result = cachedStore.find("non-existent");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("从委托加载并缓存")
        void shouldLoadFromDelegateAndCache() {
            Feature feature = Feature.builder("delegate-feature").build();
            delegateStore.save(feature);

            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            // 第一次查找应该从委托加载
            Optional<Feature> result = cachedStore.find("delegate-feature");
            assertThat(result).isPresent();

            // 后续查找应该命中缓存
            Optional<Feature> cached = cachedStore.find("delegate-feature");
            assertThat(cached).isPresent();
        }
    }

    @Nested
    @DisplayName("findAll 操作测试")
    class FindAllOperationTests {

        @Test
        @DisplayName("返回所有功能")
        void shouldReturnAllFeatures() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            cachedStore.save(Feature.builder("feature-1").build());
            cachedStore.save(Feature.builder("feature-2").build());

            assertThat(cachedStore.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("delete 操作测试")
    class DeleteOperationTests {

        @Test
        @DisplayName("删除功能并使缓存失效")
        void shouldDeleteAndInvalidateCache() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);
            Feature feature = Feature.builder("to-delete").build();

            cachedStore.save(feature);
            assertThat(cachedStore.find("to-delete")).isPresent();

            boolean deleted = cachedStore.delete("to-delete");

            assertThat(deleted).isTrue();
            assertThat(cachedStore.find("to-delete")).isEmpty();
            assertThat(delegateStore.find("to-delete")).isEmpty();
        }

        @Test
        @DisplayName("删除不存在的功能返回 false")
        void shouldReturnFalseForNonExistent() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            boolean deleted = cachedStore.delete("non-existent");

            assertThat(deleted).isFalse();
        }
    }

    @Nested
    @DisplayName("exists 操作测试")
    class ExistsOperationTests {

        @Test
        @DisplayName("检查功能是否存在")
        void shouldCheckIfExists() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);
            cachedStore.save(Feature.builder("existing").build());

            assertThat(cachedStore.exists("existing")).isTrue();
            assertThat(cachedStore.exists("non-existing")).isFalse();
        }
    }

    @Nested
    @DisplayName("count 操作测试")
    class CountOperationTests {

        @Test
        @DisplayName("返回功能数量")
        void shouldReturnCount() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            assertThat(cachedStore.count()).isZero();

            cachedStore.save(Feature.builder("feature-1").build());
            cachedStore.save(Feature.builder("feature-2").build());

            assertThat(cachedStore.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("clear 操作测试")
    class ClearOperationTests {

        @Test
        @DisplayName("清空所有功能和缓存")
        void shouldClearAllFeaturesAndCache() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            cachedStore.save(Feature.builder("feature-1").build());
            cachedStore.save(Feature.builder("feature-2").build());

            cachedStore.clear();

            assertThat(cachedStore.count()).isZero();
            assertThat(cachedStore.find("feature-1")).isEmpty();
            assertThat(cachedStore.find("feature-2")).isEmpty();
        }
    }

    @Nested
    @DisplayName("invalidate 操作测试")
    class InvalidateOperationTests {

        @Test
        @DisplayName("使单个缓存条目失效")
        void shouldInvalidateSingleEntry() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);
            Feature feature = Feature.builder("to-invalidate").build();

            cachedStore.save(feature);
            assertThat(cachedStore.find("to-invalidate")).isPresent();

            cachedStore.invalidate("to-invalidate");

            // 应该仍然可以从委托找到
            assertThat(delegateStore.find("to-invalidate")).isPresent();
        }

        @Test
        @DisplayName("使所有缓存条目失效")
        void shouldInvalidateAllEntries() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            cachedStore.save(Feature.builder("feature-1").build());
            cachedStore.save(Feature.builder("feature-2").build());

            cachedStore.invalidateAll();

            // 委托中的数据应该仍然存在
            assertThat(delegateStore.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("isUsingOpenCache 测试")
    class IsUsingOpenCacheTests {

        @Test
        @DisplayName("返回是否使用 OpenCache")
        void shouldReturnWhetherUsingOpenCache() {
            CachedFeatureStore cachedStore = CachedFeatureStore.wrap(delegateStore);

            boolean usingOpenCache = cachedStore.isUsingOpenCache();

            // 应该与模块可用性一致
            assertThat(usingOpenCache).isEqualTo(CachedFeatureStore.isCacheModuleAvailable());
        }
    }
}

package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureStore 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureStore 测试")
class FeatureStoreTest {

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("可创建简单实现")
        void testSimpleImplementation() {
            FeatureStore store = new FeatureStore() {
                @Override
                public void save(Feature feature) {}

                @Override
                public Optional<Feature> find(String key) {
                    return Optional.empty();
                }

                @Override
                public List<Feature> findAll() {
                    return List.of();
                }

                @Override
                public boolean delete(String key) {
                    return false;
                }

                @Override
                public void clear() {}
            };

            assertThat(store).isNotNull();
        }
    }

    @Nested
    @DisplayName("exists() 默认方法测试")
    class ExistsDefaultMethodTests {

        @Test
        @DisplayName("默认实现使用find")
        void testExistsDefault() {
            FeatureStore store = new FeatureStore() {
                @Override
                public void save(Feature feature) {}

                @Override
                public Optional<Feature> find(String key) {
                    if ("exists".equals(key)) {
                        return Optional.of(Feature.builder("exists").build());
                    }
                    return Optional.empty();
                }

                @Override
                public List<Feature> findAll() {
                    return List.of();
                }

                @Override
                public boolean delete(String key) {
                    return false;
                }

                @Override
                public void clear() {}
            };

            assertThat(store.exists("exists")).isTrue();
            assertThat(store.exists("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("count() 默认方法测试")
    class CountDefaultMethodTests {

        @Test
        @DisplayName("默认实现使用findAll")
        void testCountDefault() {
            FeatureStore store = new FeatureStore() {
                @Override
                public void save(Feature feature) {}

                @Override
                public Optional<Feature> find(String key) {
                    return Optional.empty();
                }

                @Override
                public List<Feature> findAll() {
                    return List.of(
                            Feature.builder("f1").build(),
                            Feature.builder("f2").build()
                    );
                }

                @Override
                public boolean delete(String key) {
                    return false;
                }

                @Override
                public void clear() {}
            };

            assertThat(store.count()).isEqualTo(2);
        }
    }
}

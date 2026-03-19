package cloud.opencode.base.cache.spi;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheWarmerTest Tests
 * CacheWarmerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheWarmer 接口测试")
class CacheWarmerTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda实现warmUp")
        void testLambdaWarmUp() {
            CacheWarmer<String, Integer> warmer = () -> Map.of("a", 1, "b", 2);

            Map<String, Integer> data = warmer.warmUp();

            assertThat(data).containsEntry("a", 1).containsEntry("b", 2);
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("priority默认返回0")
        void testDefaultPriority() {
            CacheWarmer<String, String> warmer = Map::of;
            assertThat(warmer.priority()).isEqualTo(0);
        }

        @Test
        @DisplayName("isEnabled默认返回true")
        void testDefaultIsEnabled() {
            CacheWarmer<String, String> warmer = Map::of;
            assertThat(warmer.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("onComplete默认不抛出异常")
        void testDefaultOnComplete() {
            CacheWarmer<String, String> warmer = Map::of;
            assertThatNoException().isThrownBy(() -> warmer.onComplete(10, Duration.ofSeconds(1)));
        }

        @Test
        @DisplayName("onError默认不抛出异常")
        void testDefaultOnError() {
            CacheWarmer<String, String> warmer = Map::of;
            assertThatNoException().isThrownBy(() -> warmer.onError(new RuntimeException("test")));
        }

        @Test
        @DisplayName("warmUpAsync返回异步结果")
        void testWarmUpAsync() {
            CacheWarmer<String, Integer> warmer = () -> Map.of("key", 42);

            Map<String, Integer> result = warmer.warmUpAsync(Runnable::run).join();

            assertThat(result).containsEntry("key", 42);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("from从Supplier创建预热器")
        void testFrom() {
            CacheWarmer<String, Integer> warmer = CacheWarmer.from(() -> Map.of("a", 1));

            assertThat(warmer.warmUp()).containsEntry("a", 1);
        }

        @Test
        @DisplayName("empty创建空预热器")
        void testEmpty() {
            CacheWarmer<String, String> warmer = CacheWarmer.empty();

            assertThat(warmer.warmUp()).isEmpty();
        }

        @Test
        @DisplayName("paged创建分页加载预热器")
        void testPaged() {
            CacheWarmer<Integer, String> warmer = CacheWarmer.paged(
                    offset -> {
                        if (offset >= 2) return Map.of();
                        return Map.of(offset, "v" + offset);
                    },
                    1, 5);

            Map<Integer, String> data = warmer.warmUp();

            assertThat(data).hasSize(2);
        }
    }
}

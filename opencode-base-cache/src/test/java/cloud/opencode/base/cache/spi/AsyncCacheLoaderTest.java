package cloud.opencode.base.cache.spi;

import org.junit.jupiter.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncCacheLoaderTest Tests
 * AsyncCacheLoaderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("AsyncCacheLoader 接口测试")
class AsyncCacheLoaderTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda表达式实现asyncLoad")
        void testLambdaAsyncLoad() {
            AsyncCacheLoader<String, String> loader = (key, executor) ->
                    CompletableFuture.completedFuture("value-" + key);

            String result = loader.asyncLoad("test", Runnable::run).join();

            assertThat(result).isEqualTo("value-test");
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("asyncReload默认调用asyncLoad")
        void testAsyncReloadDefault() {
            AsyncCacheLoader<String, String> loader = (key, executor) ->
                    CompletableFuture.completedFuture("new-" + key);

            String result = loader.asyncReload("key", "old", Runnable::run).join();

            assertThat(result).isEqualTo("new-key");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("from从同步加载器创建异步加载器")
        void testFromSyncLoader() {
            CacheLoader<String, String> syncLoader = key -> "sync-" + key;

            AsyncCacheLoader<String, String> asyncLoader = AsyncCacheLoader.from(syncLoader);
            String result = asyncLoader.asyncLoad("test", Runnable::run).join();

            assertThat(result).isEqualTo("sync-test");
        }
    }
}

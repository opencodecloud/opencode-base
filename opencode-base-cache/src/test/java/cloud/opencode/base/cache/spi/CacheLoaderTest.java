package cloud.opencode.base.cache.spi;

import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheLoaderTest Tests
 * CacheLoaderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheLoader 接口测试")
class CacheLoaderTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda表达式实现load")
        void testLambdaLoad() throws Exception {
            CacheLoader<String, String> loader = key -> "value-" + key;

            assertThat(loader.load("test")).isEqualTo("value-test");
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("loadAll默认逐个加载")
        void testLoadAllDefault() throws Exception {
            CacheLoader<String, Integer> loader = key -> key.length();

            Map<String, Integer> result = loader.loadAll(Set.of("a", "bb", "ccc"));

            assertThat(result).hasSize(3);
            assertThat(result.get("a")).isEqualTo(1);
            assertThat(result.get("bb")).isEqualTo(2);
            assertThat(result.get("ccc")).isEqualTo(3);
        }

        @Test
        @DisplayName("loadAll跳过null值")
        void testLoadAllSkipsNull() throws Exception {
            CacheLoader<String, String> loader = key -> key.equals("skip") ? null : "v-" + key;

            Map<String, String> result = loader.loadAll(Set.of("a", "skip"));

            assertThat(result).hasSize(1);
            assertThat(result).containsEntry("a", "v-a");
        }

        @Test
        @DisplayName("reload默认调用load")
        void testReloadDefault() throws Exception {
            CacheLoader<String, String> loader = key -> "value-" + key;

            String result = loader.reload("key", "oldValue");

            assertThat(result).isEqualTo("value-key");
        }
    }
}

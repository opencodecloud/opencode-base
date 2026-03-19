package cloud.opencode.base.yml.spi;

import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.YmlNode;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("YmlProvider 接口测试")
class YmlProviderTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getPriority默认返回100")
        void testDefaultPriority() {
            YmlProvider provider = new MinimalYmlProvider();

            assertThat(provider.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("isAvailable默认返回true")
        void testDefaultIsAvailable() {
            YmlProvider provider = new MinimalYmlProvider();

            assertThat(provider.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("自定义getName返回正确名称")
        void testGetName() {
            YmlProvider provider = new MinimalYmlProvider();

            assertThat(provider.getName()).isEqualTo("minimal-test");
        }

        @Test
        @DisplayName("覆盖getPriority返回自定义值")
        void testOverridePriority() {
            YmlProvider provider = new MinimalYmlProvider() {
                @Override
                public int getPriority() {
                    return 200;
                }
            };

            assertThat(provider.getPriority()).isEqualTo(200);
        }

        @Test
        @DisplayName("覆盖isAvailable返回false")
        void testOverrideIsAvailable() {
            YmlProvider provider = new MinimalYmlProvider() {
                @Override
                public boolean isAvailable() {
                    return false;
                }
            };

            assertThat(provider.isAvailable()).isFalse();
        }
    }

    /**
     * Minimal implementation of YmlProvider for testing
      *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
    private static class MinimalYmlProvider implements YmlProvider {
        @Override
        public String getName() {
            return "minimal-test";
        }

        @Override
        public Map<String, Object> load(String yaml) {
            return Map.of();
        }

        @Override
        public <T> T load(String yaml, Class<T> clazz) {
            return null;
        }

        @Override
        public Map<String, Object> load(InputStream input) {
            return Map.of();
        }

        @Override
        public <T> T load(InputStream input, Class<T> clazz) {
            return null;
        }

        @Override
        public List<Map<String, Object>> loadAll(String yaml) {
            return List.of();
        }

        @Override
        public <T> List<T> loadAll(String yaml, Class<T> clazz) {
            return List.of();
        }

        @Override
        public String dump(Object obj) {
            return "";
        }

        @Override
        public String dump(Object obj, YmlConfig config) {
            return "";
        }

        @Override
        public void dump(Object obj, OutputStream output) {
        }

        @Override
        public void dump(Object obj, Writer writer) {
        }

        @Override
        public String dumpAll(Iterable<?> documents) {
            return "";
        }

        @Override
        public YmlNode parseTree(String yaml) {
            return null;
        }

        @Override
        public YmlNode parseTree(InputStream input) {
            return null;
        }

        @Override
        public boolean isValid(String yaml) {
            return false;
        }

        @Override
        public YmlProvider configure(YmlConfig config) {
            return this;
        }

        @Override
        public YmlConfig getConfig() {
            return null;
        }
    }
}

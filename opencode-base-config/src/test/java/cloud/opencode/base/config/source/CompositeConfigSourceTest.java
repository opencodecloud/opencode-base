package cloud.opencode.base.config.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CompositeConfigSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("CompositeConfigSource 测试")
class CompositeConfigSourceTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("空源列表")
        void testEmptySourceList() {
            CompositeConfigSource source = new CompositeConfigSource(List.of());

            assertThat(source.getProperties()).isEmpty();
            assertThat(source.getSources()).isEmpty();
        }

        @Test
        @DisplayName("单个源")
        void testSingleSource() {
            InMemoryConfigSource inner = new InMemoryConfigSource(Map.of("key", "value"));
            CompositeConfigSource source = new CompositeConfigSource(List.of(inner));

            assertThat(source.getProperty("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("多个源 - 优先级合并")
        void testMultipleSourcesPriorityMerge() {
            // 低优先级
            InMemoryConfigSource low = new InMemoryConfigSource(Map.of(
                    "key1", "low1",
                    "key2", "low2",
                    "shared", "low"
            ));

            // 高优先级 - 通过自定义源实现
            ConfigSource high = new ConfigSource() {
                @Override
                public String getName() { return "high"; }
                @Override
                public Map<String, String> getProperties() {
                    return Map.of("shared", "high", "key3", "high3");
                }
                @Override
                public int getPriority() { return 100; }
            };

            CompositeConfigSource source = new CompositeConfigSource(List.of(low, high));

            // 高优先级覆盖低优先级
            assertThat(source.getProperty("shared")).isEqualTo("high");
            // 低优先级独有
            assertThat(source.getProperty("key1")).isEqualTo("low1");
            assertThat(source.getProperty("key2")).isEqualTo("low2");
            // 高优先级独有
            assertThat(source.getProperty("key3")).isEqualTo("high3");
        }
    }

    @Nested
    @DisplayName("ConfigSource接口测试")
    class ConfigSourceInterfaceTests {

        @Test
        @DisplayName("getName - 返回组合名称")
        void testGetName() {
            InMemoryConfigSource inner1 = new InMemoryConfigSource();
            InMemoryConfigSource inner2 = new InMemoryConfigSource();
            CompositeConfigSource source = new CompositeConfigSource(List.of(inner1, inner2));

            assertThat(source.getName()).contains("composite");
            assertThat(source.getName()).contains("2");
        }

        @Test
        @DisplayName("getPriority - 返回最高优先级")
        void testGetPriority() {
            ConfigSource low = new ConfigSource() {
                @Override
                public String getName() { return "low"; }
                @Override
                public Map<String, String> getProperties() { return Map.of(); }
                @Override
                public int getPriority() { return 10; }
            };

            ConfigSource high = new ConfigSource() {
                @Override
                public String getName() { return "high"; }
                @Override
                public Map<String, String> getProperties() { return Map.of(); }
                @Override
                public int getPriority() { return 100; }
            };

            CompositeConfigSource source = new CompositeConfigSource(List.of(low, high));

            assertThat(source.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("getProperties - 返回合并后的属性")
        void testGetProperties() {
            InMemoryConfigSource source1 = new InMemoryConfigSource(Map.of("key1", "value1"));
            InMemoryConfigSource source2 = new InMemoryConfigSource(Map.of("key2", "value2"));

            CompositeConfigSource composite = new CompositeConfigSource(List.of(source1, source2));

            Map<String, String> props = composite.getProperties();
            assertThat(props).containsEntry("key1", "value1");
            assertThat(props).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("supportsReload - 任一源支持则返回true")
        void testSupportsReload() {
            InMemoryConfigSource noReload = new InMemoryConfigSource();

            ConfigSource withReload = new ConfigSource() {
                @Override
                public String getName() { return "reload"; }
                @Override
                public Map<String, String> getProperties() { return Map.of(); }
                @Override
                public boolean supportsReload() { return true; }
            };

            CompositeConfigSource composite = new CompositeConfigSource(List.of(noReload, withReload));

            assertThat(composite.supportsReload()).isTrue();
        }

        @Test
        @DisplayName("supportsReload - 都不支持则返回false")
        void testSupportsReloadFalse() {
            InMemoryConfigSource source1 = new InMemoryConfigSource();
            InMemoryConfigSource source2 = new InMemoryConfigSource();

            CompositeConfigSource composite = new CompositeConfigSource(List.of(source1, source2));

            assertThat(composite.supportsReload()).isFalse();
        }
    }

    @Nested
    @DisplayName("getSources测试")
    class GetSourcesTests {

        @Test
        @DisplayName("getSources - 返回不可变列表")
        void testGetSourcesImmutable() {
            InMemoryConfigSource inner = new InMemoryConfigSource();
            CompositeConfigSource source = new CompositeConfigSource(List.of(inner));

            List<ConfigSource> sources = source.getSources();

            assertThatThrownBy(() -> sources.add(new InMemoryConfigSource()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getSources - 按优先级排序")
        void testGetSourcesSortedByPriority() {
            ConfigSource low = new ConfigSource() {
                @Override
                public String getName() { return "low"; }
                @Override
                public Map<String, String> getProperties() { return Map.of(); }
                @Override
                public int getPriority() { return 10; }
            };

            ConfigSource high = new ConfigSource() {
                @Override
                public String getName() { return "high"; }
                @Override
                public Map<String, String> getProperties() { return Map.of(); }
                @Override
                public int getPriority() { return 100; }
            };

            // 传入顺序: high, low
            CompositeConfigSource source = new CompositeConfigSource(List.of(high, low));

            // 排序后: low, high (优先级升序)
            List<ConfigSource> sources = source.getSources();
            assertThat(sources.get(0).getName()).isEqualTo("low");
            assertThat(sources.get(1).getName()).isEqualTo("high");
        }
    }

    @Nested
    @DisplayName("reload测试")
    class ReloadTests {

        @Test
        @DisplayName("reload - 重新合并属性")
        void testReload() {
            InMemoryConfigSource mutable = new InMemoryConfigSource(Map.of("key", "original"));

            ConfigSource reloadable = new ConfigSource() {
                private String value = "original";
                @Override
                public String getName() { return "reloadable"; }
                @Override
                public Map<String, String> getProperties() { return Map.of("key", value); }
                @Override
                public boolean supportsReload() { return true; }
                @Override
                public void reload() { this.value = "reloaded"; }
                @Override
                public int getPriority() { return 100; }
            };

            CompositeConfigSource composite = new CompositeConfigSource(List.of(mutable, reloadable));

            assertThat(composite.getProperty("key")).isEqualTo("original");

            composite.reload();

            assertThat(composite.getProperty("key")).isEqualTo("reloaded");
        }
    }
}

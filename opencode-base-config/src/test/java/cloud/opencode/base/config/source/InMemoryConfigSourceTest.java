package cloud.opencode.base.config.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * InMemoryConfigSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("InMemoryConfigSource 测试")
class InMemoryConfigSourceTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法 - 创建空源")
        void testDefaultConstructor() {
            InMemoryConfigSource source = new InMemoryConfigSource();

            assertThat(source.getName()).isEqualTo("in-memory");
            assertThat(source.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("带属性的构造方法")
        void testPropertiesConstructor() {
            Map<String, String> props = Map.of("key1", "value1", "key2", "value2");
            InMemoryConfigSource source = new InMemoryConfigSource(props);

            assertThat(source.getName()).isEqualTo("in-memory");
            assertThat(source.getProperties()).containsEntry("key1", "value1");
            assertThat(source.getProperties()).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("命名构造方法")
        void testNamedConstructor() {
            Map<String, String> props = Map.of("key", "value");
            InMemoryConfigSource source = new InMemoryConfigSource("custom-name", props);

            assertThat(source.getName()).isEqualTo("custom-name");
            assertThat(source.getProperties()).containsEntry("key", "value");
        }
    }

    @Nested
    @DisplayName("ConfigSource接口测试")
    class ConfigSourceInterfaceTests {

        @Test
        @DisplayName("getName - 返回源名称")
        void testGetName() {
            InMemoryConfigSource source = new InMemoryConfigSource();
            assertThat(source.getName()).isEqualTo("in-memory");
        }

        @Test
        @DisplayName("getProperties - 返回不可变副本")
        void testGetPropertiesImmutable() {
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key", "value"));

            Map<String, String> props = source.getProperties();

            // 返回的是不可变副本
            assertThatThrownBy(() -> props.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getProperty - 获取单个属性")
        void testGetProperty() {
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key", "value"));

            assertThat(source.getProperty("key")).isEqualTo("value");
            assertThat(source.getProperty("nonexistent")).isNull();
        }

        @Test
        @DisplayName("getPriority - 返回优先级10")
        void testGetPriority() {
            InMemoryConfigSource source = new InMemoryConfigSource();
            assertThat(source.getPriority()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("可变操作测试")
    class MutableOperationsTests {

        @Test
        @DisplayName("setProperty - 设置属性")
        void testSetProperty() {
            InMemoryConfigSource source = new InMemoryConfigSource();
            source.setProperty("key", "value");

            assertThat(source.getProperty("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("setProperty - 覆盖属性")
        void testSetPropertyOverwrite() {
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key", "old"));
            source.setProperty("key", "new");

            assertThat(source.getProperty("key")).isEqualTo("new");
        }

        @Test
        @DisplayName("removeProperty - 移除属性")
        void testRemoveProperty() {
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key", "value"));
            String removed = source.removeProperty("key");

            assertThat(removed).isEqualTo("value");
            assertThat(source.getProperty("key")).isNull();
        }

        @Test
        @DisplayName("removeProperty - 移除不存在的属性")
        void testRemoveNonexistentProperty() {
            InMemoryConfigSource source = new InMemoryConfigSource();
            String removed = source.removeProperty("nonexistent");

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("setProperties - 批量设置属性")
        void testSetProperties() {
            InMemoryConfigSource source = new InMemoryConfigSource();
            source.setProperties(Map.of("key1", "value1", "key2", "value2"));

            assertThat(source.getProperty("key1")).isEqualTo("value1");
            assertThat(source.getProperty("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("clear - 清除所有属性")
        void testClear() {
            InMemoryConfigSource source = new InMemoryConfigSource(Map.of("key1", "value1", "key2", "value2"));
            source.clear();

            assertThat(source.getProperties()).isEmpty();
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("并发读写")
        void testConcurrentReadWrite() throws InterruptedException {
            InMemoryConfigSource source = new InMemoryConfigSource();

            Thread writer = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    source.setProperty("key" + i, "value" + i);
                }
            });

            Thread reader = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    source.getProperties();
                }
            });

            writer.start();
            reader.start();
            writer.join();
            reader.join();

            // 验证写入成功
            assertThat(source.getProperty("key99")).isEqualTo("value99");
        }
    }
}

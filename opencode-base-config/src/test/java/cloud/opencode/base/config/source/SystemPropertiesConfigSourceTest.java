package cloud.opencode.base.config.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SystemPropertiesConfigSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("SystemPropertiesConfigSource 测试")
class SystemPropertiesConfigSourceTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("加载系统属性")
        void testLoadSystemProperties() {
            SystemPropertiesConfigSource source = new SystemPropertiesConfigSource();

            Map<String, String> props = source.getProperties();

            // 验证包含常见系统属性
            assertThat(props).containsKey("java.version");
            assertThat(props).containsKey("java.home");
            assertThat(props).containsKey("user.dir");
        }

        @Test
        @DisplayName("属性值正确")
        void testPropertyValues() {
            SystemPropertiesConfigSource source = new SystemPropertiesConfigSource();

            assertThat(source.getProperty("java.version"))
                    .isEqualTo(System.getProperty("java.version"));
            assertThat(source.getProperty("user.dir"))
                    .isEqualTo(System.getProperty("user.dir"));
        }
    }

    @Nested
    @DisplayName("ConfigSource接口测试")
    class ConfigSourceInterfaceTests {

        @Test
        @DisplayName("getName - 返回system-properties")
        void testGetName() {
            SystemPropertiesConfigSource source = new SystemPropertiesConfigSource();
            assertThat(source.getName()).isEqualTo("system-properties");
        }

        @Test
        @DisplayName("getPriority - 返回50")
        void testGetPriority() {
            SystemPropertiesConfigSource source = new SystemPropertiesConfigSource();
            assertThat(source.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("getProperties - 返回不可变映射")
        void testGetPropertiesImmutable() {
            SystemPropertiesConfigSource source = new SystemPropertiesConfigSource();

            Map<String, String> props = source.getProperties();

            assertThatThrownBy(() -> props.put("new.key", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getProperty - 获取单个属性")
        void testGetProperty() {
            SystemPropertiesConfigSource source = new SystemPropertiesConfigSource();

            assertThat(source.getProperty("java.version")).isNotNull();
            assertThat(source.getProperty("nonexistent.property.xyz")).isNull();
        }
    }

    @Nested
    @DisplayName("快照测试")
    class SnapshotTests {

        @Test
        @DisplayName("属性是创建时的快照")
        void testPropertiesSnapshot() {
            String testKey = "test.system.property.snapshot." + System.currentTimeMillis();

            // 创建源之前设置属性
            System.setProperty(testKey, "before");
            SystemPropertiesConfigSource source = new SystemPropertiesConfigSource();

            // 创建源之后修改属性
            System.setProperty(testKey, "after");

            // 源应该返回创建时的值
            assertThat(source.getProperty(testKey)).isEqualTo("before");

            // 清理
            System.clearProperty(testKey);
        }
    }
}

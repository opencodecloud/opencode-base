package cloud.opencode.base.config.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * EnvironmentConfigSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("EnvironmentConfigSource 测试")
class EnvironmentConfigSourceTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法 - 加载所有环境变量")
        void testDefaultConstructor() {
            EnvironmentConfigSource source = new EnvironmentConfigSource();

            // 验证包含常见环境变量（转换后的键名）
            Map<String, String> props = source.getProperties();
            assertThat(props).isNotEmpty();
            // PATH 变量通常存在
            assertThat(props.containsKey("path") || props.containsKey("PATH".toLowerCase().replace("_", ".")))
                    .isTrue();
        }

        @Test
        @DisplayName("带前缀构造方法")
        void testPrefixConstructor() {
            // 使用不太可能存在的前缀
            EnvironmentConfigSource source = new EnvironmentConfigSource("UNLIKELY_PREFIX_XYZ_");

            // 应该是空或只包含匹配的变量
            Map<String, String> props = source.getProperties();
            // 所有键应该是转换后的（不包含原始前缀）
            props.keySet().forEach(key ->
                    assertThat(key).doesNotContain("UNLIKELY_PREFIX_XYZ_"));
        }
    }

    @Nested
    @DisplayName("ConfigSource接口测试")
    class ConfigSourceInterfaceTests {

        @Test
        @DisplayName("getName - 无前缀")
        void testGetNameNoPrefix() {
            EnvironmentConfigSource source = new EnvironmentConfigSource();
            assertThat(source.getName()).isEqualTo("environment");
        }

        @Test
        @DisplayName("getName - 带前缀")
        void testGetNameWithPrefix() {
            EnvironmentConfigSource source = new EnvironmentConfigSource("APP_");
            assertThat(source.getName()).isEqualTo("environment[APP_]");
        }

        @Test
        @DisplayName("getPriority - 返回100")
        void testGetPriority() {
            EnvironmentConfigSource source = new EnvironmentConfigSource();
            assertThat(source.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("getProperties - 返回不可变映射")
        void testGetPropertiesImmutable() {
            EnvironmentConfigSource source = new EnvironmentConfigSource();

            Map<String, String> props = source.getProperties();

            assertThatThrownBy(() -> props.put("new.key", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("键名转换测试")
    class KeyConversionTests {

        @Test
        @DisplayName("键名转换 - 大写下划线转小写点号")
        void testKeyConversion() {
            // 通过检查getProperties的键格式来验证转换逻辑
            EnvironmentConfigSource source = new EnvironmentConfigSource();
            Map<String, String> props = source.getProperties();

            // 所有键应该是小写且使用点分隔
            props.keySet().forEach(key -> {
                // 键应该是小写
                assertThat(key).isEqualTo(key.toLowerCase());
            });
        }
    }
}

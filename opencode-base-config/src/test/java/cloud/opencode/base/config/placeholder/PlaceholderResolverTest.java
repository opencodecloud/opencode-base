package cloud.opencode.base.config.placeholder;

import cloud.opencode.base.config.OpenConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * PlaceholderResolver 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("PlaceholderResolver 测试")
class PlaceholderResolverTest {

    private Map<String, String> properties;
    private PlaceholderResolver resolver;

    @BeforeEach
    void setUp() {
        properties = new HashMap<>();
        properties.put("app.name", "MyApp");
        properties.put("app.version", "1.0.0");
        properties.put("base.url", "http://localhost");
        properties.put("api.url", "${base.url}/api");
        properties.put("server.port", "8080");
        resolver = new PlaceholderResolver(properties::get);
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认递归深度")
        void testDefaultRecursionDepth() {
            PlaceholderResolver resolver = new PlaceholderResolver(properties::get);
            assertThat(resolver).isNotNull();
        }

        @Test
        @DisplayName("自定义递归深度")
        void testCustomRecursionDepth() {
            PlaceholderResolver resolver = new PlaceholderResolver(properties::get, 5);
            assertThat(resolver).isNotNull();
        }
    }

    @Nested
    @DisplayName("基本占位符解析测试")
    class BasicResolutionTests {

        @Test
        @DisplayName("无占位符 - 返回原值")
        void testNoPlaceholder() {
            assertThat(resolver.resolve("simple value")).isEqualTo("simple value");
        }

        @Test
        @DisplayName("null值 - 返回null")
        void testNullValue() {
            assertThat(resolver.resolve(null)).isNull();
        }

        @Test
        @DisplayName("简单占位符")
        void testSimplePlaceholder() {
            assertThat(resolver.resolve("${app.name}")).isEqualTo("MyApp");
        }

        @Test
        @DisplayName("嵌入式占位符")
        void testEmbeddedPlaceholder() {
            assertThat(resolver.resolve("App: ${app.name} v${app.version}"))
                    .isEqualTo("App: MyApp v1.0.0");
        }

        @Test
        @DisplayName("多个占位符")
        void testMultiplePlaceholders() {
            assertThat(resolver.resolve("${app.name}-${app.version}"))
                    .isEqualTo("MyApp-1.0.0");
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("使用默认值 - 键不存在")
        void testDefaultValueUsed() {
            assertThat(resolver.resolve("${missing.key:default}")).isEqualTo("default");
        }

        @Test
        @DisplayName("不使用默认值 - 键存在")
        void testDefaultValueNotUsed() {
            assertThat(resolver.resolve("${app.name:default}")).isEqualTo("MyApp");
        }

        @Test
        @DisplayName("空默认值")
        void testEmptyDefaultValue() {
            assertThat(resolver.resolve("${missing.key:}")).isEqualTo("");
        }

        @Test
        @DisplayName("默认值包含特殊字符")
        void testDefaultValueWithSpecialChars() {
            assertThat(resolver.resolve("${missing:http://localhost:8080}"))
                    .isEqualTo("http://localhost:8080");
        }
    }

    @Nested
    @DisplayName("嵌套占位符测试")
    class NestedPlaceholderTests {

        @Test
        @DisplayName("值中包含占位符 - 递归解析")
        void testNestedPlaceholderInValue() {
            // api.url = ${base.url}/api
            assertThat(resolver.resolve("${api.url}")).isEqualTo("http://localhost/api");
        }

        @Test
        @DisplayName("多层嵌套")
        void testMultiLevelNested() {
            properties.put("level1", "${level2}");
            properties.put("level2", "${level3}");
            properties.put("level3", "final");

            assertThat(resolver.resolve("${level1}")).isEqualTo("final");
        }

        @Test
        @DisplayName("递归过深 - 抛出异常")
        void testRecursionTooDeep() {
            properties.put("self", "${self}"); // 自引用

            assertThatThrownBy(() -> resolver.resolve("${self}"))
                    .isInstanceOf(OpenConfigException.class)
                    .hasMessageContaining("recursion too deep");
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("占位符无法解析 - 抛出异常")
        void testUnresolvablePlaceholder() {
            assertThatThrownBy(() -> resolver.resolve("${nonexistent.key}"))
                    .isInstanceOf(OpenConfigException.class)
                    .hasMessageContaining("nonexistent.key");
        }

        @Test
        @DisplayName("部分占位符无法解析")
        void testPartialUnresolvable() {
            assertThatThrownBy(() -> resolver.resolve("${app.name} and ${missing}"))
                    .isInstanceOf(OpenConfigException.class)
                    .hasMessageContaining("missing");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空字符串")
        void testEmptyString() {
            assertThat(resolver.resolve("")).isEqualTo("");
        }

        @Test
        @DisplayName("只有$符号")
        void testDollarSignOnly() {
            assertThat(resolver.resolve("$")).isEqualTo("$");
        }

        @Test
        @DisplayName("不完整的占位符")
        void testIncompletePlaceholder() {
            assertThat(resolver.resolve("${")).isEqualTo("${");
            assertThat(resolver.resolve("${key")).isEqualTo("${key");
        }

        @Test
        @DisplayName("空占位符键 - 抛出异常")
        void testEmptyPlaceholderKeyWithDefault() {
            // 空占位符键不被支持,会抛出异常
            assertThatThrownBy(() -> resolver.resolve("${:default}"))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("自定义递归深度测试")
    class CustomRecursionDepthTests {

        @Test
        @DisplayName("递归深度为2")
        void testRecursionDepth2() {
            properties.put("level1", "${level2}");
            properties.put("level2", "value");

            PlaceholderResolver shallowResolver = new PlaceholderResolver(properties::get, 2);

            // 深度2可以解析一层嵌套
            assertThat(shallowResolver.resolve("${level1}")).isEqualTo("value");
        }

        @Test
        @DisplayName("递归深度超限")
        void testRecursionDepthExceeded() {
            properties.put("a", "${b}");
            properties.put("b", "${c}");
            properties.put("c", "value");

            PlaceholderResolver shallowResolver = new PlaceholderResolver(properties::get, 1);

            // 深度1无法解析两层嵌套
            assertThatThrownBy(() -> shallowResolver.resolve("${a}"))
                    .isInstanceOf(OpenConfigException.class);
        }
    }
}

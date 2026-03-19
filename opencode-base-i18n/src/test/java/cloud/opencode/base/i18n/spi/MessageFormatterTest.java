package cloud.opencode.base.i18n.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MessageFormatter 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("MessageFormatter 接口测试")
class MessageFormatterTest {

    @Nested
    @DisplayName("format方法（位置参数）测试")
    class FormatWithArgsTests {

        @Test
        @DisplayName("format(String, Locale, Object...)不是默认方法")
        void testFormatWithArgsNotDefault() throws NoSuchMethodException {
            var method = MessageFormatter.class.getMethod("format", String.class, Locale.class, Object[].class);

            assertThat(method.isDefault()).isFalse();
        }

        @Test
        @DisplayName("实现format方法")
        void testFormatImplementation() {
            MessageFormatter formatter = new MessageFormatter() {
                @Override
                public String format(String template, Locale locale, Object... args) {
                    return template + " [" + String.join(",", java.util.Arrays.stream(args)
                            .map(Object::toString)
                            .toArray(String[]::new)) + "]";
                }

                @Override
                public String format(String template, Locale locale, Map<String, Object> params) {
                    return template;
                }
            };

            String result = formatter.format("Hello", Locale.ENGLISH, "World", "!");

            assertThat(result).isEqualTo("Hello [World,!]");
        }
    }

    @Nested
    @DisplayName("format方法（命名参数）测试")
    class FormatWithMapTests {

        @Test
        @DisplayName("format(String, Locale, Map)不是默认方法")
        void testFormatWithMapNotDefault() throws NoSuchMethodException {
            var method = MessageFormatter.class.getMethod("format", String.class, Locale.class, Map.class);

            assertThat(method.isDefault()).isFalse();
        }

        @Test
        @DisplayName("实现format方法")
        void testFormatWithMapImplementation() {
            MessageFormatter formatter = new MessageFormatter() {
                @Override
                public String format(String template, Locale locale, Object... args) {
                    return template;
                }

                @Override
                public String format(String template, Locale locale, Map<String, Object> params) {
                    StringBuilder result = new StringBuilder(template);
                    for (var entry : params.entrySet()) {
                        result.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
                    }
                    return result.toString();
                }
            };

            String result = formatter.format("Template:", Locale.ENGLISH, Map.of("name", "Alice"));

            assertThat(result).contains("Template:");
            assertThat(result).contains("name=Alice");
        }
    }

    @Nested
    @DisplayName("clearCache默认方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("clearCache是默认方法")
        void testClearCacheIsDefault() throws NoSuchMethodException {
            var method = MessageFormatter.class.getMethod("clearCache");

            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("默认实现不做任何操作")
        void testClearCacheNoOp() {
            MessageFormatter formatter = new MessageFormatter() {
                @Override
                public String format(String template, Locale locale, Object... args) {
                    return template;
                }

                @Override
                public String format(String template, Locale locale, Map<String, Object> params) {
                    return template;
                }
            };

            assertThatCode(() -> formatter.clearCache()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("可以覆盖clearCache方法")
        void testClearCacheOverride() {
            var clearCalled = new boolean[]{false};
            MessageFormatter formatter = new MessageFormatter() {
                @Override
                public String format(String template, Locale locale, Object... args) {
                    return template;
                }

                @Override
                public String format(String template, Locale locale, Map<String, Object> params) {
                    return template;
                }

                @Override
                public void clearCache() {
                    clearCalled[0] = true;
                }
            };

            formatter.clearCache();

            assertThat(clearCalled[0]).isTrue();
        }
    }

    @Nested
    @DisplayName("完整实现测试")
    class FullImplementationTests {

        @Test
        @DisplayName("完整实现MessageFormatter")
        void testFullImplementation() {
            MessageFormatter formatter = new MessageFormatter() {
                private int formatCallCount = 0;

                @Override
                public String format(String template, Locale locale, Object... args) {
                    formatCallCount++;
                    if (args == null || args.length == 0) {
                        return template;
                    }
                    String result = template;
                    for (int i = 0; i < args.length; i++) {
                        result = result.replace("{" + i + "}", String.valueOf(args[i]));
                    }
                    return result;
                }

                @Override
                public String format(String template, Locale locale, Map<String, Object> params) {
                    formatCallCount++;
                    if (params == null || params.isEmpty()) {
                        return template;
                    }
                    String result = template;
                    for (var entry : params.entrySet()) {
                        result = result.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                    }
                    return result;
                }

                @Override
                public void clearCache() {
                    formatCallCount = 0;
                }
            };

            // Test positional parameters
            assertThat(formatter.format("Hello {0}!", Locale.ENGLISH, "World"))
                    .isEqualTo("Hello World!");

            // Test named parameters
            assertThat(formatter.format("Hello ${name}!", Locale.ENGLISH, Map.of("name", "Alice")))
                    .isEqualTo("Hello Alice!");

            // Test clear cache
            assertThatCode(() -> formatter.clearCache()).doesNotThrowAnyException();
        }
    }
}

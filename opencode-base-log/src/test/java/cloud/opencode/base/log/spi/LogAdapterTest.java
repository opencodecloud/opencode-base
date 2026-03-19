package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.marker.Marker;
import cloud.opencode.base.log.marker.Markers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LogAdapter 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LogAdapter 接口测试")
class LogAdapterTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("LogAdapter是接口")
        void testIsInterface() {
            assertThat(LogAdapter.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("定义了convertMarker方法")
        void testConvertMarkerMethod() throws NoSuchMethodException {
            assertThat(LogAdapter.class.getMethod("convertMarker", Marker.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了formatMessage方法")
        void testFormatMessageMethod() throws NoSuchMethodException {
            assertThat(LogAdapter.class.getMethod("formatMessage", String.class, Object[].class)).isNotNull();
        }

        @Test
        @DisplayName("定义了supportsMarkers默认方法")
        void testSupportsMarkersMethod() throws NoSuchMethodException {
            var method = LogAdapter.class.getMethod("supportsMarkers");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("定义了supportsMDC默认方法")
        void testSupportsMDCMethod() throws NoSuchMethodException {
            var method = LogAdapter.class.getMethod("supportsMDC");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }

        @Test
        @DisplayName("定义了supportsNDC默认方法")
        void testSupportsNDCMethod() throws NoSuchMethodException {
            var method = LogAdapter.class.getMethod("supportsNDC");
            assertThat(method).isNotNull();
            assertThat(method.isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        private LogAdapter createAdapter() {
            return new LogAdapter() {
                @Override
                public Object convertMarker(Marker marker) {
                    return marker;
                }

                @Override
                public String formatMessage(String format, Object... args) {
                    if (args == null || args.length == 0) return format;
                    StringBuilder sb = new StringBuilder();
                    int argIndex = 0;
                    int i = 0;
                    while (i < format.length()) {
                        if (i < format.length() - 1 && format.charAt(i) == '{' && format.charAt(i + 1) == '}') {
                            if (argIndex < args.length) {
                                sb.append(args[argIndex++]);
                            } else {
                                sb.append("{}");
                            }
                            i += 2;
                        } else {
                            sb.append(format.charAt(i));
                            i++;
                        }
                    }
                    return sb.toString();
                }
            };
        }

        @Test
        @DisplayName("supportsMarkers默认返回true")
        void testSupportsMarkersDefault() {
            LogAdapter adapter = createAdapter();
            assertThat(adapter.supportsMarkers()).isTrue();
        }

        @Test
        @DisplayName("supportsMDC默认返回true")
        void testSupportsMDCDefault() {
            LogAdapter adapter = createAdapter();
            assertThat(adapter.supportsMDC()).isTrue();
        }

        @Test
        @DisplayName("supportsNDC默认返回false")
        void testSupportsNDCDefault() {
            LogAdapter adapter = createAdapter();
            assertThat(adapter.supportsNDC()).isFalse();
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        @Test
        @DisplayName("convertMarker转换标记")
        void testConvertMarker() {
            LogAdapter adapter = new LogAdapter() {
                @Override
                public Object convertMarker(Marker marker) {
                    return "Converted:" + marker.getName();
                }

                @Override
                public String formatMessage(String format, Object... args) {
                    return format;
                }
            };

            Marker marker = Markers.getMarker("TEST");
            Object result = adapter.convertMarker(marker);

            assertThat(result).isEqualTo("Converted:TEST");
        }

        @Test
        @DisplayName("formatMessage格式化消息")
        void testFormatMessage() {
            LogAdapter adapter = new LogAdapter() {
                @Override
                public Object convertMarker(Marker marker) {
                    return marker;
                }

                @Override
                public String formatMessage(String format, Object... args) {
                    return String.format(format.replace("{}", "%s"), args);
                }
            };

            String result = adapter.formatMessage("Hello {}, {}!", "World", 123);
            assertThat(result).isEqualTo("Hello World, 123!");
        }

        @Test
        @DisplayName("formatMessage无参数")
        void testFormatMessageNoArgs() {
            LogAdapter adapter = new LogAdapter() {
                @Override
                public Object convertMarker(Marker marker) {
                    return marker;
                }

                @Override
                public String formatMessage(String format, Object... args) {
                    return format;
                }
            };

            String result = adapter.formatMessage("Simple message");
            assertThat(result).isEqualTo("Simple message");
        }
    }

    @Nested
    @DisplayName("覆盖默认方法测试")
    class OverrideDefaultMethodTests {

        @Test
        @DisplayName("覆盖supportsMarkers返回false")
        void testOverrideSupportsMarkers() {
            LogAdapter adapter = new LogAdapter() {
                @Override
                public Object convertMarker(Marker marker) {
                    return null;
                }

                @Override
                public String formatMessage(String format, Object... args) {
                    return format;
                }

                @Override
                public boolean supportsMarkers() {
                    return false;
                }
            };

            assertThat(adapter.supportsMarkers()).isFalse();
        }

        @Test
        @DisplayName("覆盖supportsNDC返回true")
        void testOverrideSupportsNDC() {
            LogAdapter adapter = new LogAdapter() {
                @Override
                public Object convertMarker(Marker marker) {
                    return null;
                }

                @Override
                public String formatMessage(String format, Object... args) {
                    return format;
                }

                @Override
                public boolean supportsNDC() {
                    return true;
                }
            };

            assertThat(adapter.supportsNDC()).isTrue();
        }
    }
}

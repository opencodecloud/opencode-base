package cloud.opencode.base.string.template;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateFilterTest Tests
 * TemplateFilterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("TemplateFilter 接口测试")
class TemplateFilterTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda实现过滤器")
        void testLambda() {
            TemplateFilter filter = (value, args) -> value.toUpperCase();
            assertThat(filter.apply("hello", new String[]{})).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("使用参数的过滤器")
        void testWithArgs() {
            TemplateFilter filter = (value, args) -> {
                if (args.length > 0 && "truncate".equals(args[0])) {
                    int len = Integer.parseInt(args[1]);
                    return value.length() > len ? value.substring(0, len) + "..." : value;
                }
                return value;
            };
            assertThat(filter.apply("hello world", new String[]{"truncate", "5"}))
                    .isEqualTo("hello...");
        }

        @Test
        @DisplayName("空参数数组")
        void testEmptyArgs() {
            TemplateFilter filter = (value, args) -> args.length == 0 ? value : value + args[0];
            assertThat(filter.apply("test", new String[]{})).isEqualTo("test");
        }

        @Test
        @DisplayName("方法引用实现")
        void testMethodReference() {
            TemplateFilter filter = TemplateFilterTest::trimFilter;
            assertThat(filter.apply("  hello  ", new String[]{})).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("@FunctionalInterface验证")
    class AnnotationTests {

        @Test
        @DisplayName("标注为@FunctionalInterface")
        void testIsFunctionalInterface() {
            assertThat(TemplateFilter.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("是接口")
        void testIsInterface() {
            assertThat(TemplateFilter.class.isInterface()).isTrue();
        }
    }

    static String trimFilter(String value, String[] args) {
        return value.trim();
    }
}

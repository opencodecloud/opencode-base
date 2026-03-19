package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenStringBase 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenStringBase 测试")
class OpenStringBaseTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("EMPTY 常量")
        void testEmptyConstant() {
            assertThat(OpenStringBase.EMPTY).isEmpty();
        }

        @Test
        @DisplayName("SPACE 常量")
        void testSpaceConstant() {
            assertThat(OpenStringBase.SPACE).isEqualTo(" ");
        }
    }

    @Nested
    @DisplayName("空值判断测试")
    class EmptyCheckTests {

        @Test
        @DisplayName("isEmpty")
        void testIsEmpty() {
            assertThat(OpenStringBase.isEmpty(null)).isTrue();
            assertThat(OpenStringBase.isEmpty("")).isTrue();
            assertThat(OpenStringBase.isEmpty(" ")).isFalse();
            assertThat(OpenStringBase.isEmpty("hello")).isFalse();
        }

        @Test
        @DisplayName("isNotEmpty")
        void testIsNotEmpty() {
            assertThat(OpenStringBase.isNotEmpty(null)).isFalse();
            assertThat(OpenStringBase.isNotEmpty("")).isFalse();
            assertThat(OpenStringBase.isNotEmpty(" ")).isTrue();
            assertThat(OpenStringBase.isNotEmpty("hello")).isTrue();
        }

        @Test
        @DisplayName("isBlank")
        void testIsBlank() {
            assertThat(OpenStringBase.isBlank(null)).isTrue();
            assertThat(OpenStringBase.isBlank("")).isTrue();
            assertThat(OpenStringBase.isBlank(" ")).isTrue();
            assertThat(OpenStringBase.isBlank("  \t\n")).isTrue();
            assertThat(OpenStringBase.isBlank("hello")).isFalse();
            assertThat(OpenStringBase.isBlank(" hello ")).isFalse();
        }

        @Test
        @DisplayName("isNotBlank")
        void testIsNotBlank() {
            assertThat(OpenStringBase.isNotBlank(null)).isFalse();
            assertThat(OpenStringBase.isNotBlank("")).isFalse();
            assertThat(OpenStringBase.isNotBlank(" ")).isFalse();
            assertThat(OpenStringBase.isNotBlank("hello")).isTrue();
        }

        @Test
        @DisplayName("hasLength")
        void testHasLength() {
            assertThat(OpenStringBase.hasLength(null)).isFalse();
            assertThat(OpenStringBase.hasLength("")).isFalse();
            assertThat(OpenStringBase.hasLength(" ")).isTrue();
            assertThat(OpenStringBase.hasLength("hello")).isTrue();
        }

        @Test
        @DisplayName("hasText")
        void testHasText() {
            assertThat(OpenStringBase.hasText(null)).isFalse();
            assertThat(OpenStringBase.hasText("")).isFalse();
            assertThat(OpenStringBase.hasText(" ")).isFalse();
            assertThat(OpenStringBase.hasText("hello")).isTrue();
        }
    }

    @Nested
    @DisplayName("长度测试")
    class LengthTests {

        @Test
        @DisplayName("length")
        void testLength() {
            assertThat(OpenStringBase.length(null)).isEqualTo(0);
            assertThat(OpenStringBase.length("")).isEqualTo(0);
            assertThat(OpenStringBase.length("hello")).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("defaultIfEmpty")
        void testDefaultIfEmpty() {
            assertThat(OpenStringBase.defaultIfEmpty(null, "default")).isEqualTo("default");
            assertThat(OpenStringBase.defaultIfEmpty("", "default")).isEqualTo("default");
            assertThat(OpenStringBase.defaultIfEmpty(" ", "default")).isEqualTo(" ");
            assertThat(OpenStringBase.defaultIfEmpty("hello", "default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("defaultIfBlank")
        void testDefaultIfBlank() {
            assertThat(OpenStringBase.defaultIfBlank(null, "default")).isEqualTo("default");
            assertThat(OpenStringBase.defaultIfBlank("", "default")).isEqualTo("default");
            assertThat(OpenStringBase.defaultIfBlank(" ", "default")).isEqualTo("default");
            assertThat(OpenStringBase.defaultIfBlank("  \t\n", "default")).isEqualTo("default");
            assertThat(OpenStringBase.defaultIfBlank("hello", "default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("nullToEmpty")
        void testNullToEmpty() {
            assertThat(OpenStringBase.nullToEmpty(null)).isEmpty();
            assertThat(OpenStringBase.nullToEmpty("")).isEmpty();
            assertThat(OpenStringBase.nullToEmpty("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("emptyToNull")
        void testEmptyToNull() {
            assertThat(OpenStringBase.emptyToNull(null)).isNull();
            assertThat(OpenStringBase.emptyToNull("")).isNull();
            assertThat(OpenStringBase.emptyToNull("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("trimToNull")
        void testTrimToNull() {
            assertThat(OpenStringBase.trimToNull(null)).isNull();
            assertThat(OpenStringBase.trimToNull("")).isNull();
            assertThat(OpenStringBase.trimToNull(" ")).isNull();
            assertThat(OpenStringBase.trimToNull("  \t\n  ")).isNull();
            assertThat(OpenStringBase.trimToNull("  hello  ")).isEqualTo("hello");
        }

        @Test
        @DisplayName("trimToEmpty")
        void testTrimToEmpty() {
            assertThat(OpenStringBase.trimToEmpty(null)).isEmpty();
            assertThat(OpenStringBase.trimToEmpty("")).isEmpty();
            assertThat(OpenStringBase.trimToEmpty(" ")).isEmpty();
            assertThat(OpenStringBase.trimToEmpty("  hello  ")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("equals")
        void testEquals() {
            assertThat(OpenStringBase.equals(null, null)).isTrue();
            assertThat(OpenStringBase.equals("hello", "hello")).isTrue();
            assertThat(OpenStringBase.equals(null, "hello")).isFalse();
            assertThat(OpenStringBase.equals("hello", null)).isFalse();
            assertThat(OpenStringBase.equals("hello", "world")).isFalse();
            assertThat(OpenStringBase.equals("hello", "Hello")).isFalse();
        }

        @Test
        @DisplayName("equals CharSequence")
        void testEqualsCharSequence() {
            StringBuilder sb = new StringBuilder("hello");
            assertThat(OpenStringBase.equals(sb, "hello")).isTrue();
            assertThat(OpenStringBase.equals("hello", sb)).isTrue();
        }

        @Test
        @DisplayName("equalsIgnoreCase")
        void testEqualsIgnoreCase() {
            assertThat(OpenStringBase.equalsIgnoreCase(null, null)).isTrue();
            assertThat(OpenStringBase.equalsIgnoreCase("hello", "HELLO")).isTrue();
            assertThat(OpenStringBase.equalsIgnoreCase("Hello", "hELLO")).isTrue();
            assertThat(OpenStringBase.equalsIgnoreCase(null, "hello")).isFalse();
            assertThat(OpenStringBase.equalsIgnoreCase("hello", null)).isFalse();
            assertThat(OpenStringBase.equalsIgnoreCase("hello", "world")).isFalse();
        }
    }

    @Nested
    @DisplayName("简单操作测试")
    class SimpleOperationTests {

        @Test
        @DisplayName("trim")
        void testTrim() {
            assertThat(OpenStringBase.trim(null)).isNull();
            assertThat(OpenStringBase.trim("")).isEmpty();
            assertThat(OpenStringBase.trim("  hello  ")).isEqualTo("hello");
            assertThat(OpenStringBase.trim("\t\nhello\t\n")).isEqualTo("hello");
        }

        @Test
        @DisplayName("toLowerCase")
        void testToLowerCase() {
            assertThat(OpenStringBase.toLowerCase(null)).isNull();
            assertThat(OpenStringBase.toLowerCase("")).isEmpty();
            assertThat(OpenStringBase.toLowerCase("HELLO")).isEqualTo("hello");
            assertThat(OpenStringBase.toLowerCase("Hello World")).isEqualTo("hello world");
        }

        @Test
        @DisplayName("toUpperCase")
        void testToUpperCase() {
            assertThat(OpenStringBase.toUpperCase(null)).isNull();
            assertThat(OpenStringBase.toUpperCase("")).isEmpty();
            assertThat(OpenStringBase.toUpperCase("hello")).isEqualTo("HELLO");
            assertThat(OpenStringBase.toUpperCase("Hello World")).isEqualTo("HELLO WORLD");
        }

        @Test
        @DisplayName("startsWith")
        void testStartsWith() {
            assertThat(OpenStringBase.startsWith("hello", "he")).isTrue();
            assertThat(OpenStringBase.startsWith("hello", "")).isTrue();
            assertThat(OpenStringBase.startsWith("hello", "world")).isFalse();
            assertThat(OpenStringBase.startsWith(null, null)).isTrue();
            assertThat(OpenStringBase.startsWith(null, "he")).isFalse();
            assertThat(OpenStringBase.startsWith("hello", null)).isFalse();
        }

        @Test
        @DisplayName("endsWith")
        void testEndsWith() {
            assertThat(OpenStringBase.endsWith("hello", "lo")).isTrue();
            assertThat(OpenStringBase.endsWith("hello", "")).isTrue();
            assertThat(OpenStringBase.endsWith("hello", "world")).isFalse();
            assertThat(OpenStringBase.endsWith(null, null)).isTrue();
            assertThat(OpenStringBase.endsWith(null, "lo")).isFalse();
            assertThat(OpenStringBase.endsWith("hello", null)).isFalse();
        }

        @Test
        @DisplayName("contains")
        void testContains() {
            assertThat(OpenStringBase.contains("hello world", "lo wo")).isTrue();
            assertThat(OpenStringBase.contains("hello", "world")).isFalse();
            assertThat(OpenStringBase.contains(null, "hello")).isFalse();
            assertThat(OpenStringBase.contains("hello", null)).isFalse();
        }
    }
}

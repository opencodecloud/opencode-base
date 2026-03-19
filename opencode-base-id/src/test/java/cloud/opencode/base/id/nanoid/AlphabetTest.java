package cloud.opencode.base.id.nanoid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Alphabet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("Alphabet 测试")
class AlphabetTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("DEFAULT字母表")
        void testDefault() {
            Alphabet alphabet = Alphabet.DEFAULT;

            assertThat(alphabet.getChars()).isNotNull();
            assertThat(alphabet.size()).isEqualTo(64);
            assertThat(alphabet.getChars()).contains("_");
            assertThat(alphabet.getChars()).contains("-");
        }

        @Test
        @DisplayName("ALPHANUMERIC字母表")
        void testAlphanumeric() {
            Alphabet alphabet = Alphabet.ALPHANUMERIC;

            assertThat(alphabet.getChars()).isNotNull();
            assertThat(alphabet.size()).isEqualTo(62);
            assertThat(alphabet.getChars()).doesNotContain("_");
            assertThat(alphabet.getChars()).doesNotContain("-");
        }

        @Test
        @DisplayName("ALPHABETIC字母表")
        void testAlphabetic() {
            Alphabet alphabet = Alphabet.ALPHABETIC;

            assertThat(alphabet.getChars()).isNotNull();
            assertThat(alphabet.size()).isEqualTo(52);
            assertThat(alphabet.getChars()).matches("[A-Za-z]+");
        }

        @Test
        @DisplayName("NUMERIC字母表")
        void testNumeric() {
            Alphabet alphabet = Alphabet.NUMERIC;

            assertThat(alphabet.getChars()).isEqualTo("0123456789");
            assertThat(alphabet.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("HEX_LOWERCASE字母表")
        void testHexLowercase() {
            Alphabet alphabet = Alphabet.HEX_LOWERCASE;

            assertThat(alphabet.getChars()).isEqualTo("0123456789abcdef");
            assertThat(alphabet.size()).isEqualTo(16);
        }

        @Test
        @DisplayName("HEX_UPPERCASE字母表")
        void testHexUppercase() {
            Alphabet alphabet = Alphabet.HEX_UPPERCASE;

            assertThat(alphabet.getChars()).isEqualTo("0123456789ABCDEF");
            assertThat(alphabet.size()).isEqualTo(16);
        }

        @Test
        @DisplayName("NOLOOK_ALIKE字母表")
        void testNoLookAlike() {
            Alphabet alphabet = Alphabet.NOLOOK_ALIKE;

            assertThat(alphabet.getChars()).isNotNull();
            // Should not contain ambiguous characters
            assertThat(alphabet.getChars()).doesNotContain("0");
            assertThat(alphabet.getChars()).doesNotContain("O");
            assertThat(alphabet.getChars()).doesNotContain("1");
            assertThat(alphabet.getChars()).doesNotContain("l");
            assertThat(alphabet.getChars()).doesNotContain("I");
        }
    }

    @Nested
    @DisplayName("方法测试")
    class MethodTests {

        @Test
        @DisplayName("getChars方法")
        void testGetChars() {
            for (Alphabet alphabet : Alphabet.values()) {
                assertThat(alphabet.getChars()).isNotNull();
                assertThat(alphabet.getChars()).isNotEmpty();
            }
        }

        @Test
        @DisplayName("size方法")
        void testSize() {
            for (Alphabet alphabet : Alphabet.values()) {
                assertThat(alphabet.size()).isGreaterThan(0);
                assertThat(alphabet.size()).isEqualTo(alphabet.getChars().length());
            }
        }

        @Test
        @DisplayName("charAt方法")
        void testCharAt() {
            Alphabet alphabet = Alphabet.NUMERIC;

            assertThat(alphabet.charAt(0)).isEqualTo('0');
            assertThat(alphabet.charAt(9)).isEqualTo('9');
        }

        @Test
        @DisplayName("charAt越界")
        void testCharAtOutOfBounds() {
            Alphabet alphabet = Alphabet.NUMERIC;

            assertThatThrownBy(() -> alphabet.charAt(10))
                    .isInstanceOf(StringIndexOutOfBoundsException.class);

            assertThatThrownBy(() -> alphabet.charAt(-1))
                    .isInstanceOf(StringIndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("枚举标准方法测试")
    class EnumStandardTests {

        @Test
        @DisplayName("values方法")
        void testValues() {
            Alphabet[] values = Alphabet.values();

            assertThat(values).isNotNull();
            assertThat(values).hasSize(7);
        }

        @Test
        @DisplayName("valueOf方法")
        void testValueOf() {
            Alphabet alphabet = Alphabet.valueOf("DEFAULT");

            assertThat(alphabet).isEqualTo(Alphabet.DEFAULT);
        }

        @Test
        @DisplayName("valueOf无效值抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> Alphabet.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("与NanoIdGenerator集成")
        void testWithNanoIdGenerator() {
            for (Alphabet alphabet : Alphabet.values()) {
                NanoIdGenerator gen = NanoIdGenerator.builder()
                        .alphabet(alphabet)
                        .size(10)
                        .build();

                String id = gen.generate();

                assertThat(id).hasSize(10);
                for (char c : id.toCharArray()) {
                    assertThat(alphabet.getChars()).contains(String.valueOf(c));
                }
            }
        }
    }
}

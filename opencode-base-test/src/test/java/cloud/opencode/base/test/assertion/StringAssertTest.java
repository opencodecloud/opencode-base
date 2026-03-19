package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * StringAssertTest Tests
 * StringAssertTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("StringAssert Tests")
class StringAssertTest {

    @Nested
    @DisplayName("Null/NotNull Tests")
    class NullNotNullTests {

        @Test
        @DisplayName("isNull should pass for null")
        void isNullShouldPassForNull() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat(null).isNull());
        }

        @Test
        @DisplayName("isNull should fail for non-null")
        void isNullShouldFailForNonNull() {
            assertThatThrownBy(() -> StringAssert.assertThat("value").isNull())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotNull should pass for non-null")
        void isNotNullShouldPassForNonNull() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("value").isNotNull());
        }

        @Test
        @DisplayName("isNotNull should fail for null")
        void isNotNullShouldFailForNull() {
            assertThatThrownBy(() -> StringAssert.assertThat(null).isNotNull())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Empty/NotEmpty Tests")
    class EmptyNotEmptyTests {

        @Test
        @DisplayName("isEmpty should pass for empty string")
        void isEmptyShouldPassForEmptyString() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("").isEmpty());
        }

        @Test
        @DisplayName("isEmpty should fail for non-empty string")
        void isEmptyShouldFailForNonEmptyString() {
            assertThatThrownBy(() -> StringAssert.assertThat("value").isEmpty())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotEmpty should pass for non-empty string")
        void isNotEmptyShouldPassForNonEmptyString() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("value").isNotEmpty());
        }

        @Test
        @DisplayName("isNotEmpty should fail for empty string")
        void isNotEmptyShouldFailForEmptyString() {
            assertThatThrownBy(() -> StringAssert.assertThat("").isNotEmpty())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Blank/NotBlank Tests")
    class BlankNotBlankTests {

        @Test
        @DisplayName("isBlank should pass for blank string")
        void isBlankShouldPassForBlankString() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("   ").isBlank());
        }

        @Test
        @DisplayName("isBlank should fail for non-blank string")
        void isBlankShouldFailForNonBlankString() {
            assertThatThrownBy(() -> StringAssert.assertThat("value").isBlank())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isNotBlank should pass for non-blank string")
        void isNotBlankShouldPassForNonBlankString() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("value").isNotBlank());
        }

        @Test
        @DisplayName("isNotBlank should fail for blank string")
        void isNotBlankShouldFailForBlankString() {
            assertThatThrownBy(() -> StringAssert.assertThat("   ").isNotBlank())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Length Tests")
    class LengthTests {

        @Test
        @DisplayName("hasLength should pass for correct length")
        void hasLengthShouldPassForCorrectLength() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello").hasLength(5));
        }

        @Test
        @DisplayName("hasLength should fail for wrong length")
        void hasLengthShouldFailForWrongLength() {
            assertThatThrownBy(() -> StringAssert.assertThat("hello").hasLength(10))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("hasLengthBetween should pass for length in range")
        void hasLengthBetweenShouldPassForLengthInRange() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello").hasLengthBetween(3, 10));
        }

        @Test
        @DisplayName("hasLengthBetween should fail for length out of range")
        void hasLengthBetweenShouldFailForLengthOutOfRange() {
            assertThatThrownBy(() -> StringAssert.assertThat("hello").hasLengthBetween(10, 20))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("isEqualTo should pass for equal strings")
        void isEqualToShouldPassForEqualStrings() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello").isEqualTo("hello"));
        }

        @Test
        @DisplayName("isEqualTo should fail for different strings")
        void isEqualToShouldFailForDifferentStrings() {
            assertThatThrownBy(() -> StringAssert.assertThat("hello").isEqualTo("world"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isEqualToIgnoringCase should pass")
        void isEqualToIgnoringCaseShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("HELLO").isEqualToIgnoringCase("hello"));
        }

        @Test
        @DisplayName("isEqualToIgnoringWhitespace should pass")
        void isEqualToIgnoringWhitespaceShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("  hello  ").isEqualToIgnoringWhitespace("hello"));
        }
    }

    @Nested
    @DisplayName("Contains Tests")
    class ContainsTests {

        @Test
        @DisplayName("contains should pass when substring exists")
        void containsShouldPassWhenSubstringExists() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello world").contains("world"));
        }

        @Test
        @DisplayName("contains should fail when substring not found")
        void containsShouldFailWhenSubstringNotFound() {
            assertThatThrownBy(() -> StringAssert.assertThat("hello").contains("world"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("doesNotContain should pass when substring not found")
        void doesNotContainShouldPassWhenSubstringNotFound() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello").doesNotContain("world"));
        }

        @Test
        @DisplayName("doesNotContain should fail when substring exists")
        void doesNotContainShouldFailWhenSubstringExists() {
            assertThatThrownBy(() -> StringAssert.assertThat("hello world").doesNotContain("world"))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("StartsWith/EndsWith Tests")
    class StartsWithEndsWithTests {

        @Test
        @DisplayName("startsWith should pass")
        void startsWithShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello world").startsWith("hello"));
        }

        @Test
        @DisplayName("startsWith should fail")
        void startsWithShouldFail() {
            assertThatThrownBy(() -> StringAssert.assertThat("hello world").startsWith("world"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("endsWith should pass")
        void endsWithShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello world").endsWith("world"));
        }

        @Test
        @DisplayName("endsWith should fail")
        void endsWithShouldFail() {
            assertThatThrownBy(() -> StringAssert.assertThat("hello world").endsWith("hello"))
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Regex Tests")
    class RegexTests {

        @Test
        @DisplayName("matches with regex string should pass")
        void matchesWithRegexStringShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("abc123").matches("[a-z]+\\d+"));
        }

        @Test
        @DisplayName("matches with regex string should fail")
        void matchesWithRegexStringShouldFail() {
            assertThatThrownBy(() -> StringAssert.assertThat("abc123").matches("\\d+"))
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("matches with Pattern should pass")
        void matchesWithPatternShouldPass() {
            Pattern pattern = Pattern.compile("[a-z]+\\d+");
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("abc123").matches(pattern));
        }
    }

    @Nested
    @DisplayName("Character Content Tests")
    class CharacterContentTests {

        @Test
        @DisplayName("containsOnlyDigits should pass")
        void containsOnlyDigitsShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("123456").containsOnlyDigits());
        }

        @Test
        @DisplayName("containsOnlyDigits should fail")
        void containsOnlyDigitsShouldFail() {
            assertThatThrownBy(() -> StringAssert.assertThat("123abc").containsOnlyDigits())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("containsOnlyLetters should pass")
        void containsOnlyLettersShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("abcXYZ").containsOnlyLetters());
        }

        @Test
        @DisplayName("containsOnlyLetters should fail")
        void containsOnlyLettersShouldFail() {
            assertThatThrownBy(() -> StringAssert.assertThat("abc123").containsOnlyLetters())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Case Tests")
    class CaseTests {

        @Test
        @DisplayName("isUpperCase should pass")
        void isUpperCaseShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("HELLO").isUpperCase());
        }

        @Test
        @DisplayName("isUpperCase should fail")
        void isUpperCaseShouldFail() {
            assertThatThrownBy(() -> StringAssert.assertThat("Hello").isUpperCase())
                .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("isLowerCase should pass")
        void isLowerCaseShouldPass() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("hello").isLowerCase());
        }

        @Test
        @DisplayName("isLowerCase should fail")
        void isLowerCaseShouldFail() {
            assertThatThrownBy(() -> StringAssert.assertThat("Hello").isLowerCase())
                .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Should support fluent chaining")
        void shouldSupportFluentChaining() {
            assertThatNoException().isThrownBy(() ->
                StringAssert.assertThat("Hello World")
                    .isNotNull()
                    .isNotEmpty()
                    .isNotBlank()
                    .startsWith("Hello")
                    .endsWith("World")
                    .contains("lo Wo")
                    .hasLength(11));
        }
    }
}

package cloud.opencode.base.string.builder;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CharMatcherTest Tests
 * CharMatcherTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("CharMatcher Tests")
class CharMatcherTest {

    @Nested
    @DisplayName("Predefined Matchers Tests")
    class PredefinedMatchersTests {

        @Test
        @DisplayName("any() should match any character")
        void anyShouldMatchAnyCharacter() {
            CharMatcher matcher = CharMatcher.any();
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('1')).isTrue();
            assertThat(matcher.matches(' ')).isTrue();
        }

        @Test
        @DisplayName("none() should match no character")
        void noneShouldMatchNoCharacter() {
            CharMatcher matcher = CharMatcher.none();
            assertThat(matcher.matches('a')).isFalse();
            assertThat(matcher.matches('1')).isFalse();
        }

        @Test
        @DisplayName("whitespace() should match whitespace")
        void whitespaceShouldMatchWhitespace() {
            CharMatcher matcher = CharMatcher.whitespace();
            assertThat(matcher.matches(' ')).isTrue();
            assertThat(matcher.matches('\t')).isTrue();
            assertThat(matcher.matches('\n')).isTrue();
            assertThat(matcher.matches('a')).isFalse();
        }

        @Test
        @DisplayName("digit() should match digits")
        void digitShouldMatchDigits() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.matches('0')).isTrue();
            assertThat(matcher.matches('9')).isTrue();
            assertThat(matcher.matches('a')).isFalse();
        }

        @Test
        @DisplayName("ascii() should match ASCII characters")
        void asciiShouldMatchAsciiCharacters() {
            CharMatcher matcher = CharMatcher.ascii();
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('Z')).isTrue();
            assertThat(matcher.matches((char) 127)).isTrue();
            assertThat(matcher.matches((char) 128)).isFalse();
        }

        @Test
        @DisplayName("javaLetter() should match letters")
        void javaLetterShouldMatchLetters() {
            CharMatcher matcher = CharMatcher.javaLetter();
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('Z')).isTrue();
            assertThat(matcher.matches('1')).isFalse();
        }

        @Test
        @DisplayName("javaLetterOrDigit() should match letters and digits")
        void javaLetterOrDigitShouldMatchLettersAndDigits() {
            CharMatcher matcher = CharMatcher.javaLetterOrDigit();
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('1')).isTrue();
            assertThat(matcher.matches(' ')).isFalse();
        }

        @Test
        @DisplayName("javaUpperCase() should match uppercase letters")
        void javaUpperCaseShouldMatchUppercaseLetters() {
            CharMatcher matcher = CharMatcher.javaUpperCase();
            assertThat(matcher.matches('A')).isTrue();
            assertThat(matcher.matches('a')).isFalse();
        }

        @Test
        @DisplayName("javaLowerCase() should match lowercase letters")
        void javaLowerCaseShouldMatchLowercaseLetters() {
            CharMatcher matcher = CharMatcher.javaLowerCase();
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('A')).isFalse();
        }

        @Test
        @DisplayName("invisible() should match invisible characters")
        void invisibleShouldMatchInvisibleCharacters() {
            CharMatcher matcher = CharMatcher.invisible();
            assertThat(matcher.matches(' ')).isTrue();
            assertThat(matcher.matches('\t')).isTrue();
            assertThat(matcher.matches('a')).isFalse();
        }

        @Test
        @DisplayName("breakingWhitespace() should match whitespace")
        void breakingWhitespaceShouldMatchWhitespace() {
            CharMatcher matcher = CharMatcher.breakingWhitespace();
            assertThat(matcher.matches(' ')).isTrue();
        }

        @Test
        @DisplayName("javaDigit() should match digits")
        void javaDigitShouldMatchDigits() {
            CharMatcher matcher = CharMatcher.javaDigit();
            assertThat(matcher.matches('5')).isTrue();
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("is() should match specific character")
        void isShouldMatchSpecificCharacter() {
            CharMatcher matcher = CharMatcher.is('a');
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('b')).isFalse();
        }

        @Test
        @DisplayName("isNot() should match all except specific character")
        void isNotShouldMatchAllExceptSpecificCharacter() {
            CharMatcher matcher = CharMatcher.isNot('a');
            assertThat(matcher.matches('a')).isFalse();
            assertThat(matcher.matches('b')).isTrue();
        }

        @Test
        @DisplayName("anyOf() should match any of the characters")
        void anyOfShouldMatchAnyOfTheCharacters() {
            CharMatcher matcher = CharMatcher.anyOf("abc");
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('c')).isTrue();
            assertThat(matcher.matches('d')).isFalse();
        }

        @Test
        @DisplayName("noneOf() should match none of the characters")
        void noneOfShouldMatchNoneOfTheCharacters() {
            CharMatcher matcher = CharMatcher.noneOf("abc");
            assertThat(matcher.matches('a')).isFalse();
            assertThat(matcher.matches('d')).isTrue();
        }

        @Test
        @DisplayName("inRange() should match characters in range")
        void inRangeShouldMatchCharactersInRange() {
            CharMatcher matcher = CharMatcher.inRange('a', 'z');
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('m')).isTrue();
            assertThat(matcher.matches('z')).isTrue();
            assertThat(matcher.matches('A')).isFalse();
        }

        @Test
        @DisplayName("forPredicate() should match using predicate")
        void forPredicateShouldMatchUsingPredicate() {
            CharMatcher matcher = CharMatcher.forPredicate(c -> c == 'x' || c == 'y');
            assertThat(matcher.matches('x')).isTrue();
            assertThat(matcher.matches('y')).isTrue();
            assertThat(matcher.matches('z')).isFalse();
        }
    }

    @Nested
    @DisplayName("Combination Methods Tests")
    class CombinationMethodsTests {

        @Test
        @DisplayName("and() should combine matchers with AND")
        void andShouldCombineMatchersWithAnd() {
            CharMatcher matcher = CharMatcher.inRange('a', 'z').and(CharMatcher.inRange('e', 'p'));
            assertThat(matcher.matches('f')).isTrue();
            assertThat(matcher.matches('a')).isFalse();
            assertThat(matcher.matches('z')).isFalse();
        }

        @Test
        @DisplayName("or() should combine matchers with OR")
        void orShouldCombineMatchersWithOr() {
            CharMatcher matcher = CharMatcher.is('a').or(CharMatcher.is('b'));
            assertThat(matcher.matches('a')).isTrue();
            assertThat(matcher.matches('b')).isTrue();
            assertThat(matcher.matches('c')).isFalse();
        }

        @Test
        @DisplayName("negate() should negate matcher")
        void negateShouldNegateMatcher() {
            CharMatcher matcher = CharMatcher.is('a').negate();
            assertThat(matcher.matches('a')).isFalse();
            assertThat(matcher.matches('b')).isTrue();
        }
    }

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("matchesAnyOf() should return true if any matches")
        void matchesAnyOfShouldReturnTrueIfAnyMatches() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.matchesAnyOf("abc123")).isTrue();
            assertThat(matcher.matchesAnyOf("abc")).isFalse();
        }

        @Test
        @DisplayName("matchesAllOf() should return true if all match")
        void matchesAllOfShouldReturnTrueIfAllMatch() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.matchesAllOf("123")).isTrue();
            assertThat(matcher.matchesAllOf("12a")).isFalse();
            assertThat(matcher.matchesAllOf("")).isFalse();
        }

        @Test
        @DisplayName("matchesNoneOf() should return true if none match")
        void matchesNoneOfShouldReturnTrueIfNoneMatch() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.matchesNoneOf("abc")).isTrue();
            assertThat(matcher.matchesNoneOf("abc1")).isFalse();
        }
    }

    @Nested
    @DisplayName("Index Methods Tests")
    class IndexMethodsTests {

        @Test
        @DisplayName("indexIn() should find first index")
        void indexInShouldFindFirstIndex() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.indexIn("abc123")).isEqualTo(3);
            assertThat(matcher.indexIn("abc")).isEqualTo(-1);
        }

        @Test
        @DisplayName("indexIn(start) should find first index from start")
        void indexInStartShouldFindFirstIndexFromStart() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.indexIn("1abc2", 1)).isEqualTo(4);
        }

        @Test
        @DisplayName("lastIndexIn() should find last index")
        void lastIndexInShouldFindLastIndex() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.lastIndexIn("1abc2")).isEqualTo(4);
            assertThat(matcher.lastIndexIn("abc")).isEqualTo(-1);
        }

        @Test
        @DisplayName("countIn() should count matches")
        void countInShouldCountMatches() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.countIn("a1b2c3")).isEqualTo(3);
            assertThat(matcher.countIn("abc")).isZero();
        }
    }

    @Nested
    @DisplayName("Transformation Methods Tests")
    class TransformationMethodsTests {

        @Test
        @DisplayName("removeFrom() should remove matched characters")
        void removeFromShouldRemoveMatchedCharacters() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.removeFrom("a1b2c3")).isEqualTo("abc");
        }

        @Test
        @DisplayName("retainFrom() should retain only matched characters")
        void retainFromShouldRetainOnlyMatchedCharacters() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.retainFrom("a1b2c3")).isEqualTo("123");
        }

        @Test
        @DisplayName("replaceFrom(char) should replace with character")
        void replaceFromCharShouldReplaceWithCharacter() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.replaceFrom("a1b2c3", '*')).isEqualTo("a*b*c*");
        }

        @Test
        @DisplayName("replaceFrom(CharSequence) should replace with string")
        void replaceFromCharSequenceShouldReplaceWithString() {
            CharMatcher matcher = CharMatcher.digit();
            assertThat(matcher.replaceFrom("a1b2", "XX")).isEqualTo("aXXbXX");
        }

        @Test
        @DisplayName("collapseFrom() should collapse consecutive matches")
        void collapseFromShouldCollapseConsecutiveMatches() {
            CharMatcher matcher = CharMatcher.whitespace();
            assertThat(matcher.collapseFrom("a   b  c", ' ')).isEqualTo("a b c");
        }

        @Test
        @DisplayName("trimFrom() should trim matched characters from both ends")
        void trimFromShouldTrimMatchedCharactersFromBothEnds() {
            CharMatcher matcher = CharMatcher.whitespace();
            assertThat(matcher.trimFrom("  hello  ")).isEqualTo("hello");
        }

        @Test
        @DisplayName("trimLeadingFrom() should trim from beginning")
        void trimLeadingFromShouldTrimFromBeginning() {
            CharMatcher matcher = CharMatcher.whitespace();
            assertThat(matcher.trimLeadingFrom("  hello  ")).isEqualTo("hello  ");
        }

        @Test
        @DisplayName("trimTrailingFrom() should trim from end")
        void trimTrailingFromShouldTrimFromEnd() {
            CharMatcher matcher = CharMatcher.whitespace();
            assertThat(matcher.trimTrailingFrom("  hello  ")).isEqualTo("  hello");
        }

        @Test
        @DisplayName("trimAndCollapseFrom() should trim and collapse")
        void trimAndCollapseFromShouldTrimAndCollapse() {
            CharMatcher matcher = CharMatcher.whitespace();
            assertThat(matcher.trimAndCollapseFrom("  a   b  ", ' ')).isEqualTo("a b");
        }
    }
}

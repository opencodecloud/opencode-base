package cloud.opencode.base.string;

import org.junit.jupiter.api.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenStringTest Tests
 * OpenStringTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenString Tests")
class OpenStringTest {

    @Nested
    @DisplayName("Padding Tests")
    class PaddingTests {

        @Test
        @DisplayName("padLeft should pad string on left")
        void padLeftShouldPadOnLeft() {
            assertThat(OpenString.padLeft("abc", 5, '0')).isEqualTo("00abc");
            assertThat(OpenString.padLeft("abc", 3, '0')).isEqualTo("abc");
            assertThat(OpenString.padLeft("abc", 2, '0')).isEqualTo("abc");
            assertThat(OpenString.padLeft(null, 5, '0')).isNull();
        }

        @Test
        @DisplayName("padRight should pad string on right")
        void padRightShouldPadOnRight() {
            assertThat(OpenString.padRight("abc", 5, '0')).isEqualTo("abc00");
            assertThat(OpenString.padRight("abc", 3, '0')).isEqualTo("abc");
            assertThat(OpenString.padRight(null, 5, '0')).isNull();
        }

        @Test
        @DisplayName("center should center string")
        void centerShouldCenterString() {
            assertThat(OpenString.center("ab", 6, '*')).isEqualTo("**ab**");
            assertThat(OpenString.center("abc", 6, '*')).isEqualTo("*abc**");
            assertThat(OpenString.center("abc", 3, '*')).isEqualTo("abc");
            assertThat(OpenString.center(null, 5, '*')).isNull();
        }
    }

    @Nested
    @DisplayName("Extraction Tests")
    class ExtractionTests {

        @Test
        @DisplayName("left should extract left part")
        void leftShouldExtractLeftPart() {
            assertThat(OpenString.left("hello", 3)).isEqualTo("hel");
            assertThat(OpenString.left("hi", 5)).isEqualTo("hi");
            assertThat(OpenString.left("hello", -1)).isEqualTo("hello");
            assertThat(OpenString.left(null, 3)).isNull();
        }

        @Test
        @DisplayName("right should extract right part")
        void rightShouldExtractRightPart() {
            assertThat(OpenString.right("hello", 3)).isEqualTo("llo");
            assertThat(OpenString.right("hi", 5)).isEqualTo("hi");
            assertThat(OpenString.right(null, 3)).isNull();
        }

        @Test
        @DisplayName("mid should extract middle part")
        void midShouldExtractMiddlePart() {
            assertThat(OpenString.mid("hello", 1, 4)).isEqualTo("ell");
            assertThat(OpenString.mid("hello", 0, 5)).isEqualTo("hello");
            assertThat(OpenString.mid(null, 1, 3)).isEmpty();
            assertThat(OpenString.mid("hello", -1, 3)).isEmpty();
            assertThat(OpenString.mid("hello", 3, 2)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Truncation Tests")
    class TruncationTests {

        @Test
        @DisplayName("truncate should truncate with ellipsis")
        void truncateShouldTruncateWithEllipsis() {
            assertThat(OpenString.truncate("hello world", 8)).isEqualTo("hello...");
            assertThat(OpenString.truncate("hi", 10)).isEqualTo("hi");
            assertThat(OpenString.truncate(null, 5)).isNull();
        }

        @Test
        @DisplayName("truncate with custom ellipsis should work")
        void truncateWithCustomEllipsisShouldWork() {
            assertThat(OpenString.truncate("hello world", 8, ">>")).isEqualTo("hello >>");
            assertThat(OpenString.truncate("hello", 2, "...")).isEqualTo("he");
        }

        @Test
        @DisplayName("truncateMiddle should keep start and end")
        void truncateMiddleShouldKeepStartAndEnd() {
            assertThat(OpenString.truncateMiddle("hello world!", 8)).isEqualTo("he...ld!");
            assertThat(OpenString.truncateMiddle("hi", 10)).isEqualTo("hi");
            assertThat(OpenString.truncateMiddle(null, 5)).isNull();
        }

        @Test
        @DisplayName("truncateByBytes should truncate by byte length")
        void truncateByBytesShouldTruncateByByteLength() {
            assertThat(OpenString.truncateByBytes("hello", 3, "UTF-8")).isEqualTo("hel");
            assertThat(OpenString.truncateByBytes("hello", 10, "UTF-8")).isEqualTo("hello");
            assertThat(OpenString.truncateByBytes(null, 5, "UTF-8")).isNull();
        }
    }

    @Nested
    @DisplayName("Case Conversion Tests")
    class CaseConversionTests {

        @Test
        @DisplayName("capitalize should capitalize first letter")
        void capitalizeShouldCapitalizeFirstLetter() {
            assertThat(OpenString.capitalize("hello")).isEqualTo("Hello");
            assertThat(OpenString.capitalize("")).isEmpty();
            assertThat(OpenString.capitalize(null)).isNull();
        }

        @Test
        @DisplayName("uncapitalize should uncapitalize first letter")
        void uncapitalizeShouldUncapitalizeFirstLetter() {
            assertThat(OpenString.uncapitalize("Hello")).isEqualTo("hello");
            assertThat(OpenString.uncapitalize("")).isEmpty();
            assertThat(OpenString.uncapitalize(null)).isNull();
        }

        @Test
        @DisplayName("swapCase should swap case of each character")
        void swapCaseShouldSwapCase() {
            assertThat(OpenString.swapCase("Hello")).isEqualTo("hELLO");
            assertThat(OpenString.swapCase("HeLLo")).isEqualTo("hEllO");
            assertThat(OpenString.swapCase(null)).isNull();
        }

        @Test
        @DisplayName("toTitleCase should capitalize each word")
        void toTitleCaseShouldCapitalizeEachWord() {
            assertThat(OpenString.toTitleCase("hello world")).isEqualTo("Hello World");
            assertThat(OpenString.toTitleCase("HELLO WORLD")).isEqualTo("Hello World");
            assertThat(OpenString.toTitleCase("")).isEmpty();
            assertThat(OpenString.toTitleCase(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Reverse and Shuffle Tests")
    class ReverseAndShuffleTests {

        @Test
        @DisplayName("reverse should reverse string")
        void reverseShouldReverseString() {
            assertThat(OpenString.reverse("hello")).isEqualTo("olleh");
            assertThat(OpenString.reverse("")).isEmpty();
            assertThat(OpenString.reverse(null)).isNull();
        }

        @Test
        @DisplayName("shuffle should shuffle string")
        void shuffleShouldShuffleString() {
            String original = "hello";
            String shuffled = OpenString.shuffle(original, new Random(42));
            assertThat(shuffled).hasSize(original.length());
            assertThat(shuffled.chars().sorted().toArray())
                .isEqualTo(original.chars().sorted().toArray());
        }

        @Test
        @DisplayName("shuffle with null should return null")
        void shuffleWithNullShouldReturnNull() {
            assertThat(OpenString.shuffle(null)).isNull();
            assertThat(OpenString.shuffle(null, new Random())).isNull();
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("countMatches should count substring occurrences")
        void countMatchesShouldCountSubstringOccurrences() {
            assertThat(OpenString.countMatches("ababa", "aba")).isEqualTo(1);
            assertThat(OpenString.countMatches("aaaa", "aa")).isEqualTo(2);
            assertThat(OpenString.countMatches("hello", "x")).isZero();
            assertThat(OpenString.countMatches(null, "a")).isZero();
            assertThat(OpenString.countMatches("hello", null)).isZero();
            assertThat(OpenString.countMatches("hello", "")).isZero();
        }

        @Test
        @DisplayName("countMatches char should count character occurrences")
        void countMatchesCharShouldCountCharOccurrences() {
            assertThat(OpenString.countMatches("hello", 'l')).isEqualTo(2);
            assertThat(OpenString.countMatches("hello", 'x')).isZero();
            assertThat(OpenString.countMatches(null, 'a')).isZero();
        }

        @Test
        @DisplayName("charFrequency should return character frequencies")
        void charFrequencyShouldReturnCharFrequencies() {
            Map<Character, Integer> freq = OpenString.charFrequency("hello");
            assertThat(freq).containsEntry('h', 1);
            assertThat(freq).containsEntry('l', 2);
            assertThat(freq).containsEntry('o', 1);
        }

        @Test
        @DisplayName("wordFrequency should return word frequencies")
        void wordFrequencyShouldReturnWordFrequencies() {
            Map<String, Integer> freq = OpenString.wordFrequency("the cat and the dog");
            assertThat(freq).containsEntry("the", 2);
            assertThat(freq).containsEntry("cat", 1);
        }
    }

    @Nested
    @DisplayName("Cleaning Tests")
    class CleaningTests {

        @Test
        @DisplayName("removeWhitespace should remove all whitespace")
        void removeWhitespaceShouldRemoveAllWhitespace() {
            assertThat(OpenString.removeWhitespace("a b c")).isEqualTo("abc");
            assertThat(OpenString.removeWhitespace("hello\tworld\n")).isEqualTo("helloworld");
            assertThat(OpenString.removeWhitespace(null)).isNull();
        }

        @Test
        @DisplayName("normalizeWhitespace should normalize whitespace")
        void normalizeWhitespaceShouldNormalizeWhitespace() {
            assertThat(OpenString.normalizeWhitespace("  a   b  c  ")).isEqualTo("a b c");
            assertThat(OpenString.normalizeWhitespace(null)).isNull();
        }

        @Test
        @DisplayName("keepDigits should keep only digits")
        void keepDigitsShouldKeepOnlyDigits() {
            assertThat(OpenString.keepDigits("abc123def456")).isEqualTo("123456");
            assertThat(OpenString.keepDigits(null)).isNull();
        }

        @Test
        @DisplayName("keepLetters should keep only letters")
        void keepLettersShouldKeepOnlyLetters() {
            assertThat(OpenString.keepLetters("abc123def")).isEqualTo("abcdef");
            assertThat(OpenString.keepLetters(null)).isNull();
        }

        @Test
        @DisplayName("keepAlphanumeric should keep only alphanumeric")
        void keepAlphanumericShouldKeepOnlyAlphanumeric() {
            assertThat(OpenString.keepAlphanumeric("abc-123_def!")).isEqualTo("abc123def");
            assertThat(OpenString.keepAlphanumeric(null)).isNull();
        }

        @Test
        @DisplayName("keepChinese should keep only Chinese characters")
        void keepChineseShouldKeepOnlyChinese() {
            assertThat(OpenString.keepChinese("hello世界123")).isEqualTo("世界");
            assertThat(OpenString.keepChinese(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isNumeric should check for numeric string")
        void isNumericShouldCheckForNumericString() {
            assertThat(OpenString.isNumeric("123")).isTrue();
            assertThat(OpenString.isNumeric("12.3")).isFalse();
            assertThat(OpenString.isNumeric("abc")).isFalse();
            assertThat(OpenString.isNumeric("")).isFalse();
            assertThat(OpenString.isNumeric(null)).isFalse();
        }

        @Test
        @DisplayName("isAlpha should check for alphabetic string")
        void isAlphaShouldCheckForAlphabeticString() {
            assertThat(OpenString.isAlpha("abc")).isTrue();
            assertThat(OpenString.isAlpha("abc123")).isFalse();
            assertThat(OpenString.isAlpha("")).isFalse();
            assertThat(OpenString.isAlpha(null)).isFalse();
        }

        @Test
        @DisplayName("isAlphanumeric should check for alphanumeric string")
        void isAlphanumericShouldCheckForAlphanumericString() {
            assertThat(OpenString.isAlphanumeric("abc123")).isTrue();
            assertThat(OpenString.isAlphanumeric("abc-123")).isFalse();
            assertThat(OpenString.isAlphanumeric("")).isFalse();
            assertThat(OpenString.isAlphanumeric(null)).isFalse();
        }

        @Test
        @DisplayName("isAscii should check for ASCII string")
        void isAsciiShouldCheckForAsciiString() {
            assertThat(OpenString.isAscii("hello")).isTrue();
            assertThat(OpenString.isAscii("hello世界")).isFalse();
            assertThat(OpenString.isAscii(null)).isFalse();
        }

        @Test
        @DisplayName("containsChinese should check for Chinese characters")
        void containsChineseShouldCheckForChineseCharacters() {
            assertThat(OpenString.containsChinese("hello世界")).isTrue();
            assertThat(OpenString.containsChinese("hello")).isFalse();
            assertThat(OpenString.containsChinese(null)).isFalse();
        }

        @Test
        @DisplayName("isAllChinese should check if all characters are Chinese")
        void isAllChineseShouldCheckIfAllCharactersAreChinese() {
            assertThat(OpenString.isAllChinese("世界")).isTrue();
            assertThat(OpenString.isAllChinese("hello世界")).isFalse();
            assertThat(OpenString.isAllChinese("")).isFalse();
            assertThat(OpenString.isAllChinese(null)).isFalse();
        }

        @Test
        @DisplayName("isAllLowerCase should check if all lowercase")
        void isAllLowerCaseShouldCheckIfAllLowercase() {
            assertThat(OpenString.isAllLowerCase("hello")).isTrue();
            assertThat(OpenString.isAllLowerCase("Hello")).isFalse();
            assertThat(OpenString.isAllLowerCase("")).isFalse();
            assertThat(OpenString.isAllLowerCase(null)).isFalse();
        }

        @Test
        @DisplayName("isAllUpperCase should check if all uppercase")
        void isAllUpperCaseShouldCheckIfAllUppercase() {
            assertThat(OpenString.isAllUpperCase("HELLO")).isTrue();
            assertThat(OpenString.isAllUpperCase("Hello")).isFalse();
            assertThat(OpenString.isAllUpperCase("")).isFalse();
            assertThat(OpenString.isAllUpperCase(null)).isFalse();
        }

        @Test
        @DisplayName("isMixedCase should check if mixed case")
        void isMixedCaseShouldCheckIfMixedCase() {
            assertThat(OpenString.isMixedCase("Hello")).isTrue();
            assertThat(OpenString.isMixedCase("hello")).isFalse();
            assertThat(OpenString.isMixedCase("HELLO")).isFalse();
            assertThat(OpenString.isMixedCase("")).isFalse();
            assertThat(OpenString.isMixedCase(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Palindrome Tests")
    class PalindromeTests {

        @Test
        @DisplayName("isPalindrome should check for palindrome")
        void isPalindromeShouldCheckForPalindrome() {
            assertThat(OpenString.isPalindrome("radar")).isTrue();
            assertThat(OpenString.isPalindrome("hello")).isFalse();
            assertThat(OpenString.isPalindrome("")).isFalse();
            assertThat(OpenString.isPalindrome(null)).isFalse();
        }

        @Test
        @DisplayName("isPalindromeIgnoreCase should ignore case and whitespace")
        void isPalindromeIgnoreCaseShouldIgnoreCaseAndWhitespace() {
            assertThat(OpenString.isPalindromeIgnoreCase("A man a plan a canal Panama".replace(" ", ""))).isTrue();
            assertThat(OpenString.isPalindromeIgnoreCase("Radar")).isTrue();
            assertThat(OpenString.isPalindromeIgnoreCase(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Prefix/Suffix Tests")
    class PrefixSuffixTests {

        @Test
        @DisplayName("ensurePrefix should add prefix if missing")
        void ensurePrefixShouldAddPrefixIfMissing() {
            assertThat(OpenString.ensurePrefix("world", "hello")).isEqualTo("helloworld");
            assertThat(OpenString.ensurePrefix("helloworld", "hello")).isEqualTo("helloworld");
            assertThat(OpenString.ensurePrefix(null, "hello")).isNull();
        }

        @Test
        @DisplayName("ensureSuffix should add suffix if missing")
        void ensureSuffixShouldAddSuffixIfMissing() {
            assertThat(OpenString.ensureSuffix("hello", "world")).isEqualTo("helloworld");
            assertThat(OpenString.ensureSuffix("helloworld", "world")).isEqualTo("helloworld");
            assertThat(OpenString.ensureSuffix(null, "world")).isNull();
        }

        @Test
        @DisplayName("removePrefix should remove prefix")
        void removePrefixShouldRemovePrefix() {
            assertThat(OpenString.removePrefix("helloworld", "hello")).isEqualTo("world");
            assertThat(OpenString.removePrefix("world", "hello")).isEqualTo("world");
            assertThat(OpenString.removePrefix(null, "hello")).isNull();
        }

        @Test
        @DisplayName("removeSuffix should remove suffix")
        void removeSuffixShouldRemoveSuffix() {
            assertThat(OpenString.removeSuffix("helloworld", "world")).isEqualTo("hello");
            assertThat(OpenString.removeSuffix("hello", "world")).isEqualTo("hello");
            assertThat(OpenString.removeSuffix(null, "world")).isNull();
        }

        @Test
        @DisplayName("commonPrefix should find common prefix")
        void commonPrefixShouldFindCommonPrefix() {
            assertThat(OpenString.commonPrefix("hello", "help", "helicopter")).isEqualTo("hel");
            assertThat(OpenString.commonPrefix("abc", "def")).isEmpty();
            assertThat(OpenString.commonPrefix()).isEmpty();
        }

        @Test
        @DisplayName("commonSuffix should find common suffix")
        void commonSuffixShouldFindCommonSuffix() {
            assertThat(OpenString.commonSuffix("testing", "running", "jumping")).isEqualTo("ing");
            assertThat(OpenString.commonSuffix("abc", "def")).isEmpty();
            assertThat(OpenString.commonSuffix()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("findAll should find all occurrences")
        void findAllShouldFindAllOccurrences() {
            assertThat(OpenString.findAll("abcabc", "bc")).containsExactly(1, 4);
            assertThat(OpenString.findAll("hello", "x")).isEmpty();
            assertThat(OpenString.findAll(null, "a")).isEmpty();
        }

        @Test
        @DisplayName("indexOfNth should find nth occurrence")
        void indexOfNthShouldFindNthOccurrence() {
            assertThat(OpenString.indexOfNth("abcabc", "bc", 1)).isEqualTo(1);
            assertThat(OpenString.indexOfNth("abcabc", "bc", 2)).isEqualTo(4);
            assertThat(OpenString.indexOfNth("abcabc", "bc", 3)).isEqualTo(-1);
            assertThat(OpenString.indexOfNth(null, "a", 1)).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastIndexOfNth should find nth occurrence from end")
        void lastIndexOfNthShouldFindNthOccurrenceFromEnd() {
            assertThat(OpenString.lastIndexOfNth("abcabc", "bc", 1)).isEqualTo(4);
            assertThat(OpenString.lastIndexOfNth("abcabc", "bc", 2)).isEqualTo(1);
            assertThat(OpenString.lastIndexOfNth(null, "a", 1)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Substring Extraction Tests")
    class SubstringExtractionTests {

        @Test
        @DisplayName("substringBefore should extract before separator")
        void substringBeforeShouldExtractBeforeSeparator() {
            assertThat(OpenString.substringBefore("hello-world", "-")).isEqualTo("hello");
            assertThat(OpenString.substringBefore("hello", "-")).isEqualTo("hello");
            assertThat(OpenString.substringBefore(null, "-")).isNull();
        }

        @Test
        @DisplayName("substringAfter should extract after separator")
        void substringAfterShouldExtractAfterSeparator() {
            assertThat(OpenString.substringAfter("hello-world", "-")).isEqualTo("world");
            assertThat(OpenString.substringAfter("hello", "-")).isEmpty();
            assertThat(OpenString.substringAfter(null, "-")).isEmpty();
        }

        @Test
        @DisplayName("substringBeforeLast should extract before last separator")
        void substringBeforeLastShouldExtractBeforeLastSeparator() {
            assertThat(OpenString.substringBeforeLast("a-b-c", "-")).isEqualTo("a-b");
            assertThat(OpenString.substringBeforeLast("hello", "-")).isEqualTo("hello");
        }

        @Test
        @DisplayName("substringAfterLast should extract after last separator")
        void substringAfterLastShouldExtractAfterLastSeparator() {
            assertThat(OpenString.substringAfterLast("a-b-c", "-")).isEqualTo("c");
            assertThat(OpenString.substringAfterLast("hello", "-")).isEmpty();
        }

        @Test
        @DisplayName("substringBetween should extract between markers")
        void substringBetweenShouldExtractBetweenMarkers() {
            assertThat(OpenString.substringBetween("[hello]", "[", "]")).isEqualTo("hello");
            assertThat(OpenString.substringBetween("hello", "[", "]")).isNull();
            assertThat(OpenString.substringBetween(null, "[", "]")).isNull();
        }

        @Test
        @DisplayName("substringsBetween should extract all between markers")
        void substringsBetweenShouldExtractAllBetweenMarkers() {
            assertThat(OpenString.substringsBetween("[a][b][c]", "[", "]"))
                .containsExactly("a", "b", "c");
            assertThat(OpenString.substringsBetween("hello", "[", "]")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Wrap/Unwrap Tests")
    class WrapUnwrapTests {

        @Test
        @DisplayName("wrap char should wrap with character")
        void wrapCharShouldWrapWithCharacter() {
            assertThat(OpenString.wrap("hello", '"')).isEqualTo("\"hello\"");
            assertThat(OpenString.wrap(null, '"')).isNull();
        }

        @Test
        @DisplayName("wrap string should wrap with string")
        void wrapStringShouldWrapWithString() {
            assertThat(OpenString.wrap("hello", "**")).isEqualTo("**hello**");
            assertThat(OpenString.wrap(null, "**")).isNull();
        }

        @Test
        @DisplayName("unwrap char should unwrap character")
        void unwrapCharShouldUnwrapCharacter() {
            assertThat(OpenString.unwrap("\"hello\"", '"')).isEqualTo("hello");
            assertThat(OpenString.unwrap("hello", '"')).isEqualTo("hello");
            assertThat(OpenString.unwrap("x", '"')).isEqualTo("x");
        }

        @Test
        @DisplayName("unwrap string should unwrap string")
        void unwrapStringShouldUnwrapString() {
            assertThat(OpenString.unwrap("**hello**", "**")).isEqualTo("hello");
            assertThat(OpenString.unwrap("hello", "**")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Enhanced Cleaning Tests")
    class EnhancedCleaningTests {

        @Test
        @DisplayName("chomp should remove trailing newline")
        void chompShouldRemoveTrailingNewline() {
            assertThat(OpenString.chomp("hello\n")).isEqualTo("hello");
            assertThat(OpenString.chomp("hello\r\n")).isEqualTo("hello");
            assertThat(OpenString.chomp("hello\r")).isEqualTo("hello");
            assertThat(OpenString.chomp("hello")).isEqualTo("hello");
            assertThat(OpenString.chomp("")).isEmpty();
            assertThat(OpenString.chomp(null)).isNull();
        }

        @Test
        @DisplayName("chop should remove last character")
        void chopShouldRemoveLastCharacter() {
            assertThat(OpenString.chop("hello")).isEqualTo("hell");
            assertThat(OpenString.chop("")).isEmpty();
            assertThat(OpenString.chop(null)).isNull();
        }

        @Test
        @DisplayName("stripAccents should remove accents")
        void stripAccentsShouldRemoveAccents() {
            assertThat(OpenString.stripAccents("café")).isEqualTo("cafe");
            assertThat(OpenString.stripAccents("résumé")).isEqualTo("resume");
            assertThat(OpenString.stripAccents(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Rotation Tests")
    class RotationTests {

        @Test
        @DisplayName("rotate should rotate string")
        void rotateShouldRotateString() {
            assertThat(OpenString.rotate("hello", 2)).isEqualTo("lohel");
            assertThat(OpenString.rotate("hello", -2)).isEqualTo("llohe");
            assertThat(OpenString.rotate("hello", 5)).isEqualTo("hello");
            assertThat(OpenString.rotate("", 2)).isEmpty();
            assertThat(OpenString.rotate(null, 2)).isNull();
        }
    }

    @Nested
    @DisplayName("Difference Tests")
    class DifferenceTests {

        @Test
        @DisplayName("difference should return differing suffix")
        void differenceShouldReturnDifferingSuffix() {
            assertThat(OpenString.difference("hello", "help")).isEqualTo("p");
            assertThat(OpenString.difference("abc", "abc")).isEmpty();
            assertThat(OpenString.difference(null, "abc")).isEqualTo("abc");
        }

        @Test
        @DisplayName("indexOfDifference should return first differing index")
        void indexOfDifferenceShouldReturnFirstDifferingIndex() {
            assertThat(OpenString.indexOfDifference("hello", "help")).isEqualTo(3);
            assertThat(OpenString.indexOfDifference("abc", "abc")).isEqualTo(-1);
            assertThat(OpenString.indexOfDifference("abc", "abcd")).isEqualTo(3);
            assertThat(OpenString.indexOfDifference(null, "abc")).isZero();
        }
    }

    @Nested
    @DisplayName("Repetition Detection Tests")
    class RepetitionDetectionTests {

        @Test
        @DisplayName("isRepeated should detect repeated pattern")
        void isRepeatedShouldDetectRepeatedPattern() {
            assertThat(OpenString.isRepeated("abcabc")).isTrue();
            assertThat(OpenString.isRepeated("aaa")).isTrue();
            assertThat(OpenString.isRepeated("hello")).isFalse();
            assertThat(OpenString.isRepeated("")).isFalse();
            assertThat(OpenString.isRepeated(null)).isFalse();
        }

        @Test
        @DisplayName("getRepeatedPattern should return the repeated pattern")
        void getRepeatedPatternShouldReturnTheRepeatedPattern() {
            assertThat(OpenString.getRepeatedPattern("abcabc")).isEqualTo("abc");
            assertThat(OpenString.getRepeatedPattern("aaa")).isEqualTo("a");
            assertThat(OpenString.getRepeatedPattern("hello")).isNull();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("defaultIfBlank should return default for blank string")
        void defaultIfBlankShouldReturnDefaultForBlankString() {
            assertThat(OpenString.defaultIfBlank(null, "default")).isEqualTo("default");
            assertThat(OpenString.defaultIfBlank("", "default")).isEqualTo("default");
            assertThat(OpenString.defaultIfBlank("  ", "default")).isEqualTo("default");
            assertThat(OpenString.defaultIfBlank("value", "default")).isEqualTo("value");
        }

        @Test
        @DisplayName("defaultIfEmpty should return default for empty string")
        void defaultIfEmptyShouldReturnDefaultForEmptyString() {
            assertThat(OpenString.defaultIfEmpty(null, "default")).isEqualTo("default");
            assertThat(OpenString.defaultIfEmpty("", "default")).isEqualTo("default");
            assertThat(OpenString.defaultIfEmpty("  ", "default")).isEqualTo("  ");
            assertThat(OpenString.defaultIfEmpty("value", "default")).isEqualTo("value");
        }

        @Test
        @DisplayName("firstNonBlank should return first non-blank string")
        void firstNonBlankShouldReturnFirstNonBlankString() {
            assertThat(OpenString.firstNonBlank(null, "", "  ", "value")).isEqualTo("value");
            assertThat(OpenString.firstNonBlank("first", "second")).isEqualTo("first");
            assertThat(OpenString.firstNonBlank(null, null)).isNull();
            assertThat(OpenString.firstNonBlank((String[]) null)).isNull();
        }

        @Test
        @DisplayName("firstNonEmpty should return first non-empty string")
        void firstNonEmptyShouldReturnFirstNonEmptyString() {
            assertThat(OpenString.firstNonEmpty(null, "", "  ", "value")).isEqualTo("  ");
            assertThat(OpenString.firstNonEmpty(null, "", "value")).isEqualTo("value");
            assertThat(OpenString.firstNonEmpty((String[]) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Class should be final")
        void classShouldBeFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(OpenString.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Constructor should throw exception")
        void constructorShouldThrowException() throws Exception {
            var constructor = OpenString.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}

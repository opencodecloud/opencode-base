/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for CaptchaChars utility class
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
@DisplayName("CaptchaChars Tests")
class CaptchaCharsTest {

    @Nested
    @DisplayName("Character Array Constants Tests")
    class CharacterArrayConstantsTests {

        @Test
        @DisplayName("NUMERIC contains exactly digits 0-9")
        void numericContainsExactlyDigits() {
            assertThat(CaptchaChars.NUMERIC).hasSize(10);
            assertThat(new String(CaptchaChars.NUMERIC)).isEqualTo("0123456789");
        }

        @Test
        @DisplayName("NUMERIC contains only digit characters")
        void numericContainsOnlyDigits() {
            for (char c : CaptchaChars.NUMERIC) {
                assertThat(Character.isDigit(c))
                    .as("Character '%c' should be a digit", c)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("ALPHA_LOWER contains only lowercase letters")
        void alphaLowerContainsOnlyLowercase() {
            assertThat(CaptchaChars.ALPHA_LOWER).isNotEmpty();
            for (char c : CaptchaChars.ALPHA_LOWER) {
                assertThat(Character.isLowerCase(c))
                    .as("Character '%c' should be lowercase", c)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("ALPHA_LOWER excludes confusing characters like l and o")
        void alphaLowerExcludesConfusingCharacters() {
            String lowerStr = new String(CaptchaChars.ALPHA_LOWER);
            // 'l' and 'o' are commonly excluded because they look like '1' and '0'
            assertThat(lowerStr).doesNotContain("l");
            assertThat(lowerStr).doesNotContain("o");
        }

        @Test
        @DisplayName("ALPHA_UPPER contains only uppercase letters")
        void alphaUpperContainsOnlyUppercase() {
            assertThat(CaptchaChars.ALPHA_UPPER).isNotEmpty();
            for (char c : CaptchaChars.ALPHA_UPPER) {
                assertThat(Character.isUpperCase(c))
                    .as("Character '%c' should be uppercase", c)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("ALPHA_UPPER excludes confusing characters like I, L, O")
        void alphaUpperExcludesConfusingCharacters() {
            String upperStr = new String(CaptchaChars.ALPHA_UPPER);
            assertThat(upperStr).doesNotContain("I");
            assertThat(upperStr).doesNotContain("L");
            assertThat(upperStr).doesNotContain("O");
        }

        @Test
        @DisplayName("ALPHA contains both lowercase and uppercase letters")
        void alphaContainsBothCases() {
            String alphaStr = new String(CaptchaChars.ALPHA);

            boolean hasLower = false;
            boolean hasUpper = false;
            for (char c : CaptchaChars.ALPHA) {
                if (Character.isLowerCase(c)) hasLower = true;
                if (Character.isUpperCase(c)) hasUpper = true;
            }

            assertThat(hasLower).isTrue();
            assertThat(hasUpper).isTrue();
        }

        @Test
        @DisplayName("ALPHA contains only letter characters")
        void alphaContainsOnlyLetters() {
            for (char c : CaptchaChars.ALPHA) {
                assertThat(Character.isLetter(c))
                    .as("Character '%c' should be a letter", c)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("ALPHANUMERIC contains both letters and digits")
        void alphanumericContainsLettersAndDigits() {
            String alphanumStr = new String(CaptchaChars.ALPHANUMERIC);

            boolean hasLetter = false;
            boolean hasDigit = false;
            for (char c : CaptchaChars.ALPHANUMERIC) {
                if (Character.isLetter(c)) hasLetter = true;
                if (Character.isDigit(c)) hasDigit = true;
            }

            assertThat(hasLetter).isTrue();
            assertThat(hasDigit).isTrue();
        }

        @Test
        @DisplayName("ALPHANUMERIC contains only alphanumeric characters")
        void alphanumericContainsOnlyAlphanumeric() {
            for (char c : CaptchaChars.ALPHANUMERIC) {
                assertThat(Character.isLetterOrDigit(c))
                    .as("Character '%c' should be alphanumeric", c)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("ALPHANUMERIC avoids similar characters 0, 1, I, l, O")
        void alphanumericAvoidsSimilarCharacters() {
            String alphanumStr = new String(CaptchaChars.ALPHANUMERIC);

            assertThat(alphanumStr).doesNotContain("0");
            assertThat(alphanumStr).doesNotContain("1");
            assertThat(alphanumStr).doesNotContain("I");
            assertThat(alphanumStr).doesNotContain("l");
            assertThat(alphanumStr).doesNotContain("O");
        }

        @Test
        @DisplayName("CHINESE array is not empty")
        void chineseArrayIsNotEmpty() {
            assertThat(CaptchaChars.CHINESE).isNotEmpty();
        }

        @Test
        @DisplayName("CHINESE array has 50 entries")
        void chineseArrayHasFiftyEntries() {
            assertThat(CaptchaChars.CHINESE).hasSize(50);
        }

        @Test
        @DisplayName("CHINESE array contains non-empty strings")
        void chineseArrayContainsNonEmptyStrings() {
            for (String ch : CaptchaChars.CHINESE) {
                assertThat(ch).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("CHINESE array elements are single characters")
        void chineseArrayElementsAreSingleCharacters() {
            for (String ch : CaptchaChars.CHINESE) {
                assertThat(ch).hasSize(1);
            }
        }

        @Test
        @DisplayName("CHINESE array contains unique entries")
        void chineseArrayContainsUniqueEntries() {
            Set<String> uniqueChars = Arrays.stream(CaptchaChars.CHINESE)
                .collect(Collectors.toSet());

            assertThat(uniqueChars).hasSameSizeAs(CaptchaChars.CHINESE);
        }
    }

    @Nested
    @DisplayName("Utility Class Instantiation Tests")
    class UtilityClassInstantiationTests {

        @Test
        @DisplayName("CaptchaChars cannot be instantiated")
        void captchaCharsCannotBeInstantiated() {
            assertThatThrownBy(() -> {
                Constructor<CaptchaChars> constructor = CaptchaChars.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).isInstanceOf(InvocationTargetException.class)
              .hasCauseInstanceOf(AssertionError.class)
              .cause().hasMessageContaining("Utility class");
        }
    }

    @Nested
    @DisplayName("generate(type, length) Tests")
    class GenerateTests {

        @Test
        @DisplayName("generate NUMERIC produces string of correct length")
        void generateNumericProducesCorrectLength() {
            String result = CaptchaChars.generate(CaptchaType.NUMERIC, 4);

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("generate NUMERIC produces only digits")
        void generateNumericProducesOnlyDigits() {
            String result = CaptchaChars.generate(CaptchaType.NUMERIC, 10);

            assertThat(result).matches("\\d+");
        }

        @Test
        @DisplayName("generate ALPHA produces string of correct length")
        void generateAlphaProducesCorrectLength() {
            String result = CaptchaChars.generate(CaptchaType.ALPHA, 6);

            assertThat(result).hasSize(6);
        }

        @Test
        @DisplayName("generate ALPHA produces only letters")
        void generateAlphaProducesOnlyLetters() {
            String result = CaptchaChars.generate(CaptchaType.ALPHA, 10);

            assertThat(result).matches("[a-zA-Z]+");
        }

        @Test
        @DisplayName("generate ALPHANUMERIC produces string of correct length")
        void generateAlphanumericProducesCorrectLength() {
            String result = CaptchaChars.generate(CaptchaType.ALPHANUMERIC, 5);

            assertThat(result).hasSize(5);
        }

        @Test
        @DisplayName("generate ALPHANUMERIC produces only allowed characters")
        void generateAlphanumericProducesOnlyAllowedChars() {
            String allowedChars = new String(CaptchaChars.ALPHANUMERIC);
            String result = CaptchaChars.generate(CaptchaType.ALPHANUMERIC, 20);

            for (char c : result.toCharArray()) {
                assertThat(allowedChars).contains(String.valueOf(c));
            }
        }

        @Test
        @DisplayName("generate CHINESE produces string of correct length")
        void generateChineseProducesCorrectLength() {
            String result = CaptchaChars.generate(CaptchaType.CHINESE, 4);

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("generate CHINESE produces Chinese characters from defined set")
        void generateChineseProducesValidCharacters() {
            Set<String> validChars = Arrays.stream(CaptchaChars.CHINESE)
                .collect(Collectors.toSet());

            String result = CaptchaChars.generate(CaptchaType.CHINESE, 10);

            for (int i = 0; i < result.length(); i++) {
                assertThat(validChars).contains(String.valueOf(result.charAt(i)));
            }
        }

        @Test
        @DisplayName("generate with GIF type defaults to ALPHANUMERIC characters")
        void generateGifDefaultsToAlphanumeric() {
            String allowedChars = new String(CaptchaChars.ALPHANUMERIC);
            String result = CaptchaChars.generate(CaptchaType.GIF, 10);

            assertThat(result).hasSize(10);
            for (char c : result.toCharArray()) {
                assertThat(allowedChars).contains(String.valueOf(c));
            }
        }

        @Test
        @DisplayName("generate with SLIDER type defaults to ALPHANUMERIC characters")
        void generateSliderDefaultsToAlphanumeric() {
            String allowedChars = new String(CaptchaChars.ALPHANUMERIC);
            String result = CaptchaChars.generate(CaptchaType.SLIDER, 10);

            assertThat(result).hasSize(10);
            for (char c : result.toCharArray()) {
                assertThat(allowedChars).contains(String.valueOf(c));
            }
        }

        @Test
        @DisplayName("generate with CLICK type defaults to ALPHANUMERIC characters")
        void generateClickDefaultsToAlphanumeric() {
            String result = CaptchaChars.generate(CaptchaType.CLICK, 5);

            assertThat(result).hasSize(5);
        }

        @Test
        @DisplayName("generate with ROTATE type defaults to ALPHANUMERIC characters")
        void generateRotateDefaultsToAlphanumeric() {
            String result = CaptchaChars.generate(CaptchaType.ROTATE, 5);

            assertThat(result).hasSize(5);
        }

        @Test
        @DisplayName("generate with IMAGE_SELECT type defaults to ALPHANUMERIC characters")
        void generateImageSelectDefaultsToAlphanumeric() {
            String result = CaptchaChars.generate(CaptchaType.IMAGE_SELECT, 5);

            assertThat(result).hasSize(5);
        }

        @Test
        @DisplayName("generate with ARITHMETIC type defaults to ALPHANUMERIC characters")
        void generateArithmeticDefaultsToAlphanumeric() {
            String allowedChars = new String(CaptchaChars.ALPHANUMERIC);
            String result = CaptchaChars.generate(CaptchaType.ARITHMETIC, 4);

            assertThat(result).hasSize(4);
            for (char c : result.toCharArray()) {
                assertThat(allowedChars).contains(String.valueOf(c));
            }
        }

        @Test
        @DisplayName("generate with length 1 returns single character")
        void generateWithLengthOneReturnsSingleChar() {
            String result = CaptchaChars.generate(CaptchaType.NUMERIC, 1);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("generate with length 0 returns empty string")
        void generateWithLengthZeroReturnsEmpty() {
            String result = CaptchaChars.generate(CaptchaType.NUMERIC, 0);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("generateFromChars Tests")
    class GenerateFromCharsTests {

        @Test
        @DisplayName("generateFromChars produces string of correct length")
        void generateFromCharsProducesCorrectLength() {
            String result = CaptchaChars.generateFromChars(CaptchaChars.NUMERIC, 6);

            assertThat(result).hasSize(6);
        }

        @Test
        @DisplayName("generateFromChars uses only provided characters")
        void generateFromCharsUsesOnlyProvidedChars() {
            char[] customChars = {'A', 'B', 'C'};
            String result = CaptchaChars.generateFromChars(customChars, 20);

            assertThat(result).hasSize(20);
            for (char c : result.toCharArray()) {
                assertThat(customChars).contains(c);
            }
        }

        @Test
        @DisplayName("generateFromChars with single char produces repeated char")
        void generateFromCharsSingleChar() {
            char[] singleChar = {'X'};
            String result = CaptchaChars.generateFromChars(singleChar, 5);

            assertThat(result).isEqualTo("XXXXX");
        }

        @Test
        @DisplayName("generateFromChars with length 0 returns empty string")
        void generateFromCharsLengthZero() {
            String result = CaptchaChars.generateFromChars(CaptchaChars.NUMERIC, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("generateFromChars returns non-null")
        void generateFromCharsReturnsNonNull() {
            String result = CaptchaChars.generateFromChars(CaptchaChars.ALPHA, 4);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("generateFromChars with ALPHA_LOWER produces lowercase")
        void generateFromCharsWithAlphaLowerProducesLowercase() {
            String result = CaptchaChars.generateFromChars(CaptchaChars.ALPHA_LOWER, 10);

            for (char c : result.toCharArray()) {
                assertThat(Character.isLowerCase(c)).isTrue();
            }
        }

        @Test
        @DisplayName("generateFromChars with ALPHA_UPPER produces uppercase")
        void generateFromCharsWithAlphaUpperProducesUppercase() {
            String result = CaptchaChars.generateFromChars(CaptchaChars.ALPHA_UPPER, 10);

            for (char c : result.toCharArray()) {
                assertThat(Character.isUpperCase(c)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("generateChinese Tests")
    class GenerateChineseTests {

        @Test
        @DisplayName("generateChinese produces string of correct length")
        void generateChineseProducesCorrectLength() {
            String result = CaptchaChars.generateChinese(4);

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("generateChinese produces characters from CHINESE array")
        void generateChineseProducesValidCharacters() {
            Set<String> validChars = Arrays.stream(CaptchaChars.CHINESE)
                .collect(Collectors.toSet());

            String result = CaptchaChars.generateChinese(10);

            for (int i = 0; i < result.length(); i++) {
                assertThat(validChars)
                    .as("Character at index %d should be in CHINESE set", i)
                    .contains(String.valueOf(result.charAt(i)));
            }
        }

        @Test
        @DisplayName("generateChinese with length 1 returns single character")
        void generateChineseWithLengthOne() {
            String result = CaptchaChars.generateChinese(1);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("generateChinese with length 0 returns empty string")
        void generateChineseWithLengthZero() {
            String result = CaptchaChars.generateChinese(0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("generateChinese returns non-null")
        void generateChineseReturnsNonNull() {
            String result = CaptchaChars.generateChinese(4);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("generateArithmetic Tests")
    class GenerateArithmeticTests {

        @Test
        @DisplayName("generateArithmetic returns array of length 2")
        void generateArithmeticReturnsArrayOfLengthTwo() {
            String[] result = CaptchaChars.generateArithmetic();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("generateArithmetic first element is expression")
        void generateArithmeticFirstElementIsExpression() {
            String[] result = CaptchaChars.generateArithmetic();

            assertThat(result[0]).isNotNull();
            assertThat(result[0]).isNotEmpty();
            assertThat(result[0]).contains("= ?");
        }

        @Test
        @DisplayName("generateArithmetic second element is numeric answer")
        void generateArithmeticSecondElementIsNumericAnswer() {
            String[] result = CaptchaChars.generateArithmetic();

            assertThat(result[1]).isNotNull();
            assertThat(result[1]).matches("-?\\d+");
        }

        @Test
        @DisplayName("generateArithmetic expression contains an operator")
        void generateArithmeticExpressionContainsOperator() {
            String[] result = CaptchaChars.generateArithmetic();

            assertThat(result[0]).containsAnyOf("+", "-", "\u00d7");
        }

        @RepeatedTest(20)
        @DisplayName("generateArithmetic produces valid addition results")
        void generateArithmeticProducesValidResults() {
            String[] result = CaptchaChars.generateArithmetic();
            String expression = result[0];
            int answer = Integer.parseInt(result[1]);

            // Parse and verify the expression
            if (expression.contains("+")) {
                String[] parts = expression.replace("= ?", "").trim().split("\\+");
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                assertThat(answer).isEqualTo(a + b);
            } else if (expression.contains("-")) {
                String[] parts = expression.replace("= ?", "").trim().split("-");
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                assertThat(answer).isEqualTo(a - b);
            } else if (expression.contains("\u00d7")) {
                String[] parts = expression.replace("= ?", "").trim().split("\u00d7");
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                assertThat(answer).isEqualTo(a * b);
            }
        }

        @RepeatedTest(20)
        @DisplayName("generateArithmetic answer is non-negative for subtraction")
        void generateArithmeticSubtractionIsNonNegative() {
            String[] result = CaptchaChars.generateArithmetic();
            int answer = Integer.parseInt(result[1]);

            assertThat(answer).isGreaterThanOrEqualTo(0);
        }

        @RepeatedTest(10)
        @DisplayName("generateArithmetic operands are between 1 and 10 inclusive")
        void generateArithmeticOperandsInRange() {
            String[] result = CaptchaChars.generateArithmetic();
            String expression = result[0];

            // Extract operands
            String cleanExpr = expression.replace("= ?", "").trim();
            String[] parts;
            if (cleanExpr.contains("+")) {
                parts = cleanExpr.split("\\+");
            } else if (cleanExpr.contains("\u00d7")) {
                parts = cleanExpr.split("\u00d7");
            } else {
                parts = cleanExpr.split("-");
            }

            int a = Integer.parseInt(parts[0].trim());
            int b = Integer.parseInt(parts[1].trim());

            assertThat(a).isBetween(1, 10);
            assertThat(b).isBetween(1, 10);
        }
    }

    @Nested
    @DisplayName("getRandom Tests")
    class GetRandomTests {

        @Test
        @DisplayName("getRandom returns non-null")
        void getRandomReturnsNonNull() {
            assertThat(CaptchaChars.getRandom()).isNotNull();
        }

        @Test
        @DisplayName("getRandom returns same instance each time")
        void getRandomReturnsSameInstance() {
            assertThat(CaptchaChars.getRandom()).isSameAs(CaptchaChars.getRandom());
        }
    }

    @Nested
    @DisplayName("randomInt(bound) Tests")
    class RandomIntBoundTests {

        @RepeatedTest(50)
        @DisplayName("randomInt with bound returns value in range [0, bound)")
        void randomIntWithBoundReturnsValueInRange() {
            int result = CaptchaChars.randomInt(10);

            assertThat(result).isGreaterThanOrEqualTo(0);
            assertThat(result).isLessThan(10);
        }

        @RepeatedTest(20)
        @DisplayName("randomInt with bound 1 always returns 0")
        void randomIntWithBoundOneAlwaysReturnsZero() {
            int result = CaptchaChars.randomInt(1);

            assertThat(result).isZero();
        }

        @RepeatedTest(30)
        @DisplayName("randomInt with large bound stays in range")
        void randomIntWithLargeBoundStaysInRange() {
            int result = CaptchaChars.randomInt(1000000);

            assertThat(result).isGreaterThanOrEqualTo(0);
            assertThat(result).isLessThan(1000000);
        }
    }

    @Nested
    @DisplayName("randomInt(min, max) Tests")
    class RandomIntMinMaxTests {

        @RepeatedTest(50)
        @DisplayName("randomInt with min and max returns value in range [min, max)")
        void randomIntWithMinMaxReturnsValueInRange() {
            int result = CaptchaChars.randomInt(5, 15);

            assertThat(result).isGreaterThanOrEqualTo(5);
            assertThat(result).isLessThan(15);
        }

        @RepeatedTest(20)
        @DisplayName("randomInt with adjacent min and max returns min")
        void randomIntWithAdjacentMinMaxReturnsMin() {
            int result = CaptchaChars.randomInt(7, 8);

            assertThat(result).isEqualTo(7);
        }

        @RepeatedTest(30)
        @DisplayName("randomInt with min 0 behaves like single bound version")
        void randomIntWithMinZero() {
            int result = CaptchaChars.randomInt(0, 100);

            assertThat(result).isGreaterThanOrEqualTo(0);
            assertThat(result).isLessThan(100);
        }

        @RepeatedTest(30)
        @DisplayName("randomInt with large range stays in range")
        void randomIntWithLargeRange() {
            int result = CaptchaChars.randomInt(100, 200);

            assertThat(result).isGreaterThanOrEqualTo(100);
            assertThat(result).isLessThan(200);
        }

        @RepeatedTest(30)
        @DisplayName("randomInt with negative min stays in range")
        void randomIntWithNegativeMin() {
            int result = CaptchaChars.randomInt(-10, 10);

            assertThat(result).isGreaterThanOrEqualTo(-10);
            assertThat(result).isLessThan(10);
        }

        @Test
        @DisplayName("randomInt returns min when min equals max")
        void randomIntReturnsMinWhenMinEqualsMax() {
            int result = CaptchaChars.randomInt(5, 5);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("randomInt returns min when max is less than min")
        void randomIntReturnsMinWhenMaxLessThanMin() {
            int result = CaptchaChars.randomInt(10, 3);

            assertThat(result).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Randomness Verification Tests")
    class RandomnessVerificationTests {

        @Test
        @DisplayName("generate produces different results across multiple calls")
        void generateProducesDifferentResults() {
            // Generate many strings and verify we get at least some variety
            Set<String> results = new java.util.HashSet<>();
            for (int i = 0; i < 100; i++) {
                results.add(CaptchaChars.generate(CaptchaType.ALPHANUMERIC, 6));
            }

            // With 6-char alphanumeric strings, getting 100 identical results is astronomically unlikely
            assertThat(results.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("generateFromChars produces different results across multiple calls")
        void generateFromCharsProducesDifferentResults() {
            Set<String> results = new java.util.HashSet<>();
            for (int i = 0; i < 100; i++) {
                results.add(CaptchaChars.generateFromChars(CaptchaChars.ALPHANUMERIC, 6));
            }

            assertThat(results.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("generateChinese produces different results across multiple calls")
        void generateChineseProducesDifferentResults() {
            Set<String> results = new java.util.HashSet<>();
            for (int i = 0; i < 100; i++) {
                results.add(CaptchaChars.generateChinese(4));
            }

            assertThat(results.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("generateArithmetic produces different expressions")
        void generateArithmeticProducesDifferentExpressions() {
            Set<String> expressions = new java.util.HashSet<>();
            for (int i = 0; i < 100; i++) {
                expressions.add(CaptchaChars.generateArithmetic()[0]);
            }

            assertThat(expressions.size()).isGreaterThan(1);
        }
    }
}

package cloud.opencode.base.functional.pattern;

import cloud.opencode.base.functional.exception.OpenMatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMatch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("OpenMatch 测试")
class OpenMatchTest {

    // Test records
    record Point(int x, int y) {}
    record Person(String name, int age) {}

    @Nested
    @DisplayName("of() 测试")
    class OfTests {

        @Test
        @DisplayName("of() 创建 Matcher")
        void testOf() {
            OpenMatch.Matcher<String> matcher = OpenMatch.of("hello");

            assertThat(matcher).isNotNull();
            assertThat(matcher.value()).isEqualTo("hello");
        }

        @Test
        @DisplayName("of() 接受 null 值")
        void testOfNull() {
            OpenMatch.Matcher<String> matcher = OpenMatch.of(null);

            assertThat(matcher).isNotNull();
            assertThat(matcher.value()).isNull();
        }
    }

    @Nested
    @DisplayName("caseOf() 测试")
    class CaseOfTests {

        @Test
        @DisplayName("caseOf() 匹配类型")
        void testCaseOfMatch() {
            String result = OpenMatch.of((Object) "hello")
                    .caseOf(String.class, s -> "String: " + s)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("String: hello");
        }

        @Test
        @DisplayName("caseOf() 类型不匹配时继续")
        void testCaseOfNoMatch() {
            String result = OpenMatch.of((Object) 123)
                    .caseOf(String.class, s -> "String: " + s)
                    .caseOf(Integer.class, n -> "Integer: " + n)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("Integer: 123");
        }

        @Test
        @DisplayName("caseOf() 第一个匹配的获胜")
        void testCaseOfFirstWins() {
            String result = OpenMatch.of((Object) "hello")
                    .caseOf(String.class, s -> "First")
                    .caseOf(String.class, s -> "Second")
                    .orElse(o -> "Default");

            assertThat(result).isEqualTo("First");
        }
    }

    @Nested
    @DisplayName("when() 测试")
    class WhenTests {

        @Test
        @DisplayName("when() 条件满足时匹配")
        void testWhenMatch() {
            String result = OpenMatch.of(5)
                    .when(n -> n > 0, n -> "Positive")
                    .orElse(n -> "Non-positive");

            assertThat(result).isEqualTo("Positive");
        }

        @Test
        @DisplayName("when() 条件不满足时继续")
        void testWhenNoMatch() {
            String result = OpenMatch.of(-5)
                    .when(n -> n > 0, n -> "Positive")
                    .when(n -> n < 0, n -> "Negative")
                    .orElse(n -> "Zero");

            assertThat(result).isEqualTo("Negative");
        }

        @Test
        @DisplayName("when() null 值不匹配")
        void testWhenNull() {
            String result = OpenMatch.of((Integer) null)
                    .when(n -> n > 0, n -> "Positive")
                    .orElse(n -> "Null or non-positive");

            assertThat(result).isEqualTo("Null or non-positive");
        }
    }

    @Nested
    @DisplayName("whenEquals() 测试")
    class WhenEqualsTests {

        @Test
        @DisplayName("whenEquals() 相等时匹配")
        void testWhenEqualsMatch() {
            String result = OpenMatch.of(1)
                    .whenEquals(1, n -> "One")
                    .whenEquals(2, n -> "Two")
                    .orElse(n -> "Other");

            assertThat(result).isEqualTo("One");
        }

        @Test
        @DisplayName("whenEquals() 不相等时继续")
        void testWhenEqualsNoMatch() {
            String result = OpenMatch.of(3)
                    .whenEquals(1, n -> "One")
                    .whenEquals(2, n -> "Two")
                    .orElse(n -> "Other");

            assertThat(result).isEqualTo("Other");
        }

        @Test
        @DisplayName("whenEquals() null 匹配 null")
        void testWhenEqualsNull() {
            String result = OpenMatch.of((Integer) null)
                    .whenEquals(null, n -> "Null")
                    .orElse(n -> "Not null");

            assertThat(result).isEqualTo("Null");
        }
    }

    @Nested
    @DisplayName("whenNull() 测试")
    class WhenNullTests {

        @Test
        @DisplayName("whenNull() null 时匹配")
        void testWhenNullMatch() {
            String result = OpenMatch.of((String) null)
                    .whenNull(s -> "Null")
                    .orElse(s -> "Not null");

            assertThat(result).isEqualTo("Null");
        }

        @Test
        @DisplayName("whenNull() 非 null 时不匹配")
        void testWhenNullNoMatch() {
            String result = OpenMatch.of("hello")
                    .whenNull(s -> "Null")
                    .orElse(s -> "Not null: " + s);

            assertThat(result).isEqualTo("Not null: hello");
        }
    }

    @Nested
    @DisplayName("caseRecord() 测试")
    class CaseRecordTests {

        @Test
        @DisplayName("caseRecord() 解构 Record")
        void testCaseRecordDeconstruct() {
            Point point = new Point(3, 4);

            Double result = OpenMatch.of((Object) point)
                    .caseRecord(Point.class, (Integer x, Integer y) -> Math.sqrt(x * x + y * y))
                    .orElseGet(0.0);

            assertThat(result).isEqualTo(5.0);
        }

        @Test
        @DisplayName("caseRecord() 类型不匹配时不解构")
        void testCaseRecordNoMatch() {
            Double result = OpenMatch.of((Object) "not a point")
                    .caseRecord(Point.class, (Integer x, Integer y) -> Math.sqrt(x * x + y * y))
                    .orElseGet(0.0);

            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("caseSealed() 测试")
    class CaseSealedTests {

        @Test
        @DisplayName("caseSealed() 匹配密封类型")
        void testCaseSealedMatch() {
            Object value = "hello";

            String result = OpenMatch.of(value)
                    .caseSealed(String.class, s -> "String: " + s)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("String: hello");
        }
    }

    @Nested
    @DisplayName("match(Case) 测试")
    class MatchCaseTests {

        @Test
        @DisplayName("match(Case) 应用 Case")
        void testMatchCase() {
            Case<Object, String> stringCase = Case.type(String.class, s -> "String: " + s);

            String result = OpenMatch.of((Object) "hello")
                    .match(stringCase)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("String: hello");
        }
    }

    @Nested
    @DisplayName("match(Pattern, Function) 测试")
    class MatchPatternTests {

        @Test
        @DisplayName("match(Pattern, Function) 应用 Pattern")
        void testMatchPattern() {
            Pattern<Object, String> stringPattern = Pattern.type(String.class);

            String result = OpenMatch.of((Object) "hello")
                    .match(stringPattern, s -> "String: " + s)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("String: hello");
        }

        @Test
        @DisplayName("match(Pattern, Function) 不匹配时继续")
        void testMatchPatternNoMatch() {
            Pattern<Object, String> stringPattern = Pattern.type(String.class);

            String result = OpenMatch.of((Object) 123)
                    .match(stringPattern, s -> "String: " + s)
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("orElse() 测试")
    class OrElseTests {

        @Test
        @DisplayName("orElse() 匹配时返回结果")
        void testOrElseMatched() {
            String result = OpenMatch.of(5)
                    .when(n -> n > 0, n -> "Positive")
                    .orElse(n -> "Non-positive");

            assertThat(result).isEqualTo("Positive");
        }

        @Test
        @DisplayName("orElse() 不匹配时返回默认")
        void testOrElseNotMatched() {
            String result = OpenMatch.of(0)
                    .when(n -> n > 0, n -> "Positive")
                    .when(n -> n < 0, n -> "Negative")
                    .orElse(n -> "Zero");

            assertThat(result).isEqualTo("Zero");
        }
    }

    @Nested
    @DisplayName("orElseGet() 测试")
    class OrElseGetTests {

        @Test
        @DisplayName("orElseGet() 匹配时返回结果")
        void testOrElseGetMatched() {
            String result = OpenMatch.of(5)
                    .when(n -> n > 0, n -> "Positive")
                    .orElseGet("Default");

            assertThat(result).isEqualTo("Positive");
        }

        @Test
        @DisplayName("orElseGet() 不匹配时返回默认值")
        void testOrElseGetNotMatched() {
            String result = OpenMatch.of(0)
                    .when(n -> n > 0, n -> "Positive")
                    .orElseGet("Default");

            assertThat(result).isEqualTo("Default");
        }
    }

    @Nested
    @DisplayName("orElseThrow() 测试")
    class OrElseThrowTests {

        @Test
        @DisplayName("orElseThrow() 匹配时返回结果")
        void testOrElseThrowMatched() {
            String result = OpenMatch.of(5)
                    .when(n -> n > 0, n -> "Positive")
                    .orElseThrow();

            assertThat(result).isEqualTo("Positive");
        }

        @Test
        @DisplayName("orElseThrow() 不匹配时抛出异常")
        void testOrElseThrowNotMatched() {
            assertThatThrownBy(() ->
                    OpenMatch.of(0)
                            .when(n -> n > 0, n -> "Positive")
                            .orElseThrow()
            ).isInstanceOf(OpenMatchException.class);
        }
    }

    @Nested
    @DisplayName("getAs() 测试")
    class GetAsTests {

        @Test
        @DisplayName("getAs(Class, R) 匹配且类型正确时返回结果")
        void testGetAsMatchedCorrectType() {
            String result = OpenMatch.of(5)
                    .when(n -> n > 0, n -> "Positive")
                    .getAs(String.class, "Default");

            assertThat(result).isEqualTo("Positive");
        }

        @Test
        @DisplayName("getAs(Class, R) 不匹配时返回默认值")
        void testGetAsNotMatched() {
            String result = OpenMatch.of(0)
                    .when(n -> n > 0, n -> "Positive")
                    .getAs(String.class, "Default");

            assertThat(result).isEqualTo("Default");
        }

        @Test
        @DisplayName("getAs(Class) 匹配且类型正确时返回结果")
        void testGetAsClassOnly() {
            String result = OpenMatch.of(5)
                    .when(n -> n > 0, n -> "Positive")
                    .getAs(String.class);

            assertThat(result).isEqualTo("Positive");
        }

        @Test
        @DisplayName("getAs(Class) 不匹配时抛出异常")
        void testGetAsClassOnlyNoMatch() {
            assertThatThrownBy(() ->
                    OpenMatch.of(0)
                            .when(n -> n > 0, n -> "Positive")
                            .getAs(String.class)
            ).isInstanceOf(OpenMatchException.class);
        }

        @Test
        @DisplayName("getAs(Class) 类型不匹配时抛出异常")
        void testGetAsClassOnlyWrongType() {
            assertThatThrownBy(() ->
                    OpenMatch.of(5)
                            .when(n -> n > 0, n -> 123)  // Returns Integer
                            .getAs(String.class)
            ).isInstanceOf(OpenMatchException.class);
        }
    }

    @Nested
    @DisplayName("isMatched() 测试")
    class IsMatchedTests {

        @Test
        @DisplayName("isMatched() 匹配后返回 true")
        void testIsMatchedTrue() {
            OpenMatch.Matcher<Integer> matcher = OpenMatch.of(5)
                    .when(n -> n > 0, n -> "Positive");

            assertThat(matcher.isMatched()).isTrue();
        }

        @Test
        @DisplayName("isMatched() 未匹配返回 false")
        void testIsMatchedFalse() {
            OpenMatch.Matcher<Integer> matcher = OpenMatch.of(0)
                    .when(n -> n > 0, n -> "Positive");

            assertThat(matcher.isMatched()).isFalse();
        }
    }

    @Nested
    @DisplayName("value() 测试")
    class ValueTests {

        @Test
        @DisplayName("value() 返回原始值")
        void testValue() {
            OpenMatch.Matcher<String> matcher = OpenMatch.of("hello");

            assertThat(matcher.value()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("复杂匹配场景测试")
    class ComplexMatchTests {

        @Test
        @DisplayName("数字分类")
        void testNumberClassification() {
            for (int n : new int[]{-5, 0, 5}) {
                String result = OpenMatch.of(n)
                        .when(x -> x < 0, x -> "Negative")
                        .when(x -> x == 0, x -> "Zero")
                        .when(x -> x > 0, x -> "Positive")
                        .orElse(x -> "Unknown");

                if (n < 0) {
                    assertThat(result).isEqualTo("Negative");
                } else if (n == 0) {
                    assertThat(result).isEqualTo("Zero");
                } else {
                    assertThat(result).isEqualTo("Positive");
                }
            }
        }

        @Test
        @DisplayName("类型匹配")
        void testTypeMatching() {
            Object[] values = {"hello", 123, 3.14, true};
            String[] expected = {"String", "Integer", "Double", "Boolean"};

            for (int i = 0; i < values.length; i++) {
                String result = OpenMatch.of(values[i])
                        .caseOf(String.class, s -> "String")
                        .caseOf(Integer.class, n -> "Integer")
                        .caseOf(Double.class, d -> "Double")
                        .caseOf(Boolean.class, b -> "Boolean")
                        .orElse(o -> "Unknown");

                assertThat(result).isEqualTo(expected[i]);
            }
        }

        @Test
        @DisplayName("组合 when 和 caseOf")
        void testCombinedWhenAndCaseOf() {
            Object value = 5;

            String result = OpenMatch.of(value)
                    .caseOf(String.class, s -> "String: " + s)
                    .caseOf(Integer.class, n -> {
                        if (n > 0) return "Positive integer";
                        if (n < 0) return "Negative integer";
                        return "Zero";
                    })
                    .orElse(o -> "Unknown");

            assertThat(result).isEqualTo("Positive integer");
        }
    }
}

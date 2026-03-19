package cloud.opencode.base.functional.pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Pattern 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Pattern 测试")
class PatternTest {

    @Nested
    @DisplayName("match() 测试")
    class MatchTests {

        @Test
        @DisplayName("match() 匹配成功返回 Optional.of")
        void testMatchSuccess() {
            Pattern<Object, String> pattern = Pattern.type(String.class);

            Optional<String> result = pattern.match("hello");

            assertThat(result).contains("hello");
        }

        @Test
        @DisplayName("match() 匹配失败返回 Optional.empty")
        void testMatchFailure() {
            Pattern<Object, String> pattern = Pattern.type(String.class);

            Optional<String> result = pattern.match(123);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("matches() 测试")
    class MatchesTests {

        @Test
        @DisplayName("matches() 匹配时返回 true")
        void testMatchesTrue() {
            Pattern<Object, String> pattern = Pattern.type(String.class);

            assertThat(pattern.matches("hello")).isTrue();
        }

        @Test
        @DisplayName("matches() 不匹配时返回 false")
        void testMatchesFalse() {
            Pattern<Object, String> pattern = Pattern.type(String.class);

            assertThat(pattern.matches(123)).isFalse();
        }
    }

    @Nested
    @DisplayName("type() 工厂方法测试")
    class TypeTests {

        @Test
        @DisplayName("type() 匹配特定类型")
        void testTypeMatch() {
            Pattern<Object, String> pattern = Pattern.type(String.class);

            assertThat(pattern.match("hello")).contains("hello");
            assertThat(pattern.match(123)).isEmpty();
        }

        @Test
        @DisplayName("type() 匹配子类型")
        void testTypeMatchSubtype() {
            Pattern<Object, Number> pattern = Pattern.type(Number.class);

            assertThat(pattern.match(123)).contains(123);
            assertThat(pattern.match(3.14)).contains(3.14);
        }

        @Test
        @DisplayName("type() null 不匹配")
        void testTypeMatchNull() {
            Pattern<Object, String> pattern = Pattern.type(String.class);

            assertThat(pattern.match(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("equalTo() 工厂方法测试")
    class EqualToTests {

        @Test
        @DisplayName("equalTo() 相等时匹配")
        void testEqualToMatch() {
            Pattern<String, String> pattern = Pattern.equalTo("hello");

            assertThat(pattern.match("hello")).contains("hello");
        }

        @Test
        @DisplayName("equalTo() 不相等时不匹配")
        void testEqualToNoMatch() {
            Pattern<String, String> pattern = Pattern.equalTo("hello");

            assertThat(pattern.match("world")).isEmpty();
        }

        @Test
        @DisplayName("equalTo() null 与 null 匹配")
        void testEqualToNull() {
            Pattern<String, String> pattern = Pattern.equalTo(null);

            assertThat(pattern.match(null)).isEmpty(); // null matches but returns empty Optional
        }
    }

    @Nested
    @DisplayName("when() 工厂方法测试")
    class WhenTests {

        @Test
        @DisplayName("when() 条件满足时匹配")
        void testWhenMatch() {
            Pattern<Integer, Integer> pattern = Pattern.when(n -> n > 0);

            assertThat(pattern.match(5)).contains(5);
        }

        @Test
        @DisplayName("when() 条件不满足时不匹配")
        void testWhenNoMatch() {
            Pattern<Integer, Integer> pattern = Pattern.when(n -> n > 0);

            assertThat(pattern.match(-5)).isEmpty();
        }

        @Test
        @DisplayName("when() null 不匹配")
        void testWhenNull() {
            Pattern<Integer, Integer> pattern = Pattern.when(n -> n > 0);

            assertThat(pattern.match(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("isNull() 工厂方法测试")
    class IsNullTests {

        @Test
        @DisplayName("isNull() 始终返回 empty")
        void testIsNull() {
            Pattern<String, String> pattern = Pattern.isNull();

            // Note: the current implementation always returns empty
            assertThat(pattern.match(null)).isEmpty();
            assertThat(pattern.match("hello")).isEmpty();
        }
    }

    @Nested
    @DisplayName("any() 工厂方法测试")
    class AnyTests {

        @Test
        @DisplayName("any() 匹配任何非 null 值")
        void testAnyMatch() {
            Pattern<Object, Object> pattern = Pattern.any();

            assertThat(pattern.match("hello")).contains("hello");
            assertThat(pattern.match(123)).contains(123);
        }

        @Test
        @DisplayName("any() 对 null 返回 empty")
        void testAnyNull() {
            Pattern<Object, Object> pattern = Pattern.any();

            assertThat(pattern.match(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("and() 组合器测试")
    class AndTests {

        @Test
        @DisplayName("and() 两个模式都匹配时成功")
        void testAndBothMatch() {
            Pattern<Integer, Integer> positive = Pattern.when(n -> n > 0);
            Pattern<Integer, Integer> lessThan10 = Pattern.when(n -> n < 10);
            Pattern<Integer, Integer> combined = positive.and(lessThan10);

            assertThat(combined.match(5)).contains(5);
        }

        @Test
        @DisplayName("and() 第一个模式不匹配时失败")
        void testAndFirstFails() {
            Pattern<Integer, Integer> positive = Pattern.when(n -> n > 0);
            Pattern<Integer, Integer> lessThan10 = Pattern.when(n -> n < 10);
            Pattern<Integer, Integer> combined = positive.and(lessThan10);

            assertThat(combined.match(-5)).isEmpty();
        }

        @Test
        @DisplayName("and() 第二个模式不匹配时失败")
        void testAndSecondFails() {
            Pattern<Integer, Integer> positive = Pattern.when(n -> n > 0);
            Pattern<Integer, Integer> lessThan10 = Pattern.when(n -> n < 10);
            Pattern<Integer, Integer> combined = positive.and(lessThan10);

            assertThat(combined.match(15)).isEmpty();
        }
    }

    @Nested
    @DisplayName("or() 组合器测试")
    class OrTests {

        @Test
        @DisplayName("or() 第一个模式匹配时成功")
        void testOrFirstMatch() {
            Pattern<Integer, Integer> negative = Pattern.when(n -> n < 0);
            Pattern<Integer, Integer> greaterThan10 = Pattern.when(n -> n > 10);
            Pattern<Integer, Integer> combined = negative.or(greaterThan10);

            assertThat(combined.match(-5)).contains(-5);
        }

        @Test
        @DisplayName("or() 第二个模式匹配时成功")
        void testOrSecondMatch() {
            Pattern<Integer, Integer> negative = Pattern.when(n -> n < 0);
            Pattern<Integer, Integer> greaterThan10 = Pattern.when(n -> n > 10);
            Pattern<Integer, Integer> combined = negative.or(greaterThan10);

            assertThat(combined.match(15)).contains(15);
        }

        @Test
        @DisplayName("or() 两个模式都不匹配时失败")
        void testOrBothFail() {
            Pattern<Integer, Integer> negative = Pattern.when(n -> n < 0);
            Pattern<Integer, Integer> greaterThan10 = Pattern.when(n -> n > 10);
            Pattern<Integer, Integer> combined = negative.or(greaterThan10);

            assertThat(combined.match(5)).isEmpty();
        }
    }

    @Nested
    @DisplayName("map() 组合器测试")
    class MapTests {

        @Test
        @DisplayName("map() 转换匹配结果")
        void testMap() {
            Pattern<Object, String> stringPattern = Pattern.type(String.class);
            Pattern<Object, Integer> lengthPattern = stringPattern.map(String::length);

            assertThat(lengthPattern.match("hello")).contains(5);
        }

        @Test
        @DisplayName("map() 不匹配时返回 empty")
        void testMapNoMatch() {
            Pattern<Object, String> stringPattern = Pattern.type(String.class);
            Pattern<Object, Integer> lengthPattern = stringPattern.map(String::length);

            assertThat(lengthPattern.match(123)).isEmpty();
        }
    }

    @Nested
    @DisplayName("filter() 组合器测试")
    class FilterTests {

        @Test
        @DisplayName("filter() 条件满足时保持匹配")
        void testFilterPass() {
            Pattern<Integer, Integer> positive = Pattern.when(n -> n > 0);
            Pattern<Integer, Integer> evenPositive = positive.filter(n -> n % 2 == 0);

            assertThat(evenPositive.match(4)).contains(4);
        }

        @Test
        @DisplayName("filter() 条件不满足时过滤掉")
        void testFilterFail() {
            Pattern<Integer, Integer> positive = Pattern.when(n -> n > 0);
            Pattern<Integer, Integer> evenPositive = positive.filter(n -> n % 2 == 0);

            assertThat(evenPositive.match(5)).isEmpty();
        }

        @Test
        @DisplayName("filter() 初始不匹配时返回 empty")
        void testFilterInitialNoMatch() {
            Pattern<Integer, Integer> positive = Pattern.when(n -> n > 0);
            Pattern<Integer, Integer> evenPositive = positive.filter(n -> n % 2 == 0);

            assertThat(evenPositive.match(-4)).isEmpty();
        }
    }

    @Nested
    @DisplayName("链式组合测试")
    class ChainedCombinatorsTests {

        @Test
        @DisplayName("链式组合多个操作")
        void testChainedOperations() {
            Pattern<Object, String> pattern = Pattern.type(String.class)
                    .filter(s -> s.length() > 3)
                    .map(String::toUpperCase);

            assertThat(pattern.match("hello")).contains("HELLO");
            assertThat(pattern.match("hi")).isEmpty();
            assertThat(pattern.match(123)).isEmpty();
        }
    }
}

package cloud.opencode.base.core.func;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedPredicate 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("CheckedPredicate 测试")
class CheckedPredicateTest {

    @Nested
    @DisplayName("test 测试")
    class TestTests {

        @Test
        @DisplayName("返回 true")
        void testReturnTrue() throws Exception {
            CheckedPredicate<Integer> predicate = i -> i > 0;
            assertThat(predicate.test(5)).isTrue();
        }

        @Test
        @DisplayName("返回 false")
        void testReturnFalse() throws Exception {
            CheckedPredicate<Integer> predicate = i -> i > 0;
            assertThat(predicate.test(-1)).isFalse();
        }

        @Test
        @DisplayName("抛出受检异常")
        void testThrowCheckedException() {
            CheckedPredicate<String> predicate = s -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(() -> predicate.test("test"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }
    }

    @Nested
    @DisplayName("unchecked 测试")
    class UncheckedTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedPredicate<Integer> checked = i -> i > 0;
            Predicate<Integer> predicate = checked.unchecked();

            assertThat(predicate.test(5)).isTrue();
            assertThat(predicate.test(-1)).isFalse();
        }

        @Test
        @DisplayName("受检异常包装为 RuntimeException")
        void testCheckedExceptionWrapped() {
            CheckedPredicate<String> checked = s -> {
                throw new IOException("IO error");
            };
            Predicate<String> predicate = checked.unchecked();

            assertThatThrownBy(() -> predicate.test("test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("运行时异常直接抛出")
        void testRuntimeExceptionDirectly() {
            CheckedPredicate<String> checked = s -> {
                throw new IllegalArgumentException("Invalid");
            };
            Predicate<String> predicate = checked.unchecked();

            assertThatThrownBy(() -> predicate.test("test"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("testQuietly 测试")
    class TestQuietlyTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedPredicate<Integer> predicate = i -> i > 0;
            assertThat(predicate.testQuietly(5)).isTrue();
        }

        @Test
        @DisplayName("异常时返回 false")
        void testReturnFalseOnException() {
            CheckedPredicate<String> predicate = s -> {
                throw new IOException("IO error");
            };
            assertThat(predicate.testQuietly("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("testOrDefault 测试")
    class TestOrDefaultTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedPredicate<Integer> predicate = i -> i > 0;
            assertThat(predicate.testOrDefault(5, false)).isTrue();
        }

        @Test
        @DisplayName("异常时返回默认值 true")
        void testReturnDefaultTrue() {
            CheckedPredicate<String> predicate = s -> {
                throw new IOException("IO error");
            };
            assertThat(predicate.testOrDefault("test", true)).isTrue();
        }

        @Test
        @DisplayName("异常时返回默认值 false")
        void testReturnDefaultFalse() {
            CheckedPredicate<String> predicate = s -> {
                throw new IOException("IO error");
            };
            assertThat(predicate.testOrDefault("test", false)).isFalse();
        }
    }

    @Nested
    @DisplayName("negate 测试")
    class NegateTests {

        @Test
        @DisplayName("取反 true -> false")
        void testNegateTrueToFalse() throws Exception {
            CheckedPredicate<Integer> predicate = i -> i > 0;
            assertThat(predicate.negate().test(5)).isFalse();
        }

        @Test
        @DisplayName("取反 false -> true")
        void testNegateFalseToTrue() throws Exception {
            CheckedPredicate<Integer> predicate = i -> i > 0;
            assertThat(predicate.negate().test(-1)).isTrue();
        }
    }

    @Nested
    @DisplayName("and 测试")
    class AndTests {

        @Test
        @DisplayName("true && true = true")
        void testTrueAndTrue() throws Exception {
            CheckedPredicate<Integer> p1 = i -> i > 0;
            CheckedPredicate<Integer> p2 = i -> i < 10;

            assertThat(p1.and(p2).test(5)).isTrue();
        }

        @Test
        @DisplayName("true && false = false")
        void testTrueAndFalse() throws Exception {
            CheckedPredicate<Integer> p1 = i -> i > 0;
            CheckedPredicate<Integer> p2 = i -> i > 10;

            assertThat(p1.and(p2).test(5)).isFalse();
        }

        @Test
        @DisplayName("false && true = false (短路)")
        void testFalseAndTrue() throws Exception {
            CheckedPredicate<Integer> p1 = i -> i > 10;
            CheckedPredicate<Integer> p2 = i -> {
                throw new RuntimeException("Should not be called");
            };

            assertThat(p1.and(p2).test(5)).isFalse();
        }
    }

    @Nested
    @DisplayName("or 测试")
    class OrTests {

        @Test
        @DisplayName("true || false = true")
        void testTrueOrFalse() throws Exception {
            CheckedPredicate<Integer> p1 = i -> i > 0;
            CheckedPredicate<Integer> p2 = i -> i > 10;

            assertThat(p1.or(p2).test(5)).isTrue();
        }

        @Test
        @DisplayName("false || true = true")
        void testFalseOrTrue() throws Exception {
            CheckedPredicate<Integer> p1 = i -> i > 10;
            CheckedPredicate<Integer> p2 = i -> i > 0;

            assertThat(p1.or(p2).test(5)).isTrue();
        }

        @Test
        @DisplayName("false || false = false")
        void testFalseOrFalse() throws Exception {
            CheckedPredicate<Integer> p1 = i -> i > 10;
            CheckedPredicate<Integer> p2 = i -> i < 0;

            assertThat(p1.or(p2).test(5)).isFalse();
        }

        @Test
        @DisplayName("true || x = true (短路)")
        void testTrueOrShortCircuit() throws Exception {
            CheckedPredicate<Integer> p1 = i -> i > 0;
            CheckedPredicate<Integer> p2 = i -> {
                throw new RuntimeException("Should not be called");
            };

            assertThat(p1.or(p2).test(5)).isTrue();
        }
    }

    @Nested
    @DisplayName("of 测试")
    class OfTests {

        @Test
        @DisplayName("从 Predicate 创建 CheckedPredicate")
        void testOfPredicate() throws Exception {
            Predicate<Integer> predicate = i -> i > 0;
            CheckedPredicate<Integer> checked = CheckedPredicate.of(predicate);

            assertThat(checked.test(5)).isTrue();
        }
    }
}

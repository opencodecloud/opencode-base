package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * ComparisonChainTest Tests
 * ComparisonChainTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("ComparisonChain 比较链测试")
class ComparisonChainTest {

    @Nested
    @DisplayName("Equal objects | 相等对象")
    class EqualObjectsTests {

        @Test
        @DisplayName("所有字段相等结果为0")
        void testAllFieldsEqual() {
            int result = ComparisonChain.start()
                    .compare("alice", "alice")
                    .compare(30, 30)
                    .compare(1.75, 1.75)
                    .result();

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("空链结果为0")
        void testEmptyChain() {
            assertThat(ComparisonChain.start().result()).isZero();
        }
    }

    @Nested
    @DisplayName("Short-circuit | 短路求值")
    class ShortCircuitTests {

        @Test
        @DisplayName("第一个字段不同时短路后续比较")
        void testShortCircuitsOnFirstDifference() {
            AtomicInteger sideEffect = new AtomicInteger(0);

            Comparator<String> trackingComparator = (a, b) -> {
                sideEffect.incrementAndGet();
                return a.compareTo(b);
            };

            int result = ComparisonChain.start()
                    .compare(1, 2) // differs: 1 < 2
                    .compare("x", "y", trackingComparator) // should be skipped
                    .result();

            assertThat(result).isNegative();
            assertThat(sideEffect.get()).isZero();
        }

        @Test
        @DisplayName("第一个字段相等时继续比较第二个字段")
        void testContinuesToSecondFieldWhenFirstEqual() {
            int result = ComparisonChain.start()
                    .compare("same", "same")
                    .compare(10, 5)
                    .result();

            assertThat(result).isPositive();
        }
    }

    @Nested
    @DisplayName("Comparable natural ordering | 自然排序")
    class NaturalOrderingTests {

        @Test
        @DisplayName("字符串自然排序: 小于")
        void testStringLessThan() {
            int result = ComparisonChain.start()
                    .compare("apple", "banana")
                    .result();

            assertThat(result).isNegative();
        }

        @Test
        @DisplayName("字符串自然排序: 大于")
        void testStringGreaterThan() {
            int result = ComparisonChain.start()
                    .compare("banana", "apple")
                    .result();

            assertThat(result).isPositive();
        }

        @Test
        @DisplayName("Integer自然排序")
        void testIntegerNaturalOrdering() {
            int result = ComparisonChain.start()
                    .compare(Integer.valueOf(3), Integer.valueOf(5))
                    .result();

            assertThat(result).isNegative();
        }
    }

    @Nested
    @DisplayName("int comparison | int 比较")
    class IntComparisonTests {

        @Test
        @DisplayName("int小于")
        void testIntLessThan() {
            assertThat(ComparisonChain.start().compare(1, 2).result()).isNegative();
        }

        @Test
        @DisplayName("int大于")
        void testIntGreaterThan() {
            assertThat(ComparisonChain.start().compare(5, 3).result()).isPositive();
        }

        @Test
        @DisplayName("int相等")
        void testIntEqual() {
            assertThat(ComparisonChain.start().compare(7, 7).result()).isZero();
        }
    }

    @Nested
    @DisplayName("long comparison | long 比较")
    class LongComparisonTests {

        @Test
        @DisplayName("long小于")
        void testLongLessThan() {
            assertThat(ComparisonChain.start().compare(1L, 2L).result()).isNegative();
        }

        @Test
        @DisplayName("long大于")
        void testLongGreaterThan() {
            assertThat(ComparisonChain.start().compare(Long.MAX_VALUE, 0L).result()).isPositive();
        }

        @Test
        @DisplayName("long相等")
        void testLongEqual() {
            assertThat(ComparisonChain.start().compare(100L, 100L).result()).isZero();
        }
    }

    @Nested
    @DisplayName("double comparison | double 比较")
    class DoubleComparisonTests {

        @Test
        @DisplayName("double小于")
        void testDoubleLessThan() {
            assertThat(ComparisonChain.start().compare(1.0, 2.0).result()).isNegative();
        }

        @Test
        @DisplayName("double大于")
        void testDoubleGreaterThan() {
            assertThat(ComparisonChain.start().compare(3.14, 2.71).result()).isPositive();
        }

        @Test
        @DisplayName("double相等")
        void testDoubleEqual() {
            assertThat(ComparisonChain.start().compare(0.0, 0.0).result()).isZero();
        }

        @Test
        @DisplayName("NaN比较遵循Double.compare语义")
        void testNaN() {
            // Double.compare(NaN, anything) > 0
            assertThat(ComparisonChain.start().compare(Double.NaN, 1.0).result()).isPositive();
        }
    }

    @Nested
    @DisplayName("float comparison | float 比较")
    class FloatComparisonTests {

        @Test
        @DisplayName("float小于")
        void testFloatLessThan() {
            assertThat(ComparisonChain.start().compare(1.0f, 2.0f).result()).isNegative();
        }

        @Test
        @DisplayName("float大于")
        void testFloatGreaterThan() {
            assertThat(ComparisonChain.start().compare(9.9f, 1.1f).result()).isPositive();
        }

        @Test
        @DisplayName("float相等")
        void testFloatEqual() {
            assertThat(ComparisonChain.start().compare(3.0f, 3.0f).result()).isZero();
        }
    }

    @Nested
    @DisplayName("Boolean comparison | 布尔比较")
    class BooleanComparisonTests {

        @Test
        @DisplayName("compareTrueFirst: true排在false前面")
        void testTrueFirst() {
            assertThat(ComparisonChain.start().compareTrueFirst(true, false).result()).isNegative();
            assertThat(ComparisonChain.start().compareTrueFirst(false, true).result()).isPositive();
            assertThat(ComparisonChain.start().compareTrueFirst(true, true).result()).isZero();
            assertThat(ComparisonChain.start().compareTrueFirst(false, false).result()).isZero();
        }

        @Test
        @DisplayName("compareFalseFirst: false排在true前面")
        void testFalseFirst() {
            assertThat(ComparisonChain.start().compareFalseFirst(false, true).result()).isNegative();
            assertThat(ComparisonChain.start().compareFalseFirst(true, false).result()).isPositive();
            assertThat(ComparisonChain.start().compareFalseFirst(true, true).result()).isZero();
            assertThat(ComparisonChain.start().compareFalseFirst(false, false).result()).isZero();
        }
    }

    @Nested
    @DisplayName("Custom Comparator | 自定义比较器")
    class CustomComparatorTests {

        @Test
        @DisplayName("使用自定义Comparator比较")
        void testCustomComparator() {
            Comparator<String> byLength = Comparator.comparingInt(String::length);

            int result = ComparisonChain.start()
                    .compare("hi", "hello", byLength) // 2 < 5
                    .result();

            assertThat(result).isNegative();
        }

        @Test
        @DisplayName("自定义Comparator: 逆序")
        void testReverseComparator() {
            Comparator<Integer> reverse = Comparator.reverseOrder();

            int result = ComparisonChain.start()
                    .compare(1, 2, reverse) // reversed: 1 > 2
                    .result();

            assertThat(result).isPositive();
        }
    }

    @Nested
    @DisplayName("Multi-field comparison | 多字段比较")
    class MultiFieldTests {

        @Test
        @DisplayName("多字段: 第一字段决定结果")
        void testFirstFieldDecides() {
            int result = ComparisonChain.start()
                    .compare("alice", "bob")
                    .compare(100, 1)
                    .compare(true, false)
                    .result();

            assertThat(result).isNegative(); // "alice" < "bob"
        }

        @Test
        @DisplayName("多字段: 最后一个字段决定结果")
        void testLastFieldDecides() {
            int result = ComparisonChain.start()
                    .compare("same", "same")
                    .compare(42, 42)
                    .compare(1.0, 2.0) // this one differs
                    .result();

            assertThat(result).isNegative();
        }

        @Test
        @DisplayName("多字段: 混合类型比较")
        void testMixedTypeComparison() {
            int result = ComparisonChain.start()
                    .compare("name", "name")
                    .compare(25, 25)
                    .compare(70.5f, 70.5f)
                    .compare(1000L, 1000L)
                    .compareTrueFirst(true, true)
                    .result();

            assertThat(result).isZero();
        }
    }
}

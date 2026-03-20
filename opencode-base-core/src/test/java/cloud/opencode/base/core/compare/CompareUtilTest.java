package cloud.opencode.base.core.compare;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CompareUtil}.
 */
@DisplayName("CompareUtil Tests")
class CompareUtilTest {

    @Nested
    @DisplayName("Operator enum")
    class OperatorTests {

        @Test
        @DisplayName("LT evaluates correctly")
        void ltEvaluates() {
            assertThat(CompareUtil.Operator.LT.evaluate(-1)).isTrue();
            assertThat(CompareUtil.Operator.LT.evaluate(0)).isFalse();
            assertThat(CompareUtil.Operator.LT.evaluate(1)).isFalse();
        }

        @Test
        @DisplayName("LE evaluates correctly")
        void leEvaluates() {
            assertThat(CompareUtil.Operator.LE.evaluate(-1)).isTrue();
            assertThat(CompareUtil.Operator.LE.evaluate(0)).isTrue();
            assertThat(CompareUtil.Operator.LE.evaluate(1)).isFalse();
        }

        @Test
        @DisplayName("EQ evaluates correctly")
        void eqEvaluates() {
            assertThat(CompareUtil.Operator.EQ.evaluate(0)).isTrue();
            assertThat(CompareUtil.Operator.EQ.evaluate(-1)).isFalse();
            assertThat(CompareUtil.Operator.EQ.evaluate(1)).isFalse();
        }

        @Test
        @DisplayName("NE evaluates correctly")
        void neEvaluates() {
            assertThat(CompareUtil.Operator.NE.evaluate(0)).isFalse();
            assertThat(CompareUtil.Operator.NE.evaluate(-1)).isTrue();
            assertThat(CompareUtil.Operator.NE.evaluate(1)).isTrue();
        }

        @Test
        @DisplayName("GE evaluates correctly")
        void geEvaluates() {
            assertThat(CompareUtil.Operator.GE.evaluate(1)).isTrue();
            assertThat(CompareUtil.Operator.GE.evaluate(0)).isTrue();
            assertThat(CompareUtil.Operator.GE.evaluate(-1)).isFalse();
        }

        @Test
        @DisplayName("GT evaluates correctly")
        void gtEvaluates() {
            assertThat(CompareUtil.Operator.GT.evaluate(1)).isTrue();
            assertThat(CompareUtil.Operator.GT.evaluate(0)).isFalse();
            assertThat(CompareUtil.Operator.GT.evaluate(-1)).isFalse();
        }

        @Test
        @DisplayName("symbol() returns correct symbols")
        void symbolReturnsCorrect() {
            assertThat(CompareUtil.Operator.LT.symbol()).isEqualTo("<");
            assertThat(CompareUtil.Operator.LE.symbol()).isEqualTo("<=");
            assertThat(CompareUtil.Operator.EQ.symbol()).isEqualTo("==");
            assertThat(CompareUtil.Operator.NE.symbol()).isEqualTo("!=");
            assertThat(CompareUtil.Operator.GE.symbol()).isEqualTo(">=");
            assertThat(CompareUtil.Operator.GT.symbol()).isEqualTo(">");
        }

        @Test
        @DisplayName("all operators are present")
        void allOperatorsPresent() {
            assertThat(CompareUtil.Operator.values()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("compare(Object, Object)")
    class CompareTests {

        @Test
        @DisplayName("compares integers")
        void comparesIntegers() {
            assertThat(CompareUtil.compare(1, 2)).isNegative();
            assertThat(CompareUtil.compare(2, 2)).isZero();
            assertThat(CompareUtil.compare(3, 2)).isPositive();
        }

        @Test
        @DisplayName("compares strings")
        void comparesStrings() {
            assertThat(CompareUtil.compare("apple", "banana")).isNegative();
            assertThat(CompareUtil.compare("same", "same")).isZero();
            assertThat(CompareUtil.compare("zebra", "apple")).isPositive();
        }

        @Test
        @DisplayName("compares doubles")
        void comparesDoubles() {
            assertThat(CompareUtil.compare(1.5, 2.5)).isNegative();
            assertThat(CompareUtil.compare(3.0, 3.0)).isZero();
        }

        @Test
        @DisplayName("falls back to string comparison for non-Comparable")
        void fallsBackToString() {
            Object obj1 = new Object() {
                @Override
                public String toString() { return "aaa"; }
            };
            Object obj2 = new Object() {
                @Override
                public String toString() { return "bbb"; }
            };
            assertThat(CompareUtil.compare(obj1, obj2)).isNegative();
        }
    }

    @Nested
    @DisplayName("equals(Object, Object)")
    class EqualsTests {

        @Test
        @DisplayName("equal objects return true")
        void equalObjects() {
            assertThat(CompareUtil.equals("hello", "hello")).isTrue();
            assertThat(CompareUtil.equals(42, 42)).isTrue();
        }

        @Test
        @DisplayName("different objects return false")
        void differentObjects() {
            assertThat(CompareUtil.equals("hello", "world")).isFalse();
            assertThat(CompareUtil.equals(1, 2)).isFalse();
        }

        @Test
        @DisplayName("null equals null")
        void nullEqualsNull() {
            assertThat(CompareUtil.equals(null, null)).isTrue();
        }

        @Test
        @DisplayName("null not equal to non-null")
        void nullNotEqualToNonNull() {
            assertThat(CompareUtil.equals(null, "hello")).isFalse();
            assertThat(CompareUtil.equals("hello", null)).isFalse();
        }
    }
}

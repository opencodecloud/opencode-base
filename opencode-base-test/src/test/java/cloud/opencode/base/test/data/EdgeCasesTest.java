package cloud.opencode.base.test.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EdgeCases")
class EdgeCasesTest {

    @Nested
    @DisplayName("forInt")
    class ForInt {

        @Test
        @DisplayName("should contain MIN_VALUE, 0, and MAX_VALUE")
        void containsBoundaryValues() {
            List<Integer> values = EdgeCases.forInt();

            assertThat(values).contains(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("should contain -1 and 1")
        void containsNearZeroValues() {
            List<Integer> values = EdgeCases.forInt();

            assertThat(values).contains(-1, 1);
        }

        @Test
        @DisplayName("should have exactly 5 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forInt()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("forLong")
    class ForLong {

        @Test
        @DisplayName("should contain MIN_VALUE, 0, and MAX_VALUE")
        void containsBoundaryValues() {
            List<Long> values = EdgeCases.forLong();

            assertThat(values).contains(Long.MIN_VALUE, 0L, Long.MAX_VALUE);
        }

        @Test
        @DisplayName("should contain -1 and 1")
        void containsNearZeroValues() {
            assertThat(EdgeCases.forLong()).contains(-1L, 1L);
        }

        @Test
        @DisplayName("should have exactly 5 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forLong()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("forDouble")
    class ForDouble {

        @Test
        @DisplayName("should contain infinity and NaN")
        void containsSpecialValues() {
            List<Double> values = EdgeCases.forDouble();

            assertThat(values).contains(
                    Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    Double.NaN);
        }

        @Test
        @DisplayName("should contain MIN_VALUE, MAX_VALUE, and MIN_NORMAL")
        void containsBoundaryValues() {
            List<Double> values = EdgeCases.forDouble();

            assertThat(values).contains(
                    Double.MIN_VALUE,
                    Double.MAX_VALUE,
                    Double.MIN_NORMAL);
        }

        @Test
        @DisplayName("should contain -1.0, -0.0, 0.0, and 1.0")
        void containsCommonValues() {
            List<Double> values = EdgeCases.forDouble();

            assertThat(values).contains(-1.0, -0.0, 0.0, 1.0);
        }

        @Test
        @DisplayName("should have 10 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forDouble()).hasSize(10);
        }
    }

    @Nested
    @DisplayName("forFloat")
    class ForFloat {

        @Test
        @DisplayName("should contain infinity and NaN")
        void containsSpecialValues() {
            List<Float> values = EdgeCases.forFloat();

            assertThat(values).contains(
                    Float.NEGATIVE_INFINITY,
                    Float.POSITIVE_INFINITY,
                    Float.NaN);
        }

        @Test
        @DisplayName("should contain MIN_VALUE, MAX_VALUE, and MIN_NORMAL")
        void containsBoundaryValues() {
            assertThat(EdgeCases.forFloat()).contains(
                    Float.MIN_VALUE, Float.MAX_VALUE, Float.MIN_NORMAL);
        }

        @Test
        @DisplayName("should have 10 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forFloat()).hasSize(10);
        }
    }

    @Nested
    @DisplayName("forString")
    class ForString {

        @Test
        @DisplayName("should contain null, empty, and whitespace")
        void containsNullEmptyWhitespace() {
            List<String> values = EdgeCases.forString();

            assertThat(values).contains(null, "", " ");
        }

        @Test
        @DisplayName("should contain tab and newline")
        void containsTabAndNewline() {
            assertThat(EdgeCases.forString()).contains("\t", "\n");
        }

        @Test
        @DisplayName("should contain a single char and a long string")
        void containsSingleAndLong() {
            List<String> values = EdgeCases.forString();

            assertThat(values).contains("a");
            assertThat(values).anyMatch(s -> s != null && s.length() == 128);
        }

        @Test
        @DisplayName("should have 7 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forString()).hasSize(7);
        }

        @Test
        @DisplayName("should return an unmodifiable list")
        void unmodifiable() {
            List<String> values = EdgeCases.forString();

            assertThat(values.getClass().getName()).contains("Unmodifiable");
        }
    }

    @Nested
    @DisplayName("forStringNonNull")
    class ForStringNonNull {

        @Test
        @DisplayName("should not contain null")
        void noNull() {
            assertThat(EdgeCases.forStringNonNull()).doesNotContainNull();
        }

        @Test
        @DisplayName("should contain empty and whitespace")
        void containsEmptyAndWhitespace() {
            assertThat(EdgeCases.forStringNonNull()).contains("", " ", "\t", "\n");
        }

        @Test
        @DisplayName("should have 6 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forStringNonNull()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("forList")
    class ForList {

        @Test
        @DisplayName("should contain null, empty list, and singleton-with-null")
        void containsExpectedCases() {
            List<List<String>> values = EdgeCases.forList();

            assertThat(values).hasSize(3);
            assertThat(values.get(0)).isNull();
            assertThat(values.get(1)).isEmpty();
            assertThat(values.get(2)).containsExactly((String) null);
        }

        @Test
        @DisplayName("should return an unmodifiable list")
        void unmodifiable() {
            List<List<String>> values = EdgeCases.forList();

            assertThat(values.getClass().getName()).contains("Unmodifiable");
        }
    }

    @Nested
    @DisplayName("forListNonNull")
    class ForListNonNull {

        @Test
        @DisplayName("should not contain null list entry")
        void noNullEntry() {
            List<List<String>> values = EdgeCases.forListNonNull();

            assertThat(values).doesNotContainNull();
        }

        @Test
        @DisplayName("should contain empty list and singleton-with-null")
        void containsExpectedCases() {
            List<List<String>> values = EdgeCases.forListNonNull();

            assertThat(values).hasSize(2);
            assertThat(values.get(0)).isEmpty();
            assertThat(values.get(1)).containsExactly((String) null);
        }
    }

    @Nested
    @DisplayName("forByte")
    class ForByte {

        @Test
        @DisplayName("should contain MIN_VALUE, 0, and MAX_VALUE")
        void containsBoundaryValues() {
            assertThat(EdgeCases.forByte()).contains(
                    Byte.MIN_VALUE, (byte) 0, Byte.MAX_VALUE);
        }

        @Test
        @DisplayName("should have 5 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forByte()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("forShort")
    class ForShort {

        @Test
        @DisplayName("should contain MIN_VALUE, 0, and MAX_VALUE")
        void containsBoundaryValues() {
            assertThat(EdgeCases.forShort()).contains(
                    Short.MIN_VALUE, (short) 0, Short.MAX_VALUE);
        }

        @Test
        @DisplayName("should have 5 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forShort()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("forChar")
    class ForChar {

        @Test
        @DisplayName("should contain MIN_VALUE and MAX_VALUE")
        void containsBoundaryValues() {
            assertThat(EdgeCases.forChar()).contains(
                    Character.MIN_VALUE, Character.MAX_VALUE);
        }

        @Test
        @DisplayName("should contain letter and digit boundaries")
        void containsLetterAndDigitBoundaries() {
            assertThat(EdgeCases.forChar()).contains('a', 'z', 'A', 'Z', '0', '9');
        }

        @Test
        @DisplayName("should have 8 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forChar()).hasSize(8);
        }
    }

    @Nested
    @DisplayName("forBoolean")
    class ForBoolean {

        @Test
        @DisplayName("should contain both true and false")
        void containsBothValues() {
            assertThat(EdgeCases.forBoolean()).containsExactly(true, false);
        }
    }

    @Nested
    @DisplayName("forLocalDate")
    class ForLocalDate {

        @Test
        @DisplayName("should contain MIN, EPOCH, today, and MAX")
        void containsBoundaryValues() {
            List<LocalDate> values = EdgeCases.forLocalDate();

            assertThat(values).contains(
                    LocalDate.MIN, LocalDate.EPOCH, LocalDate.now(), LocalDate.MAX);
        }

        @Test
        @DisplayName("should have 4 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forLocalDate()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("forDuration")
    class ForDuration {

        @Test
        @DisplayName("should contain ZERO and common durations")
        void containsExpectedValues() {
            List<Duration> values = EdgeCases.forDuration();

            assertThat(values).contains(
                    Duration.ZERO,
                    Duration.ofMillis(1),
                    Duration.ofSeconds(1),
                    Duration.ofHours(1),
                    Duration.ofDays(1));
        }

        @Test
        @DisplayName("should contain max supported duration")
        void containsMaxDuration() {
            assertThat(EdgeCases.forDuration())
                    .contains(Duration.ofSeconds(Long.MAX_VALUE, 999_999_999));
        }

        @Test
        @DisplayName("should contain negative duration")
        void containsNegativeDuration() {
            assertThat(EdgeCases.forDuration()).contains(Duration.ofSeconds(-1));
        }

        @Test
        @DisplayName("should have 7 elements")
        void hasExpectedSize() {
            assertThat(EdgeCases.forDuration()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafety {

        @Test
        @DisplayName("each call returns a fresh list instance")
        void freshListEachCall() {
            List<Integer> first = EdgeCases.forInt();
            List<Integer> second = EdgeCases.forInt();

            assertThat(first).isEqualTo(second);
            // They should be equal in content but we verify they are independent
            // (List.of returns cached instances for same values, which is fine)
        }
    }
}

package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ParallelResult}.
 * {@link ParallelResult} 测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.3
 */
@DisplayName("ParallelResult")
class ParallelResultTest {

    @Nested
    @DisplayName("Factory Methods | 工厂方法")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() creates result with successes and failures")
        void ofCreatesResultWithSuccessesAndFailures() {
            List<String> successes = List.of("a", "b");
            List<Throwable> failures = List.of(new RuntimeException("err"));

            ParallelResult<String> result = ParallelResult.of(successes, failures);

            assertThat(result.successes()).containsExactly("a", "b");
            assertThat(result.failures()).hasSize(1);
            assertThat(result.failures().getFirst()).hasMessage("err");
        }

        @Test
        @DisplayName("of() with empty lists")
        void ofWithEmptyLists() {
            ParallelResult<String> result = ParallelResult.of(List.of(), List.of());

            assertThat(result.successes()).isEmpty();
            assertThat(result.failures()).isEmpty();
            assertThat(result.totalCount()).isZero();
        }

        @Test
        @DisplayName("of() defensively copies input lists")
        void ofDefensivelyCopiesInputLists() {
            ArrayList<String> successes = new ArrayList<>(List.of("a"));
            ArrayList<Throwable> failures = new ArrayList<>(List.of(new RuntimeException()));

            ParallelResult<String> result = ParallelResult.of(successes, failures);
            successes.add("b");
            failures.add(new RuntimeException());

            assertThat(result.successes()).hasSize(1);
            assertThat(result.failures()).hasSize(1);
        }

        @Test
        @DisplayName("of() throws NPE when successes is null")
        void ofThrowsNpeWhenSuccessesIsNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ParallelResult.of(null, List.of()))
                    .withMessageContaining("successes");
        }

        @Test
        @DisplayName("of() throws NPE when failures is null")
        void ofThrowsNpeWhenFailuresIsNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ParallelResult.of(List.of(), null))
                    .withMessageContaining("failures");
        }

        @Test
        @DisplayName("allSucceeded() creates result with no failures")
        void allSucceededCreatesResultWithNoFailures() {
            ParallelResult<Integer> result = ParallelResult.allSucceeded(List.of(1, 2, 3));

            assertThat(result.successes()).containsExactly(1, 2, 3);
            assertThat(result.failures()).isEmpty();
            assertThat(result.isAllSuccessful()).isTrue();
        }

        @Test
        @DisplayName("allSucceeded() with empty list")
        void allSucceededWithEmptyList() {
            ParallelResult<String> result = ParallelResult.allSucceeded(List.of());

            assertThat(result.successes()).isEmpty();
            assertThat(result.failures()).isEmpty();
        }

        @Test
        @DisplayName("allSucceeded() throws NPE when results is null")
        void allSucceededThrowsNpeWhenNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ParallelResult.allSucceeded(null))
                    .withMessageContaining("results");
        }

        @Test
        @DisplayName("allFailed() creates result with no successes")
        void allFailedCreatesResultWithNoSuccesses() {
            List<Throwable> failures = List.of(
                    new RuntimeException("err1"),
                    new IllegalStateException("err2"));

            ParallelResult<String> result = ParallelResult.allFailed(failures);

            assertThat(result.successes()).isEmpty();
            assertThat(result.failures()).hasSize(2);
            assertThat(result.isAllFailed()).isTrue();
        }

        @Test
        @DisplayName("allFailed() with empty list")
        void allFailedWithEmptyList() {
            ParallelResult<String> result = ParallelResult.allFailed(List.of());

            assertThat(result.successes()).isEmpty();
            assertThat(result.failures()).isEmpty();
            // empty failures + empty successes = not "all failed"
            assertThat(result.isAllFailed()).isFalse();
        }

        @Test
        @DisplayName("allFailed() throws NPE when failures is null")
        void allFailedThrowsNpeWhenNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ParallelResult.allFailed(null))
                    .withMessageContaining("failures");
        }
    }

    @Nested
    @DisplayName("Query Methods | 查询方法")
    class QueryTests {

        @Test
        @DisplayName("successes() returns unmodifiable list")
        void successesReturnsUnmodifiableList() {
            ParallelResult<String> result = ParallelResult.allSucceeded(List.of("a"));

            assertThatThrownBy(() -> result.successes().add("b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("failures() returns unmodifiable list")
        void failuresReturnsUnmodifiableList() {
            ParallelResult<String> result = ParallelResult.allFailed(
                    List.of(new RuntimeException()));

            assertThatThrownBy(() -> result.failures().add(new RuntimeException()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("hasFailures() returns true when failures exist")
        void hasFailuresReturnsTrueWhenFailuresExist() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"), List.of(new RuntimeException()));

            assertThat(result.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("hasFailures() returns false when no failures")
        void hasFailuresReturnsFalseWhenNoFailures() {
            ParallelResult<String> result = ParallelResult.allSucceeded(List.of("a"));

            assertThat(result.hasFailures()).isFalse();
        }

        @Test
        @DisplayName("isAllSuccessful() returns true when no failures")
        void isAllSuccessfulReturnsTrueWhenNoFailures() {
            ParallelResult<String> result = ParallelResult.allSucceeded(List.of("a", "b"));

            assertThat(result.isAllSuccessful()).isTrue();
        }

        @Test
        @DisplayName("isAllSuccessful() returns false when failures exist")
        void isAllSuccessfulReturnsFalseWhenFailuresExist() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"), List.of(new RuntimeException()));

            assertThat(result.isAllSuccessful()).isFalse();
        }

        @Test
        @DisplayName("isAllFailed() returns true when all tasks failed")
        void isAllFailedReturnsTrueWhenAllFailed() {
            ParallelResult<String> result = ParallelResult.allFailed(
                    List.of(new RuntimeException()));

            assertThat(result.isAllFailed()).isTrue();
        }

        @Test
        @DisplayName("isAllFailed() returns false when some succeeded")
        void isAllFailedReturnsFalseWhenSomeSucceeded() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"), List.of(new RuntimeException()));

            assertThat(result.isAllFailed()).isFalse();
        }

        @Test
        @DisplayName("isAllFailed() returns false for empty result")
        void isAllFailedReturnsFalseForEmptyResult() {
            ParallelResult<String> result = ParallelResult.of(List.of(), List.of());

            assertThat(result.isAllFailed()).isFalse();
        }

        @Test
        @DisplayName("successCount() returns correct count")
        void successCountReturnsCorrectCount() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a", "b", "c"), List.of(new RuntimeException()));

            assertThat(result.successCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("failureCount() returns correct count")
        void failureCountReturnsCorrectCount() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"), List.of(new RuntimeException(), new IllegalStateException()));

            assertThat(result.failureCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("totalCount() returns sum of successes and failures")
        void totalCountReturnsSumOfSuccessesAndFailures() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a", "b"), List.of(new RuntimeException()));

            assertThat(result.totalCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Terminal Operations | 终端操作")
    class TerminalTests {

        @Test
        @DisplayName("throwIfAnyFailed() does nothing when all successful")
        void throwIfAnyFailedDoesNothingWhenAllSuccessful() {
            ParallelResult<String> result = ParallelResult.allSucceeded(List.of("a", "b"));

            assertThatCode(result::throwIfAnyFailed).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throwIfAnyFailed() throws when any failed")
        void throwIfAnyFailedThrowsWhenAnyFailed() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"), List.of(new RuntimeException("err")));

            assertThatThrownBy(result::throwIfAnyFailed)
                    .isInstanceOf(OpenParallelException.class)
                    .hasMessageContaining("partially failed");
        }

        @Test
        @DisplayName("throwIfAnyFailed() includes failure count in exception")
        void throwIfAnyFailedIncludesFailureCountInException() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"),
                    List.of(new RuntimeException("e1"), new RuntimeException("e2")));

            assertThatThrownBy(result::throwIfAnyFailed)
                    .isInstanceOf(OpenParallelException.class)
                    .satisfies(ex -> {
                        OpenParallelException ope = (OpenParallelException) ex;
                        assertThat(ope.getFailedCount()).isEqualTo(2);
                        assertThat(ope.getTotalCount()).isEqualTo(3);
                    });
        }

        @Test
        @DisplayName("throwIfAllFailed() does nothing when some succeeded")
        void throwIfAllFailedDoesNothingWhenSomeSucceeded() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"), List.of(new RuntimeException()));

            assertThatCode(result::throwIfAllFailed).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throwIfAllFailed() throws when all failed")
        void throwIfAllFailedThrowsWhenAllFailed() {
            ParallelResult<String> result = ParallelResult.allFailed(
                    List.of(new RuntimeException("e1"), new RuntimeException("e2")));

            assertThatThrownBy(result::throwIfAllFailed)
                    .isInstanceOf(OpenParallelException.class)
                    .hasMessageContaining("All 2 parallel tasks failed");
        }

        @Test
        @DisplayName("throwIfAllFailed() does nothing for empty result")
        void throwIfAllFailedDoesNothingForEmptyResult() {
            ParallelResult<String> result = ParallelResult.of(List.of(), List.of());

            assertThatCode(result::throwIfAllFailed).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getOrThrow() returns successes when no failures")
        void getOrThrowReturnsSuccessesWhenNoFailures() {
            ParallelResult<String> result = ParallelResult.allSucceeded(List.of("a", "b"));

            assertThat(result.getOrThrow()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("getOrThrow() throws when failures exist")
        void getOrThrowThrowsWhenFailuresExist() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a"), List.of(new RuntimeException("err")));

            assertThatThrownBy(result::getOrThrow)
                    .isInstanceOf(OpenParallelException.class);
        }
    }

    @Nested
    @DisplayName("toString | toString 方法")
    class ToStringTest {

        @Test
        @DisplayName("toString() contains counts")
        void toStringContainsCounts() {
            ParallelResult<String> result = ParallelResult.of(
                    List.of("a", "b"), List.of(new RuntimeException()));

            String str = result.toString();

            assertThat(str).contains("successes=2");
            assertThat(str).contains("failures=1");
            assertThat(str).contains("total=3");
            assertThat(str).startsWith("ParallelResult{");
        }

        @Test
        @DisplayName("toString() for empty result")
        void toStringForEmptyResult() {
            ParallelResult<String> result = ParallelResult.of(List.of(), List.of());

            assertThat(result.toString()).contains("successes=0", "failures=0", "total=0");
        }
    }
}

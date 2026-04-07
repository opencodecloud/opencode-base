package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Futures}.
 * {@link Futures} 测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.3
 */
@DisplayName("Futures")
class FuturesTest {

    @Nested
    @DisplayName("allAsList | 全部收集")
    class AllAsListTests {

        @Test
        @DisplayName("all futures succeed")
        void allFuturesSucceed() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");
            CompletableFuture<String> f2 = CompletableFuture.completedFuture("b");
            CompletableFuture<String> f3 = CompletableFuture.completedFuture("c");

            List<String> result = Futures.allAsList(List.of(f1, f2, f3)).get();

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("one future fails causes overall failure")
        void oneFutureFailsCausesOverallFailure() {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");
            CompletableFuture<String> f2 = CompletableFuture.failedFuture(
                    new RuntimeException("boom"));

            CompletableFuture<List<String>> result = Futures.allAsList(List.of(f1, f2));

            assertThatThrownBy(result::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("empty input returns empty list")
        void emptyInputReturnsEmptyList() throws Exception {
            List<String> result = Futures.allAsList(List.<CompletableFuture<String>>of()).get();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("varargs version works")
        void varargsVersionWorks() throws Exception {
            CompletableFuture<Integer> f1 = CompletableFuture.completedFuture(1);
            CompletableFuture<Integer> f2 = CompletableFuture.completedFuture(2);

            List<Integer> result = Futures.allAsList(f1, f2).get();

            assertThat(result).containsExactly(1, 2);
        }

        @Test
        @DisplayName("result list is unmodifiable")
        void resultListIsUnmodifiable() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");

            List<String> result = Futures.allAsList(List.of(f1)).get();

            assertThatThrownBy(() -> result.add("b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null input throws NPE")
        void nullInputThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Futures.allAsList((List<CompletableFuture<String>>) null));
        }
    }

    @Nested
    @DisplayName("successfulAsList | 成功收集")
    class SuccessfulAsListTests {

        @Test
        @DisplayName("mixed success and failure collects only successes")
        void mixedSuccessAndFailureCollectsOnlySuccesses() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");
            CompletableFuture<String> f2 = CompletableFuture.failedFuture(
                    new RuntimeException("boom"));
            CompletableFuture<String> f3 = CompletableFuture.completedFuture("c");

            List<String> result = Futures.successfulAsList(List.of(f1, f2, f3)).get();

            assertThat(result).containsExactlyInAnyOrder("a", "c");
        }

        @Test
        @DisplayName("all succeed collects all")
        void allSucceedCollectsAll() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");
            CompletableFuture<String> f2 = CompletableFuture.completedFuture("b");

            List<String> result = Futures.successfulAsList(List.of(f1, f2)).get();

            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("all fail returns empty list")
        void allFailReturnsEmptyList() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.failedFuture(
                    new RuntimeException("e1"));
            CompletableFuture<String> f2 = CompletableFuture.failedFuture(
                    new RuntimeException("e2"));

            List<String> result = Futures.successfulAsList(List.of(f1, f2)).get();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("empty input returns empty list")
        void emptyInputReturnsEmptyList() throws Exception {
            List<String> result = Futures.successfulAsList(
                    List.<CompletableFuture<String>>of()).get();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null input throws NPE")
        void nullInputThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Futures.successfulAsList(null));
        }
    }

    @Nested
    @DisplayName("settleAll | 全部结算")
    class SettleAllTests {

        @Test
        @DisplayName("mixed success and failure settles both")
        void mixedSuccessAndFailureSettlesBoth() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");
            CompletableFuture<String> f2 = CompletableFuture.failedFuture(
                    new RuntimeException("boom"));
            CompletableFuture<String> f3 = CompletableFuture.completedFuture("c");

            ParallelResult<String> result = Futures.settleAll(List.of(f1, f2, f3)).get();

            assertThat(result.successes()).containsExactlyInAnyOrder("a", "c");
            assertThat(result.failures()).hasSize(1);
            assertThat(result.failures().getFirst()).hasMessage("boom");
            assertThat(result.totalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("all succeed produces all-successful result")
        void allSucceedProducesAllSuccessfulResult() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");
            CompletableFuture<String> f2 = CompletableFuture.completedFuture("b");

            ParallelResult<String> result = Futures.settleAll(List.of(f1, f2)).get();

            assertThat(result.isAllSuccessful()).isTrue();
            assertThat(result.successes()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("all fail produces all-failed result")
        void allFailProducesAllFailedResult() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.failedFuture(
                    new RuntimeException("e1"));
            CompletableFuture<String> f2 = CompletableFuture.failedFuture(
                    new RuntimeException("e2"));

            ParallelResult<String> result = Futures.settleAll(List.of(f1, f2)).get();

            assertThat(result.isAllFailed()).isTrue();
            assertThat(result.failures()).hasSize(2);
        }

        @Test
        @DisplayName("empty input produces empty result")
        void emptyInputProducesEmptyResult() throws Exception {
            ParallelResult<String> result = Futures.settleAll(
                    List.<CompletableFuture<String>>of()).get();

            assertThat(result.successes()).isEmpty();
            assertThat(result.failures()).isEmpty();
            assertThat(result.totalCount()).isZero();
        }

        @Test
        @DisplayName("null input throws NPE")
        void nullInputThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Futures.settleAll(null));
        }
    }

    @Nested
    @DisplayName("firstSuccessful | 首个成功")
    class FirstSuccessfulTests {

        @Test
        @DisplayName("first successful wins")
        void firstSuccessfulWins() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("winner");
            CompletableFuture<String> f2 = new CompletableFuture<>(); // never completes

            String result = Futures.firstSuccessful(List.of(f1, f2)).get();

            assertThat(result).isEqualTo("winner");
        }

        @Test
        @DisplayName("skips failed futures and returns first success")
        void skipsFailedFuturesAndReturnsFirstSuccess() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.failedFuture(
                    new RuntimeException("fail"));
            CompletableFuture<String> f2 = CompletableFuture.completedFuture("success");

            String result = Futures.firstSuccessful(List.of(f1, f2)).get();

            assertThat(result).isEqualTo("success");
        }

        @Test
        @DisplayName("all fail completes exceptionally")
        void allFailCompletesExceptionally() {
            CompletableFuture<String> f1 = CompletableFuture.failedFuture(
                    new RuntimeException("e1"));
            CompletableFuture<String> f2 = CompletableFuture.failedFuture(
                    new RuntimeException("e2"));

            CompletableFuture<String> result = Futures.firstSuccessful(List.of(f1, f2));

            assertThatThrownBy(result::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(OpenParallelException.class)
                    .hasMessageContaining("All 2 parallel tasks failed");
        }

        @Test
        @DisplayName("cancels remaining futures after first success")
        void cancelsRemainingFuturesAfterFirstSuccess() throws Exception {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("winner");
            CompletableFuture<String> f2 = new CompletableFuture<>();
            CompletableFuture<String> f3 = new CompletableFuture<>();

            Futures.firstSuccessful(List.of(f1, f2, f3)).get();

            // Give a moment for cancellation to propagate
            assertThat(f2.isCancelled() || f2.isDone()).isTrue();
            assertThat(f3.isCancelled() || f3.isDone()).isTrue();
        }

        @Test
        @DisplayName("empty input throws IllegalArgumentException")
        void emptyInputThrowsIllegalArgumentException() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Futures.firstSuccessful(List.of()))
                    .withMessageContaining("empty");
        }

        @Test
        @DisplayName("null input throws NPE")
        void nullInputThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Futures.firstSuccessful(null));
        }
    }

    @Nested
    @DisplayName("withTimeout | 超时控制")
    class WithTimeoutTests {

        @Test
        @DisplayName("completes in time returns result")
        void completesInTimeReturnsResult() throws Exception {
            CompletableFuture<String> future = CompletableFuture.completedFuture("done");

            String result = Futures.withTimeout(future, Duration.ofSeconds(5)).get();

            assertThat(result).isEqualTo("done");
        }

        @Test
        @DisplayName("times out throws OpenParallelException")
        void timesOutThrowsOpenParallelException() {
            CompletableFuture<String> future = new CompletableFuture<>(); // never completes

            CompletableFuture<String> timed = Futures.withTimeout(future, Duration.ofMillis(50));

            assertThatThrownBy(timed::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(OpenParallelException.class)
                    .hasMessageContaining("timeout");
        }

        @Test
        @DisplayName("failed future propagates original exception")
        void failedFuturePropagatesOriginalException() {
            CompletableFuture<String> future = CompletableFuture.failedFuture(
                    new IllegalStateException("bad state"));

            CompletableFuture<String> timed = Futures.withTimeout(future, Duration.ofSeconds(5));

            assertThatThrownBy(timed::get)
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("null future throws NPE")
        void nullFutureThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Futures.withTimeout(null, Duration.ofSeconds(1)));
        }

        @Test
        @DisplayName("null timeout throws NPE")
        void nullTimeoutThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Futures.withTimeout(
                            CompletableFuture.completedFuture("x"), null));
        }
    }
}

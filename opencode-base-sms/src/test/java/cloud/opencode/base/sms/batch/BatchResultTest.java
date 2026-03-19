package cloud.opencode.base.sms.batch;

import cloud.opencode.base.sms.message.SmsResult;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * BatchResultTest Tests
 * BatchResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("BatchResult 测试")
class BatchResultTest {

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("创建批量结果")
        void testOf() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.success("msg2", "13800138002"),
                    SmsResult.failure("13800138003", "ERR", "failed")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.totalCount()).isEqualTo(3);
            assertThat(batchResult.successCount()).isEqualTo(2);
            assertThat(batchResult.failureCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("空列表创建空结果")
        void testOfEmpty() {
            Instant startTime = Instant.now();
            BatchResult batchResult = BatchResult.of(List.of(), startTime);

            assertThat(batchResult.totalCount()).isZero();
            assertThat(batchResult.successCount()).isZero();
            assertThat(batchResult.failureCount()).isZero();
        }
    }

    @Nested
    @DisplayName("empty方法测试")
    class EmptyTests {

        @Test
        @DisplayName("返回空结果")
        void testEmpty() {
            BatchResult batchResult = BatchResult.empty();

            assertThat(batchResult.totalCount()).isZero();
            assertThat(batchResult.results()).isEmpty();
        }
    }

    @Nested
    @DisplayName("isAllSuccess方法测试")
    class IsAllSuccessTests {

        @Test
        @DisplayName("全部成功返回true")
        void testAllSuccess() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.success("msg2", "13800138002")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.isAllSuccess()).isTrue();
        }

        @Test
        @DisplayName("有失败返回false")
        void testNotAllSuccess() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.failure("13800138002", "ERR", "failed")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.isAllSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAllFailed方法测试")
    class IsAllFailedTests {

        @Test
        @DisplayName("全部失败返回true")
        void testAllFailed() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.failure("13800138001", "ERR", "failed"),
                    SmsResult.failure("13800138002", "ERR", "failed")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.isAllFailed()).isTrue();
        }

        @Test
        @DisplayName("有成功返回false")
        void testNotAllFailed() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.failure("13800138002", "ERR", "failed")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.isAllFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("isPartialSuccess方法测试")
    class IsPartialSuccessTests {

        @Test
        @DisplayName("部分成功返回true")
        void testPartialSuccess() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.failure("13800138002", "ERR", "failed")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.isPartialSuccess()).isTrue();
        }

        @Test
        @DisplayName("全部成功返回false")
        void testAllSuccessNotPartial() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.success("msg2", "13800138002")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.isPartialSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("getSuccessRate方法测试")
    class GetSuccessRateTests {

        @Test
        @DisplayName("计算成功率")
        void testSuccessRate() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.success("msg2", "13800138002"),
                    SmsResult.failure("13800138003", "ERR", "failed"),
                    SmsResult.failure("13800138004", "ERR", "failed")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            assertThat(batchResult.getSuccessRate()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("空结果返回0")
        void testSuccessRateEmpty() {
            BatchResult batchResult = BatchResult.empty();

            assertThat(batchResult.getSuccessRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("getDuration方法测试")
    class GetDurationTests {

        @Test
        @DisplayName("返回持续时间")
        void testGetDuration() {
            Instant start = Instant.now();
            Instant end = start.plusSeconds(5);

            BatchResult batchResult = new BatchResult(
                    List.of(), 0, 0, 0, start, end
            );

            assertThat(batchResult.getDuration()).isEqualTo(Duration.ofSeconds(5));
        }
    }

    @Nested
    @DisplayName("getSuccessResults方法测试")
    class GetSuccessResultsTests {

        @Test
        @DisplayName("返回成功结果")
        void testGetSuccessResults() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.failure("13800138002", "ERR", "failed"),
                    SmsResult.success("msg2", "13800138003")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            List<SmsResult> successResults = batchResult.getSuccessResults();

            assertThat(successResults).hasSize(2);
            assertThat(successResults).allMatch(SmsResult::success);
        }
    }

    @Nested
    @DisplayName("getFailureResults方法测试")
    class GetFailureResultsTests {

        @Test
        @DisplayName("返回失败结果")
        void testGetFailureResults() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.failure("13800138002", "ERR1", "failed1"),
                    SmsResult.failure("13800138003", "ERR2", "failed2")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            List<SmsResult> failureResults = batchResult.getFailureResults();

            assertThat(failureResults).hasSize(2);
            assertThat(failureResults).allMatch(r -> !r.success());
        }
    }

    @Nested
    @DisplayName("getFailureCountsByCode方法测试")
    class GetFailureCountsByCodeTests {

        @Test
        @DisplayName("按错误码分组计数")
        void testGetFailureCountsByCode() {
            Instant startTime = Instant.now();
            List<SmsResult> results = List.of(
                    SmsResult.failure("13800138001", "ERR1", "failed"),
                    SmsResult.failure("13800138002", "ERR1", "failed"),
                    SmsResult.failure("13800138003", "ERR2", "failed")
            );

            BatchResult batchResult = BatchResult.of(results, startTime);

            Map<String, Long> countsByCode = batchResult.getFailureCountsByCode();

            assertThat(countsByCode).containsEntry("ERR1", 2L);
            assertThat(countsByCode).containsEntry("ERR2", 1L);
        }
    }

    @Nested
    @DisplayName("getThroughput方法测试")
    class GetThroughputTests {

        @Test
        @DisplayName("计算吞吐量")
        void testGetThroughput() {
            Instant start = Instant.now();
            Instant end = start.plusSeconds(2);

            List<SmsResult> results = List.of(
                    SmsResult.success("msg1", "13800138001"),
                    SmsResult.success("msg2", "13800138002"),
                    SmsResult.success("msg3", "13800138003"),
                    SmsResult.success("msg4", "13800138004")
            );

            BatchResult batchResult = new BatchResult(
                    results, 4, 4, 0, start, end
            );

            double throughput = batchResult.getThroughput();

            assertThat(throughput).isEqualTo(2.0); // 4 messages / 2 seconds
        }
    }
}

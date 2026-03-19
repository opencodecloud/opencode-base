package cloud.opencode.base.io.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * BatchResult 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("BatchResult 测试")
class BatchResultTest {

    @Nested
    @DisplayName("记录构造测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("创建BatchResult记录")
        void testCreateRecord() {
            Instant start = Instant.now();
            Instant end = start.plusMillis(100);
            Map<Path, Throwable> failures = Map.of();

            BatchResult result = new BatchResult("copy", 10, 8, 1, 1, failures, start, end);

            assertThat(result.operation()).isEqualTo("copy");
            assertThat(result.totalCount()).isEqualTo(10);
            assertThat(result.successCount()).isEqualTo(8);
            assertThat(result.failureCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(1);
            assertThat(result.failures()).isEmpty();
            assertThat(result.startTime()).isEqualTo(start);
            assertThat(result.endTime()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("isAllSuccess方法测试")
    class IsAllSuccessTests {

        @Test
        @DisplayName("全部成功返回true")
        void testAllSuccess() {
            BatchResult result = createResult(10, 10, 0, 0);

            assertThat(result.isAllSuccess()).isTrue();
        }

        @Test
        @DisplayName("有失败返回false")
        void testHasFailure() {
            BatchResult result = createResult(10, 9, 1, 0);

            assertThat(result.isAllSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasFailures方法测试")
    class HasFailuresTests {

        @Test
        @DisplayName("有失败返回true")
        void testHasFailures() {
            BatchResult result = createResult(10, 8, 2, 0);

            assertThat(result.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("无失败返回false")
        void testNoFailures() {
            BatchResult result = createResult(10, 10, 0, 0);

            assertThat(result.hasFailures()).isFalse();
        }
    }

    @Nested
    @DisplayName("isPartialSuccess方法测试")
    class IsPartialSuccessTests {

        @Test
        @DisplayName("部分成功返回true")
        void testPartialSuccess() {
            BatchResult result = createResult(10, 5, 5, 0);

            assertThat(result.isPartialSuccess()).isTrue();
        }

        @Test
        @DisplayName("全部成功返回false")
        void testAllSuccessNotPartial() {
            BatchResult result = createResult(10, 10, 0, 0);

            assertThat(result.isPartialSuccess()).isFalse();
        }

        @Test
        @DisplayName("全部失败返回false")
        void testAllFailedNotPartial() {
            BatchResult result = createResult(10, 0, 10, 0);

            assertThat(result.isPartialSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("duration方法测试")
    class DurationTests {

        @Test
        @DisplayName("计算正确耗时")
        void testDuration() {
            Instant start = Instant.now();
            Instant end = start.plusMillis(500);

            BatchResult result = new BatchResult("test", 1, 1, 0, 0, Map.of(), start, end);

            assertThat(result.duration()).isEqualTo(Duration.ofMillis(500));
        }
    }

    @Nested
    @DisplayName("successRate方法测试")
    class SuccessRateTests {

        @Test
        @DisplayName("计算成功率")
        void testSuccessRate() {
            BatchResult result = createResult(10, 8, 2, 0);

            assertThat(result.successRate()).isEqualTo(0.8);
        }

        @Test
        @DisplayName("全部成功成功率为1")
        void testAllSuccessRate() {
            BatchResult result = createResult(10, 10, 0, 0);

            assertThat(result.successRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("全部失败成功率为0")
        void testAllFailedRate() {
            BatchResult result = createResult(10, 0, 10, 0);

            assertThat(result.successRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("空结果成功率为1")
        void testEmptyResultRate() {
            BatchResult result = createResult(0, 0, 0, 0);

            assertThat(result.successRate()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("failedPaths方法测试")
    class FailedPathsTests {

        @Test
        @DisplayName("返回失败路径列表")
        void testFailedPaths() {
            Path path1 = Path.of("/test/file1.txt");
            Path path2 = Path.of("/test/file2.txt");
            Map<Path, Throwable> failures = Map.of(
                path1, new RuntimeException("error1"),
                path2, new RuntimeException("error2")
            );
            Instant now = Instant.now();
            BatchResult result = new BatchResult("copy", 4, 2, 2, 0, failures, now, now);

            assertThat(result.failedPaths()).containsExactlyInAnyOrder(path1, path2);
        }

        @Test
        @DisplayName("无失败返回空列表")
        void testNoFailedPaths() {
            BatchResult result = createResult(5, 5, 0, 0);

            assertThat(result.failedPaths()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFailure方法测试")
    class GetFailureTests {

        @Test
        @DisplayName("获取指定路径异常")
        void testGetFailure() {
            Path path = Path.of("/test/file.txt");
            RuntimeException error = new RuntimeException("test error");
            Map<Path, Throwable> failures = Map.of(path, error);
            Instant now = Instant.now();
            BatchResult result = new BatchResult("copy", 1, 0, 1, 0, failures, now, now);

            assertThat(result.getFailure(path)).contains(error);
        }

        @Test
        @DisplayName("路径不存在返回空")
        void testGetFailureNotFound() {
            BatchResult result = createResult(5, 5, 0, 0);

            assertThat(result.getFailure(Path.of("/nonexistent"))).isEmpty();
        }
    }

    @Nested
    @DisplayName("summary方法测试")
    class SummaryTests {

        @Test
        @DisplayName("生成摘要字符串")
        void testSummary() {
            BatchResult result = createResult(10, 8, 1, 1);

            String summary = result.summary();

            assertThat(summary).contains("test");
            assertThat(summary).contains("10 total");
            assertThat(summary).contains("8 success");
            assertThat(summary).contains("1 failed");
            assertThat(summary).contains("1 skipped");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回summary")
        void testToString() {
            BatchResult result = createResult(5, 5, 0, 0);

            assertThat(result.toString()).isEqualTo(result.summary());
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("创建Builder")
        void testCreateBuilder() {
            BatchResult.Builder builder = BatchResult.builder("copy");

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("记录成功")
        void testRecordSuccess() {
            BatchResult.Builder builder = BatchResult.builder("copy");

            builder.success().success().success();
            BatchResult result = builder.build();

            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.totalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("记录失败")
        void testRecordFailure() {
            BatchResult.Builder builder = BatchResult.builder("copy");
            Path path = Path.of("/test/file.txt");
            RuntimeException error = new RuntimeException("test");

            builder.failure(path, error);
            BatchResult result = builder.build();

            assertThat(result.failureCount()).isEqualTo(1);
            assertThat(result.failures()).containsKey(path);
        }

        @Test
        @DisplayName("记录跳过")
        void testRecordSkipped() {
            BatchResult.Builder builder = BatchResult.builder("copy");

            builder.skipped().skipped();
            BatchResult result = builder.build();

            assertThat(result.skippedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("原子递增成功计数")
        void testIncrementSuccess() {
            BatchResult.Builder builder = BatchResult.builder("copy");

            builder.incrementSuccess();
            builder.incrementSuccess();
            BatchResult result = builder.build();

            assertThat(result.successCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("原子记录失败")
        void testRecordFailureAtomic() {
            BatchResult.Builder builder = BatchResult.builder("copy");
            Path path = Path.of("/test/file.txt");

            builder.recordFailure(path, new RuntimeException("test"));
            BatchResult result = builder.build();

            assertThat(result.failureCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("原子递增跳过计数")
        void testIncrementSkipped() {
            BatchResult.Builder builder = BatchResult.builder("copy");

            builder.incrementSkipped();
            BatchResult result = builder.build();

            assertThat(result.skippedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("混合操作")
        void testMixedOperations() {
            BatchResult.Builder builder = BatchResult.builder("move");

            builder.success()
                   .success()
                   .failure(Path.of("/fail1"), new RuntimeException())
                   .skipped()
                   .success();

            BatchResult result = builder.build();

            assertThat(result.totalCount()).isEqualTo(5);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.failureCount()).isEqualTo(1);
            assertThat(result.skippedCount()).isEqualTo(1);
            assertThat(result.operation()).isEqualTo("move");
        }

        @Test
        @DisplayName("空构建")
        void testEmptyBuild() {
            BatchResult.Builder builder = BatchResult.builder("delete");

            BatchResult result = builder.build();

            assertThat(result.totalCount()).isEqualTo(0);
            assertThat(result.isAllSuccess()).isTrue();
        }
    }

    private BatchResult createResult(int total, int success, int failure, int skipped) {
        Instant now = Instant.now();
        return new BatchResult("test", total, success, failure, skipped, Map.of(), now, now.plusMillis(10));
    }
}

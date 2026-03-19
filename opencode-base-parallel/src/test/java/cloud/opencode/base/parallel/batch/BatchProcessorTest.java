package cloud.opencode.base.parallel.batch;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * BatchProcessorTest Tests
 * BatchProcessorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("BatchProcessor 测试")
class BatchProcessorTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder创建构建器")
        void testBuilder() {
            BatchProcessor.Builder builder = BatchProcessor.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("defaultProcessor创建默认处理器")
        void testDefaultProcessor() {
            BatchProcessor processor = BatchProcessor.defaultProcessor();

            assertThat(processor.getBatchSize()).isEqualTo(100);
            assertThat(processor.getParallelism()).isEqualTo(Runtime.getRuntime().availableProcessors());
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("设置批大小")
        void testBatchSize() {
            BatchProcessor processor = BatchProcessor.builder()
                    .batchSize(50)
                    .build();

            assertThat(processor.getBatchSize()).isEqualTo(50);
        }

        @Test
        @DisplayName("批大小必须为正数")
        void testBatchSizeMustBePositive() {
            assertThatThrownBy(() -> BatchProcessor.builder().batchSize(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");

            assertThatThrownBy(() -> BatchProcessor.builder().batchSize(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("设置并行度")
        void testParallelism() {
            BatchProcessor processor = BatchProcessor.builder()
                    .parallelism(4)
                    .build();

            assertThat(processor.getParallelism()).isEqualTo(4);
        }

        @Test
        @DisplayName("并行度必须为正数")
        void testParallelismMustBePositive() {
            assertThatThrownBy(() -> BatchProcessor.builder().parallelism(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("设置超时")
        void testTimeout() {
            BatchProcessor processor = BatchProcessor.builder()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            assertThat(processor).isNotNull();
        }

        @Test
        @DisplayName("设置停止错误标志")
        void testStopOnError() {
            BatchProcessor processor = BatchProcessor.builder()
                    .stopOnError(true)
                    .build();

            assertThat(processor).isNotNull();
        }

        @Test
        @DisplayName("链式调用构建")
        void testChainedBuilder() {
            BatchProcessor processor = BatchProcessor.builder()
                    .batchSize(25)
                    .parallelism(2)
                    .timeout(Duration.ofMinutes(1))
                    .stopOnError(true)
                    .build();

            assertThat(processor.getBatchSize()).isEqualTo(25);
            assertThat(processor.getParallelism()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("process方法测试")
    class ProcessTests {

        @Test
        @DisplayName("批量处理项目")
        void testProcess() {
            List<Integer> items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            List<Integer> processed = Collections.synchronizedList(new ArrayList<>());

            BatchProcessor processor = BatchProcessor.builder()
                    .batchSize(3)
                    .parallelism(2)
                    .build();

            processor.process(items, batch -> processed.addAll(batch));

            assertThat(processed).containsExactlyInAnyOrderElementsOf(items);
        }

        @Test
        @DisplayName("空列表不处理")
        void testProcessEmpty() {
            AtomicInteger callCount = new AtomicInteger(0);

            BatchProcessor processor = BatchProcessor.defaultProcessor();
            processor.process(List.of(), batch -> callCount.incrementAndGet());

            assertThat(callCount.get()).isZero();
        }
    }

    @Nested
    @DisplayName("processAndCollect方法测试")
    class ProcessAndCollectTests {

        @Test
        @DisplayName("批量处理并收集结果")
        void testProcessAndCollect() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            BatchProcessor processor = BatchProcessor.builder()
                    .batchSize(2)
                    .build();

            List<Integer> results = processor.processAndCollect(items,
                    batch -> batch.stream().map(x -> x * 2).toList());

            assertThat(results).containsExactlyInAnyOrder(2, 4, 6, 8, 10);
        }
    }

    @Nested
    @DisplayName("processWithProgress方法测试")
    class ProcessWithProgressTests {

        @Test
        @DisplayName("批量处理并回调进度")
        void testProcessWithProgress() {
            List<Integer> items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            List<BatchProcessor.BatchProgress> progressList = Collections.synchronizedList(new ArrayList<>());

            BatchProcessor processor = BatchProcessor.builder()
                    .batchSize(3)
                    .parallelism(1)
                    .build();

            processor.processWithProgress(items, batch -> {}, progressList::add);

            assertThat(progressList).hasSize(4); // ceil(10/3) = 4 batches
            assertThat(progressList.get(progressList.size() - 1).isComplete()).isTrue();
        }
    }

    @Nested
    @DisplayName("BatchProgress测试")
    class BatchProgressTests {

        @Test
        @DisplayName("percentage计算进度百分比")
        void testPercentage() {
            BatchProcessor.BatchProgress progress1 = new BatchProcessor.BatchProgress(5, 10, 3);
            BatchProcessor.BatchProgress progress2 = new BatchProcessor.BatchProgress(10, 10, 3);

            assertThat(progress1.percentage()).isEqualTo(50);
            assertThat(progress2.percentage()).isEqualTo(100);
        }

        @Test
        @DisplayName("totalBatches为0时percentage返回100")
        void testPercentageZeroTotal() {
            BatchProcessor.BatchProgress progress = new BatchProcessor.BatchProgress(0, 0, 0);

            assertThat(progress.percentage()).isEqualTo(100);
        }

        @Test
        @DisplayName("isComplete检查是否完成")
        void testIsComplete() {
            BatchProcessor.BatchProgress incomplete = new BatchProcessor.BatchProgress(5, 10, 3);
            BatchProcessor.BatchProgress complete = new BatchProcessor.BatchProgress(10, 10, 3);

            assertThat(incomplete.isComplete()).isFalse();
            assertThat(complete.isComplete()).isTrue();
        }

        @Test
        @DisplayName("record属性访问")
        void testRecordAccessors() {
            BatchProcessor.BatchProgress progress = new BatchProcessor.BatchProgress(5, 10, 25);

            assertThat(progress.completedBatches()).isEqualTo(5);
            assertThat(progress.totalBatches()).isEqualTo(10);
            assertThat(progress.lastBatchSize()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("stopOnError为false时继续处理并最后抛出部分失败异常")
        void testContinueOnError() {
            List<Integer> items = List.of(1, 2, 3, 4, 5, 6);
            AtomicInteger processedCount = new AtomicInteger(0);

            BatchProcessor processor = BatchProcessor.builder()
                    .batchSize(2)
                    .parallelism(1)
                    .stopOnError(false)
                    .build();

            assertThatThrownBy(() -> processor.process(items, batch -> {
                processedCount.addAndGet(batch.size());
                if (batch.contains(3)) {
                    throw new RuntimeException("error on batch with 3");
                }
            })).isInstanceOf(OpenParallelException.class);

            // All batches should be attempted
            assertThat(processedCount.get()).isEqualTo(6);
        }

        @Test
        @DisplayName("stopOnError为true时立即停止")
        void testStopOnError() {
            List<Integer> items = List.of(1, 2, 3, 4, 5, 6);

            BatchProcessor processor = BatchProcessor.builder()
                    .batchSize(2)
                    .parallelism(1)
                    .stopOnError(true)
                    .build();

            assertThatThrownBy(() -> processor.process(items, batch -> {
                if (batch.contains(1)) {
                    throw new RuntimeException("error");
                }
            })).isInstanceOf(OpenParallelException.class);
        }
    }
}

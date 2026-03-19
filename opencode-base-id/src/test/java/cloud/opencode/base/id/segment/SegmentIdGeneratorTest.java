package cloud.opencode.base.id.segment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

/**
 * SegmentIdGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("SegmentIdGenerator 测试")
class SegmentIdGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create();
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator);

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("使用业务标识创建")
        void testCreateWithBizTag() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create();
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");

            assertThat(gen).isNotNull();
            assertThat(gen.getBizTag()).isEqualTo("order");
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成ID")
        void testGenerate() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(100);
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");

            Long id = gen.generate();

            assertThat(id).isNotNull();
            assertThat(id).isEqualTo(0L);
        }

        @Test
        @DisplayName("生成递增ID")
        void testGenerateIncreasing() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(100);
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");

            Long id1 = gen.generate();
            Long id2 = gen.generate();
            Long id3 = gen.generate();

            assertThat(id1).isEqualTo(0L);
            assertThat(id2).isEqualTo(1L);
            assertThat(id3).isEqualTo(2L);
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(100);
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");
            Set<Long> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("跨号段生成")
        void testGenerateCrossSegment() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(10);
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");

            // 生成超过一个号段的ID
            Long prevId = null;
            for (int i = 0; i < 25; i++) {
                Long id = gen.generate();
                assertThat(id).isNotNull();
                if (prevId != null) {
                    // 每个ID应该大于前一个ID（双缓冲预加载可能导致跳跃）
                    assertThat(id).isGreaterThan(prevId);
                }
                prevId = id;
            }
        }
    }

    @Nested
    @DisplayName("缓冲区状态测试")
    class BufferStatusTests {

        @Test
        @DisplayName("获取缓冲区状态")
        void testGetBufferStatus() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(100);
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");

            gen.generate();

            var status = gen.getBufferStatus();

            assertThat(status).isNotNull();
            assertThat(status.maxValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("BufferStatus toString方法")
        void testBufferStatusToString() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(100);
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");

            gen.generate();

            var status = gen.getBufferStatus();

            assertThat(status.toString()).contains("BufferStatus");
            assertThat(status.toString()).contains("max=");
        }
    }

    @Nested
    @DisplayName("类型测试")
    class TypeTests {

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create();
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator);

            assertThat(gen.getType()).isEqualTo("Segment");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(1000);
            SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");
            int threadCount = 10;
            int idsPerThread = 100;
            Set<Long> ids = ConcurrentHashMap.newKeySet();
            CountDownLatch latch = new CountDownLatch(threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < idsPerThread; j++) {
                            ids.add(gen.generate());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertThat(ids).hasSize(threadCount * idsPerThread);
        }
    }
}

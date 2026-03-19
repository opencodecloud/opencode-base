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
 * MemorySegmentAllocator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("MemorySegmentAllocator 测试")
class MemorySegmentAllocatorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create();

            assertThat(allocator).isNotNull();
            assertThat(allocator.getStep()).isEqualTo(SegmentAllocator.DEFAULT_STEP);
        }

        @Test
        @DisplayName("使用自定义步长创建")
        void testCreateWithStep() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(5000);

            assertThat(allocator).isNotNull();
            assertThat(allocator.getStep()).isEqualTo(5000);
        }

        @Test
        @DisplayName("使用自定义起始值和步长创建")
        void testCreateWithStartValueAndStep() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(10000, 2000);

            assertThat(allocator).isNotNull();
            assertThat(allocator.getStep()).isEqualTo(2000);
        }

        @Test
        @DisplayName("无效步长抛出异常")
        void testInvalidStep() {
            assertThatThrownBy(() -> MemorySegmentAllocator.create(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> MemorySegmentAllocator.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("分配方法测试")
    class AllocateTests {

        @Test
        @DisplayName("分配号段")
        void testAllocate() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(1000);

            SegmentAllocator.Segment segment = allocator.allocate("order");

            assertThat(segment).isNotNull();
            assertThat(segment.start()).isEqualTo(0);
            assertThat(segment.end()).isEqualTo(1000);
            assertThat(segment.step()).isEqualTo(1000);
        }

        @Test
        @DisplayName("多次分配递增")
        void testAllocateMultiple() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(1000);

            SegmentAllocator.Segment segment1 = allocator.allocate("order");
            SegmentAllocator.Segment segment2 = allocator.allocate("order");
            SegmentAllocator.Segment segment3 = allocator.allocate("order");

            assertThat(segment1.start()).isEqualTo(0);
            assertThat(segment2.start()).isEqualTo(1000);
            assertThat(segment3.start()).isEqualTo(2000);
        }

        @Test
        @DisplayName("不同业务标识独立")
        void testAllocateDifferentTags() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(1000);

            SegmentAllocator.Segment orderSegment = allocator.allocate("order");
            SegmentAllocator.Segment userSegment = allocator.allocate("user");

            assertThat(orderSegment.start()).isEqualTo(0);
            assertThat(userSegment.start()).isEqualTo(0);
        }

        @Test
        @DisplayName("自定义起始值")
        void testAllocateWithStartValue() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(10000, 1000);

            SegmentAllocator.Segment segment = allocator.allocate("order");

            assertThat(segment.start()).isEqualTo(10000);
            assertThat(segment.end()).isEqualTo(11000);
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryTests {

        @Test
        @DisplayName("getCurrentValue方法")
        void testGetCurrentValue() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(1000);

            assertThat(allocator.getCurrentValue("order")).isEqualTo(0);

            allocator.allocate("order");

            assertThat(allocator.getCurrentValue("order")).isEqualTo(1000);
        }

        @Test
        @DisplayName("getTagCount方法")
        void testGetTagCount() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create();

            assertThat(allocator.getTagCount()).isEqualTo(0);

            allocator.allocate("order");
            allocator.allocate("user");

            assertThat(allocator.getTagCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("重置方法测试")
    class ResetTests {

        @Test
        @DisplayName("reset方法")
        void testReset() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(1000);
            allocator.allocate("order");
            allocator.allocate("order");

            allocator.reset("order");

            SegmentAllocator.Segment segment = allocator.allocate("order");
            assertThat(segment.start()).isEqualTo(0);
        }

        @Test
        @DisplayName("resetAll方法")
        void testResetAll() {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create();
            allocator.allocate("order");
            allocator.allocate("user");

            allocator.resetAll();

            assertThat(allocator.getTagCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程分配")
        void testConcurrentAllocate() throws InterruptedException {
            MemorySegmentAllocator allocator = MemorySegmentAllocator.create(100);
            int threadCount = 10;
            Set<Long> startValues = ConcurrentHashMap.newKeySet();
            CountDownLatch latch = new CountDownLatch(threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        SegmentAllocator.Segment segment = allocator.allocate("order");
                        startValues.add(segment.start());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            // 每个线程应该获得不同的起始值
            assertThat(startValues).hasSize(threadCount);
        }
    }
}

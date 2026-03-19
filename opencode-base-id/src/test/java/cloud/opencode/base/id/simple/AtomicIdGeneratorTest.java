package cloud.opencode.base.id.simple;

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
 * AtomicIdGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("AtomicIdGenerator 测试")
class AtomicIdGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            AtomicIdGenerator gen = AtomicIdGenerator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("默认从1开始")
        void testDefaultStartsFromOne() {
            AtomicIdGenerator gen = AtomicIdGenerator.create();

            assertThat(gen.generate()).isEqualTo(1L);
        }

        @Test
        @DisplayName("使用指定起始值创建")
        void testCreateWithStartValue() {
            AtomicIdGenerator gen = AtomicIdGenerator.create(100);

            assertThat(gen.generate()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成递增ID")
        void testGenerate() {
            AtomicIdGenerator gen = AtomicIdGenerator.create();

            assertThat(gen.generate()).isEqualTo(1L);
            assertThat(gen.generate()).isEqualTo(2L);
            assertThat(gen.generate()).isEqualTo(3L);
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            AtomicIdGenerator gen = AtomicIdGenerator.create();
            Set<Long> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("当前值测试")
    class CurrentValueTests {

        @Test
        @DisplayName("获取当前值不递增")
        void testGetCurrentValue() {
            AtomicIdGenerator gen = AtomicIdGenerator.create();
            gen.generate(); // 1
            gen.generate(); // 2

            assertThat(gen.getCurrentValue()).isEqualTo(3L);
            assertThat(gen.getCurrentValue()).isEqualTo(3L); // 不变
        }
    }

    @Nested
    @DisplayName("重置测试")
    class ResetTests {

        @Test
        @DisplayName("重置计数器")
        void testReset() {
            AtomicIdGenerator gen = AtomicIdGenerator.create();
            gen.generate();
            gen.generate();
            gen.generate();

            gen.reset(100);

            assertThat(gen.generate()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("类型测试")
    class TypeTests {

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            AtomicIdGenerator gen = AtomicIdGenerator.create();

            assertThat(gen.getType()).isEqualTo("Atomic");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            AtomicIdGenerator gen = AtomicIdGenerator.create();
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

package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

/**
 * SnowflakeGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("SnowflakeGenerator 测试")
class SnowflakeGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("使用默认参数创建")
        void testCreate() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();

            assertThat(gen).isNotNull();
            assertThat(gen.getType()).isEqualTo("Snowflake");
        }

        @Test
        @DisplayName("使用workerId和datacenterId创建")
        void testCreateWithIds() {
            SnowflakeGenerator gen = SnowflakeGenerator.create(1, 2);

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("无效workerId抛出异常")
        void testInvalidWorkerId() {
            assertThatThrownBy(() -> SnowflakeGenerator.create(32, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效datacenterId抛出异常")
        void testInvalidDatacenterId() {
            assertThatThrownBy(() -> SnowflakeGenerator.create(0, 32))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数workerId抛出异常")
        void testNegativeWorkerId() {
            assertThatThrownBy(() -> SnowflakeGenerator.create(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("生成ID测试")
    class GenerateTests {

        @Test
        @DisplayName("生成正数ID")
        void testGeneratePositive() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();

            long id = gen.generate();

            assertThat(id).isPositive();
        }

        @Test
        @DisplayName("生成唯一ID")
        void testGenerateUnique() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            Set<Long> ids = new HashSet<>();

            for (int i = 0; i < 10000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(10000);
        }

        @Test
        @DisplayName("ID递增")
        void testGenerateIncreasing() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();

            long id1 = gen.generate();
            long id2 = gen.generate();
            long id3 = gen.generate();

            assertThat(id2).isGreaterThan(id1);
            assertThat(id3).isGreaterThan(id2);
        }

        @Test
        @DisplayName("generateBatch生成多个ID")
        void testGenerateBatch() {
            SnowflakeGenerator gen = SnowflakeGenerator.create();

            var ids = gen.generateBatch(100);

            assertThat(ids).hasSize(100);
            assertThat(new HashSet<>(ids)).hasSize(100); // All unique
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGenerate() throws InterruptedException {
            SnowflakeGenerator gen = SnowflakeGenerator.create();
            Set<Long> ids = java.util.Collections.synchronizedSet(new HashSet<>());
            int threadCount = 10;
            int idsPerThread = 1000;
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

    @Nested
    @DisplayName("配置测试")
    class ConfigTests {

        @Test
        @DisplayName("获取workerId")
        void testGetWorkerId() {
            SnowflakeGenerator gen = SnowflakeGenerator.create(5, 10);

            assertThat(gen.getWorkerId()).isEqualTo(5);
        }

        @Test
        @DisplayName("获取datacenterId")
        void testGetDatacenterId() {
            SnowflakeGenerator gen = SnowflakeGenerator.create(5, 10);

            assertThat(gen.getDatacenterId()).isEqualTo(10);
        }
    }
}

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
 * TimestampIdGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("TimestampIdGenerator 测试")
class TimestampIdGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            TimestampIdGenerator gen = TimestampIdGenerator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("使用指定随机长度创建")
        void testCreateWithRandomLength() {
            TimestampIdGenerator gen = TimestampIdGenerator.create(10);

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("无效随机长度抛出异常")
        void testInvalidRandomLength() {
            assertThatThrownBy(() -> TimestampIdGenerator.create(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> TimestampIdGenerator.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> TimestampIdGenerator.create(21))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成有效ID")
        void testGenerate() {
            TimestampIdGenerator gen = TimestampIdGenerator.create();

            String id = gen.generate();

            assertThat(id).isNotNull();
            // 格式：yyyyMMddHHmmssSSS + 6位随机数 = 17 + 6 = 23
            assertThat(id).hasSize(23);
            assertThat(id).matches("[0-9]+");
        }

        @Test
        @DisplayName("生成带前缀的ID")
        void testGenerateWithPrefix() {
            TimestampIdGenerator gen = TimestampIdGenerator.create();

            String id = gen.generate("ORD");

            assertThat(id).isNotNull();
            assertThat(id).startsWith("ORD");
            assertThat(id).hasSize(26); // ORD + 23
        }

        @Test
        @DisplayName("自定义随机长度")
        void testCustomRandomLength() {
            TimestampIdGenerator gen = TimestampIdGenerator.create(10);

            String id = gen.generate();

            // 格式：yyyyMMddHHmmssSSS + 10位随机数 = 17 + 10 = 27
            assertThat(id).hasSize(27);
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            TimestampIdGenerator gen = TimestampIdGenerator.create();
            Set<String> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("ID时间有序")
        void testTimeOrdered() {
            TimestampIdGenerator gen = TimestampIdGenerator.create();

            String id1 = gen.generate();
            String id2 = gen.generate();
            String id3 = gen.generate();

            // 时间戳部分应该相等或递增
            String timestamp1 = id1.substring(0, 17);
            String timestamp2 = id2.substring(0, 17);
            String timestamp3 = id3.substring(0, 17);

            assertThat(timestamp1.compareTo(timestamp2)).isLessThanOrEqualTo(0);
            assertThat(timestamp2.compareTo(timestamp3)).isLessThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("类型测试")
    class TypeTests {

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            TimestampIdGenerator gen = TimestampIdGenerator.create();

            assertThat(gen.getType()).isEqualTo("Timestamp");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            TimestampIdGenerator gen = TimestampIdGenerator.create();
            int threadCount = 10;
            int idsPerThread = 100;
            Set<String> ids = ConcurrentHashMap.newKeySet();
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

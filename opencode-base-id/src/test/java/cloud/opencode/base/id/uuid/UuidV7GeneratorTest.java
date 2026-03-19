package cloud.opencode.base.id.uuid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

/**
 * UuidV7Generator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("UuidV7Generator 测试")
class UuidV7GeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            UuidV7Generator gen = UuidV7Generator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            UuidV7Generator gen1 = UuidV7Generator.create();
            UuidV7Generator gen2 = UuidV7Generator.create();

            assertThat(gen1).isSameAs(gen2);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成UUID v7")
        void testGenerate() {
            UuidV7Generator gen = UuidV7Generator.create();

            UUID uuid = gen.generate();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(7);
        }

        @Test
        @DisplayName("生成字符串")
        void testGenerateStr() {
            UuidV7Generator gen = UuidV7Generator.create();

            String uuidStr = gen.generateStr();

            assertThat(uuidStr).isNotNull();
            assertThat(uuidStr).hasSize(36);
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            UuidV7Generator gen = UuidV7Generator.create();
            Set<UUID> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("生成时间有序")
        void testTimeOrdered() {
            UuidV7Generator gen = UuidV7Generator.create();

            UUID uuid1 = gen.generate();
            UUID uuid2 = gen.generate();
            UUID uuid3 = gen.generate();

            // UUID v7 should be time-ordered (lexicographically sortable)
            assertThat(uuid1.compareTo(uuid2)).isLessThanOrEqualTo(0);
            assertThat(uuid2.compareTo(uuid3)).isLessThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("时间戳提取测试")
    class TimestampTests {

        @Test
        @DisplayName("提取时间戳")
        void testExtractTimestamp() {
            UuidV7Generator gen = UuidV7Generator.create();
            long before = System.currentTimeMillis();
            UUID uuid = gen.generate();
            long after = System.currentTimeMillis();

            long timestamp = UuidV7Generator.extractTimestamp(uuid);

            assertThat(timestamp).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("检查是否为v7")
        void testIsV7() {
            UuidV7Generator gen = UuidV7Generator.create();
            UUID uuid = gen.generate();

            assertThat(UuidV7Generator.isV7(uuid)).isTrue();
        }

        @Test
        @DisplayName("v4不是v7")
        void testIsV7ForV4() {
            UUID uuid = UUID.randomUUID();

            assertThat(UuidV7Generator.isV7(uuid)).isFalse();
        }
    }

    @Nested
    @DisplayName("类型测试")
    class TypeTests {

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            UuidV7Generator gen = UuidV7Generator.create();

            assertThat(gen.getType()).isEqualTo("UUID-v7");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            UuidV7Generator gen = UuidV7Generator.create();
            int threadCount = 10;
            int idsPerThread = 100;
            Set<UUID> ids = ConcurrentHashMap.newKeySet();
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

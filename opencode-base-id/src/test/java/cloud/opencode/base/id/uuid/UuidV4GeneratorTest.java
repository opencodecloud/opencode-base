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
 * UuidV4Generator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("UuidV4Generator 测试")
class UuidV4GeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            UuidV4Generator gen = UuidV4Generator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            UuidV4Generator gen1 = UuidV4Generator.create();
            UuidV4Generator gen2 = UuidV4Generator.create();

            assertThat(gen1).isSameAs(gen2);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成UUID")
        void testGenerate() {
            UuidV4Generator gen = UuidV4Generator.create();

            UUID uuid = gen.generate();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(4);
        }

        @Test
        @DisplayName("生成字符串")
        void testGenerateStr() {
            UuidV4Generator gen = UuidV4Generator.create();

            String uuidStr = gen.generateStr();

            assertThat(uuidStr).isNotNull();
            assertThat(uuidStr).hasSize(36);
        }

        @Test
        @DisplayName("生成简化字符串")
        void testGenerateSimple() {
            UuidV4Generator gen = UuidV4Generator.create();

            String simple = gen.generateSimple();

            assertThat(simple).isNotNull();
            assertThat(simple).hasSize(32);
            assertThat(simple).doesNotContain("-");
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            UuidV4Generator gen = UuidV4Generator.create();
            Set<UUID> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("类型测试")
    class TypeTests {

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            UuidV4Generator gen = UuidV4Generator.create();

            assertThat(gen.getType()).isEqualTo("UUID-v4");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            UuidV4Generator gen = UuidV4Generator.create();
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

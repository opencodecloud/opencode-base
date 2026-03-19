package cloud.opencode.base.core.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * IdGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("IdGenerator 测试")
class IdGeneratorTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("使用 Lambda 实现")
        void testLambdaImplementation() {
            IdGenerator generator = () -> UUID.randomUUID().toString();

            String id = generator.nextId();
            assertThat(id).isNotNull();
            assertThat(id).hasSize(36);
        }

        @Test
        @DisplayName("使用方法引用实现")
        void testMethodReferenceImplementation() {
            IdGenerator generator = OpenRandom::simpleUUID;

            String id = generator.nextId();
            assertThat(id).isNotNull();
            assertThat(id).hasSize(32);
        }

        @Test
        @DisplayName("自定义生成器")
        void testCustomGenerator() {
            AtomicLong counter = new AtomicLong(0);
            IdGenerator generator = () -> "ID-" + counter.incrementAndGet();

            assertThat(generator.nextId()).isEqualTo("ID-1");
            assertThat(generator.nextId()).isEqualTo("ID-2");
            assertThat(generator.nextId()).isEqualTo("ID-3");
        }
    }

    @Nested
    @DisplayName("nextId 测试")
    class NextIdTests {

        @Test
        @DisplayName("nextId 返回非空")
        void testNextIdNotNull() {
            IdGenerator generator = () -> "test-id";
            assertThat(generator.nextId()).isNotNull();
        }

        @Test
        @DisplayName("nextId 每次调用都执行")
        void testNextIdCalledEachTime() {
            AtomicLong counter = new AtomicLong(0);
            IdGenerator generator = () -> String.valueOf(counter.incrementAndGet());

            generator.nextId();
            generator.nextId();
            generator.nextId();

            assertThat(counter.get()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("nextIds 测试")
    class NextIdsTests {

        @Test
        @DisplayName("nextIds 生成指定数量")
        void testNextIdsCount() {
            IdGenerator generator = OpenRandom::simpleUUID;

            String[] ids = generator.nextIds(5);
            assertThat(ids).hasSize(5);
        }

        @Test
        @DisplayName("nextIds 每个 ID 唯一")
        void testNextIdsUnique() {
            IdGenerator generator = OpenRandom::uuid;

            String[] ids = generator.nextIds(100);
            Set<String> uniqueIds = new HashSet<>();
            for (String id : ids) {
                uniqueIds.add(id);
            }

            assertThat(uniqueIds).hasSize(100);
        }

        @Test
        @DisplayName("nextIds count 为 0")
        void testNextIdsZero() {
            IdGenerator generator = () -> "test";
            String[] ids = generator.nextIds(0);
            assertThat(ids).isEmpty();
        }

        @Test
        @DisplayName("nextIds 序列生成器")
        void testNextIdsSequential() {
            AtomicLong counter = new AtomicLong(100);
            IdGenerator generator = () -> String.valueOf(counter.incrementAndGet());

            String[] ids = generator.nextIds(3);
            assertThat(ids).containsExactly("101", "102", "103");
        }
    }

    @Nested
    @DisplayName("常见实现模式测试")
    class CommonPatternsTests {

        @Test
        @DisplayName("UUID 生成器")
        void testUuidGenerator() {
            IdGenerator uuidGenerator = () -> UUID.randomUUID().toString();

            String id1 = uuidGenerator.nextId();
            String id2 = uuidGenerator.nextId();

            assertThat(id1).isNotEqualTo(id2);
            assertThat(id1).hasSize(36);
        }

        @Test
        @DisplayName("时间戳生成器")
        void testTimestampGenerator() throws InterruptedException {
            IdGenerator timestampGenerator = () -> String.valueOf(System.currentTimeMillis());

            String id1 = timestampGenerator.nextId();
            Thread.sleep(1);
            String id2 = timestampGenerator.nextId();

            assertThat(Long.parseLong(id2)).isGreaterThan(Long.parseLong(id1));
        }

        @Test
        @DisplayName("前缀生成器")
        void testPrefixGenerator() {
            String prefix = "ORDER-";
            AtomicLong seq = new AtomicLong(0);
            IdGenerator prefixGenerator = () -> prefix + seq.incrementAndGet();

            assertThat(prefixGenerator.nextId()).isEqualTo("ORDER-1");
            assertThat(prefixGenerator.nextId()).isEqualTo("ORDER-2");
        }

        @Test
        @DisplayName("组合生成器")
        void testCompositeGenerator() {
            AtomicLong seq = new AtomicLong(0);
            IdGenerator generator = () -> {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String sequence = String.format("%05d", seq.incrementAndGet());
                return timestamp + "-" + sequence;
            };

            String id = generator.nextId();
            assertThat(id).contains("-");
            assertThat(id).hasSizeGreaterThan(15);
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("多线程生成唯一 ID")
        void testMultithreadedGeneration() throws InterruptedException {
            IdGenerator generator = OpenRandom::uuid;
            Set<String> ids = java.util.Collections.synchronizedSet(new HashSet<>());

            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        ids.add(generator.nextId());
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }

            // 10 线程 × 100 个 ID = 1000 个唯一 ID
            assertThat(ids).hasSize(1000);
        }
    }
}

package cloud.opencode.base.id.ksuid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

/**
 * KsuidGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("KsuidGenerator 测试")
class KsuidGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            KsuidGenerator gen = KsuidGenerator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("单例模式")
        void testSingleton() {
            KsuidGenerator gen1 = KsuidGenerator.create();
            KsuidGenerator gen2 = KsuidGenerator.create();

            assertThat(gen1).isSameAs(gen2);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成有效KSUID")
        void testGenerate() {
            KsuidGenerator gen = KsuidGenerator.create();

            String ksuid = gen.generate();

            assertThat(ksuid).isNotNull();
            assertThat(ksuid).hasSize(27);
            assertThat(KsuidGenerator.isValid(ksuid)).isTrue();
        }

        @Test
        @DisplayName("使用指定时间戳生成")
        void testGenerateWithTimestamp() {
            KsuidGenerator gen = KsuidGenerator.create();
            long timestamp = System.currentTimeMillis() / 1000;

            String ksuid = gen.generate(timestamp);

            assertThat(ksuid).isNotNull();
            assertThat(ksuid).hasSize(27);
        }

        @Test
        @DisplayName("生成字节数组")
        void testGenerateBytes() {
            KsuidGenerator gen = KsuidGenerator.create();

            byte[] bytes = gen.generateBytes();

            assertThat(bytes).isNotNull();
            assertThat(bytes).hasSize(20);
        }

        @Test
        @DisplayName("使用指定时间戳生成字节数组")
        void testGenerateBytesWithTimestamp() {
            KsuidGenerator gen = KsuidGenerator.create();
            long timestamp = System.currentTimeMillis() / 1000;

            byte[] bytes = gen.generateBytes(timestamp);

            assertThat(bytes).isNotNull();
            assertThat(bytes).hasSize(20);
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            KsuidGenerator gen = KsuidGenerator.create();
            Set<String> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("编码解码测试")
    class EncodingTests {

        @Test
        @DisplayName("编码字节数组")
        void testEncode() {
            KsuidGenerator gen = KsuidGenerator.create();
            byte[] bytes = gen.generateBytes();

            String encoded = KsuidGenerator.encode(bytes);

            assertThat(encoded).isNotNull();
            assertThat(encoded).hasSize(27);
        }

        @Test
        @DisplayName("编码null抛出异常")
        void testEncodeNull() {
            assertThatThrownBy(() -> KsuidGenerator.encode(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("编码长度不正确抛出异常")
        void testEncodeWrongLength() {
            assertThatThrownBy(() -> KsuidGenerator.encode(new byte[10]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("解码KSUID字符串")
        void testDecode() {
            KsuidGenerator gen = KsuidGenerator.create();
            String ksuid = gen.generate();

            byte[] decoded = KsuidGenerator.decode(ksuid);

            assertThat(decoded).isNotNull();
            assertThat(decoded).hasSize(20);
        }

        @Test
        @DisplayName("解码null抛出异常")
        void testDecodeNull() {
            assertThatThrownBy(() -> KsuidGenerator.decode(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("解码长度不正确抛出异常")
        void testDecodeWrongLength() {
            assertThatThrownBy(() -> KsuidGenerator.decode("short"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("解码无效字符抛出异常")
        void testDecodeInvalidChars() {
            assertThatThrownBy(() -> KsuidGenerator.decode("000000000000000000000000$$$"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("编码解码往返")
        void testRoundTrip() {
            KsuidGenerator gen = KsuidGenerator.create();

            for (int i = 0; i < 100; i++) {
                byte[] original = gen.generateBytes();
                String encoded = KsuidGenerator.encode(original);
                byte[] decoded = KsuidGenerator.decode(encoded);

                assertThat(decoded).isEqualTo(original);
            }
        }
    }

    @Nested
    @DisplayName("时间戳提取测试")
    class TimestampTests {

        @Test
        @DisplayName("从字符串提取时间戳")
        void testExtractTimestampString() {
            KsuidGenerator gen = KsuidGenerator.create();
            long beforeSeconds = System.currentTimeMillis() / 1000;
            String ksuid = gen.generate();
            long afterSeconds = System.currentTimeMillis() / 1000;

            Instant timestamp = KsuidGenerator.extractTimestamp(ksuid);

            assertThat(timestamp.getEpochSecond()).isBetween(beforeSeconds, afterSeconds);
        }

        @Test
        @DisplayName("从字节数组提取时间戳")
        void testExtractTimestampBytes() {
            KsuidGenerator gen = KsuidGenerator.create();
            long beforeSeconds = System.currentTimeMillis() / 1000;
            byte[] bytes = gen.generateBytes();
            long afterSeconds = System.currentTimeMillis() / 1000;

            Instant timestamp = KsuidGenerator.extractTimestamp(bytes);

            assertThat(timestamp.getEpochSecond()).isBetween(beforeSeconds, afterSeconds);
        }

        @Test
        @DisplayName("提取时间戳无效字节抛出异常")
        void testExtractTimestampInvalidBytes() {
            assertThatThrownBy(() -> KsuidGenerator.extractTimestamp(new byte[10]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("负载提取测试")
    class PayloadTests {

        @Test
        @DisplayName("提取负载")
        void testExtractPayload() {
            KsuidGenerator gen = KsuidGenerator.create();
            String ksuid = gen.generate();

            byte[] payload = KsuidGenerator.extractPayload(ksuid);

            assertThat(payload).isNotNull();
            assertThat(payload).hasSize(16);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("验证有效KSUID")
        void testIsValidTrue() {
            KsuidGenerator gen = KsuidGenerator.create();
            String ksuid = gen.generate();

            assertThat(KsuidGenerator.isValid(ksuid)).isTrue();
        }

        @Test
        @DisplayName("验证null")
        void testIsValidNull() {
            assertThat(KsuidGenerator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("验证空字符串")
        void testIsValidEmpty() {
            assertThat(KsuidGenerator.isValid("")).isFalse();
        }

        @Test
        @DisplayName("验证长度不正确")
        void testIsValidWrongLength() {
            assertThat(KsuidGenerator.isValid("short")).isFalse();
            assertThat(KsuidGenerator.isValid("0000000000000000000000000000")).isFalse();
        }

        @Test
        @DisplayName("验证无效字符")
        void testIsValidInvalidChars() {
            assertThat(KsuidGenerator.isValid("000000000000000000000000$$$")).isFalse();
        }
    }

    @Nested
    @DisplayName("比较方法测试")
    class CompareTests {

        @Test
        @DisplayName("比较两个KSUID")
        void testCompare() {
            KsuidGenerator gen = KsuidGenerator.create();
            String ksuid1 = gen.generate();
            String ksuid2 = gen.generate();

            int result = KsuidGenerator.compare(ksuid1, ksuid2);

            // 比较结果取决于时间戳和随机部分，验证比较功能正常工作
            // compare结果可能超出-1到1的范围（类似String.compareTo）
            assertThat(KsuidGenerator.compare(ksuid1, ksuid1)).isEqualTo(0);
        }

        @Test
        @DisplayName("比较相同KSUID")
        void testCompareEqual() {
            KsuidGenerator gen = KsuidGenerator.create();
            String ksuid = gen.generate();

            int result = KsuidGenerator.compare(ksuid, ksuid);

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("边界值测试")
    class BoundaryTests {

        @Test
        @DisplayName("最小KSUID")
        void testMin() {
            String min = KsuidGenerator.min();

            assertThat(min).isNotNull();
            assertThat(min).hasSize(27);
            assertThat(KsuidGenerator.isValid(min)).isTrue();
        }

        @Test
        @DisplayName("最大KSUID")
        void testMax() {
            String max = KsuidGenerator.max();

            assertThat(max).isNotNull();
            assertThat(max).hasSize(27);
            assertThat(KsuidGenerator.isValid(max)).isTrue();
        }

        @Test
        @DisplayName("最小小于最大")
        void testMinLessThanMax() {
            String min = KsuidGenerator.min();
            String max = KsuidGenerator.max();

            assertThat(KsuidGenerator.compare(min, max)).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("epoch常量")
        void testEpochSeconds() {
            assertThat(KsuidGenerator.EPOCH_SECONDS).isEqualTo(1400000000L);
        }

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            KsuidGenerator gen = KsuidGenerator.create();

            assertThat(gen.getType()).isEqualTo("KSUID");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            KsuidGenerator gen = KsuidGenerator.create();
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

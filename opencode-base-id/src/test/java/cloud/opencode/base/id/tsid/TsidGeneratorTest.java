package cloud.opencode.base.id.tsid;

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
 * TsidGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("TsidGenerator 测试")
class TsidGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("create工厂方法")
        void testCreate() {
            TsidGenerator gen = TsidGenerator.create();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("使用节点配置创建")
        void testCreateWithNode() {
            TsidGenerator gen = TsidGenerator.create(10, 5);

            assertThat(gen).isNotNull();
            assertThat(gen.getNodeBits()).isEqualTo(10);
            assertThat(gen.getNodeId()).isEqualTo(5);
        }

        @Test
        @DisplayName("使用完整配置创建")
        void testCreateWithFullConfig() {
            long epoch = 1609459200000L;
            TsidGenerator gen = TsidGenerator.create(epoch, 5, 3);

            assertThat(gen).isNotNull();
            assertThat(gen.getEpoch()).isEqualTo(epoch);
            assertThat(gen.getNodeBits()).isEqualTo(5);
            assertThat(gen.getNodeId()).isEqualTo(3);
        }

        @Test
        @DisplayName("无效nodeBits抛出异常")
        void testInvalidNodeBits() {
            assertThatThrownBy(() -> TsidGenerator.create(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> TsidGenerator.create(23, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成有效TSID")
        void testGenerate() {
            TsidGenerator gen = TsidGenerator.create();

            Long tsid = gen.generate();

            assertThat(tsid).isNotNull();
            assertThat(tsid).isPositive();
        }

        @Test
        @DisplayName("生成字符串TSID")
        void testGenerateStr() {
            TsidGenerator gen = TsidGenerator.create();

            String tsidStr = gen.generateStr();

            assertThat(tsidStr).isNotNull();
            assertThat(tsidStr).hasSize(13);
            assertThat(TsidGenerator.isValid(tsidStr)).isTrue();
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            TsidGenerator gen = TsidGenerator.create();
            Set<Long> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }

        @Test
        @DisplayName("使用节点配置生成")
        void testGenerateWithNode() {
            TsidGenerator gen = TsidGenerator.create(5, 3);

            Long tsid = gen.generate();

            assertThat(tsid).isNotNull();
            assertThat(tsid).isPositive();
        }
    }

    @Nested
    @DisplayName("编码解码测试")
    class EncodingTests {

        @Test
        @DisplayName("编码TSID")
        void testEncode() {
            TsidGenerator gen = TsidGenerator.create();
            Long tsid = gen.generate();

            String encoded = TsidGenerator.encode(tsid);

            assertThat(encoded).isNotNull();
            assertThat(encoded).hasSize(13);
        }

        @Test
        @DisplayName("解码TSID字符串")
        void testDecode() {
            TsidGenerator gen = TsidGenerator.create();
            Long original = gen.generate();
            String encoded = TsidGenerator.encode(original);

            Long decoded = TsidGenerator.decode(encoded);

            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("解码无效字符串抛出异常")
        void testDecodeInvalid() {
            assertThatThrownBy(() -> TsidGenerator.decode(null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> TsidGenerator.decode("short"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("编码解码往返")
        void testRoundTrip() {
            TsidGenerator gen = TsidGenerator.create();

            for (int i = 0; i < 100; i++) {
                Long original = gen.generate();
                String encoded = TsidGenerator.encode(original);
                Long decoded = TsidGenerator.decode(encoded);

                assertThat(decoded).isEqualTo(original);
            }
        }
    }

    @Nested
    @DisplayName("时间戳提取测试")
    class TimestampTests {

        @Test
        @DisplayName("提取时间戳")
        void testExtractTimestamp() {
            TsidGenerator gen = TsidGenerator.create();
            long before = System.currentTimeMillis();
            Long tsid = gen.generate();
            long after = System.currentTimeMillis();

            Instant timestamp = gen.extractTimestamp(tsid);

            assertThat(timestamp.toEpochMilli()).isBetween(before, after);
        }

        @Test
        @DisplayName("静态方法提取时间戳")
        void testExtractTimestampStatic() {
            TsidGenerator gen = TsidGenerator.create();
            long before = System.currentTimeMillis();
            Long tsid = gen.generate();
            long after = System.currentTimeMillis();

            Instant timestamp = TsidGenerator.extractTimestampStatic(tsid);

            assertThat(timestamp.toEpochMilli()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("验证有效TSID字符串")
        void testIsValidTrue() {
            TsidGenerator gen = TsidGenerator.create();
            String tsidStr = gen.generateStr();

            assertThat(TsidGenerator.isValid(tsidStr)).isTrue();
        }

        @Test
        @DisplayName("验证null")
        void testIsValidNull() {
            assertThat(TsidGenerator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("验证空字符串")
        void testIsValidEmpty() {
            assertThat(TsidGenerator.isValid("")).isFalse();
        }

        @Test
        @DisplayName("验证长度不正确")
        void testIsValidWrongLength() {
            assertThat(TsidGenerator.isValid("0ARYZ1J8P")).isFalse();
            assertThat(TsidGenerator.isValid("0ARYZ1J8P0X0R1")).isFalse();
        }

        @Test
        @DisplayName("验证无效字符")
        void testIsValidInvalidChars() {
            assertThat(TsidGenerator.isValid("0ARYZ1J8P0X0U")).isFalse();
        }

        @Test
        @DisplayName("验证大小写不敏感")
        void testIsValidCaseInsensitive() {
            TsidGenerator gen = TsidGenerator.create();
            String tsidStr = gen.generateStr();

            assertThat(TsidGenerator.isValid(tsidStr.toLowerCase())).isTrue();
        }
    }

    @Nested
    @DisplayName("属性访问测试")
    class PropertyTests {

        @Test
        @DisplayName("获取epoch")
        void testGetEpoch() {
            TsidGenerator gen = TsidGenerator.create();

            assertThat(gen.getEpoch()).isEqualTo(TsidGenerator.DEFAULT_EPOCH);
        }

        @Test
        @DisplayName("获取类型")
        void testGetType() {
            TsidGenerator gen = TsidGenerator.create();

            assertThat(gen.getType()).isEqualTo("TSID");
        }

        @Test
        @DisplayName("默认epoch常量")
        void testDefaultEpoch() {
            assertThat(TsidGenerator.DEFAULT_EPOCH).isEqualTo(1577836800000L);
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程生成唯一ID")
        void testConcurrentGeneration() throws InterruptedException {
            TsidGenerator gen = TsidGenerator.create();
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

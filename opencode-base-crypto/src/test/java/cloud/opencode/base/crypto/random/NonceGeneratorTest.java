package cloud.opencode.base.crypto.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * NonceGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("NonceGenerator 测试")
class NonceGeneratorTest {

    @Nested
    @DisplayName("random 测试")
    class RandomTests {

        @Test
        @DisplayName("生成指定长度的随机nonce")
        void testRandom() {
            byte[] nonce = NonceGenerator.random(12);
            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("生成不同的随机nonce")
        void testRandomDifferent() {
            byte[] nonce1 = NonceGenerator.random(12);
            byte[] nonce2 = NonceGenerator.random(12);

            assertThat(nonce1).isNotEqualTo(nonce2);
        }

        @Test
        @DisplayName("使用自定义SecureRandom")
        void testRandomWithCustomRandom() {
            SecureRandom random = new SecureRandom();
            byte[] nonce = NonceGenerator.random(16, random);

            assertThat(nonce).hasSize(16);
        }
    }

    @Nested
    @DisplayName("counter 测试")
    class CounterTests {

        @Test
        @DisplayName("生成基于计数器的nonce")
        void testCounter() {
            byte[] nonce = NonceGenerator.counter(1L, 12);

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("不同计数器产生不同nonce")
        void testCounterDifferent() {
            byte[] nonce1 = NonceGenerator.counter(1L, 12);
            byte[] nonce2 = NonceGenerator.counter(2L, 12);

            assertThat(nonce1).isNotEqualTo(nonce2);
        }

        @Test
        @DisplayName("计数器值被正确编码")
        void testCounterEncoding() {
            byte[] nonce = NonceGenerator.counter(0x0102030405060708L, 8);

            // 大端序编码
            assertThat(nonce[0]).isEqualTo((byte) 0x01);
            assertThat(nonce[7]).isEqualTo((byte) 0x08);
        }

        @Test
        @DisplayName("长度小于8字节抛出异常")
        void testCounterTooShort() {
            assertThatThrownBy(() -> NonceGenerator.counter(1L, 7))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("timestamp 测试")
    class TimestampTests {

        @Test
        @DisplayName("生成基于时间戳的nonce")
        void testTimestamp() {
            byte[] nonce = NonceGenerator.timestamp(4);

            assertThat(nonce).hasSize(12); // 8字节时间戳 + 4字节随机
        }

        @Test
        @DisplayName("不同时间戳产生不同nonce")
        void testTimestampDifferent() throws InterruptedException {
            byte[] nonce1 = NonceGenerator.timestamp(4);
            Thread.sleep(10);
            byte[] nonce2 = NonceGenerator.timestamp(4);

            // 由于时间戳和随机部分都不同，应该不相等
            assertThat(nonce1).isNotEqualTo(nonce2);
        }

        @Test
        @DisplayName("使用自定义SecureRandom")
        void testTimestampWithCustomRandom() {
            SecureRandom random = new SecureRandom();
            byte[] nonce = NonceGenerator.timestamp(8, random);

            assertThat(nonce).hasSize(16);
        }

        @Test
        @DisplayName("随机长度为负数抛出异常")
        void testTimestampNegativeLength() {
            assertThatThrownBy(() -> NonceGenerator.timestamp(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null SecureRandom抛出异常")
        void testTimestampNullRandom() {
            assertThatThrownBy(() -> NonceGenerator.timestamp(4, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("hybrid 测试")
    class HybridTests {

        @Test
        @DisplayName("生成混合nonce")
        void testHybrid() {
            byte[] nonce = NonceGenerator.hybrid(12);

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("使用自定义SecureRandom生成混合nonce")
        void testHybridWithCustomRandom() {
            SecureRandom random = new SecureRandom();
            byte[] nonce = NonceGenerator.hybrid(16, random);

            assertThat(nonce).hasSize(16);
        }

        @Test
        @DisplayName("长度小于8字节抛出异常")
        void testHybridTooShort() {
            assertThatThrownBy(() -> NonceGenerator.hybrid(7))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("forAesGcm 测试")
    class ForAesGcmTests {

        @Test
        @DisplayName("生成AES-GCM nonce")
        void testForAesGcm() {
            byte[] nonce = NonceGenerator.forAesGcm();

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("使用自定义SecureRandom生成AES-GCM nonce")
        void testForAesGcmWithCustomRandom() {
            SecureRandom random = new SecureRandom();
            byte[] nonce = NonceGenerator.forAesGcm(random);

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("生成唯一的AES-GCM nonce")
        void testForAesGcmUnique() {
            Set<String> nonces = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                nonces.add(java.util.HexFormat.of().formatHex(NonceGenerator.forAesGcm()));
            }
            assertThat(nonces).hasSize(100);
        }
    }

    @Nested
    @DisplayName("forChaCha20 测试")
    class ForChaCha20Tests {

        @Test
        @DisplayName("生成ChaCha20 nonce")
        void testForChaCha20() {
            byte[] nonce = NonceGenerator.forChaCha20();

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("使用自定义SecureRandom生成ChaCha20 nonce")
        void testForChaCha20WithCustomRandom() {
            SecureRandom random = new SecureRandom();
            byte[] nonce = NonceGenerator.forChaCha20(random);

            assertThat(nonce).hasSize(12);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("AES_GCM_NONCE_SIZE常量值")
        void testAesGcmNonceSize() {
            assertThat(NonceGenerator.AES_GCM_NONCE_SIZE).isEqualTo(12);
        }

        @Test
        @DisplayName("CHACHA20_NONCE_SIZE常量值")
        void testChaCha20NonceSize() {
            assertThat(NonceGenerator.CHACHA20_NONCE_SIZE).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = NonceGenerator.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(AssertionError.class);
        }
    }
}

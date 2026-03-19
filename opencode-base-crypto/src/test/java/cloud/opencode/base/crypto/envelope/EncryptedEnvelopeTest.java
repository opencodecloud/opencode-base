package cloud.opencode.base.crypto.envelope;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link EncryptedEnvelope}.
 * EncryptedEnvelope单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("EncryptedEnvelope Tests / EncryptedEnvelope测试")
class EncryptedEnvelopeTest {

    @Nested
    @DisplayName("Constructor Tests / 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("正常创建EncryptedEnvelope成功")
        void testNormalCreation() {
            byte[] encryptedKey = new byte[]{1, 2, 3, 4};
            byte[] iv = new byte[]{5, 6, 7, 8};
            byte[] ciphertext = new byte[]{9, 10, 11, 12};
            byte[] tag = new byte[]{13, 14, 15, 16};

            EncryptedEnvelope envelope = new EncryptedEnvelope(encryptedKey, iv, ciphertext, tag);

            assertThat(envelope.encryptedKey()).isEqualTo(encryptedKey);
            assertThat(envelope.iv()).isEqualTo(iv);
            assertThat(envelope.ciphertext()).isEqualTo(ciphertext);
            assertThat(envelope.tag()).isEqualTo(tag);
        }

        @Test
        @DisplayName("tag可以为null")
        void testNullTag() {
            byte[] encryptedKey = new byte[]{1, 2, 3};
            byte[] iv = new byte[]{4, 5, 6};
            byte[] ciphertext = new byte[]{7, 8, 9};

            EncryptedEnvelope envelope = new EncryptedEnvelope(encryptedKey, iv, ciphertext, null);

            assertThat(envelope.tag()).isNull();
        }

        @Test
        @DisplayName("encryptedKey为null抛出异常")
        void testNullEncryptedKeyThrows() {
            assertThatThrownBy(() -> new EncryptedEnvelope(null, new byte[1], new byte[1], null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Encrypted key");
        }

        @Test
        @DisplayName("iv为null抛出异常")
        void testNullIvThrows() {
            assertThatThrownBy(() -> new EncryptedEnvelope(new byte[1], null, new byte[1], null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("IV");
        }

        @Test
        @DisplayName("ciphertext为null抛出异常")
        void testNullCiphertextThrows() {
            assertThatThrownBy(() -> new EncryptedEnvelope(new byte[1], new byte[1], null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Ciphertext");
        }

        @Test
        @DisplayName("数组是防御性复制的")
        void testDefensiveCopy() {
            byte[] encryptedKey = new byte[]{1, 2, 3};
            byte[] iv = new byte[]{4, 5, 6};
            byte[] ciphertext = new byte[]{7, 8, 9};
            byte[] tag = new byte[]{10, 11, 12};

            EncryptedEnvelope envelope = new EncryptedEnvelope(encryptedKey, iv, ciphertext, tag);

            // Modify original arrays
            encryptedKey[0] = 100;
            iv[0] = 100;
            ciphertext[0] = 100;
            tag[0] = 100;

            // Envelope should still have original values
            assertThat(envelope.encryptedKey()[0]).isEqualTo((byte) 1);
            assertThat(envelope.iv()[0]).isEqualTo((byte) 4);
            assertThat(envelope.ciphertext()[0]).isEqualTo((byte) 7);
            assertThat(envelope.tag()[0]).isEqualTo((byte) 10);
        }

        @Test
        @DisplayName("getter返回防御性复制")
        void testGetterReturnsDefensiveCopy() {
            EncryptedEnvelope envelope = new EncryptedEnvelope(
                    new byte[]{1}, new byte[]{2}, new byte[]{3}, new byte[]{4}
            );

            // Modify returned arrays
            envelope.encryptedKey()[0] = 100;
            envelope.iv()[0] = 100;
            envelope.ciphertext()[0] = 100;
            envelope.tag()[0] = 100;

            // Envelope should still have original values
            assertThat(envelope.encryptedKey()[0]).isEqualTo((byte) 1);
            assertThat(envelope.iv()[0]).isEqualTo((byte) 2);
            assertThat(envelope.ciphertext()[0]).isEqualTo((byte) 3);
            assertThat(envelope.tag()[0]).isEqualTo((byte) 4);
        }
    }

    @Nested
    @DisplayName("Serialization Tests / 序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("toBytes序列化成功")
        void testToBytes() {
            EncryptedEnvelope envelope = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );

            byte[] bytes = envelope.toBytes();

            assertThat(bytes).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("fromBytes反序列化成功")
        void testFromBytes() {
            EncryptedEnvelope original = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );

            byte[] bytes = original.toBytes();
            EncryptedEnvelope deserialized = EncryptedEnvelope.fromBytes(bytes);

            assertThat(deserialized).isEqualTo(original);
        }

        @Test
        @DisplayName("无tag的信封序列化往返成功")
        void testRoundTripWithoutTag() {
            EncryptedEnvelope original = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    null
            );

            byte[] bytes = original.toBytes();
            EncryptedEnvelope deserialized = EncryptedEnvelope.fromBytes(bytes);

            assertThat(deserialized).isEqualTo(original);
            assertThat(deserialized.tag()).isNull();
        }

        @Test
        @DisplayName("fromBytes bytes为null抛出异常")
        void testFromBytesNullThrows() {
            assertThatThrownBy(() -> EncryptedEnvelope.fromBytes(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Bytes");
        }

        @Test
        @DisplayName("无效bytes抛出异常")
        void testFromBytesInvalidThrows() {
            byte[] invalid = new byte[]{0, 0, 0, -1}; // Invalid length

            assertThatThrownBy(() -> EncryptedEnvelope.fromBytes(invalid))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("Base64 Tests / Base64测试")
    class Base64Tests {

        @Test
        @DisplayName("toBase64编码成功")
        void testToBase64() {
            EncryptedEnvelope envelope = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );

            String base64 = envelope.toBase64();

            assertThat(base64).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("fromBase64解码成功")
        void testFromBase64() {
            EncryptedEnvelope original = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );

            String base64 = original.toBase64();
            EncryptedEnvelope deserialized = EncryptedEnvelope.fromBase64(base64);

            assertThat(deserialized).isEqualTo(original);
        }

        @Test
        @DisplayName("fromBase64 null抛出异常")
        void testFromBase64NullThrows() {
            assertThatThrownBy(() -> EncryptedEnvelope.fromBase64(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Base64");
        }

        @Test
        @DisplayName("无效Base64抛出异常")
        void testFromBase64InvalidThrows() {
            assertThatThrownBy(() -> EncryptedEnvelope.fromBase64("invalid!!!base64"))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests / equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相等的信封equals为true")
        void testEquals() {
            EncryptedEnvelope env1 = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );
            EncryptedEnvelope env2 = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );

            assertThat(env1).isEqualTo(env2);
            assertThat(env1.hashCode()).isEqualTo(env2.hashCode());
        }

        @Test
        @DisplayName("不同内容的信封equals为false")
        void testNotEquals() {
            EncryptedEnvelope env1 = new EncryptedEnvelope(
                    new byte[]{1, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );
            EncryptedEnvelope env2 = new EncryptedEnvelope(
                    new byte[]{99, 2, 3},
                    new byte[]{4, 5, 6},
                    new byte[]{7, 8, 9},
                    new byte[]{10, 11, 12}
            );

            assertThat(env1).isNotEqualTo(env2);
        }

        @Test
        @DisplayName("与null比较为false")
        void testEqualsNull() {
            EncryptedEnvelope env = new EncryptedEnvelope(
                    new byte[]{1}, new byte[]{2}, new byte[]{3}, null
            );

            assertThat(env.equals(null)).isFalse();
        }

        @Test
        @DisplayName("与自身比较为true")
        void testEqualsSelf() {
            EncryptedEnvelope env = new EncryptedEnvelope(
                    new byte[]{1}, new byte[]{2}, new byte[]{3}, null
            );

            assertThat(env.equals(env)).isTrue();
        }

        @Test
        @DisplayName("与不同类型比较为false")
        void testEqualsDifferentType() {
            EncryptedEnvelope env = new EncryptedEnvelope(
                    new byte[]{1}, new byte[]{2}, new byte[]{3}, null
            );

            assertThat(env.equals("string")).isFalse();
        }
    }

    @Nested
    @DisplayName("ToString Tests / toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含必要信息")
        void testToString() {
            EncryptedEnvelope envelope = new EncryptedEnvelope(
                    new byte[10],
                    new byte[12],
                    new byte[100],
                    new byte[16]
            );

            String str = envelope.toString();

            assertThat(str).contains("EncryptedEnvelope");
            assertThat(str).contains("encryptedKeyLength=10");
            assertThat(str).contains("ivLength=12");
            assertThat(str).contains("ciphertextLength=100");
            assertThat(str).contains("tagLength=16");
        }

        @Test
        @DisplayName("tag为null时tagLength为0")
        void testToStringNullTag() {
            EncryptedEnvelope envelope = new EncryptedEnvelope(
                    new byte[10],
                    new byte[12],
                    new byte[100],
                    null
            );

            String str = envelope.toString();

            assertThat(str).contains("tagLength=0");
        }
    }

    @Nested
    @DisplayName("Large Data Tests / 大数据测试")
    class LargeDataTests {

        @Test
        @DisplayName("大数据序列化往返成功")
        void testLargeDataRoundTrip() {
            byte[] largeKey = new byte[512];
            byte[] iv = new byte[16];
            byte[] largeCiphertext = new byte[1024 * 1024]; // 1 MB
            byte[] tag = new byte[16];

            Arrays.fill(largeKey, (byte) 0xAB);
            Arrays.fill(iv, (byte) 0xCD);
            Arrays.fill(largeCiphertext, (byte) 0xEF);
            Arrays.fill(tag, (byte) 0x12);

            EncryptedEnvelope original = new EncryptedEnvelope(largeKey, iv, largeCiphertext, tag);

            byte[] bytes = original.toBytes();
            EncryptedEnvelope deserialized = EncryptedEnvelope.fromBytes(bytes);

            assertThat(deserialized).isEqualTo(original);
        }
    }
}

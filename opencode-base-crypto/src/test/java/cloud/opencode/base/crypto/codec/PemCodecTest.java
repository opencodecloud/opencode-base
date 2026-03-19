package cloud.opencode.base.crypto.codec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.*;

/**
 * PemCodec 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PemCodec 测试")
class PemCodecTest {

    @Nested
    @DisplayName("encodePublicKey 测试")
    class EncodePublicKeyTests {

        @Test
        @DisplayName("编码公钥")
        void testEncodePublicKey() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

            String pem = PemCodec.encodePublicKey(publicKeyBytes);

            assertThat(pem).startsWith("-----BEGIN PUBLIC KEY-----");
            assertThat(pem).endsWith("-----END PUBLIC KEY-----");
        }

        @Test
        @DisplayName("编码空公钥")
        void testEncodeEmptyPublicKey() {
            String pem = PemCodec.encodePublicKey(new byte[0]);
            assertThat(pem).contains("-----BEGIN PUBLIC KEY-----");
            assertThat(pem).contains("-----END PUBLIC KEY-----");
        }
    }

    @Nested
    @DisplayName("decodePublicKey 测试")
    class DecodePublicKeyTests {

        @Test
        @DisplayName("解码公钥")
        void testDecodePublicKey() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            byte[] originalBytes = keyPair.getPublic().getEncoded();

            String pem = PemCodec.encodePublicKey(originalBytes);
            byte[] decoded = PemCodec.decodePublicKey(pem);

            assertThat(decoded).isEqualTo(originalBytes);
        }

        @Test
        @DisplayName("解码错误类型抛出异常")
        void testDecodeWrongType() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            String privatePem = PemCodec.encodePrivateKey(keyPair.getPrivate().getEncoded());

            assertThatThrownBy(() -> PemCodec.decodePublicKey(privatePem))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PUBLIC KEY");
        }
    }

    @Nested
    @DisplayName("encodePrivateKey 测试")
    class EncodePrivateKeyTests {

        @Test
        @DisplayName("编码私钥")
        void testEncodePrivateKey() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

            String pem = PemCodec.encodePrivateKey(privateKeyBytes);

            assertThat(pem).startsWith("-----BEGIN PRIVATE KEY-----");
            assertThat(pem).endsWith("-----END PRIVATE KEY-----");
        }
    }

    @Nested
    @DisplayName("decodePrivateKey 测试")
    class DecodePrivateKeyTests {

        @Test
        @DisplayName("解码私钥")
        void testDecodePrivateKey() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            byte[] originalBytes = keyPair.getPrivate().getEncoded();

            String pem = PemCodec.encodePrivateKey(originalBytes);
            byte[] decoded = PemCodec.decodePrivateKey(pem);

            assertThat(decoded).isEqualTo(originalBytes);
        }

        @Test
        @DisplayName("解码错误类型抛出异常")
        void testDecodeWrongType() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            String publicPem = PemCodec.encodePublicKey(keyPair.getPublic().getEncoded());

            assertThatThrownBy(() -> PemCodec.decodePrivateKey(publicPem))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PRIVATE KEY");
        }
    }

    @Nested
    @DisplayName("encodeCertificate 测试")
    class EncodeCertificateTests {

        @Test
        @DisplayName("编码证书")
        void testEncodeCertificate() {
            byte[] certBytes = "mock-certificate-data".getBytes();

            String pem = PemCodec.encodeCertificate(certBytes);

            assertThat(pem).startsWith("-----BEGIN CERTIFICATE-----");
            assertThat(pem).endsWith("-----END CERTIFICATE-----");
        }
    }

    @Nested
    @DisplayName("decodeCertificate 测试")
    class DecodeCertificateTests {

        @Test
        @DisplayName("解码证书")
        void testDecodeCertificate() {
            byte[] originalBytes = "mock-certificate-data".getBytes();

            String pem = PemCodec.encodeCertificate(originalBytes);
            byte[] decoded = PemCodec.decodeCertificate(pem);

            assertThat(decoded).isEqualTo(originalBytes);
        }
    }

    @Nested
    @DisplayName("encode 自定义类型测试")
    class EncodeCustomTypeTests {

        @Test
        @DisplayName("编码自定义类型")
        void testEncodeCustomType() {
            byte[] data = "test-data".getBytes();

            String pem = PemCodec.encode("RSA PRIVATE KEY", data);

            assertThat(pem).startsWith("-----BEGIN RSA PRIVATE KEY-----");
            assertThat(pem).endsWith("-----END RSA PRIVATE KEY-----");
        }

        @Test
        @DisplayName("编码null类型抛出异常")
        void testEncodeNullType() {
            assertThatThrownBy(() -> PemCodec.encode(null, new byte[0]))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Type");
        }

        @Test
        @DisplayName("编码null数据抛出异常")
        void testEncodeNullData() {
            assertThatThrownBy(() -> PemCodec.encode("TEST", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Data");
        }

        @Test
        @DisplayName("编码空类型抛出异常")
        void testEncodeEmptyType() {
            assertThatThrownBy(() -> PemCodec.encode("", new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("编码空白类型抛出异常")
        void testEncodeBlankType() {
            assertThatThrownBy(() -> PemCodec.encode("   ", new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("decode 测试")
    class DecodeTests {

        @Test
        @DisplayName("解码有效PEM")
        void testDecodeValid() {
            byte[] original = "test-data".getBytes();
            String pem = PemCodec.encode("TEST", original);

            byte[] decoded = PemCodec.decode(pem);

            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("解码null抛出异常")
        void testDecodeNull() {
            assertThatThrownBy(() -> PemCodec.decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("解码无效格式抛出异常")
        void testDecodeInvalidFormat() {
            assertThatThrownBy(() -> PemCodec.decode("not a pem"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid PEM");
        }

        @Test
        @DisplayName("解码无效Base64抛出异常")
        void testDecodeInvalidBase64() {
            String invalidPem = "-----BEGIN TEST-----\n!!!invalid!!!\n-----END TEST-----";

            assertThatThrownBy(() -> PemCodec.decode(invalidPem))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("解码带空白的PEM")
        void testDecodeWithWhitespace() {
            byte[] original = "test-data".getBytes();
            String pem = PemCodec.encode("TEST", original);
            // 添加额外空白
            String pemWithWhitespace = "  \n" + pem + "\n  ";

            byte[] decoded = PemCodec.decode(pemWithWhitespace);

            assertThat(decoded).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("getType 测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取公钥类型")
        void testGetPublicKeyType() {
            String pem = PemCodec.encodePublicKey(new byte[0]);
            assertThat(PemCodec.getType(pem)).isEqualTo("PUBLIC KEY");
        }

        @Test
        @DisplayName("获取私钥类型")
        void testGetPrivateKeyType() {
            String pem = PemCodec.encodePrivateKey(new byte[0]);
            assertThat(PemCodec.getType(pem)).isEqualTo("PRIVATE KEY");
        }

        @Test
        @DisplayName("获取证书类型")
        void testGetCertificateType() {
            String pem = PemCodec.encodeCertificate(new byte[0]);
            assertThat(PemCodec.getType(pem)).isEqualTo("CERTIFICATE");
        }

        @Test
        @DisplayName("获取自定义类型")
        void testGetCustomType() {
            String pem = PemCodec.encode("MY CUSTOM TYPE", new byte[0]);
            assertThat(PemCodec.getType(pem)).isEqualTo("MY CUSTOM TYPE");
        }

        @Test
        @DisplayName("获取null的类型抛出异常")
        void testGetTypeNull() {
            assertThatThrownBy(() -> PemCodec.getType(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("获取无效PEM的类型抛出异常")
        void testGetTypeInvalid() {
            assertThatThrownBy(() -> PemCodec.getType("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("行长度测试")
    class LineLengthTests {

        @Test
        @DisplayName("长数据被正确分行")
        void testLongDataIsSplit() {
            // 创建足够长的数据以产生多行
            byte[] longData = new byte[200];
            for (int i = 0; i < longData.length; i++) {
                longData[i] = (byte) i;
            }

            String pem = PemCodec.encode("TEST", longData);
            String[] lines = pem.split(System.lineSeparator());

            // 检查中间行长度
            for (int i = 1; i < lines.length - 1; i++) {
                if (!lines[i].startsWith("-----")) {
                    assertThat(lines[i].length()).isLessThanOrEqualTo(64);
                }
            }
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("RSA密钥对往返")
        void testRsaKeyPairRoundTrip() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

            // 公钥往返
            byte[] publicBytes = keyPair.getPublic().getEncoded();
            String publicPem = PemCodec.encodePublicKey(publicBytes);
            byte[] decodedPublic = PemCodec.decodePublicKey(publicPem);
            assertThat(decodedPublic).isEqualTo(publicBytes);

            // 私钥往返
            byte[] privateBytes = keyPair.getPrivate().getEncoded();
            String privatePem = PemCodec.encodePrivateKey(privateBytes);
            byte[] decodedPrivate = PemCodec.decodePrivateKey(privatePem);
            assertThat(decodedPrivate).isEqualTo(privateBytes);
        }

        @Test
        @DisplayName("EC密钥对往返")
        void testEcKeyPairRoundTrip() throws NoSuchAlgorithmException {
            KeyPair keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair();

            // 公钥往返
            byte[] publicBytes = keyPair.getPublic().getEncoded();
            String publicPem = PemCodec.encodePublicKey(publicBytes);
            byte[] decodedPublic = PemCodec.decodePublicKey(publicPem);
            assertThat(decodedPublic).isEqualTo(publicBytes);

            // 私钥往返
            byte[] privateBytes = keyPair.getPrivate().getEncoded();
            String privatePem = PemCodec.encodePrivateKey(privateBytes);
            byte[] decodedPrivate = PemCodec.decodePrivateKey(privatePem);
            assertThat(decodedPrivate).isEqualTo(privateBytes);
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = PemCodec.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}

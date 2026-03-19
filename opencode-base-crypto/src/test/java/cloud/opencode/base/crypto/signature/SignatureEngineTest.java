package cloud.opencode.base.crypto.signature;

import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.key.KeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * SignatureEngine 接口测试类
 * 通过测试所有实现类来验证接口行为
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SignatureEngine 接口测试")
class SignatureEngineTest {

    private static final byte[] TEST_DATA = "Hello, World! This is a test message for signing.".getBytes(StandardCharsets.UTF_8);
    private static final String TEST_STRING = "Hello, World! This is a test message for signing.";

    /**
     * 提供所有SignatureEngine实现（带密钥对）
     */
    static Stream<SignatureEngine> signatureEngineProvider() {
        return Stream.of(
                EcdsaSignature.p256WithGeneratedKeyPair(),
                EcdsaSignature.p384WithGeneratedKeyPair(),
                EddsaSignature.ed25519WithGeneratedKeyPair(),
                RsaSignature.sha256WithKeyPair(),
                RsaPssSignature.sha256WithKeyPair()
        );
    }

    @Nested
    @DisplayName("签名和验证测试")
    class SignAndVerifyTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("sign和verify对有效数据返回true")
        void testSignAndVerifyValid(SignatureEngine engine) {
            byte[] signature = engine.sign(TEST_DATA);

            boolean valid = engine.verify(TEST_DATA, signature);

            assertThat(valid).isTrue();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verify对篡改数据返回false")
        void testVerifyTamperedData(SignatureEngine engine) {
            byte[] signature = engine.sign(TEST_DATA);
            byte[] tamperedData = "Tampered data!".getBytes(StandardCharsets.UTF_8);

            boolean valid = engine.verify(tamperedData, signature);

            assertThat(valid).isFalse();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verify对篡改签名返回false或抛出异常")
        void testVerifyTamperedSignature(SignatureEngine engine) {
            byte[] signature = engine.sign(TEST_DATA);
            signature[0] ^= 0xFF; // 修改一个字节

            // 某些签名算法对格式错误的签名会抛出异常（如ECDSA），这也是正确的行为
            try {
                boolean valid = engine.verify(TEST_DATA, signature);
                assertThat(valid).isFalse();
            } catch (Exception e) {
                // 抛出异常也是合理的行为，因为签名格式已被篡改
                assertThat(e).isNotNull();
            }
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("sign字符串和字节数组一致")
        void testSignStringEqualsBytes(SignatureEngine engine) {
            byte[] sigFromBytes = engine.sign(TEST_DATA);
            byte[] sigFromString = engine.sign(TEST_STRING);

            // 对于某些算法（如ECDSA），签名是非确定性的，所以验证两者都有效
            assertThat(engine.verify(TEST_DATA, sigFromBytes)).isTrue();
            assertThat(engine.verify(TEST_STRING, sigFromString)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verify字符串和字节数组一致")
        void testVerifyStringEqualsBytes(SignatureEngine engine) {
            byte[] signature = engine.sign(TEST_DATA);

            assertThat(engine.verify(TEST_DATA, signature)).isTrue();
            assertThat(engine.verify(TEST_STRING, signature)).isTrue();
        }
    }

    @Nested
    @DisplayName("signBase64 测试")
    class SignBase64Tests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("signBase64返回Base64字符串")
        void testSignBase64(SignatureEngine engine) {
            String base64Sig = engine.signBase64(TEST_DATA);

            assertThat(base64Sig).matches("[A-Za-z0-9+/=]+");
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("signBase64字符串")
        void testSignBase64String(SignatureEngine engine) {
            String base64Sig = engine.signBase64(TEST_STRING);

            assertThat(base64Sig).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("signHex 测试")
    class SignHexTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("signHex返回十六进制字符串")
        void testSignHex(SignatureEngine engine) {
            String hexSig = engine.signHex(TEST_DATA);

            assertThat(hexSig).matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("verifyBase64 测试")
    class VerifyBase64Tests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verifyBase64对有效签名返回true")
        void testVerifyBase64Valid(SignatureEngine engine) {
            String base64Sig = engine.signBase64(TEST_DATA);

            boolean valid = engine.verifyBase64(TEST_DATA, base64Sig);

            assertThat(valid).isTrue();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verifyBase64字符串")
        void testVerifyBase64String(SignatureEngine engine) {
            String base64Sig = engine.signBase64(TEST_STRING);

            boolean valid = engine.verifyBase64(TEST_STRING, base64Sig);

            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("verifyHex 测试")
    class VerifyHexTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verifyHex对有效签名返回true")
        void testVerifyHexValid(SignatureEngine engine) {
            String hexSig = engine.signHex(TEST_DATA);

            boolean valid = engine.verifyHex(TEST_DATA, hexSig);

            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("文件签名测试")
    class FileSignTests {

        @TempDir
        Path tempDir;

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("signFile签名文件")
        void testSignFile(SignatureEngine engine) throws Exception {
            Path testFile = tempDir.resolve("test.txt");
            Files.write(testFile, TEST_DATA);

            byte[] signature = engine.signFile(testFile);

            assertThat(signature).isNotEmpty();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verifyFile验证文件签名")
        void testVerifyFile(SignatureEngine engine) throws Exception {
            Path testFile = tempDir.resolve("test.txt");
            Files.write(testFile, TEST_DATA);

            byte[] signature = engine.signFile(testFile);
            boolean valid = engine.verifyFile(testFile, signature);

            assertThat(valid).isTrue();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("verifyFile对篡改文件返回false")
        void testVerifyFileTampered(SignatureEngine engine) throws Exception {
            Path testFile = tempDir.resolve("test.txt");
            Files.write(testFile, TEST_DATA);

            byte[] signature = engine.signFile(testFile);

            // 修改文件
            Files.write(testFile, "Tampered content".getBytes());

            boolean valid = engine.verifyFile(testFile, signature);

            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("流签名测试")
    class StreamSignTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("sign InputStream")
        void testSignInputStream(SignatureEngine engine) {
            ByteArrayInputStream input = new ByteArrayInputStream(TEST_DATA);

            byte[] signature = engine.sign(input);

            assertThat(signature).isNotEmpty();
            assertThat(engine.verify(TEST_DATA, signature)).isTrue();
        }
    }

    @Nested
    @DisplayName("多部分签名测试")
    class MultiPartSignTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("update和doSign")
        void testUpdateAndDoSign(SignatureEngine engine) {
            engine.update("Hello, ".getBytes());
            engine.update("World!".getBytes());
            byte[] signature = engine.doSign();

            assertThat(signature).isNotEmpty();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("update字符串和doSign")
        void testUpdateStringAndDoSign(SignatureEngine engine) {
            engine.update("Hello, ");
            engine.update("World!");
            byte[] signature = engine.doSign();

            assertThat(signature).isNotEmpty();
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("doSignBase64")
        void testDoSignBase64(SignatureEngine engine) {
            engine.update(TEST_DATA);
            String base64Sig = engine.doSignBase64();

            assertThat(base64Sig).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("密钥配置测试")
    class KeyConfigurationTests {

        @Test
        @DisplayName("setKeyPair设置密钥对")
        void testSetKeyPair() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            SignatureEngine engine = EcdsaSignature.p256()
                    .setKeyPair(keyPair);

            byte[] signature = engine.sign(TEST_DATA);

            assertThat(engine.verify(TEST_DATA, signature)).isTrue();
        }

        @Test
        @DisplayName("分别设置公私钥")
        void testSetKeysSeparately() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            SignatureEngine signEngine = EcdsaSignature.p256()
                    .setPrivateKey(keyPair.getPrivate());
            SignatureEngine verifyEngine = EcdsaSignature.p256()
                    .setPublicKey(keyPair.getPublic());

            byte[] signature = signEngine.sign(TEST_DATA);

            assertThat(verifyEngine.verify(TEST_DATA, signature)).isTrue();
        }

        @Test
        @DisplayName("setPrivateKey bytes")
        void testSetPrivateKeyBytes() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            byte[] encodedPrivate = keyPair.getPrivate().getEncoded();

            SignatureEngine engine = EcdsaSignature.p256()
                    .setPrivateKey(encodedPrivate)
                    .setPublicKey(keyPair.getPublic());

            byte[] signature = engine.sign(TEST_DATA);
            assertThat(engine.verify(TEST_DATA, signature)).isTrue();
        }

        @Test
        @DisplayName("setPublicKey bytes")
        void testSetPublicKeyBytes() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            byte[] encodedPublic = keyPair.getPublic().getEncoded();

            SignatureEngine engine = EcdsaSignature.p256()
                    .setPrivateKey(keyPair.getPrivate())
                    .setPublicKey(encodedPublic);

            byte[] signature = engine.sign(TEST_DATA);
            assertThat(engine.verify(TEST_DATA, signature)).isTrue();
        }
    }

    @Nested
    @DisplayName("getAlgorithm 测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("ECDSA P-256算法名")
        void testEcdsaP256Algorithm() {
            SignatureEngine engine = EcdsaSignature.p256();
            assertThat(engine.getAlgorithm()).isEqualTo("SHA256withECDSA");
        }

        @Test
        @DisplayName("ECDSA P-384算法名")
        void testEcdsaP384Algorithm() {
            SignatureEngine engine = EcdsaSignature.p384();
            assertThat(engine.getAlgorithm()).isEqualTo("SHA384withECDSA");
        }

        @Test
        @DisplayName("ECDSA P-521算法名")
        void testEcdsaP521Algorithm() {
            SignatureEngine engine = EcdsaSignature.p521();
            assertThat(engine.getAlgorithm()).isEqualTo("SHA512withECDSA");
        }

        @Test
        @DisplayName("EdDSA Ed25519算法名")
        void testEddsaEd25519Algorithm() {
            SignatureEngine engine = EddsaSignature.ed25519();
            assertThat(engine.getAlgorithm()).isEqualTo("Ed25519");
        }

        @Test
        @DisplayName("RSA SHA256算法名")
        void testRsaSha256Algorithm() {
            SignatureEngine engine = RsaSignature.sha256();
            assertThat(engine.getAlgorithm()).isEqualTo("SHA256withRSA");
        }

        @Test
        @DisplayName("RSA-PSS SHA256算法名")
        void testRsaPssSha256Algorithm() {
            SignatureEngine engine = RsaPssSignature.sha256();
            assertThat(engine.getAlgorithm()).contains("RSASSA-PSS");
        }
    }

    @Nested
    @DisplayName("实现类型验证测试")
    class ImplementationTypeTests {

        @Test
        @DisplayName("EcdsaSignature实现SignatureEngine")
        void testEcdsaImplementsSignatureEngine() {
            assertThat(EcdsaSignature.p256()).isInstanceOf(SignatureEngine.class);
        }

        @Test
        @DisplayName("EddsaSignature实现SignatureEngine")
        void testEddsaImplementsSignatureEngine() {
            assertThat(EddsaSignature.ed25519()).isInstanceOf(SignatureEngine.class);
        }

        @Test
        @DisplayName("RsaSignature实现SignatureEngine")
        void testRsaImplementsSignatureEngine() {
            assertThat(RsaSignature.sha256()).isInstanceOf(SignatureEngine.class);
        }

        @Test
        @DisplayName("RsaPssSignature实现SignatureEngine")
        void testRsaPssImplementsSignatureEngine() {
            assertThat(RsaPssSignature.sha256()).isInstanceOf(SignatureEngine.class);
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("未设置私钥时sign抛出异常")
        void testSignWithoutPrivateKey() {
            SignatureEngine engine = EcdsaSignature.p256();

            assertThatThrownBy(() -> engine.sign(TEST_DATA))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key not set");
        }

        @Test
        @DisplayName("未设置公钥时verify抛出异常")
        void testVerifyWithoutPublicKey() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            SignatureEngine engine = EcdsaSignature.p256()
                    .setPrivateKey(keyPair.getPrivate());

            byte[] signature = engine.sign(TEST_DATA);

            // 创建新引擎，没有公钥
            SignatureEngine verifyEngine = EcdsaSignature.p256();

            assertThatThrownBy(() -> verifyEngine.verify(TEST_DATA, signature))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Public key not set");
        }

        @Test
        @DisplayName("null数据sign抛出NullPointerException")
        void testSignNullData() {
            SignatureEngine engine = EcdsaSignature.p256WithGeneratedKeyPair();

            assertThatThrownBy(() -> engine.sign((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null签名verify抛出NullPointerException")
        void testVerifyNullSignature() {
            SignatureEngine engine = EcdsaSignature.p256WithGeneratedKeyPair();

            assertThatThrownBy(() -> engine.verify(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class ChainCallTests {

        @Test
        @DisplayName("setPrivateKey返回this")
        void testSetPrivateKeyReturnsThis() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            SignatureEngine engine = EcdsaSignature.p256();

            SignatureEngine returned = engine.setPrivateKey(keyPair.getPrivate());

            assertThat(returned).isSameAs(engine);
        }

        @Test
        @DisplayName("setPublicKey返回this")
        void testSetPublicKeyReturnsThis() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            SignatureEngine engine = EcdsaSignature.p256();

            SignatureEngine returned = engine.setPublicKey(keyPair.getPublic());

            assertThat(returned).isSameAs(engine);
        }

        @Test
        @DisplayName("setKeyPair返回this")
        void testSetKeyPairReturnsThis() {
            KeyPair keyPair = KeyGenerator.generateP256KeyPair();
            SignatureEngine engine = EcdsaSignature.p256();

            SignatureEngine returned = engine.setKeyPair(keyPair);

            assertThat(returned).isSameAs(engine);
        }

        @Test
        @DisplayName("update返回this")
        void testUpdateReturnsThis() {
            SignatureEngine engine = EcdsaSignature.p256WithGeneratedKeyPair();

            SignatureEngine returned = engine.update(TEST_DATA);

            assertThat(returned).isSameAs(engine);
            engine.doSign(); // 完成签名以清理状态
        }
    }

    @Nested
    @DisplayName("大数据测试")
    class LargeDataTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.signature.SignatureEngineTest#signatureEngineProvider")
        @DisplayName("签名大数据")
        void testSignLargeData(SignatureEngine engine) {
            byte[] largeData = new byte[1024 * 1024]; // 1MB
            new java.security.SecureRandom().nextBytes(largeData);

            byte[] signature = engine.sign(largeData);

            assertThat(signature).isNotEmpty();
            assertThat(engine.verify(largeData, signature)).isTrue();
        }
    }
}

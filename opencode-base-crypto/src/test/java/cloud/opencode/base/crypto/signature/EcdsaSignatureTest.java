package cloud.opencode.base.crypto.signature;

import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;

import static org.assertj.core.api.Assertions.*;

/**
 * EcdsaSignature 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("EcdsaSignature 测试")
class EcdsaSignatureTest {

    private static final String TEST_DATA = "Hello, ECDSA Signature!";

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("p256创建实例")
        void testP256() {
            EcdsaSignature sig = EcdsaSignature.p256();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("SHA256withECDSA");
            assertThat(sig.getCurveType()).isEqualTo(CurveType.P_256);
        }

        @Test
        @DisplayName("p384创建实例")
        void testP384() {
            EcdsaSignature sig = EcdsaSignature.p384();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("SHA384withECDSA");
            assertThat(sig.getCurveType()).isEqualTo(CurveType.P_384);
        }

        @Test
        @DisplayName("p521创建实例")
        void testP521() {
            EcdsaSignature sig = EcdsaSignature.p521();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("SHA512withECDSA");
            assertThat(sig.getCurveType()).isEqualTo(CurveType.P_521);
        }

        @Test
        @DisplayName("withCurve创建自定义曲线实例")
        void testWithCurve() {
            EcdsaSignature sig = EcdsaSignature.withCurve(CurveType.P_384);
            assertThat(sig).isNotNull();
            assertThat(sig.getCurveType()).isEqualTo(CurveType.P_384);
        }

        @Test
        @DisplayName("withCurve null抛出异常")
        void testWithCurveNull() {
            assertThatThrownBy(() -> EcdsaSignature.withCurve(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("p256WithGeneratedKeyPair创建带密钥对的实例")
        void testP256WithGeneratedKeyPair() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("p384WithGeneratedKeyPair创建带密钥对的实例")
        void testP384WithGeneratedKeyPair() {
            EcdsaSignature sig = EcdsaSignature.p384WithGeneratedKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("p521WithGeneratedKeyPair创建带密钥对的实例")
        void testP521WithGeneratedKeyPair() {
            EcdsaSignature sig = EcdsaSignature.p521WithGeneratedKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair生成密钥对")
        void testWithGeneratedKeyPair() {
            EcdsaSignature sig = EcdsaSignature.p256().withGeneratedKeyPair();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("密钥设置测试")
    class KeySetupTests {

        @Test
        @DisplayName("setPrivateKey(PrivateKey)")
        void testSetPrivateKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            EcdsaSignature sig = EcdsaSignature.p256();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThat(sig.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPrivateKey(byte[])")
        void testSetPrivateKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPrivate().getEncoded();
            EcdsaSignature sig = EcdsaSignature.p256();
            sig.setPrivateKey(encoded);
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey(PublicKey)")
        void testSetPublicKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            EcdsaSignature sig = EcdsaSignature.p256();
            sig.setPublicKey(keyPair.getPublic());
            assertThat(sig.getPublicKey()).isEqualTo(keyPair.getPublic());
        }

        @Test
        @DisplayName("setPublicKey(byte[])")
        void testSetPublicKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPublic().getEncoded();
            EcdsaSignature sig = EcdsaSignature.p256();
            sig.setPublicKey(encoded);
            assertThat(sig.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setKeyPair设置公私钥")
        void testSetKeyPair() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            EcdsaSignature sig = EcdsaSignature.p256();
            sig.setKeyPair(keyPair);
            assertThat(sig.getPublicKey()).isEqualTo(keyPair.getPublic());
            assertThat(sig.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPrivateKey null抛出异常")
        void testSetPrivateKeyNull() {
            assertThatThrownBy(() -> EcdsaSignature.p256().setPrivateKey((java.security.PrivateKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey null抛出异常")
        void testSetPublicKeyNull() {
            assertThatThrownBy(() -> EcdsaSignature.p256().setPublicKey((java.security.PublicKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setKeyPair null抛出异常")
        void testSetKeyPairNull() {
            assertThatThrownBy(() -> EcdsaSignature.p256().setKeyPair(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey非EC密钥抛出异常")
        void testSetPrivateKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> EcdsaSignature.p256().setPrivateKey(keyPair.getPrivate()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EC");
        }

        @Test
        @DisplayName("setPublicKey非EC密钥抛出异常")
        void testSetPublicKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> EcdsaSignature.p256().setPublicKey(keyPair.getPublic()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EC");
        }
    }

    @Nested
    @DisplayName("签名和验证测试")
    class SignVerifyTests {

        @Test
        @DisplayName("sign和verify字节数组")
        void testSignVerifyBytes() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();
            byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);

            byte[] signature = sig.sign(data);
            boolean valid = sig.verify(data, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("sign和verify字符串")
        void testSignVerifyString() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            byte[] signature = sig.sign(TEST_DATA);
            boolean valid = sig.verify(TEST_DATA, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64")
        void testSignVerifyBase64() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            String base64Sig = sig.signBase64(TEST_DATA);
            boolean valid = sig.verifyBase64(TEST_DATA, base64Sig);

            assertThat(valid).isTrue();
            assertThat(base64Sig).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("signHex和verifyHex")
        void testSignVerifyHex() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();
            byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);

            String hexSig = sig.signHex(data);
            boolean valid = sig.verifyHex(data, hexSig);

            assertThat(valid).isTrue();
            assertThat(hexSig).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("不同曲线签名验证")
        void testDifferentCurves() {
            // P-256
            EcdsaSignature sig256 = EcdsaSignature.p256WithGeneratedKeyPair();
            byte[] signature = sig256.sign(TEST_DATA);
            assertThat(sig256.verify(TEST_DATA, signature)).isTrue();

            // P-384
            EcdsaSignature sig384 = EcdsaSignature.p384WithGeneratedKeyPair();
            signature = sig384.sign(TEST_DATA);
            assertThat(sig384.verify(TEST_DATA, signature)).isTrue();

            // P-521
            EcdsaSignature sig521 = EcdsaSignature.p521WithGeneratedKeyPair();
            signature = sig521.sign(TEST_DATA);
            assertThat(sig521.verify(TEST_DATA, signature)).isTrue();
        }

        @Test
        @DisplayName("sign未设置私钥抛出异常")
        void testSignWithoutPrivateKey() {
            EcdsaSignature sig = EcdsaSignature.p256();

            assertThatThrownBy(() -> sig.sign(TEST_DATA))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("verify未设置公钥抛出异常")
        void testVerifyWithoutPublicKey() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);

            EcdsaSignature verifySig = EcdsaSignature.p256();
            assertThatThrownBy(() -> verifySig.verify(TEST_DATA, signature))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("sign null data抛出异常")
        void testSignNullData() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            assertThatThrownBy(() -> sig.sign((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify null data抛出异常")
        void testVerifyNullData() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);

            assertThatThrownBy(() -> sig.verify((byte[]) null, signature))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify null signature抛出异常")
        void testVerifyNullSignature() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            assertThatThrownBy(() -> sig.verify(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("篡改数据验证失败")
        void testVerifyTamperedData() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            byte[] signature = sig.sign(TEST_DATA);
            boolean valid = sig.verify("Tampered data", signature);

            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("文件签名测试")
    class FileSigningTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("signFile和verifyFile")
        void testSignVerifyFile() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            byte[] signature = sig.signFile(file);
            boolean valid = sig.verifyFile(file, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signFile null路径抛出异常")
        void testSignFileNullPath() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            assertThatThrownBy(() -> sig.signFile(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("signFile不存在的文件抛出异常")
        void testSignFileNotExists() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();
            Path nonExistent = tempDir.resolve("nonexistent.txt");

            assertThatThrownBy(() -> sig.signFile(nonExistent))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("多部分签名测试")
    class MultiPartSigningTests {

        @Test
        @DisplayName("update和doSign")
        void testUpdateAndDoSign() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            sig.update("Part 1");
            sig.update("Part 2");
            sig.update("Part 3");
            byte[] signature = sig.doSign();

            // Verify using one-shot method with concatenated data
            byte[] data = "Part 1Part 2Part 3".getBytes(StandardCharsets.UTF_8);
            boolean valid = sig.verify(data, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("doSignBase64")
        void testDoSignBase64() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            sig.update(TEST_DATA);
            String base64 = sig.doSignBase64();

            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("update和doVerify")
        void testUpdateAndDoVerify() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(256);
            KeyPair keyPair = gen.generateKeyPair();

            // Sign
            EcdsaSignature signer = EcdsaSignature.p256();
            signer.setPrivateKey(keyPair.getPrivate());
            signer.update("Part 1");
            signer.update("Part 2");
            byte[] signature = signer.doSign();

            // Verify with separate instance
            EcdsaSignature verifier = EcdsaSignature.p256();
            verifier.setPublicKey(keyPair.getPublic());
            verifier.update("Part 1");
            verifier.update("Part 2");
            boolean valid = verifier.doVerify(signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("doSign未update抛出异常")
        void testDoSignWithoutUpdate() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();

            assertThatThrownBy(() -> sig.doSign())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("doVerify未update抛出异常")
        void testDoVerifyWithoutUpdate() {
            EcdsaSignature sig = EcdsaSignature.p256WithGeneratedKeyPair();
            byte[] signature = new byte[64];

            assertThatThrownBy(() -> sig.doVerify(signature))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            assertThat(EcdsaSignature.p256().getAlgorithm()).isEqualTo("SHA256withECDSA");
            assertThat(EcdsaSignature.p384().getAlgorithm()).isEqualTo("SHA384withECDSA");
            assertThat(EcdsaSignature.p521().getAlgorithm()).isEqualTo("SHA512withECDSA");
        }

        @Test
        @DisplayName("getCurveType")
        void testGetCurveType() {
            assertThat(EcdsaSignature.p256().getCurveType()).isEqualTo(CurveType.P_256);
            assertThat(EcdsaSignature.p384().getCurveType()).isEqualTo(CurveType.P_384);
            assertThat(EcdsaSignature.p521().getCurveType()).isEqualTo(CurveType.P_521);
        }

        @Test
        @DisplayName("实现SignatureEngine接口")
        void testImplementsSignatureEngine() {
            EcdsaSignature sig = EcdsaSignature.p256();
            assertThat(sig).isInstanceOf(SignatureEngine.class);
        }
    }

    @Nested
    @DisplayName("密钥互操作测试")
    class KeyInteroperabilityTests {

        @Test
        @DisplayName("不同实例使用相同密钥对")
        void testKeyInteroperability() {
            EcdsaSignature sig1 = EcdsaSignature.p256WithGeneratedKeyPair();

            // Create another signature with same keys
            EcdsaSignature sig2 = EcdsaSignature.p256();
            sig2.setPublicKey(sig1.getPublicKey());
            sig2.setPrivateKey(sig1.getPrivateKey());

            // Sign with sig1, verify with sig2
            byte[] signature = sig1.sign(TEST_DATA);
            boolean valid = sig2.verify(TEST_DATA, signature);

            assertThat(valid).isTrue();
        }
    }
}

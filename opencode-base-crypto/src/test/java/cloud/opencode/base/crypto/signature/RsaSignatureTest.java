package cloud.opencode.base.crypto.signature;

import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.exception.OpenSignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.*;

/**
 * RsaSignature 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("RsaSignature 测试")
class RsaSignatureTest {

    private static final String TEST_DATA = "Hello, RSA Signature!";

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("sha256创建实例")
        void testSha256() {
            RsaSignature sig = RsaSignature.sha256();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("SHA256withRSA");
        }

        @Test
        @DisplayName("sha384创建实例")
        void testSha384() {
            RsaSignature sig = RsaSignature.sha384();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("SHA384withRSA");
        }

        @Test
        @DisplayName("sha512创建实例")
        void testSha512() {
            RsaSignature sig = RsaSignature.sha512();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("SHA512withRSA");
        }

        @Test
        @DisplayName("sha256WithKeyPair创建带密钥对的实例")
        void testSha256WithKeyPair() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("sha512WithKeyPair创建带密钥对的实例")
        void testSha512WithKeyPair() {
            RsaSignature sig = RsaSignature.sha512WithKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair创建自定义大小密钥对")
        void testWithGeneratedKeyPair() {
            RsaSignature sig = RsaSignature.sha256().withGeneratedKeyPair(2048);
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("密钥大小太小抛出异常")
        void testKeySizeTooSmall() {
            assertThatThrownBy(() -> RsaSignature.sha256().withGeneratedKeyPair(1024))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2048");
        }

        @Test
        @DisplayName("密钥大小不是1024的倍数抛出异常")
        void testKeySizeNotMultipleOf1024() {
            assertThatThrownBy(() -> RsaSignature.sha256().withGeneratedKeyPair(2500))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1024");
        }
    }

    @Nested
    @DisplayName("密钥设置测试")
    class KeySetupTests {

        @Test
        @DisplayName("setPrivateKey(PrivateKey)")
        void testSetPrivateKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaSignature sig = RsaSignature.sha256();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThat(sig.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPrivateKey(byte[])")
        void testSetPrivateKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPrivate().getEncoded();
            RsaSignature sig = RsaSignature.sha256();
            sig.setPrivateKey(encoded);
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey(PublicKey)")
        void testSetPublicKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaSignature sig = RsaSignature.sha256();
            sig.setPublicKey(keyPair.getPublic());
            assertThat(sig.getPublicKey()).isEqualTo(keyPair.getPublic());
        }

        @Test
        @DisplayName("setPublicKey(byte[])")
        void testSetPublicKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPublic().getEncoded();
            RsaSignature sig = RsaSignature.sha256();
            sig.setPublicKey(encoded);
            assertThat(sig.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setKeyPair设置公私钥")
        void testSetKeyPair() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaSignature sig = RsaSignature.sha256();
            sig.setKeyPair(keyPair);
            assertThat(sig.getPublicKey()).isEqualTo(keyPair.getPublic());
            assertThat(sig.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPrivateKey null抛出异常")
        void testSetPrivateKeyNull() {
            assertThatThrownBy(() -> RsaSignature.sha256().setPrivateKey((java.security.PrivateKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey null抛出异常")
        void testSetPublicKeyNull() {
            assertThatThrownBy(() -> RsaSignature.sha256().setPublicKey((java.security.PublicKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setKeyPair null抛出异常")
        void testSetKeyPairNull() {
            assertThatThrownBy(() -> RsaSignature.sha256().setKeyPair(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem null抛出异常")
        void testSetPrivateKeyPemNull() {
            assertThatThrownBy(() -> RsaSignature.sha256().setPrivateKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem null抛出异常")
        void testSetPublicKeyPemNull() {
            assertThatThrownBy(() -> RsaSignature.sha256().setPublicKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey非RSA密钥抛出异常")
        void testSetPrivateKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(256);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> RsaSignature.sha256().setPrivateKey(keyPair.getPrivate()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("RSA");
        }

        @Test
        @DisplayName("setPublicKey非RSA密钥抛出异常")
        void testSetPublicKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(256);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> RsaSignature.sha256().setPublicKey(keyPair.getPublic()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("RSA");
        }
    }

    @Nested
    @DisplayName("签名和验证测试")
    class SignVerifyTests {

        @Test
        @DisplayName("sign和verify字节数组")
        void testSignVerifyBytes() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);

            byte[] signature = sig.sign(data);
            boolean valid = sig.verify(data, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("sign和verify字符串")
        void testSignVerifyString() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            byte[] signature = sig.sign(TEST_DATA);
            boolean valid = sig.verify(TEST_DATA, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64")
        void testSignVerifyBase64() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            String base64Sig = sig.signBase64(TEST_DATA);
            boolean valid = sig.verifyBase64(TEST_DATA, base64Sig);

            assertThat(valid).isTrue();
            assertThat(base64Sig).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("signHex和verifyHex")
        void testSignVerifyHex() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);

            String hexSig = sig.signHex(data);
            boolean valid = sig.verifyHex(data, hexSig);

            assertThat(valid).isTrue();
            assertThat(hexSig).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("不同算法签名验证")
        void testDifferentAlgorithms() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            // SHA-256
            RsaSignature sig256 = RsaSignature.sha256();
            sig256.setKeyPair(keyPair);
            byte[] signature = sig256.sign(TEST_DATA);
            assertThat(sig256.verify(TEST_DATA, signature)).isTrue();

            // SHA-384
            RsaSignature sig384 = RsaSignature.sha384();
            sig384.setKeyPair(keyPair);
            signature = sig384.sign(TEST_DATA);
            assertThat(sig384.verify(TEST_DATA, signature)).isTrue();

            // SHA-512
            RsaSignature sig512 = RsaSignature.sha512();
            sig512.setKeyPair(keyPair);
            signature = sig512.sign(TEST_DATA);
            assertThat(sig512.verify(TEST_DATA, signature)).isTrue();
        }

        @Test
        @DisplayName("sign未设置私钥抛出异常")
        void testSignWithoutPrivateKey() {
            RsaSignature sig = RsaSignature.sha256();

            assertThatThrownBy(() -> sig.sign(TEST_DATA))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("verify未设置公钥抛出异常")
        void testVerifyWithoutPublicKey() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            byte[] signature = sig.sign(TEST_DATA);

            RsaSignature verifySig = RsaSignature.sha256();
            assertThatThrownBy(() -> verifySig.verify(TEST_DATA, signature))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("sign null data抛出异常")
        void testSignNullData() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            assertThatThrownBy(() -> sig.sign((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify null data抛出异常")
        void testVerifyNullData() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            byte[] signature = sig.sign(TEST_DATA);

            assertThatThrownBy(() -> sig.verify((byte[]) null, signature))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify null signature抛出异常")
        void testVerifyNullSignature() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            assertThatThrownBy(() -> sig.verify(TEST_DATA, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("篡改数据验证失败")
        void testVerifyTamperedData() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            byte[] signature = sig.sign(TEST_DATA);
            boolean valid = sig.verify("Tampered data", signature);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("错误签名验证失败")
        void testVerifyWrongSignature() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            byte[] wrongSignature = new byte[256];

            boolean valid = sig.verify(TEST_DATA, wrongSignature);

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

            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            byte[] signature = sig.signFile(file);
            boolean valid = sig.verifyFile(file, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signFile null路径抛出异常")
        void testSignFileNullPath() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            assertThatThrownBy(() -> sig.signFile(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("signFile不存在的文件抛出异常")
        void testSignFileNotExists() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            Path nonExistent = tempDir.resolve("nonexistent.txt");

            assertThatThrownBy(() -> sig.signFile(nonExistent))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("verifyFile null路径抛出异常")
        void testVerifyFileNullPath() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            byte[] signature = new byte[256];

            assertThatThrownBy(() -> sig.verifyFile(null, signature))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyFile null签名抛出异常")
        void testVerifyFileNullSignature() throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            assertThatThrownBy(() -> sig.verifyFile(file, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("多部分签名测试")
    class MultiPartSigningTests {

        @Test
        @DisplayName("update和doSign")
        void testUpdateAndDoSign() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

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
        @DisplayName("update字节数组和doSign")
        void testUpdateBytesAndDoSign() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            sig.update("Part 1".getBytes(StandardCharsets.UTF_8));
            sig.update("Part 2".getBytes(StandardCharsets.UTF_8));
            byte[] signature = sig.doSign();

            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("doSignBase64")
        void testDoSignBase64() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            sig.update(TEST_DATA);
            String base64 = sig.doSignBase64();

            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("update和doVerify")
        void testUpdateAndDoVerify() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            // Sign
            RsaSignature signer = RsaSignature.sha256();
            signer.setPrivateKey(keyPair.getPrivate());
            signer.update("Part 1");
            signer.update("Part 2");
            byte[] signature = signer.doSign();

            // Verify with separate instance
            RsaSignature verifier = RsaSignature.sha256();
            verifier.setPublicKey(keyPair.getPublic());
            verifier.update("Part 1");
            verifier.update("Part 2");
            boolean valid = verifier.doVerify(signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("doSign未update抛出异常")
        void testDoSignWithoutUpdate() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            assertThatThrownBy(() -> sig.doSign())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("doVerify未update抛出异常")
        void testDoVerifyWithoutUpdate() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            byte[] signature = new byte[256];

            assertThatThrownBy(() -> sig.doVerify(signature))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("update null数据抛出异常")
        void testUpdateNullData() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();

            assertThatThrownBy(() -> sig.update((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("doVerify null签名抛出异常")
        void testDoVerifyNullSignature() {
            RsaSignature sig = RsaSignature.sha256WithKeyPair();
            sig.update(TEST_DATA);

            assertThatThrownBy(() -> sig.doVerify(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            assertThat(RsaSignature.sha256().getAlgorithm()).isEqualTo("SHA256withRSA");
            assertThat(RsaSignature.sha384().getAlgorithm()).isEqualTo("SHA384withRSA");
            assertThat(RsaSignature.sha512().getAlgorithm()).isEqualTo("SHA512withRSA");
        }

        @Test
        @DisplayName("实现SignatureEngine接口")
        void testImplementsSignatureEngine() {
            RsaSignature sig = RsaSignature.sha256();
            assertThat(sig).isInstanceOf(SignatureEngine.class);
        }
    }
}

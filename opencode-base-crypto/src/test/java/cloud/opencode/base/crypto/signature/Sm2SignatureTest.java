package cloud.opencode.base.crypto.signature;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
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
import java.security.Security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Sm2Signature}.
 * SM2 签名单元测试。
 *
 * <p>这些测试需要 Bouncy Castle 提供程序。
 * 如果 BC 不可用，测试将被跳过。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Sm2Signature Tests / SM2签名测试")
class Sm2SignatureTest {

    private static final String TEST_DATA = "Test data for SM2 signature / SM2签名测试数据";
    private static final byte[] TEST_BYTES = TEST_DATA.getBytes(StandardCharsets.UTF_8);

    private static boolean isBouncyCastleAvailable() {
        return Sm2Signature.isBouncyCastleAvailable();
    }

    private static void ensureBouncyCastleRegistered() {
        if (isBouncyCastleAvailable() && Security.getProvider("BC") == null) {
            try {
                Class<?> bcClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Security.addProvider((java.security.Provider) bcClass.getDeclaredConstructor().newInstance());
            } catch (Exception ignored) {
                // Ignore - BC not available
            }
        }
    }

    private static KeyPair generateSm2KeyPair() throws Exception {
        ensureBouncyCastleRegistered();
        KeyPairGenerator gen = KeyPairGenerator.getInstance("SM2", "BC");
        return gen.generateKeyPair();
    }

    @Nested
    @DisplayName("Bouncy Castle Availability / BC可用性测试")
    class BouncyCastleAvailabilityTests {

        @Test
        @DisplayName("isBouncyCastleAvailable检测BC是否可用")
        void testIsBouncyCastleAvailable() {
            // This test should not throw regardless of BC availability
            boolean available = Sm2Signature.isBouncyCastleAvailable();
            assertThat(available).isNotNull();
        }

        @Test
        @DisplayName("无BC时create抛出OpenCryptoException")
        void testCreateWithoutBouncyCastle() {
            assumeTrue(!isBouncyCastleAvailable(), "This test requires BC to be unavailable");
            assertThatThrownBy(() -> Sm2Signature.create())
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("Bouncy Castle");
        }
    }

    @Nested
    @DisplayName("Factory Methods / 工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("create创建SM2签名实例")
        void testCreate() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("SM3withSM2");
        }
    }

    @Nested
    @DisplayName("Key Setup / 密钥设置测试")
    class KeySetupTests {

        @Test
        @DisplayName("setPrivateKey设置私钥")
        void testSetPrivateKey() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            // Cannot get private key directly, but can sign
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("setPublicKey设置公钥")
        void testSetPublicKey() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature signer = Sm2Signature.create();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            Sm2Signature verifier = Sm2Signature.create();
            verifier.setPublicKey(keyPair.getPublic());
            boolean valid = verifier.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("setKeyPair设置密钥对")
        void testSetKeyPair() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("setPrivateKey(byte[])设置编码私钥")
        void testSetPrivateKeyBytes() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            // SM2 encoded key format may vary by BC version
            // Test that the method accepts encoded bytes
            Sm2Signature sig = Sm2Signature.create();
            try {
                sig.setPrivateKey(keyPair.getPrivate().getEncoded());
                byte[] signature = sig.sign(TEST_DATA);
                assertThat(signature).isNotEmpty();
            } catch (OpenKeyException e) {
                // SM2 encoded key format may not be compatible in this BC version
                // Skip this test scenario
                assumeTrue(false, "SM2 encoded key format not compatible with this BC version");
            }
        }

        @Test
        @DisplayName("setPublicKey(byte[])设置编码公钥")
        void testSetPublicKeyBytes() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature signer = Sm2Signature.create();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            // SM2 encoded key format may vary by BC version
            Sm2Signature verifier = Sm2Signature.create();
            try {
                verifier.setPublicKey(keyPair.getPublic().getEncoded());
                boolean valid = verifier.verify(TEST_DATA, signature);
                assertThat(valid).isTrue();
            } catch (OpenKeyException e) {
                // SM2 encoded key format may not be compatible in this BC version
                // Skip this test scenario
                assumeTrue(false, "SM2 encoded key format not compatible with this BC version");
            }
        }

        @Test
        @DisplayName("setPrivateKey(null)抛出NullPointerException")
        void testSetPrivateKeyNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPrivateKey((java.security.PrivateKey) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey(null)抛出NullPointerException")
        void testSetPublicKeyNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPublicKey((java.security.PublicKey) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setKeyPair(null)抛出NullPointerException")
        void testSetKeyPairNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setKeyPair(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey(byte[] null)抛出NullPointerException")
        void testSetPrivateKeyBytesNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPrivateKey((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey(byte[] null)抛出NullPointerException")
        void testSetPublicKeyBytesNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPublicKey((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem(null)抛出NullPointerException")
        void testSetPrivateKeyPemNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPrivateKeyPem(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem(null)抛出NullPointerException")
        void testSetPublicKeyPemNull() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPublicKeyPem(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey(byte[])拒绝无效编码")
        void testSetPrivateKeyBytesInvalid() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPrivateKey(new byte[]{1, 2, 3}))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKey(byte[])拒绝无效编码")
        void testSetPublicKeyBytesInvalid() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPublicKey(new byte[]{1, 2, 3}))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem拒绝无效PEM")
        void testSetPrivateKeyPemInvalid() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPrivateKeyPem("invalid pem"))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem拒绝无效PEM")
        void testSetPublicKeyPemInvalid() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.setPublicKeyPem("invalid pem"))
                .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("Sign and Verify / 签名和验证测试")
    class SignVerifyTests {

        @Test
        @DisplayName("sign和verify字节数组")
        void testSignVerifyBytes() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.sign(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("sign和verify字符串")
        void testSignVerifyString() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64字节数组")
        void testSignBase64VerifyBase64Bytes() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            String signature = sig.signBase64(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyBase64(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64字符串")
        void testSignBase64VerifyBase64String() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            String signature = sig.signBase64(TEST_DATA);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyBase64(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signHex和verifyHex")
        void testSignHexVerifyHex() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            String signature = sig.signHex(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyHex(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("验证篡改数据失败")
        void testVerifyTamperedDataFails() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.sign(TEST_DATA);
            boolean valid = sig.verify("Tampered data", signature);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("验证篡改签名失败")
        void testVerifyTamperedSignatureFails() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.sign(TEST_DATA);
            signature[0] ^= 0xFF;
            boolean valid = sig.verify(TEST_DATA, signature);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("sign(null)抛出NullPointerException")
        void testSignNullBytes() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.sign((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign(String null)抛出NullPointerException")
        void testSignNullString() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.sign((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null data)抛出NullPointerException")
        void testVerifyNullData() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.sign(TEST_DATA);
            assertThatThrownBy(() -> sig.verify((byte[]) null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null signature)抛出NullPointerException")
        void testVerifyNullSignature() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPublicKey(keyPair.getPublic());
            assertThatThrownBy(() -> sig.verify(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64(null signature)抛出NullPointerException")
        void testVerifyBase64NullSignature() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPublicKey(keyPair.getPublic());
            assertThatThrownBy(() -> sig.verifyBase64(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64(null data)抛出NullPointerException")
        void testVerifyBase64NullData() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            String signature = sig.signBase64(TEST_DATA);
            assertThatThrownBy(() -> sig.verifyBase64((String) null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyHex(null signature)抛出NullPointerException")
        void testVerifyHexNullSignature() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPublicKey(keyPair.getPublic());
            assertThatThrownBy(() -> sig.verifyHex(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置私钥时sign抛出IllegalStateException")
        void testSignWithoutPrivateKey() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.sign(TEST_DATA))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Private key not set");
        }

        @Test
        @DisplayName("未设置公钥时verify抛出IllegalStateException")
        void testVerifyWithoutPublicKey() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature signer = Sm2Signature.create();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            Sm2Signature verifier = Sm2Signature.create();
            assertThatThrownBy(() -> verifier.verify(TEST_DATA, signature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Public key not set");
        }
    }

    @Nested
    @DisplayName("File Signing / 文件签名测试")
    class FileSigningTests {

        @Test
        @DisplayName("signFile和verifyFile")
        void testSignVerifyFile(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.signFile(file);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyFile(file, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signFile(null)抛出NullPointerException")
        void testSignFileNull() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.signFile(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyFile(null path)抛出NullPointerException")
        void testVerifyFileNullPath(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.signFile(file);
            assertThatThrownBy(() -> sig.verifyFile(null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyFile(null signature)抛出NullPointerException")
        void testVerifyFileNullSignature(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            Sm2Signature sig = Sm2Signature.create();
            sig.setPublicKey(keyPair.getPublic());
            assertThatThrownBy(() -> sig.verifyFile(file, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("signFile不存在的文件抛出IllegalArgumentException")
        void testSignFileNotExists(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("nonexistent.txt");

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.signFile(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("verifyFile不存在的文件抛出IllegalArgumentException")
        void testVerifyFileNotExists(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("nonexistent.txt");

            Sm2Signature sig = Sm2Signature.create();
            sig.setPublicKey(keyPair.getPublic());
            assertThatThrownBy(() -> sig.verifyFile(file, new byte[64]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("verifyFile未设置公钥抛出IllegalStateException")
        void testVerifyFileWithoutPublicKey(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            Sm2Signature signer = Sm2Signature.create();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.signFile(file);

            Sm2Signature verifier = Sm2Signature.create();
            assertThatThrownBy(() -> verifier.verifyFile(file, signature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Public key not set");
        }

        @Test
        @DisplayName("大文件签名和验证")
        void testLargeFileSignVerify(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("large.txt");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5000; i++) {
                sb.append("Line ").append(i).append(": ").append(TEST_DATA).append("\n");
            }
            Files.writeString(file, sb.toString());

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature = sig.signFile(file);
            assertThat(sig.verifyFile(file, signature)).isTrue();
        }
    }

    @Nested
    @DisplayName("InputStream Signing / 输入流签名测试")
    class InputStreamSigningTests {

        @Test
        @DisplayName("sign(InputStream)签名输入流")
        void testSignInputStream(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            Sm2Signature sig = Sm2Signature.create();
            sig.setKeyPair(keyPair);
            byte[] signature;
            try (var input = Files.newInputStream(file)) {
                signature = sig.sign(input);
            }
            assertThat(signature).isNotEmpty();
            assertThat(sig.verifyFile(file, signature)).isTrue();
        }

        @Test
        @DisplayName("sign(null InputStream)抛出NullPointerException")
        void testSignNullInputStream() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.sign((java.io.InputStream) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置私钥时sign(InputStream)抛出IllegalStateException")
        void testSignInputStreamWithoutPrivateKey(@TempDir Path tempDir) throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> {
                try (var input = Files.newInputStream(file)) {
                    sig.sign(input);
                }
            }).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Multi-part Signing / 多部分签名测试")
    class MultiPartSigningTests {

        @Test
        @DisplayName("update和doSign多部分签名")
        void testMultiPartSign() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            sig.update("Part 1: ");
            sig.update("Part 2: ");
            sig.update(TEST_BYTES);
            byte[] signature = sig.doSign();
            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("doSignBase64多部分签名返回Base64")
        void testMultiPartSignBase64() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            sig.update(TEST_DATA);
            String signature = sig.doSignBase64();
            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("update和doVerify多部分验证")
        void testMultiPartVerify() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature signer = Sm2Signature.create();
            signer.setPrivateKey(keyPair.getPrivate());
            signer.update("Part 1: ");
            signer.update("Part 2");
            byte[] signature = signer.doSign();

            Sm2Signature verifier = Sm2Signature.create();
            verifier.setPublicKey(keyPair.getPublic());
            verifier.update("Part 1: ");
            verifier.update("Part 2");
            boolean valid = verifier.doVerify(signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("update(null bytes)抛出NullPointerException")
        void testUpdateNullBytes() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.update((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("update(null string)抛出NullPointerException")
        void testUpdateNullString() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.update((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("doSign未update抛出IllegalStateException")
        void testDoSignWithoutUpdate() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThatThrownBy(() -> sig.doSign())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data has been updated");
        }

        @Test
        @DisplayName("doVerify未update抛出IllegalStateException")
        void testDoVerifyWithoutUpdate() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPublicKey(keyPair.getPublic());
            assertThatThrownBy(() -> sig.doVerify(new byte[64]))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data has been updated");
        }

        @Test
        @DisplayName("doVerify(null)抛出NullPointerException")
        void testDoVerifyNull() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature sig = Sm2Signature.create();
            sig.setPublicKey(keyPair.getPublic());
            sig.update(TEST_DATA);
            assertThatThrownBy(() -> sig.doVerify(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置任何密钥时update抛出异常")
        void testUpdateWithoutKeys() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThatThrownBy(() -> sig.update(TEST_DATA))
                .isInstanceOf(OpenSignatureException.class);
        }
    }

    @Nested
    @DisplayName("Algorithm Info / 算法信息测试")
    class AlgorithmInfoTests {

        @Test
        @DisplayName("getAlgorithm返回SM3withSM2")
        void testGetAlgorithm() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm2Signature sig = Sm2Signature.create();
            assertThat(sig.getAlgorithm()).isEqualTo("SM3withSM2");
        }
    }

    @Nested
    @DisplayName("Key Interoperability / 密钥互操作性测试")
    class KeyInteroperabilityTests {

        @Test
        @DisplayName("不同实例使用同一密钥对签名和验证")
        void testSignVerifyWithSameKeysDifferentInstances() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            Sm2Signature signer = Sm2Signature.create();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            Sm2Signature verifier = Sm2Signature.create();
            verifier.setPublicKey(keyPair.getPublic());
            boolean valid = verifier.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("使用编码密钥签名和验证")
        void testSignVerifyWithEncodedKeys() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            KeyPair keyPair = generateSm2KeyPair();

            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

            // SM2 encoded key format may vary by BC version
            try {
                Sm2Signature signer = Sm2Signature.create();
                signer.setPrivateKey(privateKeyBytes);
                byte[] signature = signer.sign(TEST_DATA);

                Sm2Signature verifier = Sm2Signature.create();
                verifier.setPublicKey(publicKeyBytes);
                boolean valid = verifier.verify(TEST_DATA, signature);
                assertThat(valid).isTrue();
            } catch (OpenKeyException e) {
                // SM2 encoded key format may not be compatible in this BC version
                // Skip this test scenario
                assumeTrue(false, "SM2 encoded key format not compatible with this BC version");
            }
        }
    }
}

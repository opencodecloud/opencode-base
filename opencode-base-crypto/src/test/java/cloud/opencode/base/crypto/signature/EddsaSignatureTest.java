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
 * Unit tests for {@link EddsaSignature}.
 * EdDSA 签名单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("EddsaSignature Tests / EdDSA签名测试")
class EddsaSignatureTest {

    private static final String TEST_DATA = "Test data for EdDSA signature / EdDSA签名测试数据";
    private static final byte[] TEST_BYTES = TEST_DATA.getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("Factory Methods / 工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("ed25519创建Ed25519签名实例")
        void testEd25519() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("Ed25519");
        }

        @Test
        @DisplayName("ed448创建Ed448签名实例")
        void testEd448() {
            EddsaSignature sig = EddsaSignature.ed448();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).isEqualTo("Ed448");
        }

        @Test
        @DisplayName("ed25519WithGeneratedKeyPair创建带密钥对的Ed25519实例")
        void testEd25519WithGeneratedKeyPair() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("ed448WithGeneratedKeyPair创建带密钥对的Ed448实例")
        void testEd448WithGeneratedKeyPair() {
            EddsaSignature sig = EddsaSignature.ed448WithGeneratedKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Key Generation / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("withGeneratedKeyPair生成Ed25519密钥对")
        void testWithGeneratedKeyPairEd25519() {
            EddsaSignature sig = EddsaSignature.ed25519().withGeneratedKeyPair();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair生成Ed448密钥对")
        void testWithGeneratedKeyPairEd448() {
            EddsaSignature sig = EddsaSignature.ed448().withGeneratedKeyPair();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Key Setup / 密钥设置测试")
    class KeySetupTests {

        @Test
        @DisplayName("setPrivateKey设置私钥")
        void testSetPrivateKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature sig = EddsaSignature.ed25519();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey设置公钥")
        void testSetPublicKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature sig = EddsaSignature.ed25519();
            sig.setPublicKey(keyPair.getPublic());
            assertThat(sig.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setKeyPair设置密钥对")
        void testSetKeyPair() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature sig = EddsaSignature.ed25519();
            sig.setKeyPair(keyPair);
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPrivateKey(byte[])设置编码私钥")
        void testSetPrivateKeyBytes() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature sig = EddsaSignature.ed25519();
            sig.setPrivateKey(keyPair.getPrivate().getEncoded());
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey(byte[])设置编码公钥")
        void testSetPublicKeyBytes() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature sig = EddsaSignature.ed25519();
            sig.setPublicKey(keyPair.getPublic().getEncoded());
            assertThat(sig.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setPrivateKey(null)抛出NullPointerException")
        void testSetPrivateKeyNull() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPrivateKey((java.security.PrivateKey) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey(null)抛出NullPointerException")
        void testSetPublicKeyNull() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPublicKey((java.security.PublicKey) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setKeyPair(null)抛出NullPointerException")
        void testSetKeyPairNull() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setKeyPair(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey(byte[] null)抛出NullPointerException")
        void testSetPrivateKeyBytesNull() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPrivateKey((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey(byte[] null)抛出NullPointerException")
        void testSetPublicKeyBytesNull() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPublicKey((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem(null)抛出NullPointerException")
        void testSetPrivateKeyPemNull() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPrivateKeyPem(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem(null)抛出NullPointerException")
        void testSetPublicKeyPemNull() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPublicKeyPem(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey拒绝非EdDSA密钥")
        void testSetPrivateKeyRejectsNonEdDsaKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPrivateKey(keyPair.getPrivate()))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("setPublicKey拒绝非EdDSA密钥")
        void testSetPublicKeyRejectsNonEdDsaKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPublicKey(keyPair.getPublic()))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("setPrivateKey(byte[])拒绝无效编码")
        void testSetPrivateKeyBytesInvalid() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPrivateKey(new byte[]{1, 2, 3}))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKey(byte[])拒绝无效编码")
        void testSetPublicKeyBytesInvalid() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPublicKey(new byte[]{1, 2, 3}))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem拒绝无效PEM")
        void testSetPrivateKeyPemInvalid() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPrivateKeyPem("invalid pem"))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem拒绝无效PEM")
        void testSetPublicKeyPemInvalid() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.setPublicKeyPem("invalid pem"))
                .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("Sign and Verify / 签名和验证测试")
    class SignVerifyTests {

        @Test
        @DisplayName("Ed25519 sign和verify字节数组")
        void testEd25519SignVerifyBytes() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Ed448 sign和verify字节数组")
        void testEd448SignVerifyBytes() {
            EddsaSignature sig = EddsaSignature.ed448WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("sign和verify字符串")
        void testSignVerifyString() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64字节数组")
        void testSignBase64VerifyBase64Bytes() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            String signature = sig.signBase64(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyBase64(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64字符串")
        void testSignBase64VerifyBase64String() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            String signature = sig.signBase64(TEST_DATA);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyBase64(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signHex和verifyHex")
        void testSignHexVerifyHex() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            String signature = sig.signHex(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyHex(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("验证篡改数据失败")
        void testVerifyTamperedDataFails() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            boolean valid = sig.verify("Tampered data", signature);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("验证篡改签名失败")
        void testVerifyTamperedSignatureFails() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            signature[0] ^= 0xFF;
            // EdDSA may throw exception or return false for invalid signature
            try {
                boolean valid = sig.verify(TEST_DATA, signature);
                assertThat(valid).isFalse();
            } catch (Exception e) {
                // Exception is acceptable - verification failed
                assertThat(e).isNotNull();
            }
        }

        @Test
        @DisplayName("sign(null)抛出NullPointerException")
        void testSignNullBytes() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.sign((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign(String null)抛出NullPointerException")
        void testSignNullString() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.sign((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null data)抛出NullPointerException")
        void testVerifyNullData() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            assertThatThrownBy(() -> sig.verify((byte[]) null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null signature)抛出NullPointerException")
        void testVerifyNullSignature() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.verify(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64(null signature)抛出NullPointerException")
        void testVerifyBase64NullSignature() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.verifyBase64(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64(null data)抛出NullPointerException")
        void testVerifyBase64NullData() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            String signature = sig.signBase64(TEST_DATA);
            assertThatThrownBy(() -> sig.verifyBase64((String) null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyHex(null signature)抛出NullPointerException")
        void testVerifyHexNullSignature() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.verifyHex(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置私钥时sign抛出IllegalStateException")
        void testSignWithoutPrivateKey() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.sign(TEST_DATA))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Private key not set");
        }

        @Test
        @DisplayName("未设置公钥时verify抛出IllegalStateException")
        void testVerifyWithoutPublicKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature signer = EddsaSignature.ed25519();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            EddsaSignature verifier = EddsaSignature.ed25519();
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
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.signFile(file);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyFile(file, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signFile(null)抛出NullPointerException")
        void testSignFileNull() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.signFile(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyFile(null path)抛出NullPointerException")
        void testVerifyFileNullPath(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.signFile(file);
            assertThatThrownBy(() -> sig.verifyFile(null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyFile(null signature)抛出NullPointerException")
        void testVerifyFileNullSignature(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.verifyFile(file, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("signFile不存在的文件抛出IllegalArgumentException")
        void testSignFileNotExists(@TempDir Path tempDir) {
            Path file = tempDir.resolve("nonexistent.txt");
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.signFile(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("verifyFile不存在的文件抛出IllegalArgumentException")
        void testVerifyFileNotExists(@TempDir Path tempDir) {
            Path file = tempDir.resolve("nonexistent.txt");
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.verifyFile(file, new byte[64]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("verifyFile未设置公钥抛出IllegalStateException")
        void testVerifyFileWithoutPublicKey(@TempDir Path tempDir) throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            EddsaSignature signer = EddsaSignature.ed25519();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.signFile(file);

            EddsaSignature verifier = EddsaSignature.ed25519();
            assertThatThrownBy(() -> verifier.verifyFile(file, signature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Public key not set");
        }

        @Test
        @DisplayName("大文件签名和验证")
        void testLargeFileSignVerify(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("large.txt");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append("Line ").append(i).append(": ").append(TEST_DATA).append("\n");
            }
            Files.writeString(file, sb.toString());

            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.signFile(file);
            assertThat(sig.verifyFile(file, signature)).isTrue();
        }

        @Test
        @DisplayName("Ed448大文件签名和验证")
        void testEd448LargeFileSignVerify(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("large.txt");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5000; i++) {
                sb.append("Line ").append(i).append(": ").append(TEST_DATA).append("\n");
            }
            Files.writeString(file, sb.toString());

            EddsaSignature sig = EddsaSignature.ed448WithGeneratedKeyPair();
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
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature;
            try (var input = Files.newInputStream(file)) {
                signature = sig.sign(input);
            }
            assertThat(signature).isNotEmpty();
            assertThat(sig.verifyFile(file, signature)).isTrue();
        }

        @Test
        @DisplayName("sign(null InputStream)抛出NullPointerException")
        void testSignNullInputStream() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.sign((java.io.InputStream) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置私钥时sign(InputStream)抛出IllegalStateException")
        void testSignInputStreamWithoutPrivateKey(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            EddsaSignature sig = EddsaSignature.ed25519();
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
        void testMultiPartSign() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            sig.update("Part 1: ");
            sig.update("Part 2: ");
            sig.update(TEST_BYTES);
            byte[] signature = sig.doSign();
            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("doSignBase64多部分签名返回Base64")
        void testMultiPartSignBase64() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            sig.update(TEST_DATA);
            String signature = sig.doSignBase64();
            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("update和doVerify多部分验证")
        void testMultiPartVerify() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature signer = EddsaSignature.ed25519();
            signer.setPrivateKey(keyPair.getPrivate());
            signer.update("Part 1: ");
            signer.update("Part 2");
            byte[] signature = signer.doSign();

            EddsaSignature verifier = EddsaSignature.ed25519();
            verifier.setPublicKey(keyPair.getPublic());
            verifier.update("Part 1: ");
            verifier.update("Part 2");
            boolean valid = verifier.doVerify(signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("update(null bytes)抛出NullPointerException")
        void testUpdateNullBytes() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.update((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("update(null string)抛出NullPointerException")
        void testUpdateNullString() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.update((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("doSign未update抛出IllegalStateException")
        void testDoSignWithoutUpdate() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.doSign())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data has been updated");
        }

        @Test
        @DisplayName("doVerify未update抛出IllegalStateException")
        void testDoVerifyWithoutUpdate() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            assertThatThrownBy(() -> sig.doVerify(new byte[64]))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data has been updated");
        }

        @Test
        @DisplayName("doVerify(null)抛出NullPointerException")
        void testDoVerifyNull() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            sig.update(TEST_DATA);
            assertThatThrownBy(() -> sig.doVerify(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置任何密钥时update抛出异常")
        void testUpdateWithoutKeys() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThatThrownBy(() -> sig.update(TEST_DATA))
                .isInstanceOf(OpenSignatureException.class);
        }
    }

    @Nested
    @DisplayName("Algorithm Info / 算法信息测试")
    class AlgorithmInfoTests {

        @Test
        @DisplayName("Ed25519 getAlgorithm返回Ed25519")
        void testGetAlgorithmEd25519() {
            EddsaSignature sig = EddsaSignature.ed25519();
            assertThat(sig.getAlgorithm()).isEqualTo("Ed25519");
        }

        @Test
        @DisplayName("Ed448 getAlgorithm返回Ed448")
        void testGetAlgorithmEd448() {
            EddsaSignature sig = EddsaSignature.ed448();
            assertThat(sig.getAlgorithm()).isEqualTo("Ed448");
        }
    }

    @Nested
    @DisplayName("Key Interoperability / 密钥互操作性测试")
    class KeyInteroperabilityTests {

        @Test
        @DisplayName("不同实例使用同一密钥对签名和验证")
        void testSignVerifyWithSameKeysDifferentInstances() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            EddsaSignature signer = EddsaSignature.ed25519();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            EddsaSignature verifier = EddsaSignature.ed25519();
            verifier.setPublicKey(keyPair.getPublic());
            boolean valid = verifier.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("使用编码密钥签名和验证")
        void testSignVerifyWithEncodedKeys() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed25519");
            KeyPair keyPair = gen.generateKeyPair();

            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

            EddsaSignature signer = EddsaSignature.ed25519();
            signer.setPrivateKey(privateKeyBytes);
            byte[] signature = signer.sign(TEST_DATA);

            EddsaSignature verifier = EddsaSignature.ed25519();
            verifier.setPublicKey(publicKeyBytes);
            boolean valid = verifier.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Ed448使用编码密钥签名和验证")
        void testEd448SignVerifyWithEncodedKeys() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("Ed448");
            KeyPair keyPair = gen.generateKeyPair();

            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

            EddsaSignature signer = EddsaSignature.ed448();
            signer.setPrivateKey(privateKeyBytes);
            byte[] signature = signer.sign(TEST_DATA);

            EddsaSignature verifier = EddsaSignature.ed448();
            verifier.setPublicKey(publicKeyBytes);
            boolean valid = verifier.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("Signature Properties / 签名属性测试")
    class SignaturePropertiesTests {

        @Test
        @DisplayName("Ed25519签名长度为64字节")
        void testEd25519SignatureLength() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(signature).hasSize(64);
        }

        @Test
        @DisplayName("Ed448签名长度为114字节")
        void testEd448SignatureLength() {
            EddsaSignature sig = EddsaSignature.ed448WithGeneratedKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(signature).hasSize(114);
        }

        @Test
        @DisplayName("Ed25519签名是确定性的")
        void testEd25519DeterministicSignature() {
            EddsaSignature sig = EddsaSignature.ed25519WithGeneratedKeyPair();
            byte[] signature1 = sig.sign(TEST_DATA);
            byte[] signature2 = sig.sign(TEST_DATA);
            assertThat(signature1).isEqualTo(signature2);
        }

        @Test
        @DisplayName("Ed448签名是确定性的")
        void testEd448DeterministicSignature() {
            EddsaSignature sig = EddsaSignature.ed448WithGeneratedKeyPair();
            byte[] signature1 = sig.sign(TEST_DATA);
            byte[] signature2 = sig.sign(TEST_DATA);
            assertThat(signature1).isEqualTo(signature2);
        }
    }
}

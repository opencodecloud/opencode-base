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
 * Unit tests for {@link RsaPssSignature}.
 * RSA-PSS 签名单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("RsaPssSignature Tests / RSA-PSS签名测试")
class RsaPssSignatureTest {

    private static final String TEST_DATA = "Test data for RSA-PSS signature / RSA-PSS签名测试数据";
    private static final byte[] TEST_BYTES = TEST_DATA.getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("Factory Methods / 工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("sha256创建RSA-PSS SHA-256签名实例")
        void testSha256() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).contains("SHA-256");
        }

        @Test
        @DisplayName("sha384创建RSA-PSS SHA-384签名实例")
        void testSha384() {
            RsaPssSignature sig = RsaPssSignature.sha384();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).contains("SHA-384");
        }

        @Test
        @DisplayName("sha512创建RSA-PSS SHA-512签名实例")
        void testSha512() {
            RsaPssSignature sig = RsaPssSignature.sha512();
            assertThat(sig).isNotNull();
            assertThat(sig.getAlgorithm()).contains("SHA-512");
        }

        @Test
        @DisplayName("sha256WithKeyPair创建带密钥对的实例")
        void testSha256WithKeyPair() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("sha512WithKeyPair创建带4096位密钥对的实例")
        void testSha512WithKeyPair() {
            RsaPssSignature sig = RsaPssSignature.sha512WithKeyPair();
            assertThat(sig).isNotNull();
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Key Generation / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("withGeneratedKeyPair生成2048位密钥对")
        void testWithGeneratedKeyPair2048() {
            RsaPssSignature sig = RsaPssSignature.sha256().withGeneratedKeyPair(2048);
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair生成3072位密钥对")
        void testWithGeneratedKeyPair3072() {
            RsaPssSignature sig = RsaPssSignature.sha384().withGeneratedKeyPair(3072);
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair生成4096位密钥对")
        void testWithGeneratedKeyPair4096() {
            RsaPssSignature sig = RsaPssSignature.sha512().withGeneratedKeyPair(4096);
            assertThat(sig.getPublicKey()).isNotNull();
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair拒绝小于2048位的密钥")
        void testWithGeneratedKeyPairRejectsSmallKey() {
            assertThatThrownBy(() -> RsaPssSignature.sha256().withGeneratedKeyPair(1024))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2048");
        }

        @Test
        @DisplayName("withGeneratedKeyPair拒绝非1024倍数的密钥大小")
        void testWithGeneratedKeyPairRejectsInvalidSize() {
            assertThatThrownBy(() -> RsaPssSignature.sha256().withGeneratedKeyPair(2500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multiple of 1024");
        }
    }

    @Nested
    @DisplayName("Key Setup / 密钥设置测试")
    class KeySetupTests {

        @Test
        @DisplayName("setPrivateKey设置私钥")
        void testSetPrivateKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature sig = RsaPssSignature.sha256();
            sig.setPrivateKey(keyPair.getPrivate());
            assertThat(sig.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPublicKey设置公钥")
        void testSetPublicKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature sig = RsaPssSignature.sha256();
            sig.setPublicKey(keyPair.getPublic());
            assertThat(sig.getPublicKey()).isEqualTo(keyPair.getPublic());
        }

        @Test
        @DisplayName("setKeyPair设置密钥对")
        void testSetKeyPair() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature sig = RsaPssSignature.sha256();
            sig.setKeyPair(keyPair);
            assertThat(sig.getPublicKey()).isEqualTo(keyPair.getPublic());
            assertThat(sig.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPrivateKey(byte[])设置编码私钥")
        void testSetPrivateKeyBytes() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature sig = RsaPssSignature.sha256();
            sig.setPrivateKey(keyPair.getPrivate().getEncoded());
            assertThat(sig.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey(byte[])设置编码公钥")
        void testSetPublicKeyBytes() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature sig = RsaPssSignature.sha256();
            sig.setPublicKey(keyPair.getPublic().getEncoded());
            assertThat(sig.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setPrivateKey(null)抛出NullPointerException")
        void testSetPrivateKeyNull() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPrivateKey((java.security.PrivateKey) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey(null)抛出NullPointerException")
        void testSetPublicKeyNull() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPublicKey((java.security.PublicKey) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setKeyPair(null)抛出NullPointerException")
        void testSetKeyPairNull() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setKeyPair(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey(byte[] null)抛出NullPointerException")
        void testSetPrivateKeyBytesNull() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPrivateKey((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey(byte[] null)抛出NullPointerException")
        void testSetPublicKeyBytesNull() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPublicKey((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem(null)抛出NullPointerException")
        void testSetPrivateKeyPemNull() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPrivateKeyPem(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem(null)抛出NullPointerException")
        void testSetPublicKeyPemNull() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPublicKeyPem(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey拒绝非RSA密钥")
        void testSetPrivateKeyRejectsNonRsaKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(256);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPrivateKey(keyPair.getPrivate()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RSA");
        }

        @Test
        @DisplayName("setPublicKey拒绝非RSA密钥")
        void testSetPublicKeyRejectsNonRsaKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(256);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPublicKey(keyPair.getPublic()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RSA");
        }

        @Test
        @DisplayName("setPrivateKey(byte[])拒绝无效编码")
        void testSetPrivateKeyBytesInvalid() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPrivateKey(new byte[]{1, 2, 3}))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKey(byte[])拒绝无效编码")
        void testSetPublicKeyBytesInvalid() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPublicKey(new byte[]{1, 2, 3}))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem拒绝无效PEM")
        void testSetPrivateKeyPemInvalid() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPrivateKeyPem("invalid pem"))
                .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem拒绝无效PEM")
        void testSetPublicKeyPemInvalid() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.setPublicKeyPem("invalid pem"))
                .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("Sign and Verify / 签名和验证测试")
    class SignVerifyTests {

        @Test
        @DisplayName("sign和verify字节数组")
        void testSignVerifyBytes() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            byte[] signature = sig.sign(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("sign和verify字符串")
        void testSignVerifyString() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64字节数组")
        void testSignBase64VerifyBase64Bytes() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            String signature = sig.signBase64(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyBase64(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signBase64和verifyBase64字符串")
        void testSignBase64VerifyBase64String() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            String signature = sig.signBase64(TEST_DATA);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyBase64(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signHex和verifyHex")
        void testSignHexVerifyHex() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            String signature = sig.signHex(TEST_BYTES);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyHex(TEST_BYTES, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("SHA-384签名和验证")
        void testSha384SignVerify() {
            RsaPssSignature sig = RsaPssSignature.sha384().withGeneratedKeyPair(3072);
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(sig.verify(TEST_DATA, signature)).isTrue();
        }

        @Test
        @DisplayName("SHA-512签名和验证")
        void testSha512SignVerify() {
            RsaPssSignature sig = RsaPssSignature.sha512WithKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            assertThat(sig.verify(TEST_DATA, signature)).isTrue();
        }

        @Test
        @DisplayName("验证篡改数据失败")
        void testVerifyTamperedDataFails() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            boolean valid = sig.verify("Tampered data", signature);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("验证篡改签名失败")
        void testVerifyTamperedSignatureFails() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            signature[0] ^= 0xFF;
            boolean valid = sig.verify(TEST_DATA, signature);
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("sign(null)抛出NullPointerException")
        void testSignNullBytes() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.sign((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("sign(String null)抛出NullPointerException")
        void testSignNullString() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.sign((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null data)抛出NullPointerException")
        void testVerifyNullData() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            byte[] signature = sig.sign(TEST_DATA);
            assertThatThrownBy(() -> sig.verify((byte[]) null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verify(null signature)抛出NullPointerException")
        void testVerifyNullSignature() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.verify(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64(null signature)抛出NullPointerException")
        void testVerifyBase64NullSignature() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.verifyBase64(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyBase64(null data)抛出NullPointerException")
        void testVerifyBase64NullData() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            String signature = sig.signBase64(TEST_DATA);
            assertThatThrownBy(() -> sig.verifyBase64((String) null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyHex(null signature)抛出NullPointerException")
        void testVerifyHexNullSignature() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.verifyHex(TEST_BYTES, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置私钥时sign抛出IllegalStateException")
        void testSignWithoutPrivateKey() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.sign(TEST_DATA))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Private key not set");
        }

        @Test
        @DisplayName("未设置公钥时verify抛出IllegalStateException")
        void testVerifyWithoutPublicKey() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature signer = RsaPssSignature.sha256();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            RsaPssSignature verifier = RsaPssSignature.sha256();
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

            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            byte[] signature = sig.signFile(file);
            assertThat(signature).isNotEmpty();
            boolean valid = sig.verifyFile(file, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("signFile(null)抛出NullPointerException")
        void testSignFileNull() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.signFile(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyFile(null path)抛出NullPointerException")
        void testVerifyFileNullPath(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            byte[] signature = sig.signFile(file);
            assertThatThrownBy(() -> sig.verifyFile(null, signature))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("verifyFile(null signature)抛出NullPointerException")
        void testVerifyFileNullSignature(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.verifyFile(file, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("signFile不存在的文件抛出IllegalArgumentException")
        void testSignFileNotExists(@TempDir Path tempDir) {
            Path file = tempDir.resolve("nonexistent.txt");
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.signFile(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("verifyFile不存在的文件抛出IllegalArgumentException")
        void testVerifyFileNotExists(@TempDir Path tempDir) {
            Path file = tempDir.resolve("nonexistent.txt");
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.verifyFile(file, new byte[256]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("verifyFile未设置公钥抛出IllegalStateException")
        void testVerifyFileWithoutPublicKey(@TempDir Path tempDir) throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            RsaPssSignature signer = RsaPssSignature.sha256();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.signFile(file);

            RsaPssSignature verifier = RsaPssSignature.sha256();
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

            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
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

            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
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
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.sign((java.io.InputStream) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置私钥时sign(InputStream)抛出IllegalStateException")
        void testSignInputStreamWithoutPrivateKey(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.txt");
            Files.writeString(file, TEST_DATA);

            RsaPssSignature sig = RsaPssSignature.sha256();
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
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            sig.update("Part 1: ");
            sig.update("Part 2: ");
            sig.update(TEST_BYTES);
            byte[] signature = sig.doSign();
            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("doSignBase64多部分签名返回Base64")
        void testMultiPartSignBase64() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            sig.update(TEST_DATA);
            String signature = sig.doSignBase64();
            assertThat(signature).isNotEmpty();
        }

        @Test
        @DisplayName("update和doVerify多部分验证")
        void testMultiPartVerify() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature signer = RsaPssSignature.sha256();
            signer.setPrivateKey(keyPair.getPrivate());
            signer.update("Part 1: ");
            signer.update("Part 2");
            byte[] signature = signer.doSign();

            RsaPssSignature verifier = RsaPssSignature.sha256();
            verifier.setPublicKey(keyPair.getPublic());
            verifier.update("Part 1: ");
            verifier.update("Part 2");
            boolean valid = verifier.doVerify(signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("update(null bytes)抛出NullPointerException")
        void testUpdateNullBytes() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.update((byte[]) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("update(null string)抛出NullPointerException")
        void testUpdateNullString() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.update((String) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("doSign未update抛出IllegalStateException")
        void testDoSignWithoutUpdate() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.doSign())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data has been updated");
        }

        @Test
        @DisplayName("doVerify未update抛出IllegalStateException")
        void testDoVerifyWithoutUpdate() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            assertThatThrownBy(() -> sig.doVerify(new byte[256]))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data has been updated");
        }

        @Test
        @DisplayName("doVerify(null)抛出NullPointerException")
        void testDoVerifyNull() {
            RsaPssSignature sig = RsaPssSignature.sha256WithKeyPair();
            sig.update(TEST_DATA);
            assertThatThrownBy(() -> sig.doVerify(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("未设置任何密钥时update抛出IllegalStateException")
        void testUpdateWithoutKeys() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThatThrownBy(() -> sig.update(TEST_DATA))
                .isInstanceOf(OpenSignatureException.class);
        }
    }

    @Nested
    @DisplayName("Algorithm Info / 算法信息测试")
    class AlgorithmInfoTests {

        @Test
        @DisplayName("getAlgorithm返回RSASSA-PSS (SHA-256)")
        void testGetAlgorithmSha256() {
            RsaPssSignature sig = RsaPssSignature.sha256();
            assertThat(sig.getAlgorithm()).isEqualTo("RSASSA-PSS (SHA-256)");
        }

        @Test
        @DisplayName("getAlgorithm返回RSASSA-PSS (SHA-384)")
        void testGetAlgorithmSha384() {
            RsaPssSignature sig = RsaPssSignature.sha384();
            assertThat(sig.getAlgorithm()).isEqualTo("RSASSA-PSS (SHA-384)");
        }

        @Test
        @DisplayName("getAlgorithm返回RSASSA-PSS (SHA-512)")
        void testGetAlgorithmSha512() {
            RsaPssSignature sig = RsaPssSignature.sha512();
            assertThat(sig.getAlgorithm()).isEqualTo("RSASSA-PSS (SHA-512)");
        }
    }

    @Nested
    @DisplayName("Key Interoperability / 密钥互操作性测试")
    class KeyInteroperabilityTests {

        @Test
        @DisplayName("不同实例使用同一密钥对签名和验证")
        void testSignVerifyWithSameKeysDifferentInstances() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            RsaPssSignature signer = RsaPssSignature.sha256();
            signer.setPrivateKey(keyPair.getPrivate());
            byte[] signature = signer.sign(TEST_DATA);

            RsaPssSignature verifier = RsaPssSignature.sha256();
            verifier.setPublicKey(keyPair.getPublic());
            boolean valid = verifier.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("使用编码密钥签名和验证")
        void testSignVerifyWithEncodedKeys() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

            RsaPssSignature signer = RsaPssSignature.sha256();
            signer.setPrivateKey(privateKeyBytes);
            byte[] signature = signer.sign(TEST_DATA);

            RsaPssSignature verifier = RsaPssSignature.sha256();
            verifier.setPublicKey(publicKeyBytes);
            boolean valid = verifier.verify(TEST_DATA, signature);
            assertThat(valid).isTrue();
        }
    }
}

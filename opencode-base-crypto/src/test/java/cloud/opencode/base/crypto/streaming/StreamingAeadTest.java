package cloud.opencode.base.crypto.streaming;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

class StreamingAeadTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static byte[] generateKey(int length) {
        byte[] key = new byte[length];
        RANDOM.nextBytes(key);
        return key;
    }

    private static byte[] generateData(int length) {
        byte[] data = new byte[length];
        RANDOM.nextBytes(data);
        return data;
    }

    private static byte[] encryptToBytes(StreamingAead aead, byte[] plaintext) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        aead.encrypt(new ByteArrayInputStream(plaintext), out);
        return out.toByteArray();
    }

    private static byte[] decryptFromBytes(StreamingAead aead, byte[] ciphertext) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        aead.decrypt(new ByteArrayInputStream(ciphertext), out);
        return out.toByteArray();
    }

    @Nested
    class AesGcm {

        @Test
        void shouldEncryptDecryptSmallData() {
            byte[] key = generateKey(32);
            byte[] plaintext = "Hello, streaming!".getBytes();

            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key), plaintext);
            byte[] decrypted = decryptFromBytes(StreamingAead.aesGcm(key), ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        void shouldEncryptDecryptEmptyData() {
            byte[] key = generateKey(32);
            byte[] plaintext = new byte[0];

            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key), plaintext);
            byte[] decrypted = decryptFromBytes(StreamingAead.aesGcm(key), ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        void shouldEncryptDecryptExactlyOneSegment() {
            byte[] key = generateKey(32);
            int segSize = 512;
            byte[] plaintext = generateData(segSize);

            StreamingAead enc = StreamingAead.aesGcm(key).setSegmentSize(segSize);
            StreamingAead dec = StreamingAead.aesGcm(key);

            byte[] ciphertext = encryptToBytes(enc, plaintext);
            byte[] decrypted = decryptFromBytes(dec, ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        void shouldEncryptDecryptMultipleSegments() {
            byte[] key = generateKey(32);
            int segSize = 256;
            byte[] plaintext = generateData(segSize * 3 + 100); // 3.39 segments

            StreamingAead enc = StreamingAead.aesGcm(key).setSegmentSize(segSize);
            StreamingAead dec = StreamingAead.aesGcm(key);

            byte[] ciphertext = encryptToBytes(enc, plaintext);
            byte[] decrypted = decryptFromBytes(dec, ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        void shouldEncryptDecryptWithAad() {
            byte[] key = generateKey(32);
            byte[] aad = "context-data".getBytes();
            byte[] plaintext = generateData(1000);

            StreamingAead enc = StreamingAead.aesGcm(key).setSegmentSize(256).setAad(aad);
            StreamingAead dec = StreamingAead.aesGcm(key).setAad(aad);

            byte[] ciphertext = encryptToBytes(enc, plaintext);
            byte[] decrypted = decryptFromBytes(dec, ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        void shouldFailDecryptWithWrongAad() {
            byte[] key = generateKey(32);
            byte[] plaintext = generateData(500);

            StreamingAead enc = StreamingAead.aesGcm(key).setSegmentSize(256).setAad("correct".getBytes());
            StreamingAead dec = StreamingAead.aesGcm(key).setAad("wrong".getBytes());

            byte[] ciphertext = encryptToBytes(enc, plaintext);

            assertThatThrownBy(() -> decryptFromBytes(dec, ciphertext))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        void shouldFailDecryptWithWrongKey() {
            byte[] key1 = generateKey(32);
            byte[] key2 = generateKey(32);
            byte[] plaintext = generateData(500);

            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key1).setSegmentSize(256), plaintext);

            assertThatThrownBy(() -> decryptFromBytes(StreamingAead.aesGcm(key2), ciphertext))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        void shouldDetectTamperedCiphertext() {
            byte[] key = generateKey(32);
            byte[] plaintext = generateData(500);

            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key).setSegmentSize(256), plaintext);
            // Tamper with a byte in the middle
            ciphertext[ciphertext.length / 2] ^= 0xFF;

            assertThatThrownBy(() -> decryptFromBytes(StreamingAead.aesGcm(key), ciphertext))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        void shouldSupportAes128Key() {
            byte[] key = generateKey(16);
            byte[] plaintext = "AES-128 test".getBytes();

            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key), plaintext);
            byte[] decrypted = decryptFromBytes(StreamingAead.aesGcm(key), ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    class ChaCha20 {

        @Test
        void shouldEncryptDecrypt() {
            byte[] key = generateKey(32);
            byte[] plaintext = generateData(1000);

            StreamingAead enc = StreamingAead.chaCha20(key).setSegmentSize(256);
            StreamingAead dec = StreamingAead.chaCha20(key);

            byte[] ciphertext = encryptToBytes(enc, plaintext);
            byte[] decrypted = decryptFromBytes(dec, ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        void shouldRejectNon32ByteKey() {
            assertThatThrownBy(() -> StreamingAead.chaCha20(generateKey(16)))
                    .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    class FileEncryption {

        @Test
        void shouldEncryptDecryptFile(@TempDir Path tempDir) throws IOException {
            byte[] key = generateKey(32);
            byte[] data = generateData(2048);

            Path source = tempDir.resolve("source.bin");
            Path encrypted = tempDir.resolve("encrypted.bin");
            Path decrypted = tempDir.resolve("decrypted.bin");

            Files.write(source, data);

            StreamingAead enc = StreamingAead.aesGcm(key).setSegmentSize(512);
            enc.encryptFile(source, encrypted);

            StreamingAead dec = StreamingAead.aesGcm(key);
            dec.decryptFile(encrypted, decrypted);

            assertThat(Files.readAllBytes(decrypted)).isEqualTo(data);
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldRejectNullKey() {
            assertThatThrownBy(() -> StreamingAead.aesGcm(null))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        void shouldRejectInvalidAesKeyLength() {
            assertThatThrownBy(() -> StreamingAead.aesGcm(generateKey(15)))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        void shouldRejectTooSmallSegmentSize() {
            assertThatThrownBy(() -> StreamingAead.aesGcm(generateKey(32)).setSegmentSize(100))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldAcceptMinSegmentSize() {
            byte[] key = generateKey(32);
            byte[] plaintext = generateData(500);

            StreamingAead enc = StreamingAead.aesGcm(key).setSegmentSize(256);
            byte[] ciphertext = encryptToBytes(enc, plaintext);
            byte[] decrypted = decryptFromBytes(StreamingAead.aesGcm(key), ciphertext);
            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    class Lifecycle {

        @Test
        void shouldRejectEncryptAfterClose() {
            StreamingAead aead = StreamingAead.aesGcm(generateKey(32));
            aead.close();
            assertThatThrownBy(() -> encryptToBytes(aead, new byte[]{1, 2, 3}))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldRejectDecryptAfterClose() {
            byte[] key = generateKey(32);
            byte[] ct = encryptToBytes(StreamingAead.aesGcm(key), new byte[]{1, 2, 3});
            StreamingAead aead = StreamingAead.aesGcm(key);
            aead.close();
            assertThatThrownBy(() -> decryptFromBytes(aead, ct))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldAllowDoubleClose() {
            StreamingAead aead = StreamingAead.aesGcm(generateKey(32));
            aead.close();
            assertThatCode(aead::close).doesNotThrowAnyException();
        }
    }

    @Nested
    class SecurityProperties {

        @Test
        void shouldDetectSegmentReordering() {
            byte[] key = generateKey(32);
            int segSize = 256;
            byte[] plaintext = generateData(segSize * 3);

            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key).setSegmentSize(segSize), plaintext);

            int headerLen = 4 + 12; // 4-byte seg size + 12-byte base nonce
            int encSegLen = 12 + segSize + 16; // nonce + ciphertext + tag

            // Swap segment 0 and segment 1
            byte[] reordered = ciphertext.clone();
            System.arraycopy(ciphertext, headerLen, reordered, headerLen + encSegLen, encSegLen);
            System.arraycopy(ciphertext, headerLen + encSegLen, reordered, headerLen, encSegLen);

            assertThatThrownBy(() -> decryptFromBytes(StreamingAead.aesGcm(key), reordered))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        void shouldDetectTruncatedStream() {
            byte[] key = generateKey(32);
            int segSize = 256;
            byte[] plaintext = generateData(segSize * 2 + 50);
            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key).setSegmentSize(segSize), plaintext);

            // Truncate after the first encrypted segment
            int headerLen = 4 + 12;
            int encSegLen = 12 + segSize + 16;
            byte[] truncated = Arrays.copyOf(ciphertext, headerLen + encSegLen);

            assertThatThrownBy(() -> decryptFromBytes(StreamingAead.aesGcm(key), truncated))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        void shouldSupportAes192Key() {
            byte[] key = generateKey(24);
            byte[] plaintext = "AES-192 test".getBytes();
            byte[] ciphertext = encryptToBytes(StreamingAead.aesGcm(key), plaintext);
            byte[] decrypted = decryptFromBytes(StreamingAead.aesGcm(key), ciphertext);
            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}

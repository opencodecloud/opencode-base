package cloud.opencode.base.io.checksum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenChecksum 工具类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("OpenChecksum 工具类测试")
class OpenChecksumTest {

    @TempDir
    Path tempDir;

    private Path testFile;
    private byte[] testData;

    @BeforeEach
    void setUp() throws IOException {
        testData = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        testFile = tempDir.resolve("test.txt");
        Files.write(testFile, testData);
    }

    @Nested
    @DisplayName("CRC32测试")
    class CRC32Tests {

        @Test
        @DisplayName("crc32(Path)")
        void testCrc32Path() {
            long crc = OpenChecksum.crc32(testFile);

            assertThat(crc).isGreaterThan(0);
        }

        @Test
        @DisplayName("crc32(InputStream)")
        void testCrc32InputStream() {
            InputStream is = new ByteArrayInputStream(testData);

            long crc = OpenChecksum.crc32(is);

            assertThat(crc).isGreaterThan(0);
        }

        @Test
        @DisplayName("crc32(byte[])")
        void testCrc32Bytes() {
            long crc = OpenChecksum.crc32(testData);

            assertThat(crc).isGreaterThan(0);
        }

        @Test
        @DisplayName("相同数据产生相同CRC32")
        void testCrc32Consistent() {
            long crc1 = OpenChecksum.crc32(testData);
            long crc2 = OpenChecksum.crc32(testFile);

            assertThat(crc1).isEqualTo(crc2);
        }
    }

    @Nested
    @DisplayName("MD5测试")
    class MD5Tests {

        @Test
        @DisplayName("md5(Path)")
        void testMd5Path() {
            String hash = OpenChecksum.md5(testFile);

            assertThat(hash).hasSize(32);
            assertThat(hash).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("md5(InputStream)")
        void testMd5InputStream() {
            InputStream is = new ByteArrayInputStream(testData);

            String hash = OpenChecksum.md5(is);

            assertThat(hash).hasSize(32);
        }

        @Test
        @DisplayName("md5(byte[])")
        void testMd5Bytes() {
            String hash = OpenChecksum.md5(testData);

            assertThat(hash).hasSize(32);
        }

        @Test
        @DisplayName("相同数据产生相同MD5")
        void testMd5Consistent() {
            String hash1 = OpenChecksum.md5(testData);
            String hash2 = OpenChecksum.md5(testFile);

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("已知MD5值验证")
        void testMd5KnownValue() {
            // "Hello, World!" 的 MD5
            String expected = "65a8e27d8879283831b664bd8b7f0ad4";
            String hash = OpenChecksum.md5(testData);

            assertThat(hash).isEqualToIgnoringCase(expected);
        }
    }

    @Nested
    @DisplayName("SHA-1测试")
    class SHA1Tests {

        @Test
        @DisplayName("sha1(Path)")
        void testSha1Path() {
            String hash = OpenChecksum.sha1(testFile);

            assertThat(hash).hasSize(40);
        }

        @Test
        @DisplayName("sha1(InputStream)")
        void testSha1InputStream() {
            InputStream is = new ByteArrayInputStream(testData);

            String hash = OpenChecksum.sha1(is);

            assertThat(hash).hasSize(40);
        }

        @Test
        @DisplayName("sha1(byte[])")
        void testSha1Bytes() {
            String hash = OpenChecksum.sha1(testData);

            assertThat(hash).hasSize(40);
        }
    }

    @Nested
    @DisplayName("SHA-256测试")
    class SHA256Tests {

        @Test
        @DisplayName("sha256(Path)")
        void testSha256Path() {
            String hash = OpenChecksum.sha256(testFile);

            assertThat(hash).hasSize(64);
        }

        @Test
        @DisplayName("sha256(InputStream)")
        void testSha256InputStream() {
            InputStream is = new ByteArrayInputStream(testData);

            String hash = OpenChecksum.sha256(is);

            assertThat(hash).hasSize(64);
        }

        @Test
        @DisplayName("sha256(byte[])")
        void testSha256Bytes() {
            String hash = OpenChecksum.sha256(testData);

            assertThat(hash).hasSize(64);
        }
    }

    @Nested
    @DisplayName("SHA-512测试")
    class SHA512Tests {

        @Test
        @DisplayName("sha512(Path)")
        void testSha512Path() {
            String hash = OpenChecksum.sha512(testFile);

            assertThat(hash).hasSize(128);
        }

        @Test
        @DisplayName("sha512(InputStream)")
        void testSha512InputStream() {
            InputStream is = new ByteArrayInputStream(testData);

            String hash = OpenChecksum.sha512(is);

            assertThat(hash).hasSize(128);
        }

        @Test
        @DisplayName("sha512(byte[])")
        void testSha512Bytes() {
            String hash = OpenChecksum.sha512(testData);

            assertThat(hash).hasSize(128);
        }
    }

    @Nested
    @DisplayName("digest方法测试")
    class DigestTests {

        @Test
        @DisplayName("digest(Path, algorithm)")
        void testDigestPath() {
            String hash = OpenChecksum.digest(testFile, "SHA-256");

            assertThat(hash).hasSize(64);
        }

        @Test
        @DisplayName("digest(InputStream, algorithm)")
        void testDigestInputStream() {
            InputStream is = new ByteArrayInputStream(testData);

            String hash = OpenChecksum.digest(is, "SHA-256");

            assertThat(hash).hasSize(64);
        }

        @Test
        @DisplayName("digest(byte[], algorithm)")
        void testDigestBytes() {
            String hash = OpenChecksum.digest(testData, "SHA-256");

            assertThat(hash).hasSize(64);
        }
    }

    @Nested
    @DisplayName("calculate方法测试")
    class CalculateTests {

        @Test
        @DisplayName("calculate(Path, algorithm)")
        void testCalculatePath() {
            Checksum checksum = OpenChecksum.calculate(testFile, "MD5");

            assertThat(checksum.algorithm()).isEqualTo("MD5");
            assertThat(checksum.hex()).hasSize(32);
            assertThat(checksum.bytes()).hasSize(16);
        }

        @Test
        @DisplayName("calculate(InputStream, algorithm)")
        void testCalculateInputStream() {
            InputStream is = new ByteArrayInputStream(testData);

            Checksum checksum = OpenChecksum.calculate(is, "SHA-256");

            assertThat(checksum.algorithm()).isEqualTo("SHA-256");
            assertThat(checksum.hex()).hasSize(64);
        }
    }

    @Nested
    @DisplayName("verify方法测试")
    class VerifyTests {

        @Test
        @DisplayName("verify(Path, expectedHash, algorithm)成功")
        void testVerifyPathSuccess() {
            String expectedHash = OpenChecksum.md5(testFile);

            boolean result = OpenChecksum.verify(testFile, expectedHash, "MD5");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("verify(Path, expectedHash, algorithm)失败")
        void testVerifyPathFailure() {
            boolean result = OpenChecksum.verify(testFile, "invalid_hash", "MD5");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("verify(InputStream, expectedHash, algorithm)")
        void testVerifyInputStream() {
            InputStream is = new ByteArrayInputStream(testData);
            String expectedHash = OpenChecksum.md5(testData);

            boolean result = OpenChecksum.verify(is, expectedHash, "MD5");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("verify(byte[], expectedHash, algorithm)")
        void testVerifyBytes() {
            String expectedHash = OpenChecksum.sha256(testData);

            boolean result = OpenChecksum.verify(testData, expectedHash, "SHA-256");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("大小写不敏感验证")
        void testVerifyCaseInsensitive() {
            String hash = OpenChecksum.md5(testData);

            assertThat(OpenChecksum.verify(testData, hash.toUpperCase(), "MD5")).isTrue();
            assertThat(OpenChecksum.verify(testData, hash.toLowerCase(), "MD5")).isTrue();
        }
    }

    @Nested
    @DisplayName("边界测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空文件校验")
        void testEmptyFile() throws IOException {
            Path emptyFile = tempDir.resolve("empty.txt");
            Files.createFile(emptyFile);

            String hash = OpenChecksum.md5(emptyFile);

            assertThat(hash).hasSize(32);
        }

        @Test
        @DisplayName("空字节数组校验")
        void testEmptyBytes() {
            String hash = OpenChecksum.md5(new byte[0]);

            assertThat(hash).hasSize(32);
        }

        @Test
        @DisplayName("大文件校验")
        void testLargeFile() throws IOException {
            Path largeFile = tempDir.resolve("large.txt");
            byte[] largeData = new byte[1024 * 1024]; // 1MB
            Files.write(largeFile, largeData);

            String hash = OpenChecksum.sha256(largeFile);

            assertThat(hash).hasSize(64);
        }
    }
}

package cloud.opencode.base.io.hex;

import cloud.opencode.base.io.exception.OpenIOOperationException;
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
 * HexDump 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
@DisplayName("HexDump 测试")
class HexDumpTest {

    @Nested
    @DisplayName("format(byte[])方法测试")
    class FormatByteArrayTests {

        @Test
        @DisplayName("格式化已知数据")
        void testFormatKnownData() {
            byte[] data = "Hello, World!".getBytes(StandardCharsets.US_ASCII);
            String result = HexDump.format(data);

            // Should have offset, hex, and ASCII columns
            assertThat(result).startsWith("00000000");
            assertThat(result).contains("48 65 6C 6C 6F 2C 20 57");
            assertThat(result).contains("|Hello, World!|");
        }

        @Test
        @DisplayName("格式化空数组")
        void testFormatEmptyArray() {
            String result = HexDump.format(new byte[0]);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("格式化单字节")
        void testFormatSingleByte() {
            String result = HexDump.format(new byte[]{0x41});

            assertThat(result).startsWith("00000000");
            assertThat(result).contains("41");
            assertThat(result).contains("|A|");
        }

        @Test
        @DisplayName("格式化正好16字节（一行）")
        void testFormatExactlyOneLine() {
            byte[] data = new byte[16];
            for (int i = 0; i < 16; i++) {
                data[i] = (byte) (0x30 + i); // '0', '1', '2', ...
            }
            String result = HexDump.format(data);

            // Should be exactly one line
            assertThat(result).doesNotContain("\n");
            assertThat(result).startsWith("00000000");
        }

        @Test
        @DisplayName("格式化多行数据")
        void testFormatMultipleLines() {
            byte[] data = new byte[32];
            for (int i = 0; i < 32; i++) {
                data[i] = (byte) i;
            }
            String result = HexDump.format(data);

            String[] lines = result.split("\n");
            assertThat(lines).hasSize(2);
            assertThat(lines[0]).startsWith("00000000");
            assertThat(lines[1]).startsWith("00000010");
        }

        @Test
        @DisplayName("不可打印字符显示为点")
        void testNonPrintableCharsAsDots() {
            byte[] data = new byte[]{0x00, 0x01, 0x1F, 0x7F, (byte) 0xFF, (byte) 0x80};
            String result = HexDump.format(data);

            // All non-printable chars should be dots in the ASCII column
            assertThat(result).contains("|......|");
        }

        @Test
        @DisplayName("可打印字符范围")
        void testPrintableCharRange() {
            byte[] data = new byte[]{0x20, 0x7E}; // space and tilde
            String result = HexDump.format(data);

            assertThat(result).contains("| ~|");
        }

        @Test
        @DisplayName("null输入抛出异常")
        void testNullByteArray() {
            assertThatThrownBy(() -> HexDump.format((byte[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("format(byte[], offset, length)方法测试")
    class FormatByteArrayRangeTests {

        @Test
        @DisplayName("格式化子范围")
        void testFormatRange() {
            byte[] data = {0x00, 0x41, 0x42, 0x43, 0x00};
            String result = HexDump.format(data, 1, 3);

            assertThat(result).contains("41 42 43");
            assertThat(result).contains("|ABC|");
        }

        @Test
        @DisplayName("长度为0返回空字符串")
        void testFormatZeroLength() {
            String result = HexDump.format(new byte[]{1, 2, 3}, 0, 0);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("无效范围抛出异常")
        void testInvalidRange() {
            byte[] data = new byte[5];

            assertThatThrownBy(() -> HexDump.format(data, -1, 3))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> HexDump.format(data, 0, 10))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> HexDump.format(data, 3, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("format(InputStream)方法测试")
    class FormatInputStreamTests {

        @Test
        @DisplayName("格式化InputStream")
        void testFormatInputStream() {
            byte[] data = "Test".getBytes(StandardCharsets.US_ASCII);
            InputStream in = new ByteArrayInputStream(data);

            String result = HexDump.format(in);

            assertThat(result).contains("54 65 73 74");
            assertThat(result).contains("|Test|");
        }

        @Test
        @DisplayName("格式化空InputStream")
        void testFormatEmptyInputStream() {
            InputStream in = new ByteArrayInputStream(new byte[0]);

            String result = HexDump.format(in);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("带限制格式化InputStream")
        void testFormatInputStreamWithLimit() {
            byte[] data = new byte[100];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) i;
            }
            InputStream in = new ByteArrayInputStream(data);

            String result = HexDump.format(in, 16);

            // Should only have one line (16 bytes)
            assertThat(result).doesNotContain("\n");
            assertThat(result).startsWith("00000000");
        }

        @Test
        @DisplayName("maxBytes为0返回空字符串")
        void testFormatInputStreamZeroMax() {
            InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});

            String result = HexDump.format(in, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null InputStream抛出异常")
        void testNullInputStream() {
            assertThatThrownBy(() -> HexDump.format((InputStream) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("负maxBytes抛出异常")
        void testNegativeMaxBytes() {
            InputStream in = new ByteArrayInputStream(new byte[0]);

            assertThatThrownBy(() -> HexDump.format(in, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("format(Path)方法测试")
    class FormatPathTests {

        @Test
        @DisplayName("格式化文件")
        void testFormatFile(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.bin");
            byte[] data = "Hello".getBytes(StandardCharsets.US_ASCII);
            Files.write(file, data);

            String result = HexDump.format(file);

            assertThat(result).contains("48 65 6C 6C 6F");
            assertThat(result).contains("|Hello|");
        }

        @Test
        @DisplayName("格式化空文件")
        void testFormatEmptyFile(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("empty.bin");
            Files.write(file, new byte[0]);

            String result = HexDump.format(file);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("不存在文件抛出异常")
        void testFormatNonExistentFile(@TempDir Path tempDir) {
            Path file = tempDir.resolve("nonexistent.bin");

            assertThatThrownBy(() -> HexDump.format(file))
                    .isInstanceOf(OpenIOOperationException.class);
        }

        @Test
        @DisplayName("null路径抛出异常")
        void testNullPath() {
            assertThatThrownBy(() -> HexDump.format((Path) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("format(Path, offset, length)方法测试")
    class FormatPathRangeTests {

        @Test
        @DisplayName("格式化文件范围")
        void testFormatFileRange(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.bin");
            byte[] data = "AABBBCC".getBytes(StandardCharsets.US_ASCII);
            Files.write(file, data);

            String result = HexDump.format(file, 2, 3);

            assertThat(result).contains("42 42 42"); // BBB
            assertThat(result).contains("|BBB|");
            // Offset should start from 2
            assertThat(result).startsWith("00000002");
        }

        @Test
        @DisplayName("长度为0返回空字符串")
        void testFormatFileRangeZeroLength(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.bin");
            Files.write(file, new byte[]{1, 2, 3});

            String result = HexDump.format(file, 0, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("负偏移量抛出异常")
        void testNegativeOffset(@TempDir Path tempDir) throws IOException {
            Path file = tempDir.resolve("test.bin");
            Files.write(file, new byte[]{1, 2, 3});

            assertThatThrownBy(() -> HexDump.format(file, -1, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("toHex方法测试")
    class ToHexTests {

        @Test
        @DisplayName("转换为小写十六进制字符串")
        void testToHex() {
            byte[] data = {0x0A, 0x0B, (byte) 0xFF};
            String result = HexDump.toHex(data);

            assertThat(result).isEqualTo("0a0bff");
        }

        @Test
        @DisplayName("空数组返回空字符串")
        void testToHexEmpty() {
            String result = HexDump.toHex(new byte[0]);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("单字节")
        void testToHexSingleByte() {
            assertThat(HexDump.toHex(new byte[]{0x00})).isEqualTo("00");
            assertThat(HexDump.toHex(new byte[]{(byte) 0xFF})).isEqualTo("ff");
        }

        @Test
        @DisplayName("子范围转换")
        void testToHexRange() {
            byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
            String result = HexDump.toHex(data, 1, 3);

            assertThat(result).isEqualTo("020304");
        }

        @Test
        @DisplayName("null输入抛出异常")
        void testToHexNull() {
            assertThatThrownBy(() -> HexDump.toHex(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("toHexUpper方法测试")
    class ToHexUpperTests {

        @Test
        @DisplayName("转换为大写十六进制字符串")
        void testToHexUpper() {
            byte[] data = {0x0A, 0x0B, (byte) 0xFF};
            String result = HexDump.toHexUpper(data);

            assertThat(result).isEqualTo("0A0BFF");
        }

        @Test
        @DisplayName("空数组返回空字符串")
        void testToHexUpperEmpty() {
            String result = HexDump.toHexUpper(new byte[0]);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null输入抛出异常")
        void testToHexUpperNull() {
            assertThatThrownBy(() -> HexDump.toHexUpper(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fromHex方法测试")
    class FromHexTests {

        @Test
        @DisplayName("解析十六进制字符串")
        void testFromHex() {
            byte[] result = HexDump.fromHex("0a0bff");
            assertThat(result).containsExactly(0x0A, 0x0B, (byte) 0xFF);
        }

        @Test
        @DisplayName("解析大写十六进制字符串")
        void testFromHexUpper() {
            byte[] result = HexDump.fromHex("0A0BFF");
            assertThat(result).containsExactly(0x0A, 0x0B, (byte) 0xFF);
        }

        @Test
        @DisplayName("解析混合大小写")
        void testFromHexMixedCase() {
            byte[] result = HexDump.fromHex("aAbBcC");
            assertThat(result).containsExactly((byte) 0xAA, (byte) 0xBB, (byte) 0xCC);
        }

        @Test
        @DisplayName("空字符串返回空数组")
        void testFromHexEmpty() {
            byte[] result = HexDump.fromHex("");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("奇数长度抛出异常")
        void testFromHexOddLength() {
            assertThatThrownBy(() -> HexDump.fromHex("abc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("even length");
        }

        @Test
        @DisplayName("无效字符抛出异常")
        void testFromHexInvalidChars() {
            assertThatThrownBy(() -> HexDump.fromHex("GGGG"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid hex character");

            assertThatThrownBy(() -> HexDump.fromHex("zz"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null输入抛出异常")
        void testFromHexNull() {
            assertThatThrownBy(() -> HexDump.fromHex(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("往返转换测试")
    class RoundTripTests {

        @Test
        @DisplayName("toHex/fromHex往返转换")
        void testRoundTrip() {
            byte[] original = new byte[256];
            for (int i = 0; i < 256; i++) {
                original[i] = (byte) i;
            }

            String hex = HexDump.toHex(original);
            byte[] decoded = HexDump.fromHex(hex);

            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("toHexUpper/fromHex往返转换")
        void testRoundTripUpper() {
            byte[] original = {0x00, 0x7F, (byte) 0x80, (byte) 0xFF};

            String hex = HexDump.toHexUpper(original);
            byte[] decoded = HexDump.fromHex(hex);

            assertThat(decoded).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("hex dump格式验证测试")
    class FormatVerificationTests {

        @Test
        @DisplayName("偏移量为8位十六进制数")
        void testOffsetFormat() {
            byte[] data = new byte[48]; // 3 lines
            String result = HexDump.format(data);
            String[] lines = result.split("\n");

            assertThat(lines[0]).startsWith("00000000");
            assertThat(lines[1]).startsWith("00000010");
            assertThat(lines[2]).startsWith("00000020");
        }

        @Test
        @DisplayName("第8字节后有额外空格")
        void testExtraSpaceAfterEighthByte() {
            // 16 bytes, check that there's an extra space between byte 8 and 9
            byte[] data = new byte[16];
            for (int i = 0; i < 16; i++) {
                data[i] = (byte) (0x41 + i);
            }
            String result = HexDump.format(data);

            // After "00000000  " the hex should have extra space between 8th and 9th bytes
            // Pattern: "XX XX XX XX XX XX XX XX  XX XX XX XX XX XX XX XX"
            // The double space between the 8th and 9th byte groups
            assertThat(result).contains("48  49");
        }

        @Test
        @DisplayName("不完整最后一行正确填充")
        void testIncompleteLastLinePadding() {
            byte[] data = new byte[]{0x41}; // just 'A'
            String result = HexDump.format(data);

            // Should still have the ASCII column properly formatted
            assertThat(result).contains("|A|");
            // The line should have proper spacing to align ASCII column
            assertThat(result).startsWith("00000000");
        }
    }
}

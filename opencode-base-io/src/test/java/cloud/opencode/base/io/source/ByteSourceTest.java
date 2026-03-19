package cloud.opencode.base.io.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ByteSource 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("ByteSource 测试")
class ByteSourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("wrap方法测试")
    class WrapTests {

        @Test
        @DisplayName("包装字节数组")
        void testWrap() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteSource source = ByteSource.wrap(data);

            assertThat(source.read()).isEqualTo(data);
        }

        @Test
        @DisplayName("包装空数组")
        void testWrapEmpty() {
            ByteSource source = ByteSource.wrap(new byte[0]);

            assertThat(source.isEmpty()).isTrue();
            assertThat(source.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("sizeIfKnown返回正确大小")
        void testSizeIfKnown() {
            byte[] data = new byte[100];
            ByteSource source = ByteSource.wrap(data);

            assertThat(source.sizeIfKnown()).contains(100L);
        }

        @Test
        @DisplayName("toString返回描述")
        void testToString() {
            ByteSource source = ByteSource.wrap(new byte[10]);

            assertThat(source.toString()).contains("ByteSource.wrap");
        }
    }

    @Nested
    @DisplayName("empty方法测试")
    class EmptyTests {

        @Test
        @DisplayName("创建空ByteSource")
        void testEmpty() {
            ByteSource source = ByteSource.empty();

            assertThat(source.isEmpty()).isTrue();
            assertThat(source.read()).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromPath方法测试")
    class FromPathTests {

        @Test
        @DisplayName("从文件路径创建")
        void testFromPath() throws IOException {
            Path file = tempDir.resolve("test.txt");
            byte[] data = {10, 20, 30, 40, 50};
            Files.write(file, data);

            ByteSource source = ByteSource.fromPath(file);

            assertThat(source.read()).isEqualTo(data);
        }

        @Test
        @DisplayName("sizeIfKnown返回文件大小")
        void testFromPathSizeIfKnown() throws IOException {
            Path file = tempDir.resolve("test.bin");
            Files.write(file, new byte[256]);

            ByteSource source = ByteSource.fromPath(file);

            assertThat(source.sizeIfKnown()).contains(256L);
        }

        @Test
        @DisplayName("toString返回路径")
        void testFromPathToString() throws IOException {
            Path file = tempDir.resolve("test.bin");
            Files.createFile(file);

            ByteSource source = ByteSource.fromPath(file);

            assertThat(source.toString()).contains("ByteSource.fromPath");
        }
    }

    @Nested
    @DisplayName("openStream方法测试")
    class OpenStreamTests {

        @Test
        @DisplayName("打开输入流")
        void testOpenStream() throws IOException {
            byte[] data = {1, 2, 3};
            ByteSource source = ByteSource.wrap(data);

            try (InputStream is = source.openStream()) {
                assertThat(is.readAllBytes()).isEqualTo(data);
            }
        }
    }

    @Nested
    @DisplayName("asCharSource方法测试")
    class AsCharSourceTests {

        @Test
        @DisplayName("转换为CharSource(UTF-8)")
        void testAsCharSourceUtf8() {
            String content = "Hello, World!";
            ByteSource source = ByteSource.wrap(content.getBytes(StandardCharsets.UTF_8));

            CharSource charSource = source.asCharSource();

            assertThat(charSource.read()).isEqualTo(content);
        }

        @Test
        @DisplayName("转换为CharSource(指定字符集)")
        void testAsCharSourceWithCharset() {
            String content = "你好，世界！";
            ByteSource source = ByteSource.wrap(content.getBytes(StandardCharsets.UTF_8));

            CharSource charSource = source.asCharSource(StandardCharsets.UTF_8);

            assertThat(charSource.read()).isEqualTo(content);
        }

        @Test
        @DisplayName("toString包含字符集信息")
        void testAsCharSourceToString() {
            ByteSource source = ByteSource.wrap(new byte[0]);
            CharSource charSource = source.asCharSource(StandardCharsets.UTF_8);

            assertThat(charSource.toString()).contains("asCharSource");
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("返回正确大小")
        void testSize() {
            ByteSource source = ByteSource.wrap(new byte[50]);

            assertThat(source.size()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("copyTo方法测试")
    class CopyToTests {

        @Test
        @DisplayName("复制到OutputStream")
        void testCopyToOutputStream() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteSource source = ByteSource.wrap(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            long copied = source.copyTo(out);

            assertThat(copied).isEqualTo(5);
            assertThat(out.toByteArray()).isEqualTo(data);
        }

        @Test
        @DisplayName("复制到ByteSink")
        void testCopyToByteSink() throws IOException {
            byte[] data = {10, 20, 30};
            ByteSource source = ByteSource.wrap(data);
            Path targetFile = tempDir.resolve("target.bin");
            ByteSink sink = ByteSink.toPath(targetFile);

            long copied = source.copyTo(sink);

            assertThat(copied).isEqualTo(3);
            assertThat(Files.readAllBytes(targetFile)).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("contentEquals方法测试")
    class ContentEqualsTests {

        @Test
        @DisplayName("相同内容返回true")
        void testContentEqualsTrue() {
            byte[] data = {1, 2, 3, 4, 5};
            ByteSource source1 = ByteSource.wrap(data);
            ByteSource source2 = ByteSource.wrap(data.clone());

            assertThat(source1.contentEquals(source2)).isTrue();
        }

        @Test
        @DisplayName("不同内容返回false")
        void testContentEqualsFalse() {
            ByteSource source1 = ByteSource.wrap(new byte[]{1, 2, 3});
            ByteSource source2 = ByteSource.wrap(new byte[]{1, 2, 4});

            assertThat(source1.contentEquals(source2)).isFalse();
        }

        @Test
        @DisplayName("不同大小快速返回false")
        void testContentEqualsDifferentSize() {
            ByteSource source1 = ByteSource.wrap(new byte[10]);
            ByteSource source2 = ByteSource.wrap(new byte[20]);

            assertThat(source1.contentEquals(source2)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEmpty方法测试")
    class IsEmptyTests {

        @Test
        @DisplayName("空源返回true")
        void testIsEmptyTrue() {
            ByteSource source = ByteSource.empty();

            assertThat(source.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("非空源返回false")
        void testIsEmptyFalse() {
            ByteSource source = ByteSource.wrap(new byte[]{1});

            assertThat(source.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("hash方法测试")
    class HashTests {

        @Test
        @DisplayName("计算SHA-256哈希")
        void testHashSha256() {
            byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
            ByteSource source = ByteSource.wrap(data);

            byte[] hash = source.hash("SHA-256");

            assertThat(hash).hasSize(32);
        }

        @Test
        @DisplayName("计算MD5哈希")
        void testHashMd5() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
            ByteSource source = ByteSource.wrap(data);

            byte[] hash = source.hash("MD5");

            assertThat(hash).hasSize(16);
        }

        @Test
        @DisplayName("不支持的算法抛出异常")
        void testHashInvalidAlgorithm() {
            ByteSource source = ByteSource.wrap(new byte[0]);

            assertThatThrownBy(() -> source.hash("INVALID"))
                .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("slice方法测试")
    class SliceTests {

        @Test
        @DisplayName("切片返回部分数据")
        void testSlice() {
            byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
            ByteSource source = ByteSource.wrap(data);

            ByteSource sliced = source.slice(2, 5);
            byte[] result = sliced.read();

            assertThat(result).containsExactly(2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("切片sizeIfKnown正确")
        void testSliceSizeIfKnown() {
            ByteSource source = ByteSource.wrap(new byte[100]);
            ByteSource sliced = source.slice(10, 50);

            assertThat(sliced.sizeIfKnown()).contains(50L);
        }

        @Test
        @DisplayName("offset为0且length为MAX_VALUE返回原始源")
        void testSliceNoOp() {
            ByteSource source = ByteSource.wrap(new byte[10]);

            ByteSource sliced = source.slice(0, Long.MAX_VALUE);

            assertThat(sliced).isSameAs(source);
        }

        @Test
        @DisplayName("负offset抛出异常")
        void testSliceNegativeOffset() {
            ByteSource source = ByteSource.wrap(new byte[10]);

            assertThatThrownBy(() -> source.slice(-1, 5))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负length抛出异常")
        void testSliceNegativeLength() {
            ByteSource source = ByteSource.wrap(new byte[10]);

            assertThatThrownBy(() -> source.slice(0, -1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString返回切片描述")
        void testSliceToString() {
            ByteSource source = ByteSource.wrap(new byte[10]);
            ByteSource sliced = source.slice(2, 5);

            assertThat(sliced.toString()).contains("slice");
        }
    }

    @Nested
    @DisplayName("concat方法测试")
    class ConcatTests {

        @Test
        @DisplayName("连接多个ByteSource(数组)")
        void testConcatVarargs() {
            ByteSource s1 = ByteSource.wrap(new byte[]{1, 2});
            ByteSource s2 = ByteSource.wrap(new byte[]{3, 4});
            ByteSource s3 = ByteSource.wrap(new byte[]{5});

            ByteSource concatenated = ByteSource.concat(s1, s2, s3);

            assertThat(concatenated.read()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("连接多个ByteSource(Iterable)")
        void testConcatIterable() {
            List<ByteSource> sources = List.of(
                ByteSource.wrap(new byte[]{1}),
                ByteSource.wrap(new byte[]{2}),
                ByteSource.wrap(new byte[]{3})
            );

            ByteSource concatenated = ByteSource.concat(sources);

            assertThat(concatenated.read()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("连接的sizeIfKnown正确")
        void testConcatSizeIfKnown() {
            ByteSource s1 = ByteSource.wrap(new byte[10]);
            ByteSource s2 = ByteSource.wrap(new byte[20]);

            ByteSource concatenated = ByteSource.concat(s1, s2);

            assertThat(concatenated.sizeIfKnown()).contains(30L);
        }

        @Test
        @DisplayName("toString返回连接描述")
        void testConcatToString() {
            ByteSource concatenated = ByteSource.concat(
                ByteSource.empty(),
                ByteSource.empty()
            );

            assertThat(concatenated.toString()).contains("concat");
        }
    }
}

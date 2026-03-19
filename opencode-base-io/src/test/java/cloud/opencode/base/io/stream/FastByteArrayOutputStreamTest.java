package cloud.opencode.base.io.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * FastByteArrayOutputStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("FastByteArrayOutputStream 测试")
class FastByteArrayOutputStreamTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造函数")
        void testDefaultConstructor() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();

            assertThat(out.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("指定初始容量构造函数")
        void testCapacityConstructor() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream(1024);

            assertThat(out.size()).isEqualTo(0);
            assertThat(out.getBuffer().length).isGreaterThanOrEqualTo(1024);
        }

        @Test
        @DisplayName("负容量抛出异常")
        void testNegativeCapacity() {
            assertThatThrownBy(() -> new FastByteArrayOutputStream(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("零容量允许")
        void testZeroCapacity() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream(0);

            assertThat(out.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("write方法测试")
    class WriteTests {

        @Test
        @DisplayName("写入单字节")
        void testWriteSingleByte() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();

            out.write(65);

            assertThat(out.size()).isEqualTo(1);
            assertThat(out.toByteArray()).containsExactly(65);
        }

        @Test
        @DisplayName("写入字节数组")
        void testWriteByteArray() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            byte[] data = {1, 2, 3, 4, 5};

            out.write(data, 0, data.length);

            assertThat(out.size()).isEqualTo(5);
            assertThat(out.toByteArray()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("部分写入字节数组")
        void testWritePartialByteArray() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            byte[] data = {1, 2, 3, 4, 5};

            out.write(data, 1, 3);

            assertThat(out.size()).isEqualTo(3);
            assertThat(out.toByteArray()).containsExactly(2, 3, 4);
        }

        @Test
        @DisplayName("超出初始容量自动扩展")
        void testAutoExpand() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream(4);
            byte[] data = new byte[100];
            for (int i = 0; i < 100; i++) {
                data[i] = (byte) i;
            }

            out.write(data, 0, data.length);

            assertThat(out.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("越界参数抛出异常")
        void testWriteOutOfBounds() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            byte[] data = {1, 2, 3};

            assertThatThrownBy(() -> out.write(data, -1, 2))
                .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> out.write(data, 0, 10))
                .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("写入0字节不改变状态")
        void testWriteZeroBytes() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            byte[] data = {1, 2, 3};

            out.write(data, 0, 0);

            assertThat(out.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("初始大小为0")
        void testInitialSize() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();

            assertThat(out.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("写入后大小更新")
        void testSizeAfterWrite() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();

            out.write(1);
            out.write(2);
            out.write(3);

            assertThat(out.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toByteArray方法测试")
    class ToByteArrayTests {

        @Test
        @DisplayName("返回正确内容")
        void testToByteArray() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write(new byte[]{1, 2, 3}, 0, 3);

            byte[] result = out.toByteArray();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("空流返回空数组")
        void testToByteArrayEmpty() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();

            byte[] result = out.toByteArray();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toByteArrayCopy方法测试")
    class ToByteArrayCopyTests {

        @Test
        @DisplayName("返回数组副本")
        void testToByteArrayCopy() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write(new byte[]{1, 2, 3}, 0, 3);

            byte[] copy = out.toByteArrayCopy();
            copy[0] = 99;

            // 原始数据不受影响
            assertThat(out.toByteArray()[0]).isEqualTo((byte) 1);
        }
    }

    @Nested
    @DisplayName("writeTo方法测试")
    class WriteToTests {

        @Test
        @DisplayName("写入另一个输出流")
        void testWriteTo() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);

            ByteArrayOutputStream target = new ByteArrayOutputStream();
            out.writeTo(target);

            assertThat(target.toByteArray()).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("reset方法测试")
    class ResetTests {

        @Test
        @DisplayName("重置大小")
        void testReset() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);

            out.reset();

            assertThat(out.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("重置后可继续写入")
        void testWriteAfterReset() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write(new byte[]{1, 2, 3}, 0, 3);
            out.reset();
            out.write(new byte[]{4, 5}, 0, 2);

            assertThat(out.toByteArray()).containsExactly(4, 5);
        }
    }

    @Nested
    @DisplayName("toInputStream方法测试")
    class ToInputStreamTests {

        @Test
        @DisplayName("转换为输入流")
        void testToInputStream() throws Exception {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);

            InputStream is = out.toInputStream();
            byte[] result = is.readAllBytes();

            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("getBuffer方法测试")
    class GetBufferTests {

        @Test
        @DisplayName("获取内部缓冲区")
        void testGetBuffer() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream(100);

            byte[] buffer = out.getBuffer();

            assertThat(buffer.length).isGreaterThanOrEqualTo(100);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("默认toString")
        void testToString() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write("Hello".getBytes(StandardCharsets.UTF_8), 0, 5);

            String result = out.toString();

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("带字符集的toString")
        void testToStringWithCharset() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write("Hello".getBytes(StandardCharsets.UTF_8), 0, 5);

            String result = out.toString("UTF-8");

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("不支持的字符集抛出异常")
        void testToStringUnsupportedCharset() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            out.write("Hello".getBytes(StandardCharsets.UTF_8), 0, 5);

            assertThatThrownBy(() -> out.toString("INVALID_CHARSET"))
                .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("大数据测试")
    class LargeDataTests {

        @Test
        @DisplayName("处理大量数据")
        void testLargeData() {
            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            byte[] data = new byte[1024 * 1024]; // 1MB
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            out.write(data, 0, data.length);

            assertThat(out.size()).isEqualTo(1024 * 1024);
        }
    }
}

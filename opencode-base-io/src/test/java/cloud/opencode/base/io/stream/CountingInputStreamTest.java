package cloud.opencode.base.io.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * CountingInputStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("CountingInputStream 测试")
class CountingInputStreamTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建计数流")
        void testConstructor() {
            byte[] data = {1, 2, 3};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            assertThat(counting.getCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("read方法测试")
    class ReadTests {

        @Test
        @DisplayName("读取单字节计数")
        void testReadSingleByte() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            counting.read();
            counting.read();
            counting.read();

            assertThat(counting.getCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("读取-1不增加计数")
        void testReadMinusOne() throws IOException {
            byte[] data = {1};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            counting.read();
            counting.read(); // 返回-1

            assertThat(counting.getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("读取字节数组计数")
        void testReadByteArray() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            byte[] buffer = new byte[5];
            counting.read(buffer, 0, 5);

            assertThat(counting.getCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("多次读取累积计数")
        void testMultipleReads() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            byte[] buffer = new byte[3];
            counting.read(buffer, 0, 3);
            counting.read(buffer, 0, 3);
            counting.read();

            assertThat(counting.getCount()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("skip方法测试")
    class SkipTests {

        @Test
        @DisplayName("跳过字节计数")
        void testSkip() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            counting.skip(5);

            assertThat(counting.getCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("混合读取和跳过")
        void testMixedReadAndSkip() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            counting.read();
            counting.read();
            counting.skip(3);
            counting.read();

            assertThat(counting.getCount()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("mark/reset方法测试")
    class MarkResetTests {

        @Test
        @DisplayName("reset后计数恢复")
        void testMarkReset() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            counting.read();
            counting.read();
            counting.mark(10);
            counting.read();
            counting.read();

            assertThat(counting.getCount()).isEqualTo(4);

            counting.reset();

            assertThat(counting.getCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getCount方法测试")
    class GetCountTests {

        @Test
        @DisplayName("初始计数为0")
        void testInitialCount() {
            byte[] data = {1, 2, 3};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            assertThat(counting.getCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("resetCount方法测试")
    class ResetCountTests {

        @Test
        @DisplayName("重置计数返回之前值")
        void testResetCount() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            counting.read();
            counting.read();
            counting.read();

            long previousCount = counting.resetCount();

            assertThat(previousCount).isEqualTo(3);
            assertThat(counting.getCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("重置后继续计数")
        void testCountAfterReset() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            counting.read();
            counting.read();
            counting.resetCount();
            counting.read();
            counting.read();

            assertThat(counting.getCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("边界测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("空流计数")
        void testEmptyStream() throws IOException {
            byte[] data = {};
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            int result = counting.read();

            assertThat(result).isEqualTo(-1);
            assertThat(counting.getCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("大数据计数")
        void testLargeData() throws IOException {
            byte[] data = new byte[100000];
            CountingInputStream counting = new CountingInputStream(new ByteArrayInputStream(data));

            byte[] buffer = new byte[10000];
            while (counting.read(buffer, 0, buffer.length) != -1) {
                // continue reading
            }

            assertThat(counting.getCount()).isEqualTo(100000);
        }
    }
}

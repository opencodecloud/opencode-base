package cloud.opencode.base.io.stream;

import cloud.opencode.base.io.exception.OpenIOOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * BoundedInputStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("BoundedInputStream 测试")
class BoundedInputStreamTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("双参数构造函数")
        void testTwoArgConstructor() {
            byte[] data = new byte[100];
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            BoundedInputStream bounded = new BoundedInputStream(bais, 50);

            assertThat(bounded.getMaxSize()).isEqualTo(50);
            assertThat(bounded.getBytesRead()).isEqualTo(0);
        }

        @Test
        @DisplayName("三参数构造函数")
        void testThreeArgConstructor() {
            byte[] data = new byte[100];
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            BoundedInputStream bounded = new BoundedInputStream(bais, 50, true);

            assertThat(bounded.getMaxSize()).isEqualTo(50);
        }

        @Test
        @DisplayName("负maxSize抛出异常")
        void testNegativeMaxSize() {
            byte[] data = new byte[10];
            ByteArrayInputStream bais = new ByteArrayInputStream(data);

            assertThatThrownBy(() -> new BoundedInputStream(bais, -1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("read方法测试")
    class ReadTests {

        @Test
        @DisplayName("读取单字节")
        void testReadSingleByte() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 10);

            int b = bounded.read();

            assertThat(b).isEqualTo(1);
            assertThat(bounded.getBytesRead()).isEqualTo(1);
        }

        @Test
        @DisplayName("达到限制返回-1")
        void testReadReturnsMinusOneAtLimit() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 3);

            bounded.read();
            bounded.read();
            bounded.read();
            int result = bounded.read();

            assertThat(result).isEqualTo(-1);
            assertThat(bounded.isLimitReached()).isTrue();
        }

        @Test
        @DisplayName("达到限制时抛出异常")
        void testReadThrowsAtLimit() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 3, true);

            bounded.read();
            bounded.read();
            bounded.read();

            assertThatThrownBy(bounded::read)
                .isInstanceOf(OpenIOOperationException.class);
        }

        @Test
        @DisplayName("读取字节数组")
        void testReadByteArray() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 5);

            byte[] buffer = new byte[10];
            int read = bounded.read(buffer, 0, 10);

            assertThat(read).isEqualTo(5);
            assertThat(bounded.getBytesRead()).isEqualTo(5);
        }

        @Test
        @DisplayName("部分读取后继续读取")
        void testPartialReads() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 7);

            byte[] buffer = new byte[3];
            bounded.read(buffer, 0, 3);
            bounded.read(buffer, 0, 3);
            int read = bounded.read(buffer, 0, 3);

            assertThat(read).isEqualTo(1); // 只剩1字节
        }
    }

    @Nested
    @DisplayName("skip方法测试")
    class SkipTests {

        @Test
        @DisplayName("跳过字节")
        void testSkip() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 10);

            long skipped = bounded.skip(5);

            assertThat(skipped).isEqualTo(5);
            assertThat(bounded.getBytesRead()).isEqualTo(5);
        }

        @Test
        @DisplayName("跳过受限制")
        void testSkipLimited() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 5);

            long skipped = bounded.skip(10);

            assertThat(skipped).isLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("available方法测试")
    class AvailableTests {

        @Test
        @DisplayName("获取可用字节数")
        void testAvailable() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 5);

            int available = bounded.available();

            assertThat(available).isLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("mark/reset方法测试")
    class MarkResetTests {

        @Test
        @DisplayName("mark和reset")
        void testMarkReset() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 10);

            bounded.read();
            bounded.read();
            bounded.mark(10);
            bounded.read();
            bounded.read();
            bounded.reset();

            assertThat(bounded.getBytesRead()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("状态查询方法测试")
    class StateQueryTests {

        @Test
        @DisplayName("getBytesRead方法")
        void testGetBytesRead() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 10);

            bounded.read();
            bounded.read();
            bounded.read();

            assertThat(bounded.getBytesRead()).isEqualTo(3);
        }

        @Test
        @DisplayName("getRemaining方法")
        void testGetRemaining() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 10);

            bounded.read();
            bounded.read();

            assertThat(bounded.getRemaining()).isEqualTo(8);
        }

        @Test
        @DisplayName("getMaxSize方法")
        void testGetMaxSize() {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 100);

            assertThat(bounded.getMaxSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("isLimitReached方法")
        void testIsLimitReached() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            BoundedInputStream bounded = new BoundedInputStream(new ByteArrayInputStream(data), 3);

            assertThat(bounded.isLimitReached()).isFalse();
            bounded.read();
            bounded.read();
            bounded.read();
            assertThat(bounded.isLimitReached()).isTrue();
        }
    }
}

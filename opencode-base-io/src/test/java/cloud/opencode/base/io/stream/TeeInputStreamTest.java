package cloud.opencode.base.io.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * TeeInputStream 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("TeeInputStream 测试")
class TeeInputStreamTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("双参数构造函数")
        void testTwoArgConstructor() {
            byte[] data = {1, 2, 3};
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(bais, baos);

            assertThat(tee.getBranch()).isEqualTo(baos);
        }

        @Test
        @DisplayName("三参数构造函数")
        void testThreeArgConstructor() {
            byte[] data = {1, 2, 3};
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(bais, baos, false);

            assertThat(tee.getBranch()).isEqualTo(baos);
        }
    }

    @Nested
    @DisplayName("read方法测试")
    class ReadTests {

        @Test
        @DisplayName("读取单字节复制到分支")
        void testReadSingleByte() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos);

            int b1 = tee.read();
            int b2 = tee.read();
            int b3 = tee.read();

            assertThat(b1).isEqualTo(1);
            assertThat(b2).isEqualTo(2);
            assertThat(b3).isEqualTo(3);
            assertThat(baos.toByteArray()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("读取字节数组复制到分支")
        void testReadByteArray() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos);

            byte[] buffer = new byte[5];
            int read = tee.read(buffer, 0, 5);

            assertThat(read).isEqualTo(5);
            assertThat(buffer).containsExactly(1, 2, 3, 4, 5);
            assertThat(baos.toByteArray()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("读取-1不写入分支")
        void testReadMinusOne() throws IOException {
            byte[] data = {1};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos);

            tee.read();
            tee.read(); // 返回-1

            assertThat(baos.toByteArray()).containsExactly(1);
        }

        @Test
        @DisplayName("部分读取复制到分支")
        void testPartialRead() throws IOException {
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos);

            byte[] buffer = new byte[3];
            tee.read(buffer, 0, 3);
            tee.read(buffer, 0, 3);

            assertThat(baos.toByteArray()).containsExactly(1, 2, 3, 4, 5, 6);
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭时关闭分支")
        void testCloseWithBranch() throws IOException {
            byte[] data = {1, 2, 3};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos, true);

            tee.close();
            // ByteArrayOutputStream关闭后仍可使用,无法直接验证
            // 只验证不抛异常
        }

        @Test
        @DisplayName("关闭时不关闭分支")
        void testCloseWithoutBranch() throws IOException {
            byte[] data = {1, 2, 3};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos, false);

            tee.close();
            // 分支流应该仍然可用
            baos.write(99);
            assertThat(baos.toByteArray()).contains((byte) 99);
        }
    }

    @Nested
    @DisplayName("getBranch方法测试")
    class GetBranchTests {

        @Test
        @DisplayName("返回分支流")
        void testGetBranch() {
            byte[] data = {1, 2, 3};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos);

            assertThat(tee.getBranch()).isSameAs(baos);
        }
    }

    @Nested
    @DisplayName("flushBranch方法测试")
    class FlushBranchTests {

        @Test
        @DisplayName("刷新分支流")
        void testFlushBranch() throws IOException {
            byte[] data = {1, 2, 3};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos);

            tee.read();
            tee.flushBranch();

            assertThat(baos.toByteArray()).containsExactly(1);
        }

        @Test
        @DisplayName("null分支流不抛异常")
        void testFlushNullBranch() {
            byte[] data = {1, 2, 3};
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), null);

            assertThatCode(tee::flushBranch).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("完整流程测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整读取并复制")
        void testFullReadAndCopy() throws IOException {
            byte[] data = "Hello, World!".getBytes();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), baos);

            byte[] result = tee.readAllBytes();
            tee.close();

            assertThat(result).isEqualTo(data);
            assertThat(baos.toByteArray()).isEqualTo(data);
        }

        @Test
        @DisplayName("边读边处理")
        void testReadAndProcess() throws IOException {
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayOutputStream copy = new ByteArrayOutputStream();
            TeeInputStream tee = new TeeInputStream(new ByteArrayInputStream(data), copy);

            int sum = 0;
            int b;
            while ((b = tee.read()) != -1) {
                sum += b;
            }

            assertThat(sum).isEqualTo(15);
            assertThat(copy.toByteArray()).containsExactly(1, 2, 3, 4, 5);
        }
    }
}

package cloud.opencode.base.crypto.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureEraser 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SecureEraser 测试")
class SecureEraserTest {

    @Nested
    @DisplayName("erase(byte[]) 测试")
    class EraseByteArrayTests {

        @Test
        @DisplayName("擦除字节数组")
        void testEraseByteArray() {
            byte[] data = {1, 2, 3, 4, 5};

            SecureEraser.erase(data);

            assertThat(data).containsOnly((byte) 0);
        }

        @Test
        @DisplayName("擦除空数组")
        void testEraseEmptyByteArray() {
            byte[] data = {};

            SecureEraser.erase(data);

            assertThat(data).isEmpty();
        }

        @Test
        @DisplayName("擦除null不抛出异常")
        void testEraseNullByteArray() {
            assertThatCode(() -> SecureEraser.erase((byte[]) null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("erase(char[]) 测试")
    class EraseCharArrayTests {

        @Test
        @DisplayName("擦除字符数组")
        void testEraseCharArray() {
            char[] data = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};

            SecureEraser.erase(data);

            assertThat(data).containsOnly('\0');
        }

        @Test
        @DisplayName("擦除空字符数组")
        void testEraseEmptyCharArray() {
            char[] data = {};

            SecureEraser.erase(data);

            assertThat(data).isEmpty();
        }

        @Test
        @DisplayName("擦除null字符数组不抛出异常")
        void testEraseNullCharArray() {
            assertThatCode(() -> SecureEraser.erase((char[]) null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("erase(ByteBuffer) 测试")
    class EraseByteBufferTests {

        @Test
        @DisplayName("擦除HeapByteBuffer")
        void testEraseHeapByteBuffer() {
            ByteBuffer buffer = ByteBuffer.allocate(10);
            buffer.put(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
            buffer.position(5);
            buffer.limit(8);

            int originalPosition = buffer.position();
            int originalLimit = buffer.limit();

            SecureEraser.erase(buffer);

            // 验证位置和限制被恢复
            assertThat(buffer.position()).isEqualTo(originalPosition);
            assertThat(buffer.limit()).isEqualTo(originalLimit);

            // 验证内容被擦除
            buffer.clear();
            while (buffer.hasRemaining()) {
                assertThat(buffer.get()).isEqualTo((byte) 0);
            }
        }

        @Test
        @DisplayName("擦除DirectByteBuffer")
        void testEraseDirectByteBuffer() {
            ByteBuffer buffer = ByteBuffer.allocateDirect(10);
            buffer.put(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

            SecureEraser.erase(buffer);

            buffer.clear();
            while (buffer.hasRemaining()) {
                assertThat(buffer.get()).isEqualTo((byte) 0);
            }
        }

        @Test
        @DisplayName("擦除null ByteBuffer不抛出异常")
        void testEraseNullByteBuffer() {
            assertThatCode(() -> SecureEraser.erase((ByteBuffer) null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("erase(int[]) 测试")
    class EraseIntArrayTests {

        @Test
        @DisplayName("擦除整数数组")
        void testEraseIntArray() {
            int[] data = {1, 2, 3, 4, 5};

            SecureEraser.erase(data);

            assertThat(data).containsOnly(0);
        }

        @Test
        @DisplayName("擦除null整数数组不抛出异常")
        void testEraseNullIntArray() {
            assertThatCode(() -> SecureEraser.erase((int[]) null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("erase(long[]) 测试")
    class EraseLongArrayTests {

        @Test
        @DisplayName("擦除长整数数组")
        void testEraseLongArray() {
            long[] data = {1L, 2L, 3L, 4L, 5L};

            SecureEraser.erase(data);

            assertThat(data).containsOnly(0L);
        }

        @Test
        @DisplayName("擦除null长整数数组不抛出异常")
        void testEraseNullLongArray() {
            assertThatCode(() -> SecureEraser.erase((long[]) null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = SecureEraser.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(AssertionError.class);
        }
    }
}

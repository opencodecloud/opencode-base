package cloud.opencode.base.crypto.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureBytes 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
@DisplayName("SecureBytes 测试")
class SecureBytesTest {

    @Nested
    @DisplayName("of() 防御性拷贝测试")
    class OfTests {

        @Test
        @DisplayName("of() 创建防御性拷贝，修改原数组不影响 SecureBytes")
        void testDefensiveCopy() {
            byte[] original = {1, 2, 3, 4, 5};
            SecureBytes sb = SecureBytes.of(original);

            // Modify original
            original[0] = 99;

            // SecureBytes should be unaffected
            assertThat(sb.getBytes()).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("of() null 输入抛出 NullPointerException")
        void testOfNull() {
            assertThatThrownBy(() -> SecureBytes.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of() 空数组可以正常创建")
        void testOfEmptyArray() {
            SecureBytes sb = SecureBytes.of(new byte[0]);
            assertThat(sb.length()).isZero();
            assertThat(sb.getBytes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("wrap() 零拷贝测试")
    class WrapTests {

        @Test
        @DisplayName("wrap() 零拷贝，共享同一内部数组")
        void testZeroCopy() {
            byte[] original = {10, 20, 30};
            SecureBytes sb = SecureBytes.wrap(original);

            // getBytesUnsafe returns the same array reference
            assertThat(sb.getBytesUnsafe()).isSameAs(original);
        }

        @Test
        @DisplayName("wrap() null 输入抛出 NullPointerException")
        void testWrapNull() {
            assertThatThrownBy(() -> SecureBytes.wrap(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getBytes() 测试")
    class GetBytesTests {

        @Test
        @DisplayName("getBytes() 返回副本，修改副本不影响内部数据")
        void testGetBytesReturnsCopy() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3});
            byte[] copy = sb.getBytes();
            copy[0] = 99;

            assertThat(sb.getBytes()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("getBytesUnsafe() 返回内部引用")
        void testGetBytesUnsafe() {
            byte[] data = {5, 6, 7};
            SecureBytes sb = SecureBytes.wrap(data);

            assertThat(sb.getBytesUnsafe()).isSameAs(data);
        }
    }

    @Nested
    @DisplayName("close() 擦除测试")
    class CloseTests {

        @Test
        @DisplayName("close() 后内部数据被清零")
        void testCloseErasesData() {
            byte[] data = {1, 2, 3, 4, 5};
            SecureBytes sb = SecureBytes.wrap(data);

            sb.close();

            // The original array should be zeroed
            assertThat(data).containsOnly((byte) 0);
            assertThat(sb.isClosed()).isTrue();
        }

        @Test
        @DisplayName("close() 后 getBytes() 抛出 IllegalStateException")
        void testGetBytesAfterClose() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3});
            sb.close();

            assertThatThrownBy(sb::getBytes)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("close() 后 getBytesUnsafe() 抛出 IllegalStateException")
        void testGetBytesUnsafeAfterClose() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3});
            sb.close();

            assertThatThrownBy(sb::getBytesUnsafe)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("close() 后 length() 抛出 IllegalStateException")
        void testLengthAfterClose() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3});
            sb.close();

            assertThatThrownBy(sb::length)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("多次 close() 不抛异常")
        void testDoubleClose() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3});
            sb.close();

            assertThatCode(sb::close).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("try-with-resources 测试")
    class TryWithResourcesTests {

        @Test
        @DisplayName("try-with-resources 自动关闭并擦除数据")
        void testAutoClose() {
            byte[] data = {10, 20, 30};
            SecureBytes sb;

            try (SecureBytes secure = SecureBytes.wrap(data)) {
                sb = secure;
                assertThat(secure.length()).isEqualTo(3);
            }

            assertThat(sb.isClosed()).isTrue();
            assertThat(data).containsOnly((byte) 0);
        }
    }

    @Nested
    @DisplayName("equals() 常量时间比较测试")
    class EqualsTests {

        @Test
        @DisplayName("相同内容的 SecureBytes 相等")
        void testEqualContent() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            SecureBytes b = SecureBytes.of(new byte[]{1, 2, 3});

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("不同内容的 SecureBytes 不相等")
        void testUnequalContent() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            SecureBytes b = SecureBytes.of(new byte[]{1, 2, 4});

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("不同长度的 SecureBytes 不相等")
        void testDifferentLength() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            SecureBytes b = SecureBytes.of(new byte[]{1, 2});

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("自身相等")
        void testSelfEquality() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            assertThat(a).isEqualTo(a);
        }

        @Test
        @DisplayName("与非 SecureBytes 对象不相等")
        void testNotEqualToOtherType() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            assertThat(a).isNotEqualTo("not a SecureBytes");
        }

        @Test
        @DisplayName("closed 的 SecureBytes 不等于任何对象")
        void testClosedNotEqual() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            SecureBytes b = SecureBytes.of(new byte[]{1, 2, 3});
            a.close();

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("两个 closed 的 SecureBytes 不相等")
        void testBothClosedNotEqual() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            SecureBytes b = SecureBytes.of(new byte[]{1, 2, 3});
            a.close();
            b.close();

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 显示长度但不泄露内容")
        void testToStringShowsLength() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3, 4, 5});
            assertThat(sb.toString()).isEqualTo("SecureBytes[length=5]");
        }

        @Test
        @DisplayName("toString() 关闭后显示 closed")
        void testToStringAfterClose() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3});
            sb.close();
            assertThat(sb.toString()).isEqualTo("SecureBytes[closed]");
        }
    }

    @Nested
    @DisplayName("hashCode() 测试")
    class HashCodeTests {

        @Test
        @DisplayName("hashCode returns constant to prevent key leakage")
        void testHashCodeConstant() {
            SecureBytes a = SecureBytes.of(new byte[]{1, 2, 3});
            SecureBytes b = SecureBytes.of(new byte[]{4, 5, 6});
            assertThat(a.hashCode()).isEqualTo(b.hashCode()).isEqualTo(31);
        }

        @Test
        @DisplayName("closed 后 hashCode 仍返回常量")
        void testHashCodeAfterClose() {
            SecureBytes sb = SecureBytes.of(new byte[]{1, 2, 3});
            sb.close();
            assertThat(sb.hashCode()).isEqualTo(31);
        }
    }
}

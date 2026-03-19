package cloud.opencode.base.crypto.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ConstantTimeUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("ConstantTimeUtil 测试")
class ConstantTimeUtilTest {

    @Nested
    @DisplayName("equals(byte[], byte[]) 测试")
    class ByteArrayEqualsTests {

        @Test
        @DisplayName("相等数组返回true")
        void testEqualsArrays() {
            byte[] a = {1, 2, 3, 4, 5};
            byte[] b = {1, 2, 3, 4, 5};

            assertThat(ConstantTimeUtil.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("不等数组返回false")
        void testNotEqualsArrays() {
            byte[] a = {1, 2, 3, 4, 5};
            byte[] b = {1, 2, 3, 4, 6};

            assertThat(ConstantTimeUtil.equals(a, b)).isFalse();
        }

        @Test
        @DisplayName("不同长度数组返回false")
        void testDifferentLengths() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 3, 4};

            assertThat(ConstantTimeUtil.equals(a, b)).isFalse();
        }

        @Test
        @DisplayName("空数组相等")
        void testEmptyArrays() {
            byte[] a = {};
            byte[] b = {};

            assertThat(ConstantTimeUtil.equals(a, b)).isTrue();
        }

        @Test
        @DisplayName("null与null相等")
        void testBothNull() {
            assertThat(ConstantTimeUtil.equals((byte[]) null, (byte[]) null)).isTrue();
        }

        @Test
        @DisplayName("null与非null不等")
        void testOneNull() {
            byte[] a = {1, 2, 3};

            assertThat(ConstantTimeUtil.equals(a, null)).isFalse();
            assertThat(ConstantTimeUtil.equals(null, a)).isFalse();
        }

        @Test
        @DisplayName("同一引用相等")
        void testSameReference() {
            byte[] a = {1, 2, 3};

            assertThat(ConstantTimeUtil.equals(a, a)).isTrue();
        }
    }

    @Nested
    @DisplayName("equals(String, String) 测试")
    class StringEqualsTests {

        @Test
        @DisplayName("相等字符串返回true")
        void testEqualsStrings() {
            assertThat(ConstantTimeUtil.equals("hello", "hello")).isTrue();
        }

        @Test
        @DisplayName("不等字符串返回false")
        void testNotEqualsStrings() {
            assertThat(ConstantTimeUtil.equals("hello", "world")).isFalse();
        }

        @Test
        @DisplayName("不同长度字符串返回false")
        void testDifferentLengthStrings() {
            assertThat(ConstantTimeUtil.equals("hello", "hello!")).isFalse();
        }

        @Test
        @DisplayName("空字符串相等")
        void testEmptyStrings() {
            assertThat(ConstantTimeUtil.equals("", "")).isTrue();
        }

        @Test
        @DisplayName("null与null相等")
        void testBothNullStrings() {
            assertThat(ConstantTimeUtil.equals((String) null, (String) null)).isTrue();
        }

        @Test
        @DisplayName("null与非null不等")
        void testOneNullString() {
            assertThat(ConstantTimeUtil.equals("hello", null)).isFalse();
            assertThat(ConstantTimeUtil.equals(null, "hello")).isFalse();
        }

        @Test
        @DisplayName("Unicode字符串比较")
        void testUnicodeStrings() {
            assertThat(ConstantTimeUtil.equals("你好世界", "你好世界")).isTrue();
            assertThat(ConstantTimeUtil.equals("你好世界", "你好")).isFalse();
        }
    }

    @Nested
    @DisplayName("compare 测试")
    class CompareTests {

        @Test
        @DisplayName("相等数组返回0")
        void testCompareEqual() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 3};

            assertThat(ConstantTimeUtil.compare(a, b)).isEqualTo(0);
        }

        @Test
        @DisplayName("第一个数组更小返回负数")
        void testCompareLess() {
            byte[] a = {1, 2, 3};
            byte[] b = {1, 2, 4};

            assertThat(ConstantTimeUtil.compare(a, b)).isLessThan(0);
        }

        @Test
        @DisplayName("第一个数组更大返回正数")
        void testCompareGreater() {
            byte[] a = {1, 2, 5};
            byte[] b = {1, 2, 4};

            assertThat(ConstantTimeUtil.compare(a, b)).isGreaterThan(0);
        }

        @Test
        @DisplayName("不同长度数组可以比较")
        void testCompareDifferentLengths() {
            byte[] a = {1, 2};
            byte[] b = {1, 2, 3};

            // 方法应该能够处理不同长度的数组而不抛出异常
            int result1 = ConstantTimeUtil.compare(a, b);
            int result2 = ConstantTimeUtil.compare(b, a);

            // 验证方法正常执行并返回结果
            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
        }

        @Test
        @DisplayName("比较null抛出异常")
        void testCompareNull() {
            assertThatThrownBy(() -> ConstantTimeUtil.compare(null, new byte[1]))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ConstantTimeUtil.compare(new byte[1], null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("比较空数组")
        void testCompareEmpty() {
            byte[] a = {};
            byte[] b = {};

            assertThat(ConstantTimeUtil.compare(a, b)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = ConstantTimeUtil.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(AssertionError.class);
        }
    }
}

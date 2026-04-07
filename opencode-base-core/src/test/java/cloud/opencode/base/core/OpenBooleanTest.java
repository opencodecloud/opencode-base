package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBoolean 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenBoolean 测试")
class OpenBooleanTest {

    @Nested
    @DisplayName("转换为 boolean 测试")
    class ToBooleanTests {

        @Test
        @DisplayName("Boolean 转 boolean")
        void testToBooleanFromBoolean() {
            assertThat(OpenBoolean.toBoolean(Boolean.TRUE)).isTrue();
            assertThat(OpenBoolean.toBoolean(Boolean.FALSE)).isFalse();
            assertThat(OpenBoolean.toBoolean((Boolean) null)).isFalse();
        }

        @Test
        @DisplayName("字符串转 boolean - true 值")
        void testToBooleanFromStringTrue() {
            assertThat(OpenBoolean.toBoolean("true")).isTrue();
            assertThat(OpenBoolean.toBoolean("TRUE")).isTrue();
            assertThat(OpenBoolean.toBoolean("yes")).isTrue();
            assertThat(OpenBoolean.toBoolean("YES")).isTrue();
            assertThat(OpenBoolean.toBoolean("y")).isTrue();
            assertThat(OpenBoolean.toBoolean("Y")).isTrue();
            assertThat(OpenBoolean.toBoolean("on")).isTrue();
            assertThat(OpenBoolean.toBoolean("ON")).isTrue();
            assertThat(OpenBoolean.toBoolean("1")).isTrue();
        }

        @Test
        @DisplayName("字符串转 boolean - false 值")
        void testToBooleanFromStringFalse() {
            assertThat(OpenBoolean.toBoolean("false")).isFalse();
            assertThat(OpenBoolean.toBoolean("no")).isFalse();
            assertThat(OpenBoolean.toBoolean("0")).isFalse();
            assertThat(OpenBoolean.toBoolean("off")).isFalse();
            assertThat(OpenBoolean.toBoolean("invalid")).isFalse();
            assertThat(OpenBoolean.toBoolean((String) null)).isFalse();
        }

        @Test
        @DisplayName("整数转 boolean")
        void testToBooleanFromInt() {
            assertThat(OpenBoolean.toBoolean(1)).isTrue();
            assertThat(OpenBoolean.toBoolean(-1)).isTrue();
            assertThat(OpenBoolean.toBoolean(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("转换为 Boolean 测试")
    class ToBooleanObjectTests {

        @Test
        @DisplayName("boolean 转 Boolean")
        void testToBooleanObject() {
            assertThat(OpenBoolean.toBooleanObject(true)).isTrue();
            assertThat(OpenBoolean.toBooleanObject(false)).isFalse();
        }

        @Test
        @DisplayName("字符串转 Boolean")
        void testToBooleanObjectFromString() {
            assertThat(OpenBoolean.toBooleanObject("true")).isEqualTo(Boolean.TRUE);
            assertThat(OpenBoolean.toBooleanObject("false")).isEqualTo(Boolean.FALSE);
            assertThat(OpenBoolean.toBooleanObject("yes")).isEqualTo(Boolean.TRUE);
            assertThat(OpenBoolean.toBooleanObject("no")).isEqualTo(Boolean.FALSE);
            assertThat(OpenBoolean.toBooleanObject("invalid")).isNull();
            assertThat(OpenBoolean.toBooleanObject(null)).isNull();
        }
    }

    @Nested
    @DisplayName("转换为字符串测试")
    class ToStringTests {

        @Test
        @DisplayName("Boolean 转字符串")
        void testToString() {
            assertThat(OpenBoolean.toString(Boolean.TRUE)).isEqualTo("true");
            assertThat(OpenBoolean.toString(Boolean.FALSE)).isEqualTo("false");
            assertThat(OpenBoolean.toString(null)).isNull();
        }

        @Test
        @DisplayName("转为 yes/no")
        void testToStringYesNo() {
            assertThat(OpenBoolean.toStringYesNo(true)).isEqualTo("yes");
            assertThat(OpenBoolean.toStringYesNo(false)).isEqualTo("no");
            assertThat(OpenBoolean.toStringYesNo(null)).isNull();
        }

        @Test
        @DisplayName("转为 on/off")
        void testToStringOnOff() {
            assertThat(OpenBoolean.toStringOnOff(true)).isEqualTo("on");
            assertThat(OpenBoolean.toStringOnOff(false)).isEqualTo("off");
            assertThat(OpenBoolean.toStringOnOff(null)).isNull();
        }

        @Test
        @DisplayName("转为 Y/N")
        void testToStringYN() {
            assertThat(OpenBoolean.toStringYN(true)).isEqualTo("Y");
            assertThat(OpenBoolean.toStringYN(false)).isEqualTo("N");
            assertThat(OpenBoolean.toStringYN(null)).isNull();
        }
    }

    @Nested
    @DisplayName("转换为整数测试")
    class ToIntegerTests {

        @Test
        @DisplayName("boolean 转整数")
        void testToIntegerBoolean() {
            assertThat(OpenBoolean.toInteger(true)).isEqualTo(1);
            assertThat(OpenBoolean.toInteger(false)).isEqualTo(0);
        }

        @Test
        @DisplayName("Boolean 转整数")
        void testToIntegerBooleanObject() {
            assertThat(OpenBoolean.toInteger(Boolean.TRUE)).isEqualTo(1);
            assertThat(OpenBoolean.toInteger(Boolean.FALSE)).isEqualTo(0);
            assertThat(OpenBoolean.toInteger((Boolean) null)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("判断测试")
    class CheckTests {

        @Test
        @DisplayName("isTrue")
        void testIsTrue() {
            assertThat(OpenBoolean.isTrue(Boolean.TRUE)).isTrue();
            assertThat(OpenBoolean.isTrue(Boolean.FALSE)).isFalse();
            assertThat(OpenBoolean.isTrue(null)).isFalse();
        }

        @Test
        @DisplayName("isFalse")
        void testIsFalse() {
            assertThat(OpenBoolean.isFalse(Boolean.FALSE)).isTrue();
            assertThat(OpenBoolean.isFalse(Boolean.TRUE)).isFalse();
            assertThat(OpenBoolean.isFalse(null)).isFalse();
        }

        @Test
        @DisplayName("isNotTrue")
        void testIsNotTrue() {
            assertThat(OpenBoolean.isNotTrue(Boolean.FALSE)).isTrue();
            assertThat(OpenBoolean.isNotTrue(null)).isTrue();
            assertThat(OpenBoolean.isNotTrue(Boolean.TRUE)).isFalse();
        }

        @Test
        @DisplayName("isNotFalse")
        void testIsNotFalse() {
            assertThat(OpenBoolean.isNotFalse(Boolean.TRUE)).isTrue();
            assertThat(OpenBoolean.isNotFalse(null)).isTrue();
            assertThat(OpenBoolean.isNotFalse(Boolean.FALSE)).isFalse();
        }
    }

    @Nested
    @DisplayName("逻辑运算测试")
    class LogicalOperationsTests {

        @Test
        @DisplayName("negate boolean")
        void testNegateBoolean() {
            assertThat(OpenBoolean.negate(true)).isFalse();
            assertThat(OpenBoolean.negate(false)).isTrue();
        }

        @Test
        @DisplayName("negate Boolean")
        void testNegateBooleanObject() {
            assertThat(OpenBoolean.negate(Boolean.TRUE)).isFalse();
            assertThat(OpenBoolean.negate(Boolean.FALSE)).isTrue();
            assertThat(OpenBoolean.negate((Boolean) null)).isNull();
        }

        @Test
        @DisplayName("and 逻辑与")
        void testAnd() {
            assertThat(OpenBoolean.and(true, true, true)).isTrue();
            assertThat(OpenBoolean.and(true, false, true)).isFalse();
            assertThat(OpenBoolean.and()).isTrue();
        }

        @Test
        @DisplayName("or 逻辑或")
        void testOr() {
            assertThat(OpenBoolean.or(false, false, true)).isTrue();
            assertThat(OpenBoolean.or(false, false, false)).isFalse();
            assertThat(OpenBoolean.or()).isFalse();
        }

        @Test
        @DisplayName("xor 逻辑异或")
        void testXor() {
            assertThat(OpenBoolean.xor(true, false, false)).isTrue();
            assertThat(OpenBoolean.xor(true, true, false)).isFalse();
            assertThat(OpenBoolean.xor(true, true, true)).isTrue();
            assertThat(OpenBoolean.xor()).isFalse();
        }

        @Test
        @DisplayName("compare 比较")
        void testCompare() {
            assertThat(OpenBoolean.compare(true, true)).isEqualTo(0);
            assertThat(OpenBoolean.compare(false, false)).isEqualTo(0);
            assertThat(OpenBoolean.compare(false, true)).isLessThan(0);
            assertThat(OpenBoolean.compare(true, false)).isGreaterThan(0);
        }
    }
}

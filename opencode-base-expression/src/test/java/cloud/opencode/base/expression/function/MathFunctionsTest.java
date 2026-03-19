package cloud.opencode.base.expression.function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MathFunctions Tests
 * MathFunctions 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("MathFunctions Tests | MathFunctions 测试")
class MathFunctionsTest {

    private static Map<String, Function> functions;

    @BeforeAll
    static void setup() {
        functions = MathFunctions.getFunctions();
    }

    @Nested
    @DisplayName("Basic Arithmetic Tests | 基本算术测试")
    class BasicArithmeticTests {

        @Test
        @DisplayName("abs function | abs 函数")
        void testAbs() {
            Function abs = functions.get("abs");
            assertThat(abs.apply(-5)).isEqualTo(5.0);
            assertThat(abs.apply(5)).isEqualTo(5.0);
            assertThat(abs.apply(-3.14)).isEqualTo(3.14);
            assertThat(abs.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("neg function | neg 函数")
        void testNeg() {
            Function neg = functions.get("neg");
            assertThat(neg.apply(5)).isEqualTo(-5.0);
            assertThat(neg.apply(-5)).isEqualTo(5.0);
            assertThat(neg.apply()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Rounding Tests | 取整测试")
    class RoundingTests {

        @Test
        @DisplayName("ceil function | ceil 函数")
        void testCeil() {
            Function ceil = functions.get("ceil");
            assertThat(ceil.apply(3.2)).isEqualTo(4.0);
            assertThat(ceil.apply(3.9)).isEqualTo(4.0);
            assertThat(ceil.apply(-3.2)).isEqualTo(-3.0);
            assertThat(ceil.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("floor function | floor 函数")
        void testFloor() {
            Function floor = functions.get("floor");
            assertThat(floor.apply(3.9)).isEqualTo(3.0);
            assertThat(floor.apply(3.2)).isEqualTo(3.0);
            assertThat(floor.apply(-3.2)).isEqualTo(-4.0);
            assertThat(floor.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("round function | round 函数")
        void testRound() {
            Function round = functions.get("round");
            assertThat(round.apply(3.4)).isEqualTo(3L);
            assertThat(round.apply(3.5)).isEqualTo(4L);
            assertThat(round.apply(3.14159, 2)).isEqualTo(3.14);
            assertThat(round.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("trunc function | trunc 函数")
        void testTrunc() {
            Function trunc = functions.get("trunc");
            assertThat(trunc.apply(3.9)).isEqualTo(3.0);
            assertThat(trunc.apply(-3.9)).isEqualTo(-3.0);
            assertThat(trunc.apply()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Min/Max Tests | 最小/最大测试")
    class MinMaxTests {

        @Test
        @DisplayName("min function | min 函数")
        void testMin() {
            Function min = functions.get("min");
            assertThat(min.apply(1, 2, 3)).isEqualTo(1.0);
            assertThat(min.apply(5, 3, 8, 1)).isEqualTo(1.0);
            assertThat(min.apply(42)).isEqualTo(42.0);
            assertThat(min.apply()).isNull();
        }

        @Test
        @DisplayName("max function | max 函数")
        void testMax() {
            Function max = functions.get("max");
            assertThat(max.apply(1, 2, 3)).isEqualTo(3.0);
            assertThat(max.apply(5, 3, 8, 1)).isEqualTo(8.0);
            assertThat(max.apply(42)).isEqualTo(42.0);
            assertThat(max.apply()).isNull();
        }
    }

    @Nested
    @DisplayName("Power/Root Tests | 幂/根测试")
    class PowerRootTests {

        @Test
        @DisplayName("pow function | pow 函数")
        void testPow() {
            Function pow = functions.get("pow");
            assertThat(pow.apply(2, 3)).isEqualTo(8.0);
            assertThat(pow.apply(4, 0.5)).isEqualTo(2.0);
            assertThat(pow.apply(2)).isEqualTo(0);
        }

        @Test
        @DisplayName("sqrt function | sqrt 函数")
        void testSqrt() {
            Function sqrt = functions.get("sqrt");
            assertThat(sqrt.apply(4)).isEqualTo(2.0);
            assertThat(sqrt.apply(9)).isEqualTo(3.0);
            assertThat(sqrt.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("cbrt function | cbrt 函数")
        void testCbrt() {
            Function cbrt = functions.get("cbrt");
            assertThat(cbrt.apply(8)).isEqualTo(2.0);
            assertThat(cbrt.apply(27)).isEqualTo(3.0);
            assertThat(cbrt.apply()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Exponential/Logarithmic Tests | 指数/对数测试")
    class ExpLogTests {

        @Test
        @DisplayName("exp function | exp 函数")
        void testExp() {
            Function exp = functions.get("exp");
            assertThat((Double) exp.apply(0)).isCloseTo(1.0, within(0.0001));
            assertThat((Double) exp.apply(1)).isCloseTo(Math.E, within(0.0001));
            assertThat(exp.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("log function | log 函数")
        void testLog() {
            Function log = functions.get("log");
            assertThat((Double) log.apply(Math.E)).isCloseTo(1.0, within(0.0001));
            assertThat(log.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("log10 function | log10 函数")
        void testLog10() {
            Function log10 = functions.get("log10");
            assertThat(log10.apply(100)).isEqualTo(2.0);
            assertThat(log10.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("log2 function | log2 函数")
        void testLog2() {
            Function log2 = functions.get("log2");
            assertThat((Double) log2.apply(8)).isCloseTo(3.0, within(0.0001));
            assertThat(log2.apply()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Trigonometric Tests | 三角函数测试")
    class TrigonometricTests {

        @Test
        @DisplayName("sin function | sin 函数")
        void testSin() {
            Function sin = functions.get("sin");
            assertThat((Double) sin.apply(0)).isCloseTo(0.0, within(0.0001));
            assertThat((Double) sin.apply(Math.PI / 2)).isCloseTo(1.0, within(0.0001));
            assertThat(sin.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("cos function | cos 函数")
        void testCos() {
            Function cos = functions.get("cos");
            assertThat((Double) cos.apply(0)).isCloseTo(1.0, within(0.0001));
            assertThat((Double) cos.apply(Math.PI)).isCloseTo(-1.0, within(0.0001));
            assertThat(cos.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("tan function | tan 函数")
        void testTan() {
            Function tan = functions.get("tan");
            assertThat((Double) tan.apply(0)).isCloseTo(0.0, within(0.0001));
            assertThat(tan.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("asin function | asin 函数")
        void testAsin() {
            Function asin = functions.get("asin");
            assertThat((Double) asin.apply(0)).isCloseTo(0.0, within(0.0001));
        }

        @Test
        @DisplayName("acos function | acos 函数")
        void testAcos() {
            Function acos = functions.get("acos");
            assertThat((Double) acos.apply(1)).isCloseTo(0.0, within(0.0001));
        }

        @Test
        @DisplayName("atan function | atan 函数")
        void testAtan() {
            Function atan = functions.get("atan");
            assertThat((Double) atan.apply(0)).isCloseTo(0.0, within(0.0001));
        }

        @Test
        @DisplayName("atan2 function | atan2 函数")
        void testAtan2() {
            Function atan2 = functions.get("atan2");
            assertThat((Double) atan2.apply(0, 1)).isCloseTo(0.0, within(0.0001));
            assertThat(atan2.apply(0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Hyperbolic Tests | 双曲函数测试")
    class HyperbolicTests {

        @Test
        @DisplayName("sinh function | sinh 函数")
        void testSinh() {
            Function sinh = functions.get("sinh");
            assertThat((Double) sinh.apply(0)).isCloseTo(0.0, within(0.0001));
        }

        @Test
        @DisplayName("cosh function | cosh 函数")
        void testCosh() {
            Function cosh = functions.get("cosh");
            assertThat((Double) cosh.apply(0)).isCloseTo(1.0, within(0.0001));
        }

        @Test
        @DisplayName("tanh function | tanh 函数")
        void testTanh() {
            Function tanh = functions.get("tanh");
            assertThat((Double) tanh.apply(0)).isCloseTo(0.0, within(0.0001));
        }
    }

    @Nested
    @DisplayName("Conversion Tests | 转换测试")
    class ConversionTests {

        @Test
        @DisplayName("todegrees function | todegrees 函数")
        void testToDegrees() {
            Function todegrees = functions.get("todegrees");
            assertThat((Double) todegrees.apply(Math.PI)).isCloseTo(180.0, within(0.0001));
        }

        @Test
        @DisplayName("toradians function | toradians 函数")
        void testToRadians() {
            Function toradians = functions.get("toradians");
            assertThat((Double) toradians.apply(180)).isCloseTo(Math.PI, within(0.0001));
        }
    }

    @Nested
    @DisplayName("Sign Tests | 符号测试")
    class SignTests {

        @Test
        @DisplayName("sign function | sign 函数")
        void testSign() {
            Function sign = functions.get("sign");
            assertThat(sign.apply(5)).isEqualTo(1.0);
            assertThat(sign.apply(-5)).isEqualTo(-1.0);
            assertThat(sign.apply(0)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Random Tests | 随机数测试")
    class RandomTests {

        @Test
        @DisplayName("random function no args | random 函数无参数")
        void testRandomNoArgs() {
            Function random = functions.get("random");
            double result = (Double) random.apply();
            assertThat(result).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("random function with max | random 函数带最大值")
        void testRandomWithMax() {
            Function random = functions.get("random");
            double result = (Double) random.apply(10);
            assertThat(result).isBetween(0.0, 10.0);
        }

        @Test
        @DisplayName("random function with range | random 函数带范围")
        void testRandomWithRange() {
            Function random = functions.get("random");
            double result = (Double) random.apply(5, 10);
            assertThat(result).isBetween(5.0, 10.0);
        }

        @Test
        @DisplayName("randomint function | randomint 函数")
        void testRandomInt() {
            Function randomint = functions.get("randomint");
            int result = (Integer) randomint.apply(1, 10);
            assertThat(result).isBetween(1, 10);
        }
    }

    @Nested
    @DisplayName("Modulo Tests | 取模测试")
    class ModuloTests {

        @Test
        @DisplayName("mod function | mod 函数")
        void testMod() {
            Function mod = functions.get("mod");
            assertThat(mod.apply(10, 3)).isEqualTo(1.0);
            assertThat(mod.apply(10, 5)).isEqualTo(0.0);
            assertThat(mod.apply(10)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Sum/Average Tests | 求和/平均测试")
    class SumAverageTests {

        @Test
        @DisplayName("sum function | sum 函数")
        void testSum() {
            Function sum = functions.get("sum");
            assertThat(sum.apply(1, 2, 3, 4, 5)).isEqualTo(15.0);
            assertThat(sum.apply()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("avg function | avg 函数")
        void testAvg() {
            Function avg = functions.get("avg");
            assertThat(avg.apply(1, 2, 3, 4, 5)).isEqualTo(3.0);
            assertThat(avg.apply(10)).isEqualTo(10.0);
            assertThat(avg.apply()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Constants Tests | 常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("pi function | pi 函数")
        void testPi() {
            Function pi = functions.get("pi");
            assertThat(pi.apply()).isEqualTo(Math.PI);
        }

        @Test
        @DisplayName("e function | e 函数")
        void testE() {
            Function e = functions.get("e");
            assertThat(e.apply()).isEqualTo(Math.E);
        }
    }

    @Nested
    @DisplayName("Type Conversion Tests | 类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("int function | int 函数")
        void testInt() {
            Function intFunc = functions.get("int");
            assertThat(intFunc.apply(3.14)).isEqualTo(3);
            assertThat(intFunc.apply("42")).isEqualTo(42);
            assertThat(intFunc.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("long function | long 函数")
        void testLong() {
            Function longFunc = functions.get("long");
            assertThat(longFunc.apply(3.14)).isEqualTo(3L);
            assertThat(longFunc.apply()).isEqualTo(0L);
        }

        @Test
        @DisplayName("double function | double 函数")
        void testDouble() {
            Function doubleFunc = functions.get("double");
            assertThat(doubleFunc.apply(42)).isEqualTo(42.0);
            assertThat(doubleFunc.apply("3.14")).isEqualTo(3.14);
            assertThat(doubleFunc.apply()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("decimal function | decimal 函数")
        void testDecimal() {
            Function decimal = functions.get("decimal");
            Object result = decimal.apply(3.14);
            assertThat(result).isInstanceOf(BigDecimal.class);
            assertThat(decimal.apply()).isEqualTo(BigDecimal.ZERO);
            assertThat(decimal.apply("invalid")).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Clamp Tests | 限制测试")
    class ClampTests {

        @Test
        @DisplayName("clamp function | clamp 函数")
        void testClamp() {
            Function clamp = functions.get("clamp");
            assertThat(clamp.apply(5, 0, 10)).isEqualTo(5.0);
            assertThat(clamp.apply(-5, 0, 10)).isEqualTo(0.0);
            assertThat(clamp.apply(15, 0, 10)).isEqualTo(10.0);
            assertThat(clamp.apply(5)).isEqualTo(5.0);
            assertThat(clamp.apply()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("GetFunctions Tests | getFunctions 测试")
    class GetFunctionsTests {

        @Test
        @DisplayName("getFunctions returns all functions | getFunctions 返回所有函数")
        void testGetFunctionsReturnsAll() {
            Map<String, Function> funcs = MathFunctions.getFunctions();
            assertThat(funcs).isNotEmpty();
            assertThat(funcs).containsKey("abs");
            assertThat(funcs).containsKey("ceil");
            assertThat(funcs).containsKey("floor");
            assertThat(funcs).containsKey("sqrt");
        }
    }
}

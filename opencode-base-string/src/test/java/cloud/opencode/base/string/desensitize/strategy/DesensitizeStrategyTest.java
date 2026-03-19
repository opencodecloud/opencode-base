package cloud.opencode.base.string.desensitize.strategy;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeStrategyTest Tests
 * DesensitizeStrategyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DesensitizeStrategy 接口测试")
class DesensitizeStrategyTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda表达式实现脱敏")
        void testLambda() {
            DesensitizeStrategy strategy = original -> original.replaceAll(".", "*");
            assertThat(strategy.desensitize("hello")).isEqualTo("*****");
        }

        @Test
        @DisplayName("方法引用实现脱敏")
        void testMethodReference() {
            DesensitizeStrategy strategy = String::toUpperCase;
            assertThat(strategy.desensitize("hello")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("手机号脱敏策略")
        void testPhoneMask() {
            DesensitizeStrategy strategy = original -> {
                if (original == null || original.length() < 7) return original;
                return original.substring(0, 3) + "****" + original.substring(7);
            };
            assertThat(strategy.desensitize("13812345678")).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("空字符串脱敏")
        void testEmptyString() {
            DesensitizeStrategy strategy = original -> original.isEmpty() ? "" : "***";
            assertThat(strategy.desensitize("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("@FunctionalInterface验证")
    class FunctionalInterfaceAnnotationTests {

        @Test
        @DisplayName("标注为@FunctionalInterface")
        void testIsFunctionalInterface() {
            assertThat(DesensitizeStrategy.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("是接口")
        void testIsInterface() {
            assertThat(DesensitizeStrategy.class.isInterface()).isTrue();
        }
    }
}

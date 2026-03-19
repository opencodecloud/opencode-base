package cloud.opencode.base.string.desensitize.handler;

import cloud.opencode.base.string.desensitize.strategy.DesensitizeStrategy;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * StringHandlerTest Tests
 * StringHandlerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("StringHandler Tests")
class StringHandlerTest {

    @Nested
    @DisplayName("handle Tests")
    class HandleTests {

        @Test
        @DisplayName("Should apply strategy to value")
        void shouldApplyStrategyToValue() {
            DesensitizeStrategy strategy = value -> value.toUpperCase();
            String result = StringHandler.handle("hello", strategy);
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should return null for null value")
        void shouldReturnNullForNullValue() {
            DesensitizeStrategy strategy = value -> value.toUpperCase();
            assertThat(StringHandler.handle(null, strategy)).isNull();
        }

        @Test
        @DisplayName("Should mask with custom strategy")
        void shouldMaskWithCustomStrategy() {
            DesensitizeStrategy maskMiddle = value -> {
                if (value.length() <= 4) return value;
                return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
            };
            String result = StringHandler.handle("sensitive", maskMiddle);
            assertThat(result).isEqualTo("se***ve");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = StringHandler.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}

package cloud.opencode.base.string.desensitize.strategy;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * StrategyRegistryTest Tests
 * StrategyRegistryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("StrategyRegistry Tests")
class StrategyRegistryTest {

    private StrategyRegistry registry;

    @BeforeEach
    void setUp() {
        registry = StrategyRegistry.getInstance();
    }

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("Should return same instance")
        void shouldReturnSameInstance() {
            StrategyRegistry first = StrategyRegistry.getInstance();
            StrategyRegistry second = StrategyRegistry.getInstance();
            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("Default Strategies Tests")
    class DefaultStrategiesTests {

        @Test
        @DisplayName("Should have mobile strategy")
        void shouldHaveMobileStrategy() {
            DesensitizeStrategy strategy = registry.get("mobile");
            assertThat(strategy).isNotNull();
            assertThat(strategy.desensitize("13812345678")).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("Should have idCard strategy")
        void shouldHaveIdCardStrategy() {
            DesensitizeStrategy strategy = registry.get("idCard");
            assertThat(strategy).isNotNull();
            assertThat(strategy.desensitize("110101199001011234")).isEqualTo("110101********1234");
        }

        @Test
        @DisplayName("Should have email strategy")
        void shouldHaveEmailStrategy() {
            DesensitizeStrategy strategy = registry.get("email");
            assertThat(strategy).isNotNull();
            assertThat(strategy.desensitize("test@example.com")).isEqualTo("t***@example.com");
        }

        @Test
        @DisplayName("Should have bankCard strategy")
        void shouldHaveBankCardStrategy() {
            DesensitizeStrategy strategy = registry.get("bankCard");
            assertThat(strategy).isNotNull();
            assertThat(strategy.desensitize("6222021234567890123")).isEqualTo("6222***********0123");
        }

        @Test
        @DisplayName("Should have name strategy")
        void shouldHaveNameStrategy() {
            DesensitizeStrategy strategy = registry.get("name");
            assertThat(strategy).isNotNull();
            assertThat(strategy.desensitize("张三")).isEqualTo("张*");
            assertThat(strategy.desensitize("张三丰")).isEqualTo("张**");
        }

        @Test
        @DisplayName("Should have password strategy")
        void shouldHavePasswordStrategy() {
            DesensitizeStrategy strategy = registry.get("password");
            assertThat(strategy).isNotNull();
            assertThat(strategy.desensitize("mysecretpassword")).isEqualTo("******");
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register custom strategy")
        void shouldRegisterCustomStrategy() {
            registry.register("custom", str -> str.substring(0, 1) + "###");
            DesensitizeStrategy strategy = registry.get("custom");
            assertThat(strategy).isNotNull();
            assertThat(strategy.desensitize("hello")).isEqualTo("h###");
        }

        @Test
        @DisplayName("Should override existing strategy")
        void shouldOverrideExistingStrategy() {
            registry.register("testOverride", str -> "original");
            registry.register("testOverride", str -> "overridden");
            assertThat(registry.get("testOverride").desensitize("x")).isEqualTo("overridden");
        }
    }

    @Nested
    @DisplayName("Get Tests")
    class GetTests {

        @Test
        @DisplayName("Should return null for unknown strategy")
        void shouldReturnNullForUnknownStrategy() {
            assertThat(registry.get("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Mobile strategy should handle short strings")
        void mobileStrategyShouldHandleShortStrings() {
            DesensitizeStrategy strategy = registry.get("mobile");
            assertThat(strategy.desensitize("123")).isEqualTo("123");
        }

        @Test
        @DisplayName("Email strategy should handle invalid email")
        void emailStrategyShouldHandleInvalidEmail() {
            DesensitizeStrategy strategy = registry.get("email");
            assertThat(strategy.desensitize("notanemail")).isEqualTo("notanemail");
        }

        @Test
        @DisplayName("Email strategy should handle single char local part")
        void emailStrategyShouldHandleSingleCharLocalPart() {
            DesensitizeStrategy strategy = registry.get("email");
            assertThat(strategy.desensitize("a@example.com")).isEqualTo("a@example.com");
        }

        @Test
        @DisplayName("Name strategy should handle null")
        void nameStrategyShouldHandleNull() {
            DesensitizeStrategy strategy = registry.get("name");
            assertThat(strategy.desensitize(null)).isNull();
        }

        @Test
        @DisplayName("Name strategy should handle single char")
        void nameStrategyShouldHandleSingleChar() {
            DesensitizeStrategy strategy = registry.get("name");
            assertThat(strategy.desensitize("张")).isEqualTo("张");
        }
    }
}
